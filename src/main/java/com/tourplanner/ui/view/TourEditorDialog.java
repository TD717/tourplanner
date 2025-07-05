package com.tourplanner.ui.view;

import com.tourplanner.backend.dto.TourDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import com.tourplanner.backend.service.MapService;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Dialog for editing tour information following MVVM pattern.
 * Provides a modal dialog for adding and editing tours.
 */
public class TourEditorDialog extends Dialog<TourDTO> {

    private static final Logger logger = Logger.getLogger(TourEditorDialog.class.getName());

    /* ---------- FXML fields ---------- */
    @FXML private TextField nameField;
    @FXML private TextField fromField;
    @FXML private TextField toField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> transportTypeBox;



    private MapService mapService;
    private ContextMenu fromSuggestions = new ContextMenu();
    private ContextMenu toSuggestions = new ContextMenu();

    public void setMapService(MapService mapService) {
        this.mapService = mapService;
        setupAutocomplete();
    }

    private void setupAutocomplete() {
        fromField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            String text = fromField.getText();
            if (text.length() < 2 || mapService == null) {
                fromSuggestions.hide();
                return;
            }
            mapService.geocodeSuggestions(text).thenAccept(suggestions -> {
                Platform.runLater(() -> {
                    fromSuggestions.getItems().clear();
                    if (suggestions != null && !suggestions.isEmpty()) {
                        for (var geo : suggestions) {
                            String label = geo.getLabel() != null ? geo.getLabel() : text;
                            MenuItem item = new MenuItem(label);
                            item.setOnAction(e -> fromField.setText(label));
                            fromSuggestions.getItems().add(item);
                        }
                        fromSuggestions.show(fromField, javafx.geometry.Side.BOTTOM, 0, 0);
                    } else {
                        fromSuggestions.hide();
                    }
                });
            });
        });
        toField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            String text = toField.getText();
            if (text.length() < 2 || mapService == null) {
                toSuggestions.hide();
                return;
            }
            mapService.geocodeSuggestions(text).thenAccept(suggestions -> {
                Platform.runLater(() -> {
                    toSuggestions.getItems().clear();
                    if (suggestions != null && !suggestions.isEmpty()) {
                        for (var geo : suggestions) {
                            String label = geo.getLabel() != null ? geo.getLabel() : text;
                            MenuItem item = new MenuItem(label);
                            item.setOnAction(e -> toField.setText(label));
                            toSuggestions.getItems().add(item);
                        }
                        toSuggestions.show(toField, javafx.geometry.Side.BOTTOM, 0, 0);
                    } else {
                        toSuggestions.hide();
                    }
                });
            });
        });
    }

    /* ---------- factory helpers ---------- */
    public static Optional<TourDTO> showAddDialog(MapService mapService) {
        TourEditorDialog dialog = new TourEditorDialog(null);
        dialog.setMapService(mapService);
        return dialog.showAndWait();
    }
    
    public static Optional<TourDTO> showEditDialog(TourDTO dto, MapService mapService) {
        TourEditorDialog dialog = new TourEditorDialog(dto);
        dialog.setMapService(mapService);
        return dialog.showAndWait();
    }

    /* ---------- constructor ---------- */
    private TourEditorDialog(TourDTO toEdit) {
        setTitle(toEdit == null ? "Add Tour" : "Edit Tour");

        try {
            URL url = getClass().getResource("/com/tourplanner/fxml/toureditordialog.fxml");
            logger.fine("Loading FXML from: " + url);
            Objects.requireNonNull(url, "toureditordialog.fxml NOT found!");

            FXMLLoader loader = new FXMLLoader(url);
            loader.setController(this);
            getDialogPane().setContent(loader.load());
            
            logger.fine("TourEditorDialog FXML loaded successfully");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to load TourEditorDialog FXML", ex);
            throw new RuntimeException("Failed to load dialog", ex);
        }

        // Initialize transport type combo box
        transportTypeBox.getItems().addAll("Car", "Bicycle", "Foot", "Public Transport");
        transportTypeBox.setValue("Car");

        // Add dialog buttons
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Pre-fill fields for editing
        if (toEdit != null) {
            nameField.setText(toEdit.getName());
            fromField.setText(toEdit.getFromLocation());
            toField.setText(toEdit.getToLocation());
            descriptionField.setText(toEdit.getDescription());
            if (toEdit.getTransportType() != null) {
                transportTypeBox.setValue(toEdit.getTransportType());
            }
            logger.fine("Pre-filled dialog for editing tour: " + toEdit.getName());
        }

        /* Convert dialog result with validation */
        setResultConverter(btn -> {
            if (btn != ButtonType.OK) {
                logger.fine("Dialog cancelled");
                return null;
            }
            
            return createTourFromInput(toEdit);
        });
    }

    /**
     * Create a TourDTO from the input fields with validation.
     * 
     * @param originalTour The original tour being edited (null for new tours)
     * @return TourDTO if validation passes, null otherwise
     */
    private TourDTO createTourFromInput(TourDTO originalTour) {
        try {
            // Basic validation
            String name = nameField.getText().trim();
            if (name.isBlank()) {
                showValidationError("Name is required");
                return null;
            }

            String fromLocation = fromField.getText().trim();
            if (fromLocation.isBlank()) {
                showValidationError("From location is required");
                return null;
            }

            String toLocation = toField.getText().trim();
            if (toLocation.isBlank()) {
                showValidationError("To location is required");
                return null;
            }

            String description = descriptionField.getText().trim();
            if (description.isBlank()) {
                showValidationError("Description is required");
                return null;
            }

            String transportType = transportTypeBox.getValue();
            if (transportType == null || transportType.isBlank()) {
                showValidationError("Transport type is required");
                return null;
            }

            // Create tour with default values for distance and time
            // These will be calculated by the route service later
            TourDTO tour = new TourDTO();
            
            // Preserve the ID if editing an existing tour
            if (originalTour != null && originalTour.getId() != null) {
                tour.setId(originalTour.getId());
                logger.fine("Preserving tour ID for edit: " + originalTour.getId());
            }
            
            tour.setName(name);
            tour.setFromLocation(fromLocation);
            tour.setToLocation(toLocation);
            tour.setDescription(description);
            tour.setTransportType(transportType);
            tour.setDistance(0.0); // Will be calculated by route service
            tour.setEstimatedTime(""); // Will be calculated by route service
            
            logger.fine("Created tour from dialog input: " + name);
            return tour;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating tour from input", e);
            showValidationError("An error occurred while creating the tour");
            return null;
        }
    }

    /**
     * Show a validation error dialog.
     * 
     * @param message Error message to display
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        logger.warning("Validation error: " + message);
    }


}
