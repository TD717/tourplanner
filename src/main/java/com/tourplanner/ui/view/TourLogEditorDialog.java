package com.tourplanner.ui.view;

import com.tourplanner.backend.dto.TourLogDTO;
import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.service.TourService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class TourLogEditorDialog extends Stage {
    @FXML private ComboBox<TourDTO> tourComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextArea commentField;
    @FXML private Spinner<Integer> difficultySpinner;
    @FXML private TextField distanceField;
    @FXML private TextField totalTimeField;
    @FXML private Spinner<Integer> ratingSpinner;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private TourLogDTO result;
    private boolean editMode = false;
    private TourService tourService;
    private Long editingLogId = null;

    public TourLogEditorDialog() {}

    public static Optional<TourLogDTO> showDialog(TourLogDTO existing, TourService tourService) {
        return showDialog(existing, tourService, null);
    }
    
    public static Optional<TourLogDTO> showDialog(TourLogDTO existing, TourService tourService, TourDTO preSelectedTour) {
        try {
            FXMLLoader loader = new FXMLLoader(TourLogEditorDialog.class.getResource("/com/tourplanner/fxml/tourlogeditordialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(existing == null ? "Add Tour Log" : "Edit Tour Log");
            dialogStage.setScene(new Scene(loader.load()));
            TourLogEditorDialog controller = loader.getController();
            controller.tourService = tourService;
            controller.populateTourComboBox();
            if (existing != null) {
                controller.setFields(existing);
            } else if (preSelectedTour != null) {
                // Pre-select the tour if provided
                controller.tourComboBox.setValue(preSelectedTour);
            }
            dialogStage.showAndWait();
            return Optional.ofNullable(controller.result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void populateTourComboBox() {
        if (tourService != null) {
            try {
                List<TourDTO> tours = tourService.getAllTours();
                tourComboBox.getItems().addAll(tours);
                
                // Set cell factory to display tour names
                tourComboBox.setCellFactory(param -> new ListCell<TourDTO>() {
                    @Override
                    protected void updateItem(TourDTO item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                });
                
                // Set button cell factory for the selected item display
                tourComboBox.setButtonCell(new ListCell<TourDTO>() {
                    @Override
                    protected void updateItem(TourDTO item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                });
            } catch (Exception e) {
                errorLabel.setText("Failed to load tours: " + e.getMessage());
            }
        }
    }

    private void setFields(TourLogDTO log) {
        editMode = true;
        editingLogId = log.getId();
        
        // Set the tour if it has a tour ID
        if (log.getTourId() != null && tourService != null) {
            try {
                TourDTO tour = tourService.getTourById(log.getTourId());
                if (tour != null) {
                    tourComboBox.setValue(tour);
                }
            } catch (Exception e) {
                // Tour not found, leave combo box empty
            }
        }
        
        if (log.getDateTime() != null) {
            datePicker.setValue(log.getDateTime().toLocalDate());
            // Format time as HH:mm for better user experience
            timeField.setText(log.getDateTime().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        }
        commentField.setText(log.getComment());
        difficultySpinner.getValueFactory().setValue(log.getDifficulty() != null ? log.getDifficulty().intValue() : 3);
        distanceField.setText(log.getTotalDistance() != null ? log.getTotalDistance().toString() : "");
        totalTimeField.setText(log.getTotalTime() != null ? log.getTotalTime().toString() : "");
        ratingSpinner.getValueFactory().setValue(log.getRating() != null ? log.getRating().intValue() : 3);
    }

    @FXML
    private void initialize() {
        difficultySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3));
        ratingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3));
        errorLabel.setText("");
        
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Set default time to current time
        timeField.setText(LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");
        try {
            // Validate tour selection
            TourDTO selectedTour = tourComboBox.getValue();
            if (selectedTour == null) throw new Exception("Please select a tour");
            
            LocalDate date = datePicker.getValue();
            if (date == null) throw new Exception("Date is required");
            String timeStr = timeField.getText().trim();
            if (timeStr.isEmpty()) throw new Exception("Time is required");
            
            // Parse time with proper format (HH:mm)
            LocalTime time;
            try {
                // First try the default format
                time = LocalTime.parse(timeStr);
            } catch (Exception e) {
                try {
                    // Try parsing with HH:mm format
                    time = LocalTime.parse(timeStr, java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                } catch (Exception e2) {
                    throw new Exception("Invalid time format. Please use HH:mm (e.g., 14:30)");
                }
            }
            
            String comment = commentField.getText().trim();
            if (comment.isEmpty()) throw new Exception("Comment is required");
            int difficulty = difficultySpinner.getValue();
            double distance = Double.parseDouble(distanceField.getText().trim());
            double totalTime = Double.parseDouble(totalTimeField.getText().trim());
            int rating = ratingSpinner.getValue();
            
            // Create the result with the proper ID (editingLogId for edit mode, null for new)
            result = new TourLogDTO(editingLogId, selectedTour.getId(), LocalDateTime.of(date, time), comment, (double)difficulty, distance, totalTime, (double)rating);
            ((Stage)saveBtn.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText("Invalid input: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        result = null;
        ((Stage)cancelBtn.getScene().getWindow()).close();
    }
} 