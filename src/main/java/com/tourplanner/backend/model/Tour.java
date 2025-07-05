package com.tourplanner.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// JPA Entity for Tour representing a tour entry in the database.

@Entity
@Table(name = "tours")
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double distance;

    @Column(name = "estimated_time", length = 50)
    private String estimatedTime;

    @Column(name = "transport_type", length = 50)
    private String transportType;

    @Column(name = "from_location", length = 255)
    private String fromLocation;

    @Column(name = "to_location", length = 255)
    private String toLocation;

    @Column(name = "route_image_path", length = 500)
    private String routeImagePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TourLog> tourLogs = new ArrayList<>();

    // Constructors
    public Tour() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Tour(String name, String description, Double distance, String estimatedTime) {
        this();
        this.name = name;
        this.description = description;
        this.distance = distance;
        this.estimatedTime = estimatedTime;
    }

    public Tour(String name, String description, Double distance, String estimatedTime, 
                String transportType, String fromLocation, String toLocation) {
        this(name, description, distance, estimatedTime);
        this.transportType = transportType;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
        this.updatedAt = LocalDateTime.now();
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
        this.updatedAt = LocalDateTime.now();
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
        this.updatedAt = LocalDateTime.now();
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TourLog> getTourLogs() {
        return tourLogs;
    }

    public void setTourLogs(List<TourLog> tourLogs) {
        this.tourLogs = tourLogs;
    }

    public int getLogCount() {
        return tourLogs.size();
    }

    public double getAverageTime() {
        if (tourLogs.isEmpty()) {
            return 0.0;
        }
        return tourLogs.stream()
                .mapToDouble(TourLog::getTotalTime)
                .average()
                .orElse(0.0);
    }

    @Override
    public String toString() {
        return name;
    }
} 