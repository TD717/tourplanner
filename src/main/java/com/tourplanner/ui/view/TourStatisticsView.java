package com.tourplanner.ui.view;

import com.tourplanner.ui.viewmodel.TourStatisticsViewModel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import com.tourplanner.backend.service.PdfGenerator;
import java.io.File;

public class TourStatisticsView {
    private final TourStatisticsViewModel viewModel;

    public TourStatisticsView(TourStatisticsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Label totalToursLabel = new Label();
        Label totalLogsLabel = new Label();
        Label avgDistLabel = new Label();
        Label avgRatingLabel = new Label();
        Label mostPopularLabel = new Label();
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        // Function to update all labels
        Runnable updateLabels = () -> {
            totalToursLabel.setText("Total Tours: " + viewModel.getTotalTours());
            totalLogsLabel.setText("Total Tour Logs: " + viewModel.getTotalLogs());
            avgDistLabel.setText(String.format("Average Tour Distance: %.2f km", viewModel.getAverageDistance()));
            avgRatingLabel.setText(String.format("Average Tour Rating: %.2f", viewModel.getAverageRating()));
            mostPopularLabel.setText(viewModel.getMostPopularTour() != null ?
                    "Most Popular Tour: " + viewModel.getMostPopularTour().getName() +
                            " (" + viewModel.getMostPopularTourLogCount() + " logs)" :
                    "Most Popular Tour: N/A");
            errorLabel.setText(viewModel.getErrorMessage());
        };

        // Initial update
        updateLabels.run();

        // Bar chart for tour popularity - showing all tours
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Tour Popularity (Logs per Tour)");
        xAxis.setLabel("Tour");
        yAxis.setLabel("Log Count");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Logs per Tour");
        
        // Function to update the bar chart with all tours
        Runnable updateChart = () -> {
            series.getData().clear();
            var tourStats = viewModel.getTourStats();
            for (var entry : tourStats.entrySet()) {
                String tourName = entry.getKey().getName();
                int logCount = viewModel.getTourLogCount(entry.getKey().getId());
                series.getData().add(new XYChart.Data<>(tourName, logCount));
            }
        };
        
        // Initial chart update
        updateChart.run();
        barChart.getData().add(series);

        Button summaryReportBtn = new Button("Generate Summary Report");
        summaryReportBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Summary Report");
            fileChooser.setInitialFileName("tour_summary_report.pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    PdfGenerator.generateSummaryReport(file, viewModel.getTourStats());
                } catch (Exception ex) {
                    errorLabel.setText("Failed to generate summary report: " + ex.getMessage());
                }
            }
        });

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> {
            viewModel.loadData();
            // Update all labels and chart after reload
            updateLabels.run();
            updateChart.run();
        });

        // Set preferred size for the chart
        barChart.setPrefSize(600, 300);
        
        VBox vbox = new VBox(10, totalToursLabel, totalLogsLabel, avgDistLabel, avgRatingLabel, mostPopularLabel, barChart, summaryReportBtn, refreshBtn, errorLabel);
        root.setCenter(vbox);

        Scene scene = new Scene(root, 700, 600);
        stage.setTitle("Tour Statistics Dashboard");
        stage.setScene(scene);
        stage.show();
    }
} 