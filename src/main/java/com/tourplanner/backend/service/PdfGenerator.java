package com.tourplanner.backend.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.Document;

import com.tourplanner.backend.dto.TourDTO;
import com.tourplanner.backend.dto.TourLogDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PdfGenerator {
    public static void generateTourReport(File file, TourDTO tour, List<TourLogDTO> logs, byte[] imageBytes) throws IOException {
        PdfWriter writer = new PdfWriter(file.getAbsolutePath());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Paragraph title = new Paragraph("Tour Report")
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        document.add(new Paragraph("General Information")
                .setFontSize(18)
                .setUnderline()
                .setMarginBottom(10)
        );

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        infoTable.addCell(new Cell().add(new Paragraph("Name")));
        infoTable.addCell(new Cell().add(new Paragraph(tour.getName())));
        infoTable.addCell(new Cell().add(new Paragraph("Description")));
        infoTable.addCell(new Cell().add(new Paragraph(tour.getDescription())));
        infoTable.addCell(new Cell().add(new Paragraph("From")));
        infoTable.addCell(new Cell().add(new Paragraph(tour.getFromLocation())));
        infoTable.addCell(new Cell().add(new Paragraph("To")));
        infoTable.addCell(new Cell().add(new Paragraph(tour.getToLocation())));
        infoTable.addCell(new Cell().add(new Paragraph("Transport type")));
        infoTable.addCell(new Cell().add(new Paragraph(tour.getTransportType())));
        infoTable.addCell(new Cell().add(new Paragraph("Distance (km)")));
        infoTable.addCell(new Cell().add(new Paragraph(String.valueOf(tour.getDistance()))));
        infoTable.addCell(new Cell().add(new Paragraph("Estimated time")));
        infoTable.addCell(new Cell().add(new Paragraph(tour.getEstimatedTime())));
        document.add(infoTable);

        document.add(new Paragraph("Logs Overview")
                .setFontSize(18)
                .setUnderline()
                .setMarginBottom(10)
        );

        Table logTable = new Table(UnitValue.createPercentArray(new float[]{3, 4, 2, 2, 2, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
        logTable.addHeaderCell(new Cell().add(new Paragraph("Date / Time")));
        logTable.addHeaderCell(new Cell().add(new Paragraph("Comment")));
        logTable.addHeaderCell(new Cell().add(new Paragraph("Difficulty")));
        logTable.addHeaderCell(new Cell().add(new Paragraph("Distance")));
        logTable.addHeaderCell(new Cell().add(new Paragraph("Duration")));
        logTable.addHeaderCell(new Cell().add(new Paragraph("Rating")));
        for (TourLogDTO log : logs) {
            logTable.addCell(new Cell().add(new Paragraph(log.getFormattedDateTime())));
            logTable.addCell(new Cell().add(new Paragraph(log.getComment() != null ? log.getComment() : "")));
            logTable.addCell(new Cell().add(new Paragraph(log.getDifficulty() != null ? String.valueOf(log.getDifficulty()) : "")));
            logTable.addCell(new Cell().add(new Paragraph(log.getTotalDistance() != null ? log.getTotalDistance() + " km" : "")));
            logTable.addCell(new Cell().add(new Paragraph(log.getTotalTime() != null ? log.getTotalTime() + " h" : "")));
            logTable.addCell(new Cell().add(new Paragraph(log.getRating() != null ? String.valueOf(log.getRating()) : "")));
        }
        document.add(logTable);

        document.add(new Paragraph("Route Map")
                .setFontSize(18)
                .setUnderline()
                .setMarginBottom(10)
        );
        if (imageBytes != null) {
            Image image = new Image(ImageDataFactory.create(imageBytes));
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            image.setMaxWidth(UnitValue.createPercentValue(60));
            document.add(image);
        }
        document.close();
    }

    public static void generateSummaryReport(File file, Map<TourDTO, ? extends Object> tourStatsMap) throws IOException {
        PdfWriter writer = new PdfWriter(file.getAbsolutePath());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Paragraph title = new Paragraph("Tour Summary Report")
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        document.add(new Paragraph("Overview and Statistics per Tour")
                .setFontSize(18)
                .setUnderline()
                .setMarginBottom(10)
        );

        Table tourTable = new Table(UnitValue.createPercentArray(new float[]{3, 3, 2, 2, 2, 2, 2, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
        tourTable.addHeaderCell(new Cell().add(new Paragraph("Name")));
        tourTable.addHeaderCell(new Cell().add(new Paragraph("From - To")));
        tourTable.addHeaderCell(new Cell().add(new Paragraph("Distance (km)")));
        tourTable.addHeaderCell(new Cell().add(new Paragraph("Estimated Time")));
        tourTable.addHeaderCell(new Cell().add(new Paragraph("Transport Type")));
        tourTable.addHeaderCell(new Cell().add(new Paragraph("Avg Time (h)")));
        tourTable.addHeaderCell(new Cell().add(new Paragraph("Avg Distance (km)")));
        tourTable.addHeaderCell(new Cell().add(new Paragraph("Avg Rating")));
        for (Map.Entry<TourDTO, ? extends Object> entry : tourStatsMap.entrySet()) {
            TourDTO tour = entry.getKey();
            Object statsObj = entry.getValue();
            double avgTime = 0, avgDistance = 0, avgRating = 0;
            if (statsObj != null) {
                try {
                    var stats = (com.tourplanner.ui.viewmodel.TourStatisticsViewModel.TourStats) statsObj;
                    avgTime = stats.avgTime;
                    avgDistance = stats.avgDistance;
                    avgRating = stats.avgRating;
                } catch (Exception ignored) {}
            }
            tourTable.addCell(new Cell().add(new Paragraph(tour.getName() != null ? tour.getName() : "")));
            tourTable.addCell(new Cell().add(new Paragraph((tour.getFromLocation() != null ? tour.getFromLocation() : "") + " to " + (tour.getToLocation() != null ? tour.getToLocation() : ""))));
            tourTable.addCell(new Cell().add(new Paragraph(String.valueOf(tour.getDistance()))));
            tourTable.addCell(new Cell().add(new Paragraph(tour.getEstimatedTime() != null ? tour.getEstimatedTime() : "")));
            tourTable.addCell(new Cell().add(new Paragraph(tour.getTransportType() != null ? tour.getTransportType() : "")));
            tourTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", avgTime))));
            tourTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", avgDistance))));
            tourTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", avgRating))));
        }
        document.add(tourTable);
        document.close();
    }
} 