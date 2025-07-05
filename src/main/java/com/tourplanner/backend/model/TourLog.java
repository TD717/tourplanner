package com.tourplanner.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// JPA Entity for TourLog representing a tour log entry in the database.

@Entity
@Table(name = "tour_logs")
public class TourLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private Double difficulty; // 1-5 scale

    @Column(name = "total_distance", nullable = false)
    private Double totalDistance;

    @Column(name = "total_time", nullable = false)
    private Double totalTime; // in hours

    @Column(nullable = false)
    private Double rating; // 1-5 scale

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public TourLog() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TourLog(Tour tour, LocalDateTime dateTime, String comment, Double difficulty, 
                   Double totalDistance, Double totalTime, Double rating) {
        this();
        this.tour = tour;
        this.dateTime = dateTime;
        this.comment = comment;
        this.difficulty = difficulty;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.rating = rating;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        this.updatedAt = LocalDateTime.now();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        this.updatedAt = LocalDateTime.now();
    }

    public Double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Double difficulty) {
        this.difficulty = difficulty;
        this.updatedAt = LocalDateTime.now();
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
        this.updatedAt = LocalDateTime.now();
    }

    public Double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Double totalTime) {
        this.totalTime = totalTime;
        this.updatedAt = LocalDateTime.now();
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    public String getFormattedDateTime() {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        return String.format("TourLog{id=%d, tour='%s', dateTime=%s, rating=%.1f}", 
                           id, tour != null ? tour.getName() : "null", 
                           getFormattedDateTime(), rating);
    }
} 