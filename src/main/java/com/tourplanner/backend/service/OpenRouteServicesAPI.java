package com.tourplanner.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourplanner.backend.model.GeoCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OpenRouteServicesAPI implements MapService {
    private static final Logger log = LogManager.getLogger(OpenRouteServicesAPI.class);
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();

    @Value("${openrouteservice.api.key}")
    private String apiKey;

    @Override
    public CompletableFuture<GeoCode> geocode(String location) {
        log.debug("Getting GeoCode for '{}'", location);

        // Build the request URL
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String key = apiKey != null ? apiKey.trim() : "";
        String uri = String.format("https://api.openrouteservice.org/geocode/search?api_key=%s&text=%s", key, encodedLocation);

        log.info("Geocode request URL: {}", uri);
        log.info("Geocode request headers: Accept=application/json, User-Agent=TourPlanner/1.0");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Accept", "application/json")
                .header("User-Agent", "TourPlanner/1.0")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {

                    // If the response code is not 200
                    if (response.statusCode() != 200) {
                        log.error("Geocoding API error: HTTP {} - {}", response.statusCode(), response.body());
                        return null;
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    try {
                        OpenRouteServicesResponse res = mapper.readValue(response.body(), new TypeReference<>() {});
                        return res.convertToGeoCode();
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing geocoding response", e);
                        return null;
                    }
                })
                .exceptionally(e -> {
                    log.error("Error in geocoding request", e);
                    return null;
                });
    }

    @Override
    // CompletableFuture allows running code asynchronously
    public CompletableFuture<List<GeoCode>> geocodeRoute(String from, String to) {
        CompletableFuture<GeoCode> first = geocode(from);
        CompletableFuture<GeoCode> second = geocode(to);
        return CompletableFuture.allOf(first, second)
                .thenApply(v -> {
                    List<GeoCode> results = new ArrayList<>();
                    results.add(first.join());
                    results.add(second.join());
                    return results;
                });
    }

    @Override
    public CompletableFuture<List<GeoCode>> geocodeSuggestions(String location) {
        log.debug("Getting GeoCode suggestions for '{}'", location);
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String key = apiKey != null ? apiKey.trim() : "";
        String uri = String.format("https://api.openrouteservice.org/geocode/search?api_key=%s&text=%s", key, encodedLocation);

        log.info("Geocode request URL: {}", uri);
        log.info("Geocode request headers: Accept=application/json, User-Agent=TourPlanner/1.0");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Accept", "application/json")
                .header("User-Agent", "TourPlanner/1.0")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        log.error("Geocoding API error: HTTP {} - {}", response.statusCode(), response.body());
                        return new java.util.ArrayList<GeoCode>();
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    try {
                        OpenRouteServicesResponse res = mapper.readValue(response.body(), new TypeReference<OpenRouteServicesResponse>() {});
                        return res.toGeoCodeList();
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing geocoding response", e);
                        return new java.util.ArrayList<GeoCode>();
                    }
                })
                .exceptionally(e -> {
                    log.error("Error in geocoding request", e);
                    return new java.util.ArrayList<GeoCode>();
                });
    }

    // Inner class for mapping the OpenRouteService response
    public static class OpenRouteServicesResponse {
        public List<Feature> features;
        public GeoCode convertToGeoCode() {
            if (features == null || features.isEmpty()) return null;
            double[] coordinates = features.get(0).geometry.coordinates;
            String label = features.get(0).properties != null ? features.get(0).properties.label : null;
            return new GeoCode(coordinates[0], coordinates[1], label);
        }
        public List<GeoCode> toGeoCodeList() {
            List<GeoCode> list = new ArrayList<GeoCode>();
            if (features != null) {
                for (Feature f : features) {
                    if (f.geometry != null && f.geometry.coordinates != null && f.geometry.coordinates.length == 2) {
                        String label = f.properties != null ? f.properties.label : null;
                        list.add(new GeoCode(f.geometry.coordinates[0], f.geometry.coordinates[1], label));
                    }
                }
            }
            return list;
        }
        public static class Feature {
            public Geometry geometry;
            public Properties properties;
        }
        public static class Geometry {
            public double[] coordinates;
        }
        public static class Properties {
            public String label;
        }
    }
} 