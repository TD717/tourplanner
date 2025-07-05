package com.tourplanner.backend.model;

public class GeoCode {
    private double longitude;
    private double latitude;
    private String label;

    public GeoCode(double longitude, double latitude, String label) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.label = label;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getLabel() {
        return label;
    }

    public GeoCode(double longitude, double latitude) {
        this(longitude, latitude, null);
    }

    @Override
    public String toString() {
        return "GeoCode{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                (label != null ? ", label='" + label + '\'' : "") +
                '}';
    }
} 