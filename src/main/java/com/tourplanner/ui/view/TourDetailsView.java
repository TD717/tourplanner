package com.tourplanner.ui.view;

import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.model.RouteData;
import com.tourplanner.backend.service.RouteService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.layout.Pane;
import java.util.logging.Logger;
import java.util.logging.Level;

// View controller for Tour Details following MVVM pattern.
public class TourDetailsView {

    private static final Logger logger = Logger.getLogger(TourDetailsView.class.getName());

    @FXML private Label tourNameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label transportLabel;
    @FXML private Label distanceLabel;
    @FXML private Label timeLabel;
    @FXML private ImageView mapImage;
    @FXML private Pane mapContainer;

    private RouteService routeService;
    private WebView mapView;
    private RouteData currentRoute;

    // No-arg constructor required for FXML loading.
    public TourDetailsView() {
        // RouteService will be injected via setRouteService method
    }

    // Constructor with RouteService dependency (for manual instantiation).
    public TourDetailsView(RouteService routeService) {
        this.routeService = routeService;
    }

    // Set the RouteService dependency (called by ViewFactory).
    public void setRouteService(RouteService routeService) {
        this.routeService = routeService;
    }

    @FXML
    private void initialize() {
        try {
            // Initialize WebView for interactive map
            if (mapContainer != null) {
                mapView = new WebView();
                mapView.setPrefSize(600, 400);
                mapContainer.getChildren().add(mapView);
                mapView.setVisible(false);
            }
            logger.fine("TourDetailsView initialized");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to initialize TourDetailsView", e);
        }
    }

    // Bind this view to the selected tour property from the tour list.
    public void bindTo(javafx.beans.property.ReadOnlyObjectProperty<TourDTO> selectedTourProperty) {
        selectedTourProperty.addListener((observable, oldValue, newValue) -> {
            show(newValue);
        });
    }

    // Clear all tour details display.
    private void clear() {
        tourNameLabel.setText("Select a tour to view details");
        descriptionLabel.setText("Description: No tour selected");
        transportLabel.setText("Transport: --");
        distanceLabel.setText("Distance: -- km");
        timeLabel.setText("Estimated Time: --");
    }

    // Display tour details.
    private void show(TourDTO tour) {
        if (tour == null) {
            clear();
            return;
        }
        
        try {
            tourNameLabel.setText(tour.getName());
            descriptionLabel.setText("Description: " + tour.getDescription());
            transportLabel.setText("Transport: " + (tour.getTransportType() != null ? tour.getTransportType() : "Not specified"));
            distanceLabel.setText("Distance: " + tour.getDistance() + " km");
            timeLabel.setText("Estimated Time: " + tour.getEstimatedTime());
            
            // Show interactive map if route service is available
            if (routeService != null && mapView != null) {
                showInteractiveMap(tour);
            }
            logger.fine("Displayed tour details for: " + tour.getName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error displaying tour details", e);
            clear();
        }
    }

    // Show interactive map for the tour
    private void showInteractiveMap(TourDTO tour) {
        try {
            String fromLocation = tour.getFromLocation() != null ? tour.getFromLocation() : "Vienna, Austria";
            String toLocation = tour.getToLocation() != null ? tour.getToLocation() : "Salzburg, Austria";
            String transportType = tour.getTransportType() != null ? tour.getTransportType() : "Car";
            logger.info(String.format("Requesting route for map: %s -> %s by %s", fromLocation, toLocation, transportType));
            currentRoute = routeService.getRouteData(fromLocation, toLocation, transportType);
            if (currentRoute != null && currentRoute.getCoordinates() != null && !currentRoute.getCoordinates().isEmpty()) {
                logger.info(String.format("RouteData received: %d coordinates", currentRoute.getCoordinates().size()));
                for (var c : currentRoute.getCoordinates()) {
                    logger.info(String.format("Coordinate: lat=%f, lon=%f", c.getLatitude(), c.getLongitude()));
                }
                String leafletHtml = buildLeafletHtml(currentRoute);
                WebEngine engine = mapView.getEngine();
                engine.loadContent(leafletHtml);
                mapView.setVisible(true);
                mapImage.setVisible(false);
                logger.info(String.format("Map loaded successfully for tour: %s", tour.getName()));
            } else {
                logger.warning("No route or coordinates found for map. Showing error message.");
                WebEngine engine = mapView.getEngine();
                engine.loadContent("<html><body><div style='color:red;font-size:16px;padding:20px;'>Could not load route map. No route or coordinates found for these locations. Check your OpenRouteService API key, internet connection, or try different locations.</div></body></html>");
                mapView.setVisible(true);
                mapImage.setVisible(false);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load interactive map", e);
            WebEngine engine = mapView.getEngine();
            engine.loadContent("<html><body><div style='color:red;font-size:16px;padding:20px;'>Could not load route map due to an error. Check your OpenRouteService API key and internet connection.</div></body></html>");
            mapView.setVisible(true);
            mapImage.setVisible(false);
        }
    }

    // Build Leaflet HTML for interactive map.
    private String buildLeafletHtml(RouteData route) {
        StringBuilder coords = new StringBuilder();
        for (RouteData.Coordinate c : route.getCoordinates()) {
            coords.append("[").append(c.getLatitude()).append(",").append(c.getLongitude()).append("],");
        }
        if (coords.length() > 0) {
            coords.setLength(coords.length() - 1); // remove last comma
        }
        
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='utf-8'/>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css'/>" +
               "<style>#map { height: 100%; width: 100%; min-height: 400px; } body { margin: 0; padding: 0; }</style>" +
               "</head>" +
               "<body>" +
               "<div id='map'></div>" +
               "<script src='https://unpkg.com/leaflet/dist/leaflet.js'></script>" +
               "<script>" +
               "var map = L.map('map').setView([" + route.getCoordinates().get(0).getLatitude() + ", " + route.getCoordinates().get(0).getLongitude() + "], 10);" +
               "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 18 }).addTo(map);" +
               "var latlngs = [" + coords + "];" +
               "var polyline = L.polyline(latlngs, {color: 'blue', weight: 3}).addTo(map);" +
               "map.fitBounds(polyline.getBounds());" +
               "L.marker(latlngs[0]).addTo(map).bindPopup('Start: " + route.getFromLocation() + "');" +
               "L.marker(latlngs[latlngs.length-1]).addTo(map).bindPopup('End: " + route.getToLocation() + "');" +
               "</script>" +
               "</body>" +
               "</html>";
    }
}
