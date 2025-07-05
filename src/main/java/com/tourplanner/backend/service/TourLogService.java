package com.tourplanner.backend.service;

import com.tourplanner.backend.dto.TourLogDTO;
import java.time.LocalDateTime;
import java.util.List;

// Service interface for tour log.
public interface TourLogService {

    List<TourLogDTO> getAllTourLogs();

    List<TourLogDTO> getTourLogsByTourId(Long tourId);

    TourLogDTO createTourLog(TourLogDTO tourLog);

    TourLogDTO updateTourLog(TourLogDTO tourLog);


    void deleteTourLog(Long id);

    List<TourLogDTO> searchTourLogs(String searchText);

    Long getTourLogCountByTourId(Long tourId);
} 