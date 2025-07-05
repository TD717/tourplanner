package com.tourplanner.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.dto.TourLogDTO;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportExportServiceImpl implements ImportExportService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean exportToursToJson(List<TourDTO> tours, String filePath) {
        try {
            objectMapper.writeValue(new File(filePath), tours);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<TourDTO> importToursFromJson(String filePath) {
        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<List<TourDTO>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean exportToursToCsv(List<TourDTO> tours, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("ID,Name,Description,Distance,EstimatedTime");
            
            // Write data
            for (TourDTO tour : tours) {
                writer.printf("%d,\"%s\",\"%s\",%.2f,\"%s\"%n",
                    tour.getId(),
                    escapeCsv(tour.getName()),
                    escapeCsv(tour.getDescription()),
                    tour.getDistance(),
                    escapeCsv(tour.getEstimatedTime()));
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<TourDTO> importToursFromCsv(String filePath) {
        List<TourDTO> tours = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length >= 5) {
                    TourDTO tour = new TourDTO(
                        Long.parseLong(parts[0]),
                        parts[1],
                        parts[2],
                        Double.parseDouble(parts[3]),
                        parts[4]
                    );
                    tours.add(tour);
                }
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
        return tours;
    }

    @Override
    public boolean exportTourLogsToJson(List<TourLogDTO> tourLogs, String filePath) {
        try {
            objectMapper.writeValue(new File(filePath), tourLogs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<TourLogDTO> importTourLogsFromJson(String filePath) {
        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<List<TourLogDTO>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean exportTourLogsToCsv(List<TourLogDTO> tourLogs, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("ID,TourID,DateTime,Comment,Difficulty,TotalDistance,TotalTime,Rating");
            
            // Write data
            for (TourLogDTO log : tourLogs) {
                writer.printf("%d,%d,\"%s\",\"%s\",%.1f,%.2f,%.2f,%.1f%n",
                    log.getId(),
                    log.getTourId(),
                    log.getDateTime().format(dateFormatter),
                    escapeCsv(log.getComment()),
                    log.getDifficulty(),
                    log.getTotalDistance(),
                    log.getTotalTime(),
                    log.getRating());
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<TourLogDTO> importTourLogsFromCsv(String filePath) {
        List<TourLogDTO> tourLogs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length >= 8) {
                    TourLogDTO log = new TourLogDTO(
                        Long.parseLong(parts[0]),
                        Long.parseLong(parts[1]),
                        LocalDateTime.parse(parts[2], dateFormatter),
                        parts[3],
                        Double.parseDouble(parts[4]),
                        Double.parseDouble(parts[5]),
                        Double.parseDouble(parts[6]),
                        Double.parseDouble(parts[7])
                    );
                    tourLogs.add(log);
                }
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
        return tourLogs;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
} 