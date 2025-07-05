package com.tourplanner.backend.service;

import com.tourplanner.backend.model.RouteData;

// Service interface for route-related operations, which integrates with external APIs for route data.
public interface RouteService {

    RouteData getRouteData(String fromLocation, String toLocation, String transportType);

    //Geocode an address to coordinates (latitude, longitude).
    double[] geocode(String address);
} 