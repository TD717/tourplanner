package com.tourplanner.backend.service;

import com.tourplanner.backend.model.GeoCode;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MapService {
    CompletableFuture<GeoCode> geocode(String location);
    CompletableFuture<List<GeoCode>> geocodeRoute(String from, String to);

    java.util.concurrent.CompletableFuture<java.util.List<com.tourplanner.backend.model.GeoCode>> geocodeSuggestions(String location);
} 