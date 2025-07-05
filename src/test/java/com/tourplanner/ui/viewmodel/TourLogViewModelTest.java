package com.tourplanner.ui.viewmodel;

import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.dto.TourLogDTO;
import com.tourplanner.backend.service.TourLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TourLogViewModelTest {

    private TestTourLogService tourLogService;
    private TourLogViewModel viewModel;
    private TourDTO testTour;
    private TourLogDTO testLog;

    @BeforeEach
    void setUp() {
        tourLogService = new TestTourLogService();
        viewModel = new TourLogViewModel(tourLogService);
        testTour = new TourDTO(1L, "Test Tour", "Test Description", 10.0, "2h 30m");
        testLog = new TourLogDTO(1L, 1L, LocalDateTime.now(), "Test log", 3.0, 10.5, 2.5, 4.0);
    }

    @Test
    void testSetSelectedTour() {
        // Given
        tourLogService.addTestLog(testLog);
        // When
        viewModel.setSelectedTour(testTour);
        // Then
        assertEquals(testTour, viewModel.getSelectedTour());
        assertEquals(1, viewModel.getTourLogs().size());
        assertEquals("Tour Logs - Test Tour", viewModel.getTitle());
    }

    @Test
    void testSetSelectedTourNull() {
        // When
        viewModel.setSelectedTour(null);
        // Then
        assertNull(viewModel.getSelectedTour());
        assertTrue(viewModel.getTourLogs().isEmpty());
        assertEquals("Tour Logs", viewModel.getTitle());
    }

    @Test
    void testAddTourLogWithSelectedTour() {
        // Given
        viewModel.setSelectedTour(testTour);
        TourLogDTO newLog = new TourLogDTO(null, null, LocalDateTime.now(), "New log", 2.0, 8.0, 1.5, 5.0);
        // When
        viewModel.addTourLog(newLog);
        // Then
        assertEquals(1, viewModel.getTourLogs().size());
        assertEquals(1L, viewModel.getTourLogs().get(0).getTourId());
    }

    @Test
    void testAddTourLogWithoutSelectedTour() {
        // Given
        viewModel.getTourLogs().clear(); // Ensure clean state
        TourLogDTO newLog = new TourLogDTO(null, null, LocalDateTime.now(), "New log", 2.0, 8.0, 1.5, 5.0);
        // When
        viewModel.addTourLog(newLog);
        // Then
        assertEquals(0, viewModel.getTourLogs().size());
    }

    @Test
    void testSearchTourLogsWithSelectedTour() {
        // Given
        viewModel.setSelectedTour(testTour);
        TourLogDTO log1 = new TourLogDTO(1L, 1L, LocalDateTime.now(), "Test log 1", 3.0, 10.5, 2.5, 4.0);
        TourLogDTO log2 = new TourLogDTO(2L, 2L, LocalDateTime.now(), "Test log 2", 4.0, 12.0, 3.0, 5.0);
        tourLogService.addTestLog(log1);
        tourLogService.addTestLog(log2);
        // When
        viewModel.searchTourLogs("test");
        // Then
        assertEquals(1, viewModel.getTourLogs().size()); // Only logs for selected tour
        assertEquals(1L, viewModel.getTourLogs().get(0).getTourId());
    }

    @Test
    void testSearchTourLogsWithoutSelectedTour() {
        // Given
        tourLogService.addTestLog(testLog);
        // When
        viewModel.searchTourLogs("test");
        // Then
        assertEquals(1, viewModel.getTourLogs().size());
    }

    @Test
    void testDispose() {
        // Given
        viewModel.setSelectedTour(testTour);
        viewModel.getTourLogs().add(testLog);
        // When
        viewModel.dispose();
        // Then
        assertNull(viewModel.getSelectedTour());
        assertTrue(viewModel.getTourLogs().isEmpty());
    }

    @Test
    void testLoadDataWithSelectedTour() {
        // Given
        viewModel.setSelectedTour(testTour);
        tourLogService.addTestLog(testLog);
        // When
        viewModel.loadData();
        // Then
        assertEquals(1, viewModel.getTourLogs().size());
    }

    @Test
    void testLoadDataWithoutSelectedTour() {
        // Given
        tourLogService.addTestLog(testLog);
        // When
        viewModel.loadData();
        // Then
        assertEquals(1, viewModel.getTourLogs().size());
    }

    // Simple test double for TourLogService
    private static class TestTourLogService implements TourLogService {
        private final List<TourLogDTO> testLogs = new ArrayList<>();
        private boolean shouldThrowException = false;

        public void addTestLog(TourLogDTO log) {
            testLogs.add(log);
        }

        public void setShouldThrowException(boolean shouldThrow) {
            this.shouldThrowException = shouldThrow;
        }

        @Override
        public List<TourLogDTO> getAllTourLogs() {
            if (shouldThrowException) {
                throw new RuntimeException("Database error");
            }
            return new ArrayList<>(testLogs);
        }



        @Override
        public List<TourLogDTO> getTourLogsByTourId(Long tourId) {
            if (shouldThrowException) {
                throw new RuntimeException("Database error");
            }
            return testLogs.stream()
                    .filter(log -> log.getTourId().equals(tourId))
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public TourLogDTO createTourLog(TourLogDTO tourLogDTO) {
            if (shouldThrowException) {
                throw new RuntimeException("Save error");
            }
            TourLogDTO savedLog = new TourLogDTO(
                    (long) (testLogs.size() + 1),
                    tourLogDTO.getTourId(),
                    tourLogDTO.getDateTime(),
                    tourLogDTO.getComment(),
                    tourLogDTO.getDifficulty(),
                    tourLogDTO.getTotalDistance(),
                    tourLogDTO.getTotalTime(),
                    tourLogDTO.getRating()
            );
            testLogs.add(savedLog);
            return savedLog;
        }

        @Override
        public TourLogDTO updateTourLog(TourLogDTO tourLogDTO) {
            if (shouldThrowException) {
                throw new RuntimeException("Update error");
            }
            for (int i = 0; i < testLogs.size(); i++) {
                if (testLogs.get(i).getId().equals(tourLogDTO.getId())) {
                    testLogs.set(i, tourLogDTO);
                    return tourLogDTO;
                }
            }
            throw new IllegalArgumentException("Tour log not found");
        }

        @Override
        public void deleteTourLog(Long id) {
            if (shouldThrowException) {
                throw new RuntimeException("Delete error");
            }
            testLogs.removeIf(log -> log.getId().equals(id));
        }

        @Override
        public List<TourLogDTO> searchTourLogs(String searchText) {
            if (shouldThrowException) {
                throw new RuntimeException("Search error");
            }
            if (searchText == null || searchText.trim().isEmpty()) {
                return new ArrayList<>(testLogs);
            }
            return testLogs.stream()
                    .filter(log -> log.getComment().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public Long getTourLogCountByTourId(Long tourId) {
            if (shouldThrowException) {
                throw new RuntimeException("Database error");
            }
            return testLogs.stream()
                    .filter(log -> log.getTourId().equals(tourId))
                    .count();
        }
    }
} 