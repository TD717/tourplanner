package com.tourplanner.backend.model;

import java.util.List;

// Route information from OpenRouteService API
public class RouteData {
    
    private String fromLocation;
    private String toLocation;
    private String transportType;
    private double distance; // in kilometers
    private double duration; // in hours
    private List<Coordinate> coordinates;
    private String routeImagePath;
    private String summary;

    // Constructors
    public RouteData() {}

    public RouteData(String fromLocation, String toLocation, String transportType, 
                    double distance, double duration) {
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.transportType = transportType;
        this.distance = distance;
        this.duration = duration;
    }

    // This constructor has also the coordinates
    public RouteData(String fromLocation, String toLocation, String transportType, 
                    double distance, double duration, List<Coordinate> coordinates) {
        this(fromLocation, toLocation, transportType, distance, duration);
        this.coordinates = coordinates;
    }

    // Getters and Setters
    public String getFromLocation() {
        return fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public String getTransportType() {
        return transportType;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public String getRouteImagePath() {
        return routeImagePath;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }


    @Override
    public String toString() {
        return String.format("RouteData{from='%s', to='%s', distance=%.1fkm, duration=%.1fh}", 
                           fromLocation, toLocation, distance, duration);
    }

    // Inner class for coordinates using latitude and longitude
    public static class Coordinate {
        private double latitude;
        private double longitude;

        public Coordinate() {}

        public Coordinate(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        @Override
        public String toString() {
            return String.format("Coordinate{lat=%.6f, lng=%.6f}", latitude, longitude);
        }
    }
} 