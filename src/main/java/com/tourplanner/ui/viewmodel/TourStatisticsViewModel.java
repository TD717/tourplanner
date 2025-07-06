package com.tourplanner.ui.viewmodel;

import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.dto.TourLogDTO;
import com.tourplanner.backend.service.TourLogService;
import com.tourplanner.backend.service.TourService;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

public class TourStatisticsViewModel extends BaseViewModel {
    private final TourService tourService;
    private final TourLogService tourLogService;

    private final IntegerProperty totalTours = new SimpleIntegerProperty(0);
    private final IntegerProperty totalLogs = new SimpleIntegerProperty(0);
    private final DoubleProperty averageDistance = new SimpleDoubleProperty(0.0);
    private final DoubleProperty averageRating = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<TourDTO> mostPopularTour = new SimpleObjectProperty<>();
    private final IntegerProperty mostPopularTourLogCount = new SimpleIntegerProperty(0);

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
        setLoading(true);
        clearError();
        try {
            List<TourDTO> tours = tourService.getAllTours();
            List<TourLogDTO> logs = tourLogService.getAllTourLogs();
            
            totalTours.set(tours.size());
            totalLogs.set(logs.size());
            averageDistance.set(tours.stream().mapToDouble(TourDTO::getDistance).average().orElse(0.0));
            averageRating.set(logs.stream().mapToDouble(l -> l.getRating() != null ? l.getRating() : 0.0).average().orElse(0.0));
            
            Optional<TourDTO> mostPopular = tours.stream()
                .max(Comparator.comparingInt(t -> tourLogService.getTourLogCountByTourId(t.getId()).intValue()));
            
            if (mostPopular.isPresent()) {
                mostPopularTour.set(mostPopular.get());
                mostPopularTourLogCount.set(tourLogService.getTourLogCountByTourId(mostPopular.get().getId()).intValue());
            } else {
                mostPopularTour.set(null);
                mostPopularTourLogCount.set(0);
            }
        } catch (Exception e) {
            setError("Failed to load statistics: " + e.getMessage());
        } finally {
            setLoading(false);
        }
    }

    // Getters
    public int getTotalTours() { return totalTours.get(); }
    public IntegerProperty totalToursProperty() { return totalTours; }
    
    public int getTotalLogs() { return totalLogs.get(); }
    public IntegerProperty totalLogsProperty() { return totalLogs; }
    
    public double getAverageDistance() { return averageDistance.get(); }
    public DoubleProperty averageDistanceProperty() { return averageDistance; }
    
    public double getAverageRating() { return averageRating.get(); }
    public DoubleProperty averageRatingProperty() { return averageRating; }
    
    public TourDTO getMostPopularTour() { return mostPopularTour.get(); }
    public ObjectProperty<TourDTO> mostPopularTourProperty() { return mostPopularTour; }
    
    public int getMostPopularTourLogCount() { return mostPopularTourLogCount.get(); }
    public IntegerProperty mostPopularTourLogCountProperty() { return mostPopularTourLogCount; }
    
    // Get log count for a specific tour
    public int getTourLogCount(Long tourId) {
        return tourLogService.getTourLogCountByTourId(tourId).intValue();
    }

    @Override
    public String getTitle() { return "Tour Statistics"; }

    @Override
    public void dispose() {
        totalTours.set(0);
        totalLogs.set(0);
        averageDistance.set(0.0);
        averageRating.set(0.0);
        mostPopularTour.set(null);
        mostPopularTourLogCount.set(0);
    }

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