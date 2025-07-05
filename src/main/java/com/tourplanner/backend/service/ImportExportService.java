package com.tourplanner.backend.service;

import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.dto.TourLogDTO;

import java.util.List;

// Service for importing and exporting tour data in various formats,
// which supports JSON and CSV formats for tour and tour log data
public interface ImportExportService {

    boolean exportToursToJson(List<TourDTO> tours, String filePath);

    List<TourDTO> importToursFromJson(String filePath);

    boolean exportToursToCsv(List<TourDTO> tours, String filePath);

    List<TourDTO> importToursFromCsv(String filePath);

    boolean exportTourLogsToJson(List<TourLogDTO> tourLogs, String filePath);

    List<TourLogDTO> importTourLogsFromJson(String filePath);

    boolean exportTourLogsToCsv(List<TourLogDTO> tourLogs, String filePath);

    List<TourLogDTO> importTourLogsFromCsv(String filePath);
} 