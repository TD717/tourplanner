package com.tourplanner.backend.repository;

import com.tourplanner.backend.model.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// Simple Spring Data repository for TourLog entity
@Repository
public interface TourLogRepository extends JpaRepository<TourLog, Long> {

    List<TourLog> findByTourId(Long tourId); // Find all tour logs for a specific tour

    List<TourLog> findByCommentContainingIgnoreCase(String comment); // Find tour logs by comment containing text (case-insensitive)

    List<TourLog> findByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate); // Find tour logs by date range.

    List<TourLog> findByDifficultyGreaterThanEqual(Double minDifficulty); // Find tour logs by minimum difficulty

    List<TourLog> findByRatingGreaterThanEqual(Double minRating); // Find tour logs by minimum rating

    // Queries
    @Query("SELECT AVG(tl.rating) FROM TourLog tl WHERE tl.tour.id = ?1") // Get average rating for a specific tour
    Double getAverageRatingByTourId(Long tourId);

    @Query("SELECT AVG(tl.difficulty) FROM TourLog tl WHERE tl.tour.id = ?1") // Get average difficulty for a specific tour
    Double getAverageDifficultyByTourId(Long tourId);

    @Query("SELECT AVG(tl.totalDistance) FROM TourLog tl WHERE tl.tour.id = ?1") //Get average distance for a specific tour
    Double getAverageDistanceByTourId(Long tourId);

    @Query("SELECT AVG(tl.totalTime) FROM TourLog tl WHERE tl.tour.id = ?1") //Get average time for a specific tour
    Double getAverageTimeByTourId(Long tourId);

    Long countByTourId(Long tourId); // Count tour logs for a specific tour
} 