package com.tourplanner.ui.viewmodel;

import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.dto.TourLogDTO;
import com.tourplanner.backend.service.TourLogService;
import com.tourplanner.backend.service.TourService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

public class TourStatisticsViewModel extends BaseViewModel {
    private final TourService tourService;
    private final TourLogService tourLogService;

    private int totalTours;
    private int totalLogs;
    private double averageDistance;
    private double averageRating;
    private TourDTO mostPopularTour;
    private int mostPopularTourLogCount;
    private String errorMessage = "";

    public TourStatisticsViewModel(TourService tourService, TourLogService tourLogService) {
        this.tourService = tourService;
        this.tourLogService = tourLogService;
    }

    @Override
    public void initialize() {
        loadData();
    }

    @Override
    public void loadData() {
        try {
            List<TourDTO> tours = tourService.getAllTours();
            List<TourLogDTO> logs = tourLogService.getAllTourLogs();
            totalTours = tours.size();
            totalLogs = logs.size();
            averageDistance = tours.stream().mapToDouble(TourDTO::getDistance).average().orElse(0.0);
            averageRating = logs.stream().mapToDouble(l -> l.getRating() != null ? l.getRating() : 0.0).average().orElse(0.0);
            Optional<TourDTO> mostPopular = tours.stream().max(Comparator.comparingInt(t -> tourLogService.getTourLogCountByTourId(t.getId()).intValue()));
            if (mostPopular.isPresent()) {
                mostPopularTour = mostPopular.get();
                mostPopularTourLogCount = tourLogService.getTourLogCountByTourId(mostPopularTour.getId()).intValue();
            } else {
                mostPopularTour = null;
                mostPopularTourLogCount = 0;
            }
            errorMessage = "";
        } catch (Exception e) {
            errorMessage = "Failed to load statistics: " + e.getMessage();
        }
    }

    public int getTotalTours() { return totalTours; }
    public int getTotalLogs() { return totalLogs; }
    public double getAverageDistance() { return averageDistance; }
    public double getAverageRating() { return averageRating; }
    public TourDTO getMostPopularTour() { return mostPopularTour; }
    public int getMostPopularTourLogCount() { return mostPopularTourLogCount; }
    public String getErrorMessage() { return errorMessage; }
    
    // Get log count for a specific tour
    public int getTourLogCount(Long tourId) {
        return tourLogService.getTourLogCountByTourId(tourId).intValue();
    }

    @Override
    public String getTitle() { return "Tour Statistics"; }

    @Override
    public void dispose() {}

    // Method to refresh statistics when tour logs change
    public void refreshStatistics() {
        loadData();
    }

    // Returns a map from TourDTO to a stats object for each tour.
    public Map<TourDTO, TourStats> getTourStats() {
        List<TourDTO> tours = tourService.getAllTours();
        List<TourLogDTO> logs = tourLogService.getAllTourLogs();
        Map<Long, List<TourLogDTO>> logsByTour = logs.stream().collect(Collectors.groupingBy(TourLogDTO::getTourId));
        Map<TourDTO, TourStats> stats = new HashMap<>();

        for (TourDTO tour : tours) {
            List<TourLogDTO> tourLogs = logsByTour.getOrDefault(tour.getId(), List.of());
            double avgTime = tourLogs.stream().mapToDouble(l -> l.getTotalTime() != null ? l.getTotalTime() : 0.0).average().orElse(0.0);
            double avgDistance = tourLogs.stream().mapToDouble(l -> l.getTotalDistance() != null ? l.getTotalDistance() : 0.0).average().orElse(0.0);
            double avgRating = tourLogs.stream().mapToDouble(l -> l.getRating() != null ? l.getRating() : 0.0).average().orElse(0.0);
            stats.put(tour, new TourStats(avgTime, avgDistance, avgRating));
        }
        return stats;
    }

    public static class TourStats {
        public final double avgTime;
        public final double avgDistance;
        public final double avgRating;
        public TourStats(double avgTime, double avgDistance, double avgRating) {
            this.avgTime = avgTime;
            this.avgDistance = avgDistance;
            this.avgRating = avgRating;
        }
    }
} 