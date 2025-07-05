package com.tourplanner.backend.dto;

import java.time.LocalDateTime;

// Data transfer object for TourLog
public class TourLogDTO {

    private Long id;
    private Long tourId;
    private LocalDateTime dateTime;
    private String comment;
    private Double difficulty;
    private Double totalDistance;
    private Double totalTime;
    private Double rating;

    // Constructors
    public TourLogDTO() {}

    public TourLogDTO(Long tourId, LocalDateTime dateTime, String comment, 
                     Double difficulty, Double totalDistance, Double totalTime, Double rating) {
        this.tourId = tourId;
        this.dateTime = dateTime;
        this.comment = comment;
        this.difficulty = difficulty;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.rating = rating;
    }

    public TourLogDTO(Long id, Long tourId, LocalDateTime dateTime, String comment, 
                     Double difficulty, Double totalDistance, Double totalTime, Double rating) {
        this(tourId, dateTime, comment, difficulty, totalDistance, totalTime, rating);
        this.id = id;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTourId() {
        return tourId;
    }

    public void setTourId(Long tourId) {
        this.tourId = tourId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getComment() {
        return comment;
    }

    public Double getDifficulty() {
        return difficulty;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public Double getTotalTime() {
        return totalTime;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    // Business methods
    public String getFormattedDateTime() {
        if (dateTime == null) return "";
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getFormattedTotalTime() {
        if (totalTime == null)
            return "";
        int hours = (int) Math.floor(totalTime); // (int) casts the totalTime from double to int
        int minutes = (int) Math.round((totalTime - hours) * 60);
        return String.format("%dh %02dm", hours, minutes);
    }

    public String getDifficultyDescription() {
        if (difficulty == null)
            return "";
        if (difficulty <= 1.5)
            return "Very Easy";
        if (difficulty <= 2.5)
            return "Easy";
        if (difficulty <= 3.5)
            return "Moderate";
        if (difficulty <= 4.5)
            return "Hard";
        return "Very Hard";
    }

    public String getRatingDescription() {
        if (rating == null)
            return "";
        if (rating <= 1.5)
            return "Poor";
        if (rating <= 2.5)
            return "Fair";
        if (rating <= 3.5)
            return "Good";
        if (rating <= 4.5)
            return "Very Good";
        return "Excellent";
    }

    @Override
    public String toString() {
        return String.format("TourLogDTO{id=%d, tourId=%d, dateTime=%s, rating=%.1f}", 
                           id, tourId, getFormattedDateTime(), rating);
    }
} 