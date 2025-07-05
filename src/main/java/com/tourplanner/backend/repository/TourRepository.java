package com.tourplanner.backend.repository;

import com.tourplanner.backend.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Simple Spring Data repository for Tour entity.
@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    // Simple search by name containing the given text.
    List<Tour> findByNameContainingIgnoreCase(String searchText);
} 