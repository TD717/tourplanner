package com.tourplanner.ui.viewmodel;

import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.service.TourService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.logging.Level;

// ViewModel for the Tour List view following MVVM pattern. Manages the list of tours and provides data binding for the UI.
public class TourListViewModel extends BaseViewModel {

    private final TourService tourService;
    private final ObservableList<TourDTO> tours = FXCollections.observableArrayList();
    private final ObjectProperty<TourDTO> selectedTour = new SimpleObjectProperty<>();

    public TourListViewModel(TourService tourService) {
        this.tourService = tourService;
        setTitle("Tour List");
    }

    @Override
    public void initialize() {
        logger.fine("Initializing TourListViewModel");
        loadData();
    }

    @Override
    public void dispose() {
        logger.fine("Disposing TourListViewModel");
        tours.clear();
        selectedTour.set(null);
    }

    @Override
    public void loadData() {
        setLoading(true);
        clearError();
        
        try {
            // Load tours from service
            var tourList = tourService.getAllTours();
            tours.clear();
            tours.addAll(tourList);
            
            logger.fine("Loaded " + tours.size() + " tours");
        } catch (Exception e) {
            setError("Failed to load tours: " + e.getMessage());
            logger.log(Level.SEVERE, "Error loading tours", e);
        } finally {
            setLoading(false);
        }
    }

    // Add a new tour to the list.
    public void addTour(TourDTO tour) {
        if (tour != null) {
            try {
                TourDTO savedTour = tourService.createTour(tour);
                tours.add(savedTour);
                logger.fine("Added tour: " + savedTour.getName());
            } catch (Exception e) {
                setError("Failed to add tour: " + e.getMessage());
                logger.log(Level.SEVERE, "Error adding tour", e);
            }
        }
    }

    // Update an existing tour in the list.
    public void updateTour(int index, TourDTO updatedTour) {
        if (index >= 0 && index < tours.size() && updatedTour != null) {
            try {
                TourDTO savedTour = tourService.updateTour(updatedTour);
                tours.set(index, savedTour);
                logger.fine("Updated tour: " + savedTour.getName());
            } catch (Exception e) {
                setError("Failed to update tour: " + e.getMessage());
                logger.log(Level.SEVERE, "Error updating tour", e);
            }
        }
    }

    // Delete a tour from the list.
    public void deleteTour(int index) {
        if (index >= 0 && index < tours.size()) {
            try {
                TourDTO tourToDelete = tours.get(index);
                tourService.deleteTour(tourToDelete.getId());
            tours.remove(index);
                logger.fine("Deleted tour: " + tourToDelete.getName());
            } catch (Exception e) {
                setError("Failed to delete tour: " + e.getMessage());
                logger.log(Level.SEVERE, "Error deleting tour", e);
            }
        }
    }

    // Search tours by text.
    public void searchTours(String searchText) {
        setLoading(true);
        clearError();
        
        try {
            var searchResults = tourService.searchTours(searchText);
            tours.clear();
            tours.addAll(searchResults);
            logger.fine("Search completed with " + tours.size() + " results");
        } catch (Exception e) {
            setError("Search failed: " + e.getMessage());
            logger.log(Level.SEVERE, "Error searching tours", e);
        } finally {
            setLoading(false);
        }
    }

    // Getters
    public ObservableList<TourDTO> getTours() {
        return tours;
    }

    public TourDTO getSelectedTour() {
        return selectedTour.get();
    }

    public void setSelectedTour(TourDTO tour) {
        selectedTour.set(tour);
    }

    public ObjectProperty<TourDTO> selectedTourProperty() {
        return selectedTour;
    }
}
