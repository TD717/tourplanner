package com.tourplanner.ui.viewmodel;

import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.service.TourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TourListViewModelTest {

    private TestTourService tourService;
    private TourListViewModel viewModel;

    @BeforeEach
    void setUp() {
        tourService = new TestTourService();
        viewModel = new TourListViewModel(tourService);
    }

    @Test
    void testInitialize() {
        // Given
        tourService.addTestTour(new TourDTO(1L, "Test Tour 1", "Description 1", 5.0, "1h 30min"));
        tourService.addTestTour(new TourDTO(2L, "Test Tour 2", "Description 2", 8.0, "2h 15min"));
        // When
        viewModel.initialize();
        // Then
        assertEquals(2, viewModel.getTours().size());
        assertEquals("Test Tour 1", viewModel.getTours().get(0).getName());
        assertEquals("Test Tour 2", viewModel.getTours().get(1).getName());
    }

    @Test
    void testLoadData() {
        // Given
        tourService.addTestTour(new TourDTO(1L, "Test Tour", "Description", 5.0, "1h 30min"));
        // When
        viewModel.loadData();
        // Then
        assertEquals(1, viewModel.getTours().size());
        assertFalse(viewModel.isLoading());
        assertEquals("", viewModel.getErrorMessage());
    }

    @Test
    void testAddTour() {
        // Given
        TourDTO newTour = new TourDTO("New Tour", "New Description", 6.0, "2h 00min");
        // When
        viewModel.addTour(newTour);
        // Then
        assertEquals(1, viewModel.getTours().size());
        assertEquals("New Tour", viewModel.getTours().get(0).getName());
    }

    @Test
    void testUpdateTour() {
        // Given
        TourDTO existingTour = new TourDTO(1L, "Old Tour", "Old Description", 5.0, "1h 30min");
        tourService.addTestTour(existingTour);
        viewModel.getTours().add(existingTour);
        TourDTO updatedTour = new TourDTO(1L, "Updated Tour", "Updated Description", 6.0, "2h 00min");
        // When
        viewModel.updateTour(0, updatedTour);
        // Then
        assertEquals(1, viewModel.getTours().size());
        assertEquals("Updated Tour", viewModel.getTours().get(0).getName());
    }

    @Test
    void testDeleteTour() {
        // Given
        TourDTO tourToDelete = new TourDTO(1L, "Tour to Delete", "Description", 5.0, "1h 30min");
        viewModel.getTours().add(tourToDelete);
        // When
        viewModel.deleteTour(0);
        // Then
        assertTrue(viewModel.getTours().isEmpty());
    }

    @Test
    void testSearchTours() {
        // Given
        tourService.addTestTour(new TourDTO(1L, "Search Result", "Description", 5.0, "1h 30min"));
        // When
        viewModel.searchTours("search");
        // Then
        assertEquals(1, viewModel.getTours().size());
        assertEquals("Search Result", viewModel.getTours().get(0).getName());
    }

    @Test
    void testGetTitle() {
        // When
        String title = viewModel.getTitle();
        // Then
        assertEquals("Tour List", title);
    }

    @Test
    void testClearError() {
        // Given
        viewModel.setError("Test error");
        // When
        viewModel.clearError();
        // Then
        assertEquals("", viewModel.getErrorMessage());
    }

    // Simple test double for TourService
    private static class TestTourService implements TourService {
        private final List<TourDTO> testTours = new ArrayList<>();
        private boolean shouldThrowException = false;

        public void addTestTour(TourDTO tour) {
            testTours.add(tour);
        }

        public void setShouldThrowException(boolean shouldThrow) {
            this.shouldThrowException = shouldThrow;
        }

        @Override
        public List<TourDTO> getAllTours() {
            if (shouldThrowException) {
                throw new RuntimeException("Database error");
            }
            return new ArrayList<>(testTours);
        }

        @Override
        public TourDTO getTourById(Long id) {
            if (shouldThrowException) {
                throw new RuntimeException("Database error");
            }
            return testTours.stream()
                    .filter(tour -> tour.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public TourDTO createTour(TourDTO tourDTO) {
            if (shouldThrowException) {
                throw new RuntimeException("Save error");
            }
            TourDTO savedTour = new TourDTO(
                    (long) (testTours.size() + 1),
                    tourDTO.getName(),
                    tourDTO.getDescription(),
                    tourDTO.getDistance(),
                    tourDTO.getEstimatedTime(),
                    tourDTO.getTransportType(),
                    tourDTO.getFromLocation(),
                    tourDTO.getToLocation()
            );
            testTours.add(savedTour);
            return savedTour;
        }

        @Override
        public TourDTO updateTour(TourDTO tourDTO) {
            if (shouldThrowException) {
                throw new RuntimeException("Update error");
            }
            for (int i = 0; i < testTours.size(); i++) {
                if (testTours.get(i).getId().equals(tourDTO.getId())) {
                    testTours.set(i, tourDTO);
                    return tourDTO;
                }
            }
            throw new IllegalArgumentException("Tour not found");
        }

        @Override
        public void deleteTour(Long id) {
            if (shouldThrowException) {
                throw new RuntimeException("Delete error");
            }
            testTours.removeIf(tour -> tour.getId().equals(id));
        }

        @Override
        public List<TourDTO> searchTours(String searchText) {
            if (shouldThrowException) {
                throw new RuntimeException("Search error");
            }
            if (searchText == null || searchText.trim().isEmpty()) {
                return new ArrayList<>(testTours);
            }
            return testTours.stream()
                    .filter(tour -> tour.getName().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }
    }
} 