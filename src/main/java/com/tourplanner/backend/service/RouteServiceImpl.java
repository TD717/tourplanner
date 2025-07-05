package com.tourplanner.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourplanner.backend.model.RouteData;
import com.tourplanner.backend.model.GeoCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class RouteServiceImpl implements RouteService {
    private static final Logger logger = LogManager.getLogger(RouteServiceImpl.class);

    @Value("${openrouteservice.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openrouteservice.org/v2/directions/";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MapService mapService;

    @Autowired
    public RouteServiceImpl(@Autowired MapService mapService) {
        this.mapService = mapService;
    }

    @Override
    public double[] geocode(String address) {
        try {
            String key = apiKey != null ? apiKey.trim() : "";
            String url = "https://api.openrouteservice.org/geocode/search?api_key=" + key + "&text=" + java.net.URLEncoder.encode(address, java.nio.charset.StandardCharsets.UTF_8);

            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode features = root.get("features");
                if (features != null && features.isArray() && features.size() > 0) {
                    JsonNode coords = features.get(0).get("geometry").get("coordinates");
                    double lon = coords.get(0).asDouble();
                    double lat = coords.get(1).asDouble();
                    return new double[]{lat, lon};
                }
            } else {
                logger.error("Geocoding API error: HTTP {} - {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            logger.error("Error calling geocoding API", e);
        }
        return null;
    }

    @Override
    public RouteData getRouteData(String fromLocation, String toLocation, String transportType) {
        logger.info("Requesting route from '{}' to '{}' by '{}'", fromLocation, toLocation, transportType);
        try {
            // Use MapService to geocode addresses
            GeoCode fromCoords = mapService.geocode(fromLocation).get();
            GeoCode toCoords = mapService.geocode(toLocation).get();
            if (fromCoords == null || toCoords == null) {
                logger.error("Could not geocode one or both locations: {} -> {}", fromLocation, toLocation);
                return null;
            }
            String profile = getProfile(transportType);
            String url = API_URL + profile + "/geojson";
            
            // Debug: Log the coordinates
            logger.info("From coordinates: lon={}, lat={}", fromCoords.getLongitude(), fromCoords.getLatitude());
            logger.info("To coordinates: lon={}, lat={}", toCoords.getLongitude(), toCoords.getLatitude());
            
            String body = String.format(java.util.Locale.US,
                    "{\"coordinates\":[[%f,%f],[%f,%f]],\"radiuses\":[5000,5000]}",
                    fromCoords.getLongitude(), fromCoords.getLatitude(), toCoords.getLongitude(), toCoords.getLatitude());
            
            // Log the request body
            logger.info("Request body: {}", body);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "TourPlanner/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("ORS API response status: {}", response.statusCode());
            logger.debug("ORS API response: {}", response.body());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                double distance = root.at("/features/0/properties/segments/0/distance").asDouble() / 1000.0;
                double duration = root.at("/features/0/properties/segments/0/duration").asDouble() / 3600.0;
                List<RouteData.Coordinate> coords = new ArrayList<>();
                for (JsonNode coord : root.at("/features/0/geometry/coordinates")) {
                    coords.add(new RouteData.Coordinate(coord.get(1).asDouble(), coord.get(0).asDouble()));
                }
                logger.info("Parsed {} coordinates from ORS response", coords.size());
                for (RouteData.Coordinate c : coords) {
                    logger.info("Coordinate: lat={}, lon={}", c.getLatitude(), c.getLongitude());
                }
                RouteData data = new RouteData(fromLocation, toLocation, transportType, distance, duration, coords);
                data.setSummary(root.at("/features/0/properties/summary").toString());
                return data;
            } else {
                logger.error("ORS API error: HTTP {} - {}", response.statusCode(), response.body());
                // Fallback: calculate approximate distance using Haversine formula
                return createFallbackRouteData(fromLocation, toLocation, transportType, fromCoords, toCoords);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error geocoding locations", e);
            Thread.currentThread().interrupt();
            return createFallbackRouteData(fromLocation, toLocation, transportType, null, null);
        } catch (Exception e) {
            logger.error("Error getting route data", e);
            return createFallbackRouteData(fromLocation, toLocation, transportType, null, null);
        }
    }

    private String getProfile(String transportType) {
        if (transportType == null) return "driving-car";
        switch (transportType.toLowerCase()) {
            case "foot": return "foot-walking";
            case "bicycle": return "cycling-regular";
            case "public transport": return "driving-car"; // Public transport uses car routing
            default: return "driving-car";
        }
    }
    
    //Create fallback route data when API fails, using approximate calculations.
    private RouteData createFallbackRouteData(String fromLocation, String toLocation, String transportType, 
                                             GeoCode fromCoords, GeoCode toCoords) {
        double distance = 0.0;
        double duration = 0.0;
        
        logger.info("Creating fallback route data for {} to {} by {}", fromLocation, toLocation, transportType);
        
        if (fromCoords != null && toCoords != null) {
            // Calculate distance using Haversine formula
            distance = calculateHaversineDistance(fromCoords, toCoords);
            logger.info("Calculated fallback distance: {} km", distance);
            
            // Estimate duration based on transport type
            String transportLower = transportType != null ? transportType.toLowerCase() : "car";
            switch (transportLower) {
                case "foot":
                    duration = distance / 5.0; // 5 km/h walking speed
                    break;
                case "bicycle":
                    duration = distance / 15.0; // 15 km/h cycling speed
                    break;
                case "public transport":
                    duration = distance / 25.0; // 25 km/h public transport speed
                    break;
                default: // car
                    duration = distance / 50.0; // 50 km/h average speed
                    break;
            }
            logger.info("Calculated fallback duration: {} hours", duration);
        } else {
            logger.warn("Cannot calculate fallback route - coordinates are null");
        }
        
        List<RouteData.Coordinate> coords = new ArrayList<>();
        if (fromCoords != null && toCoords != null) {
            // Used to create a more realistic route with intermediate points
            coords = createIntermediateRoute(fromCoords, toCoords, transportType);
        }
        
        RouteData data = new RouteData(fromLocation, toLocation, transportType, distance, duration, coords);
        data.setSummary("Approximate route (API unavailable)");
        return data;
    }
    
    // Create intermediate route points to make the route look more realistic on the map.
    private List<RouteData.Coordinate> createIntermediateRoute(GeoCode from, GeoCode to, String transportType) {
        List<RouteData.Coordinate> route = new ArrayList<>();
        route.add(new RouteData.Coordinate(from.getLatitude(), from.getLongitude()));
        
        // Calculate intermediate points based on distance
        double distance = calculateHaversineDistance(from, to);
        int numPoints = Math.max(3, Math.min(10, (int)(distance / 10))); // 1 point per 10km, min 3, max 10
        
        for (int i = 1; i < numPoints; i++) {
            double ratio = (double) i / numPoints;
            
            // Linear interpolation between start and end points
            double lat = from.getLatitude() + (to.getLatitude() - from.getLatitude()) * ratio;
            double lon = from.getLongitude() + (to.getLongitude() - from.getLongitude()) * ratio;
            
            // Used to add some realistic variation based on transport type
            if (transportType != null) {
                String transportLower = transportType.toLowerCase();
                if (transportLower.equals("car") || transportLower.equals("public transport")) {
                    // For cars and public transport, add slight curves to simulate road paths
                    double variation = Math.sin(ratio * Math.PI) * 0.01; // Small variation
                    lat += variation;
                    lon += variation * 0.5;
                } else if (transportLower.equals("bicycle")) {
                    // For bicycles, add smaller curves
                    double variation = Math.sin(ratio * Math.PI) * 0.005; // Smaller variation
                    lat += variation;
                    lon += variation * 0.3;
                }
            }
            route.add(new RouteData.Coordinate(lat, lon));
        }
        
        route.add(new RouteData.Coordinate(to.getLatitude(), to.getLongitude()));
        return route;
    }
    
    // Calculate distance between two points using Haversine formula.
    private double calculateHaversineDistance(GeoCode from, GeoCode to) {
        final double R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(to.getLatitude() - from.getLatitude());
        double lonDistance = Math.toRadians(to.getLongitude() - from.getLongitude());
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(from.getLatitude())) * Math.cos(Math.toRadians(to.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double distance = R * c;
        logger.info("Haversine calculation: from ({}, {}) to ({}, {}) = {} km", 
                   from.getLatitude(), from.getLongitude(), 
                   to.getLatitude(), to.getLongitude(), distance);
        return distance;
    }
} 