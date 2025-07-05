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

        Label totalToursLabel = new Label("Total Tours: " + viewModel.getTotalTours());
        Label totalLogsLabel = new Label("Total Tour Logs: " + viewModel.getTotalLogs());
        Label avgDistLabel = new Label(String.format("Average Tour Distance: %.2f km", viewModel.getAverageDistance()));
        Label avgRatingLabel = new Label(String.format("Average Tour Rating: %.2f", viewModel.getAverageRating()));
        Label mostPopularLabel = new Label(viewModel.getMostPopularTour() != null ?
                "Most Popular Tour: " + viewModel.getMostPopularTour().getName() +
                        " (" + viewModel.getMostPopularTourLogCount() + " logs)" :
                "Most Popular Tour: N/A");
        Label errorLabel = new Label(viewModel.getErrorMessage());
        errorLabel.setStyle("-fx-text-fill: red;");

        // Optional: Bar chart for tour popularity
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Tour Popularity");
        xAxis.setLabel("Tour");
        yAxis.setLabel("Log Count");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Logs per Tour");
        if (viewModel.getMostPopularTour() != null) {
            series.getData().add(new XYChart.Data<>(viewModel.getMostPopularTour().getName(), viewModel.getMostPopularTourLogCount()));
        }
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
            totalToursLabel.setText("Total Tours: " + viewModel.getTotalTours());
            totalLogsLabel.setText("Total Tour Logs: " + viewModel.getTotalLogs());
            avgDistLabel.setText(String.format("Average Tour Distance: %.2f km", viewModel.getAverageDistance()));
            avgRatingLabel.setText(String.format("Average Tour Rating: %.2f", viewModel.getAverageRating()));
            mostPopularLabel.setText(viewModel.getMostPopularTour() != null ?
                "Most Popular Tour: " + viewModel.getMostPopularTour().getName() +
                " (" + viewModel.getMostPopularTourLogCount() + " logs)" :
                "Most Popular Tour: N/A");
            errorLabel.setText(viewModel.getErrorMessage());
            // Update bar chart
            series.getData().clear();
            if (viewModel.getMostPopularTour() != null) {
                series.getData().add(new XYChart.Data<>(viewModel.getMostPopularTour().getName(), viewModel.getMostPopularTourLogCount()));
            }
        });

        VBox vbox = new VBox(10, totalToursLabel, totalLogsLabel, avgDistLabel, avgRatingLabel, mostPopularLabel, barChart, summaryReportBtn, refreshBtn, errorLabel);
        root.setCenter(vbox);

        Scene scene = new Scene(root, 500, 400);
        stage.setTitle("Tour Statistics Dashboard");
        stage.setScene(scene);
        stage.show();
    }
} 