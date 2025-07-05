package com.tourplanner.ui.view;

import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.service.MapService;
import com.tourplanner.backend.service.PdfGenerator;
import com.tourplanner.backend.service.TourLogService;
import com.tourplanner.backend.service.RouteService;
import com.tourplanner.backend.service.ImportExportService;
import com.tourplanner.ui.viewmodel.TourListViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// View controller for the Tour List following MVVM pattern
public class TourListView {

    @FXML private ListView<TourDTO> tourList;
    @FXML private Button editBtn, deleteBtn, addBtn, pdfBtn, importBtn, exportBtn;
    @FXML private TextField searchField;
    @FXML private ProgressIndicator loadingIndicator;

    private TourListViewModel viewModel;
    private MapService mapService;
    private TourLogService tourLogService;
    private RouteService routeService;
    private ImportExportService importExportService;

    // Set the ViewModel for this view, called by ViewFactory
    public void setViewModel(TourListViewModel viewModel) {
        this.viewModel = viewModel;
        if (tourList != null) {
            bindToViewModel();
        }
        // Enable/disable PDF button based on selection
        pdfBtn.disableProperty().bind(selectedTourProperty().isNull());
    }

    @FXML
    private void initialize() {
        if (viewModel != null) {
            bindToViewModel();
        }
    }

    private void bindToViewModel() {
        if (viewModel == null || tourList == null) return;
        // Bind list items
        tourList.setItems(viewModel.getTours());
        // Bind button states
        editBtn.disableProperty().bind(selectedTourProperty().isNull());
        deleteBtn.disableProperty().bind(selectedTourProperty().isNull());
        // Bind selection
        tourList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            setSelectedTour(newVal);
        });
        // Bind search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                viewModel.searchTours(newVal.trim());
            } else {
                viewModel.loadData();
            }
        });
        // Bind loading state
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        // Initialize the ViewModel
        viewModel.initialize();
    }

    // Get the selected tour property for binding.
    public ObjectProperty<TourDTO> selectedTourProperty() {
        return viewModel.selectedTourProperty();
    }

    // Set the selected tour
    public void setSelectedTour(TourDTO tour) {
        viewModel.setSelectedTour(tour);
    }

    public void setMapService(MapService mapService) {
        this.mapService = mapService;
    }

    public void setTourLogService(TourLogService tourLogService) {
        this.tourLogService = tourLogService;
    }

    public void setRouteService(RouteService routeService) {
        this.routeService = routeService;
    }

    public void setImportExportService(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    @FXML
    private void onAdd() {
        if (mapService == null) {
            new Alert(Alert.AlertType.ERROR, "Map service not available").showAndWait();
            return;
        }
        
        TourEditorDialog.showAddDialog(mapService).ifPresent(tour -> {
            viewModel.addTour(tour);
        });
    }

    @FXML
    private void onEdit() {
        TourDTO selected = viewModel.getSelectedTour();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a tour to edit").showAndWait();
            return;
        }
        
        if (mapService == null) {
            new Alert(Alert.AlertType.ERROR, "Map service not available").showAndWait();
            return;
        }
        
        TourEditorDialog.showEditDialog(selected, mapService).ifPresent(updatedTour -> {
            int index = viewModel.getTours().indexOf(selected);
            if (index >= 0) {
                viewModel.updateTour(index, updatedTour);
            }
        });
    }

    @FXML
    private void onDelete() {
        TourDTO selected = viewModel.getSelectedTour();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a tour to delete").showAndWait();
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Tour");
        confirmDialog.setContentText("Are you sure you want to delete the tour '" + selected.getName() + "'?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int index = viewModel.getTours().indexOf(selected);
                if (index >= 0) {
                    viewModel.deleteTour(index);
                }
            }
        });
    }

    @FXML
    private void onRefresh() {
        viewModel.refresh();
    }

    @FXML
    private void onGeneratePdf() {
        TourDTO selectedTour = viewModel.getSelectedTour();
        if (selectedTour == null) {
            showError("Please select a tour from the list before generating a PDF report.");
            return;
        }
        if (tourLogService == null) {
            showError("TourLogService is not available.");
            return;
        }
        // Fetch logs for the selected tour
        java.util.List<com.tourplanner.backend.dto.TourLogDTO> logs = tourLogService.getTourLogsByTourId(selectedTour.getId());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.setInitialFileName(selectedTour.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_report.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(pdfBtn.getScene().getWindow());
        if (file != null) {
            try {
                PdfGenerator.generateTourReport(file, selectedTour, logs, null);
                showInfo("PDF report generated successfully!\nSaved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                showError("Failed to generate PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Tours");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(importBtn.getScene().getWindow());
        if (file != null && importExportService != null) {
            java.util.List<com.tourplanner.backend.dto.TourDTO> importedTours = importExportService.importToursFromJson(file.getAbsolutePath());
            if (importedTours != null && !importedTours.isEmpty()) {
                for (com.tourplanner.backend.dto.TourDTO tour : importedTours) {
                    viewModel.addTour(tour);
                }
                showInfo("Tours imported successfully!");
                viewModel.refresh();
            } else {
                showError("Failed to import tours or file is empty.");
            }
        }
    }

    @FXML
    private void onExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Tours");
        fileChooser.setInitialFileName("tours_export.json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());
        if (file != null && importExportService != null) {
            boolean success = importExportService.exportToursToJson(viewModel.getTours(), file.getAbsolutePath());
            if (success) {
                showInfo("Tours exported successfully!\nSaved to: " + file.getAbsolutePath());
            } else {
                showError("Failed to export tours.");
            }
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Success");
        alert.setHeaderText("PDF Generation");
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.showAndWait();
    }

    // Public getter for the tourList ListView.
    public ListView<TourDTO> getTourList() {
        return tourList;
    }
}
