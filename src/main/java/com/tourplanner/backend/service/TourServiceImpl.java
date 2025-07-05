package com.tourplanner.backend.service;

import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Implementation of TourService using JPA entities and repositories.
@Service
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final RouteService routeService;

    @Autowired
    public TourServiceImpl(TourRepository tourRepository, RouteService routeService) {
        this.tourRepository = tourRepository;
        this.routeService = routeService;
    }

    @Override
    // Get all Tours from the DB, convert them to DTOs and return the list of DTOs
    public List<TourDTO> getAllTours() {
        return tourRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    // Optional <List> will hold the entity if found or remain empty if no row matches.
    public TourDTO getTourById(Long id) {
        Optional<Tour> tour = tourRepository.findById(id);
        return tour.map(this::convertToDTO).orElse(null);
    }

    @Override
    public TourDTO createTour(TourDTO tourDTO) {
        Tour tour = convertToEntity(tourDTO);

        // Call RouteService to get distance and estimated time
        if (tour.getFromLocation() != null && tour.getToLocation() != null && tour.getTransportType() != null) {
            var route = routeService.getRouteData(tour.getFromLocation(), tour.getToLocation(), tour.getTransportType());
            if (route != null) {
                tour.setDistance(route.getDistance());
                double hours = Math.floor(route.getDuration());
                double minutes = Math.round((route.getDuration() - hours) * 60);
                String formattedTime = (int)hours + "h " + (int)minutes + "m";
                tour.setEstimatedTime(formattedTime);
            }
        }
        Tour savedTour = tourRepository.save(tour);
        return convertToDTO(savedTour);
    }

    @Override
    public TourDTO updateTour(TourDTO tourDTO) {
        if (tourDTO.getId() == null) {
            throw new IllegalArgumentException("Tour ID cannot be null for update");
        }
        
        Optional<Tour> existingTour = tourRepository.findById(tourDTO.getId());
        if (existingTour.isEmpty()) {
            throw new IllegalArgumentException("Tour not found with ID: " + tourDTO.getId());
        }
        
        Tour tour = existingTour.get();

        // Detect whether route fields changed
        boolean recalc = false;
        if (!equalsOrNull(tour.getFromLocation(), tourDTO.getFromLocation()) ||
            !equalsOrNull(tour.getToLocation(), tourDTO.getToLocation()) ||
            !equalsOrNull(tour.getTransportType(), tourDTO.getTransportType())) {
            recalc = true;
        }
        updateTourFromDTO(tour, tourDTO);

        // If route-relevant fields changed and are all present, re-query the route service
        if (recalc && tour.getFromLocation() != null && tour.getToLocation() != null && tour.getTransportType() != null) {
            var route = routeService.getRouteData(tour.getFromLocation(), tour.getToLocation(), tour.getTransportType());
            if (route != null) {
                tour.setDistance(route.getDistance());
                double hours = Math.floor(route.getDuration());
                double minutes = Math.round((route.getDuration() - hours) * 60);
                String formattedTime = (int)hours + "h " + (int)minutes + "m";
                tour.setEstimatedTime(formattedTime);
            }
        }
        Tour savedTour = tourRepository.save(tour);
        return convertToDTO(savedTour);
    }

    @Override
    public void deleteTour(Long id) {
        if (!tourRepository.existsById(id)) {
            throw new IllegalArgumentException("Tour not found with ID: " + id);
        }
        tourRepository.deleteById(id);
    }

    @Override
    public List<TourDTO> searchTours(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllTours();
        }
        
        List<Tour> tours = tourRepository.findByNameContainingIgnoreCase(searchText.trim());
        return tours.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Conversion methods
    private TourDTO convertToDTO(Tour tour) {
        TourDTO dto = new TourDTO(
                tour.getId(),
                tour.getName(),
                tour.getDescription(),
                tour.getDistance(),
                tour.getEstimatedTime(),
                tour.getTransportType(),
                tour.getFromLocation(),
                tour.getToLocation()
        );
        return dto;
    }

    private Tour convertToEntity(TourDTO dto) {
        Tour tour = new Tour(
                dto.getName(),
                dto.getDescription(),
                dto.getDistance(),
                dto.getEstimatedTime(),
                dto.getTransportType(),
                dto.getFromLocation(),
                dto.getToLocation()
        );
        if (dto.getId() != null) {
            tour.setId(dto.getId());
        }
        return tour;
    }

    private void updateTourFromDTO(Tour tour, TourDTO dto) {
        tour.setName(dto.getName());
        tour.setDescription(dto.getDescription());
        tour.setDistance(dto.getDistance());
        tour.setEstimatedTime(dto.getEstimatedTime());
        tour.setTransportType(dto.getTransportType());
        tour.setFromLocation(dto.getFromLocation());
        tour.setToLocation(dto.getToLocation());
    }

    private boolean equalsOrNull(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
} 