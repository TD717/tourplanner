package com.tourplanner.ui.view;

import com.tourplanner.backend.dto.TourLogDTO;
import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.service.TourService;
import com.tourplanner.ui.viewmodel.TourLogViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import com.tourplanner.backend.service.PdfGenerator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.scene.control.Tooltip;
import javafx.beans.binding.Bindings;

public class TourLogView {
    @FXML private TableView<TourLogDTO> logTable;
    @FXML private TableColumn<TourLogDTO, String> dateCol;
    @FXML private TableColumn<TourLogDTO, String> commentCol;
    @FXML private TableColumn<TourLogDTO, String> difficultyCol;
    @FXML private TableColumn<TourLogDTO, String> distanceCol;
    @FXML private TableColumn<TourLogDTO, String> timeCol;
    @FXML private TableColumn<TourLogDTO, String> ratingCol;
    @FXML private TextField searchField;
    @FXML private Button searchBtn, addBtn, editBtn, deleteBtn;
    @FXML private Label errorLabel;
    @FXML private Label tourInfoLabel;

    private TourLogViewModel viewModel;
    private TourService tourService;

    public TourLogView() {}
    public TourLogView(TourLogViewModel viewModel) { this.viewModel = viewModel; }
    public void setViewModel(TourLogViewModel viewModel) {
        this.viewModel = viewModel;
        bindToViewModel();
        // Bind button states
        editBtn.disableProperty().bind(logTable.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(logTable.getSelectionModel().selectedItemProperty().isNull());
        updateTourInfo();
    }
    public void setTourService(TourService tourService) { this.tourService = tourService; }

    @FXML
    private void initialize() {
        // Table column bindings (static, do not depend on viewModel)
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedDateTime()));
        commentCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getComment()));
        difficultyCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDifficultyDescription()));
        distanceCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getTotalDistance())));
        timeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedTotalTime()));
        ratingCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRatingDescription()));

        if (viewModel != null) {
            bindToViewModel();
        }
    }

    private void bindToViewModel() {
        if (viewModel == null) return;
        // Bind to the observable list directly
        logTable.setItems(viewModel.getTourLogs());
        // Bind error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        updateTourInfo();
    }
    
    private void updateTourInfo() {
        if (viewModel != null && viewModel.getSelectedTour() != null) {
            TourDTO tour = viewModel.getSelectedTour();
            int logCount = viewModel.getTourLogs().size();
            tourInfoLabel.setText("Tour: " + tour.getName() + " (" + logCount + " logs)");
            if (logCount == 0) {
                tourInfoLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            } else {
                tourInfoLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            }
        } else {
            tourInfoLabel.setText("Select a tour from the list to view its logs");
            tourInfoLabel.setStyle("-fx-text-fill: gray;");
        }
    }

    @FXML
    private void onSearch() {
        if (viewModel != null) viewModel.searchTourLogs(searchField.getText());
        updateTourInfo();
    }

    @FXML
    private void onAdd() {
        // Get the currently selected tour from the view model
        TourDTO currentTour = viewModel != null ? viewModel.getSelectedTour() : null;
        
        TourLogEditorDialog.showDialog(null, tourService, currentTour).ifPresent(log -> {
            if (viewModel != null) {
                viewModel.addTourLog(log);
                updateTourInfo();
            }
        });
    }

    @FXML
    private void onEdit() {
        int idx = logTable.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && viewModel != null) {
            TourLogDTO oldLog = logTable.getItems().get(idx);
            TourLogEditorDialog.showDialog(oldLog, tourService).ifPresent(newLog -> {
                viewModel.updateTourLog(idx, newLog);
                updateTourInfo();
            });
        }
    }

    @FXML
    private void onDelete() {
        int idx = logTable.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && viewModel != null) {
            TourLogDTO logToDelete = logTable.getItems().get(idx);
            
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Delete");
            confirmDialog.setHeaderText("Delete Tour Log");
            confirmDialog.setContentText("Are you sure you want to delete this tour log?");
            
            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    viewModel.deleteTourLog(idx);
                    updateTourInfo();
                }
            });
        }
    }
} 