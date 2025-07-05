package com.tourplanner.ui.viewmodel;

import com.tourplanner.backend.dto.TourLogDTO;
import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.service.TourLogService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

public class TourLogViewModel extends BaseViewModel {
    private final TourLogService tourLogService;
    private final List<TourLogDTO> tourLogs = new ArrayList<>();
    private final ObjectProperty<TourDTO> selectedTourProperty = new SimpleObjectProperty<>();
    private TourDTO selectedTour;
    private TourLogDTO selectedTourLog;
    private String errorMessage = "";
    private boolean loading = false;
    private TourStatisticsViewModel statisticsViewModel; // Reference to statistics view model

    public TourLogViewModel(TourLogService tourLogService) {
        this.tourLogService = tourLogService;
    }

    // Set the statistics view model for notifications
    public void setStatisticsViewModel(TourStatisticsViewModel statisticsViewModel) {
        this.statisticsViewModel = statisticsViewModel;
    }

    public void setSelectedTour(TourDTO tour) {
        this.selectedTour = tour;
        selectedTourProperty.set(tour);
        if (tour != null) {
            loadLogsForTour(tour.getId());
        } else {
            tourLogs.clear();
        }
    }

    public ObjectProperty<TourDTO> selectedTourProperty() {
        return selectedTourProperty;
    }

    public TourDTO getSelectedTour() {
        return selectedTour;
    }

    public void loadLogsForTour(Long tourId) {
        loading = true;
        try {
            tourLogs.clear();
            tourLogs.addAll(tourLogService.getTourLogsByTourId(tourId));
            errorMessage = "";
        } catch (Exception e) {
            errorMessage = "Failed to load tour logs: " + e.getMessage();
        } finally {
            loading = false;
        }
    }

    public void addTourLog(TourLogDTO log) {
        try {
            // If no tour ID is provided, use the currently selected tour
            if (log.getTourId() == null) {
                if (selectedTour == null) {
                    errorMessage = "No tour selected and no tour ID provided";
                    return;
                }
                log.setTourId(selectedTour.getId());
            }
            
            TourLogDTO saved = tourLogService.createTourLog(log);
            
            // Only add to the current list if it's for the currently selected tour
            // or if no tour is currently selected (show all logs)
            if (selectedTour == null || selectedTour.getId().equals(saved.getTourId())) {
                tourLogs.add(saved);
            }
            
            // Refresh statistics after adding a log
            if (statisticsViewModel != null) {
                statisticsViewModel.refreshStatistics();
            }
            
            errorMessage = "";
        } catch (Exception e) {
            errorMessage = "Failed to add tour log: " + e.getMessage();
        }
    }

    public void updateTourLog(int index, TourLogDTO log) {
        try {
            // Ensure the log has a valid ID for updating
            if (log.getId() == null) {
                errorMessage = "Cannot update log without ID";
                return;
            }
            
            // If no tour ID is provided, use the currently selected tour
            if (log.getTourId() == null && selectedTour != null) {
                log.setTourId(selectedTour.getId());
            }
            
            TourLogDTO updated = tourLogService.updateTourLog(log);
            tourLogs.set(index, updated);
            
            // Refresh statistics after updating a log
            if (statisticsViewModel != null) {
                statisticsViewModel.refreshStatistics();
            }
            
            errorMessage = "";
        } catch (Exception e) {
            errorMessage = "Failed to update tour log: " + e.getMessage();
        }
    }

    public void deleteTourLog(int index) {
        try {
            TourLogDTO log = tourLogs.get(index);
            tourLogService.deleteTourLog(log.getId());
            tourLogs.remove(index);
            
            // Refresh statistics after deleting a log
            if (statisticsViewModel != null) {
                statisticsViewModel.refreshStatistics();
            }
            
            errorMessage = "";
        } catch (Exception e) {
            errorMessage = "Failed to delete tour log: " + e.getMessage();
        }
    }

    public void searchTourLogs(String searchText) {
        loading = true;
        try {
            if (selectedTour != null) {
                tourLogs.clear();
                List<TourLogDTO> searchResults = tourLogService.searchTourLogs(searchText);
                searchResults.stream()
                    .filter(log -> selectedTour.getId().equals(log.getTourId()))
                    .forEach(tourLogs::add);
            } else {
                tourLogs.clear();
                tourLogs.addAll(tourLogService.searchTourLogs(searchText));
            }
            errorMessage = "";
        } catch (Exception e) {
            errorMessage = "Failed to search tour logs: " + e.getMessage();
        } finally {
            loading = false;
        }
    }

    public void refresh(Long tourId) {
        if (tourId != null) {
            loadLogsForTour(tourId);
        } else if (selectedTour != null) {
            loadLogsForTour(selectedTour.getId());
        }
    }

    public List<TourLogDTO> getTourLogs() {
        return tourLogs;
    }

    public TourLogDTO getSelectedTourLog() {
        return selectedTourLog;
    }

    public void setSelectedTourLog(TourLogDTO selectedTourLog) {
        this.selectedTourLog = selectedTourLog;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isLoading() {
        return loading;
    }

    public void clearError() {
        errorMessage = "";
    }

    @Override
    public String getTitle() {
        return selectedTour != null ? "Tour Logs - " + selectedTour.getName() : "Tour Logs";
    }

    @Override
    public void dispose() {
        tourLogs.clear();
        selectedTourLog = null;
        selectedTour = null;
    }

    @Override
    public void initialize() {
        loadData();
    }

    @Override
    public void loadData() {
        loading = true;
        try {
            if (selectedTour != null) {
                loadLogsForTour(selectedTour.getId());
            } else {
                tourLogs.clear();
                tourLogs.addAll(tourLogService.getAllTourLogs());
            }
            errorMessage = "";
        } catch (Exception e) {
            errorMessage = "Failed to load tour logs: " + e.getMessage();
        } finally {
            loading = false;
        }
    }
} 