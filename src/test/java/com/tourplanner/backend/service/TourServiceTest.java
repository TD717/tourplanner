package com.tourplanner.backend.service;

import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TourServiceTest {

    @Autowired
    private TourService tourService;

    @Autowired
    private TourRepository tourRepository;

    private TourDTO testTour1;
    private TourDTO testTour2;

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        tourRepository.deleteAll();
        
        testTour1 = new TourDTO("Test Tour 1", "Description 1", 5.0, "1h 30min");
        testTour2 = new TourDTO("Test Tour 2", "Description 2", 8.0, "2h 15min");
    }

    @Test
    void testGetAllTours() {
        // Given
        TourDTO savedTour1 = tourService.createTour(testTour1);
        TourDTO savedTour2 = tourService.createTour(testTour2);

        // When
        List<TourDTO> result = tourService.getAllTours();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(tour -> "Test Tour 1".equals(tour.getName())));
        assertTrue(result.stream().anyMatch(tour -> "Test Tour 2".equals(tour.getName())));
    }

    @Test
    void testGetAllToursEmpty() {
        // When
        List<TourDTO> result = tourService.getAllTours();
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTourById() {
        // Given
        TourDTO savedTour = tourService.createTour(testTour1);
        // When
        TourDTO result = tourService.getTourById(savedTour.getId());
        // Then
        assertNotNull(result);
        assertEquals("Test Tour 1", result.getName());
        assertEquals(savedTour.getId(), result.getId());
    }

    @Test
    void testGetTourByIdNotFound() {
        // When
        TourDTO result = tourService.getTourById(999L);
        // Then
        assertNull(result);
    }

    @Test
    void testCreateTour() {
        // When
        TourDTO result = tourService.createTour(testTour1);
        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Tour 1", result.getName());
        assertEquals("Description 1", result.getDescription());
        assertEquals(5.0, result.getDistance());
        assertEquals("1h 30min", result.getEstimatedTime());
    }

    @Test
    void testUpdateTour() {
        // Given
        TourDTO savedTour = tourService.createTour(testTour1);
        TourDTO updatedTour = new TourDTO(savedTour.getId(), "Updated Tour", "Updated Description", 7.0, "2h 30min");
        // When
        TourDTO result = tourService.updateTour(updatedTour);
        // Then
        assertNotNull(result);
        assertEquals("Updated Tour", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(7.0, result.getDistance());
        assertEquals("2h 30min", result.getEstimatedTime());
    }

    @Test
    void testUpdateTourNotFound() {
        // Given
        TourDTO nonExistentTour = new TourDTO(999L, "Non-existent Tour", "Description", 5.0, "1h 30min");
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            tourService.updateTour(nonExistentTour);
        });
    }

    @Test
    void testDeleteTour() {
        // Given
        TourDTO savedTour = tourService.createTour(testTour1);
        // When
        tourService.deleteTour(savedTour.getId());
        // Then
        assertNull(tourService.getTourById(savedTour.getId()));
    }

    @Test
    void testSearchTours() {
        // Given
        tourService.createTour(testTour1);
        tourService.createTour(testTour2);
        // When
        List<TourDTO> result = tourService.searchTours("Test Tour 1");
        // Then
        assertEquals(1, result.size());
        assertEquals("Test Tour 1", result.get(0).getName());
    }

} 