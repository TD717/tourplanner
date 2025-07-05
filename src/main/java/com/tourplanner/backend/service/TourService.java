package com.tourplanner.backend.service;

import com.tourplanner.backend.dto.TourDTO;
import java.util.List;

// Service interface for tour business operations.
public interface TourService {

    List<TourDTO> getAllTours();
    TourDTO getTourById(Long id);
    TourDTO createTour(TourDTO tour);
    TourDTO updateTour(TourDTO tour);
    void deleteTour(Long id);
    List<TourDTO> searchTours(String searchText);
}