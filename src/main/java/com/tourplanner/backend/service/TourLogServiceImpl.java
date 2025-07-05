package com.tourplanner.backend.service;

import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.model.TourLog;
import com.tourplanner.backend.dto.TourLogDTO;
import com.tourplanner.backend.repository.TourLogRepository;
import com.tourplanner.backend.repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TourLogServiceImpl implements TourLogService {
    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;

    @Autowired
    public TourLogServiceImpl(TourLogRepository tourLogRepository, TourRepository tourRepository) {
        this.tourLogRepository = tourLogRepository;
        this.tourRepository = tourRepository;
    }

    @Override
    public List<TourLogDTO> getAllTourLogs() {
        return tourLogRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TourLogDTO> getTourLogsByTourId(Long tourId) {
        return tourLogRepository.findByTourId(tourId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TourLogDTO createTourLog(TourLogDTO dto) {
        Tour tour = tourRepository.findById(dto.getTourId()).orElseThrow(() -> new IllegalArgumentException("Tour not found"));
        TourLog log = toEntity(dto);
        log.setTour(tour);
        TourLog saved = tourLogRepository.save(log);
        return toDTO(saved);
    }

    @Override
    public TourLogDTO updateTourLog(TourLogDTO dto) {
        TourLog log = tourLogRepository.findById(dto.getId()).orElseThrow(() -> new IllegalArgumentException("TourLog not found"));
        
        // Update all fields
        log.setDateTime(dto.getDateTime());
        log.setComment(dto.getComment());
        log.setDifficulty(dto.getDifficulty());
        log.setTotalDistance(dto.getTotalDistance());
        log.setTotalTime(dto.getTotalTime());
        log.setRating(dto.getRating());
        
        // Update tour if it has changed
        if (dto.getTourId() != null && (log.getTour() == null || !log.getTour().getId().equals(dto.getTourId()))) {
            Tour newTour = tourRepository.findById(dto.getTourId()).orElseThrow(() -> new IllegalArgumentException("Tour not found"));
            log.setTour(newTour);
        }
        
        TourLog saved = tourLogRepository.save(log);
        return toDTO(saved);
    }

    @Override
    public void deleteTourLog(Long id) {
        tourLogRepository.deleteById(id);
    }

    @Override
    public List<TourLogDTO> searchTourLogs(String searchText) {
        return tourLogRepository.findByCommentContainingIgnoreCase(searchText).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Long getTourLogCountByTourId(Long tourId) {
        Long count = tourLogRepository.countByTourId(tourId);
        return count != null ? count : 0L;
    }

    // --- Helper conversion methods ---
    private TourLogDTO toDTO(TourLog log) {
        return new TourLogDTO(
                log.getId(),
                log.getTour() != null ? log.getTour().getId() : null,
                log.getDateTime(),
                log.getComment(),
                log.getDifficulty(),
                log.getTotalDistance(),
                log.getTotalTime(),
                log.getRating()
        );
    }

    private TourLog toEntity(TourLogDTO dto) {
        TourLog log = new TourLog();
        log.setId(dto.getId());
        log.setDateTime(dto.getDateTime());
        log.setComment(dto.getComment());
        log.setDifficulty(dto.getDifficulty());
        log.setTotalDistance(dto.getTotalDistance());
        log.setTotalTime(dto.getTotalTime());
        log.setRating(dto.getRating());
        return log;
    }
} 