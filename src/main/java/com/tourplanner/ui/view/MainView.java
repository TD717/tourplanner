package com.tourplanner.ui.view;

import com.tourplanner.ui.ViewFactory;
import com.tourplanner.ui.viewmodel.TourLogViewModel;
import com.tourplanner.ui.viewmodel.TourStatisticsViewModel;
import com.tourplanner.ui.view.TourStatisticsView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

// Main view controller that manages the overall application layout. Uses ViewFactory to create child views following MVVM pattern.
public class MainView {

    @FXML private BorderPane root;
    @FXML private AnchorPane leftContainer;
    @FXML private AnchorPane centerContainer;

    private ViewFactory viewFactory;
    private TourListView tourListView;
    private TourLogView tourLogView;
    private TourLogViewModel tourLogViewModel;
    private TourDetailsView tourDetailsView;
    private Node tourLogRoot;
    private Node tourDetailsRoot;
    private Node defaultCenterView;

    // No-arg constructor required for FXML loading.
    public MainView() {
        // ViewFactory will be injected via setViewFactory method
    }

    // Constructor with ViewFactory dependency
    public MainView(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    // Set the ViewFactory dependency
    public void setViewFactory(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
        initializeViews();
    }

    @FXML
    private void initialize() {
        if (viewFactory != null) {
            initializeViews();
        }
    }

    private void initializeViews() {
        try {
            // Create tour list view
            var listViewPair = viewFactory.createView("tourlist-view.fxml", com.tourplanner.ui.viewmodel.TourListViewModel.class);
            Node listRoot = listViewPair.root();
            tourListView = (TourListView) listViewPair.controller();
            
            // Create tour log view
            var logViewPair = viewFactory.createView("tourlog-view.fxml", TourLogViewModel.class);
            tourLogRoot = logViewPair.root();
            tourLogView = (TourLogView) logViewPair.controller();
            tourLogViewModel = viewFactory.getTourLogViewModel();
            tourLogView.setViewModel(tourLogViewModel);
            
            // Create tour details view
            var detailsViewPair = viewFactory.createViewWithoutViewModel("tourdetails-view.fxml");
            tourDetailsRoot = detailsViewPair.root();
            tourDetailsView = (TourDetailsView) detailsViewPair.controller();
            
            // Add tour list to left container
            leftContainer.getChildren().clear();
            AnchorPane.setTopAnchor(listRoot, 0.0);
            AnchorPane.setBottomAnchor(listRoot, 0.0);
            AnchorPane.setLeftAnchor(listRoot, 0.0);
            AnchorPane.setRightAnchor(listRoot, 0.0);
            leftContainer.getChildren().add(listRoot);
            
            // Set up default center view (welcome message)
            if (defaultCenterView == null) {
                javafx.scene.control.Label welcome = new javafx.scene.control.Label("Welcome to TourPlanner!\nSelect a tour or use the menu to get started.");
                welcome.setStyle("-fx-font-size: 20px; -fx-text-fill: #333; -fx-alignment: center;");
                AnchorPane pane = new AnchorPane(welcome);
                AnchorPane.setTopAnchor(welcome, 0.0);
                AnchorPane.setBottomAnchor(welcome, 0.0);
                AnchorPane.setLeftAnchor(welcome, 0.0);
                AnchorPane.setRightAnchor(welcome, 0.0);
                defaultCenterView = pane;
            }
            showHome();
            
            // Connect tour selection to tour details, but only auto-switch if on Home
            connectTourSelectionToConditionalDetails();
            
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to initialize views: " + e.getMessage()).showAndWait();
        }
    }

    private void connectTourSelectionToConditionalDetails() {
        if (tourListView != null && tourDetailsView != null) {
            tourListView.selectedTourProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && isOnHomePage()) {
                    showTourDetails();
                }
            });
            tourDetailsView.bindTo(tourListView.selectedTourProperty());
        }
    }

    private boolean isOnHomePage() {
        return centerContainer.getChildren().size() == 1 && centerContainer.getChildren().get(0) == defaultCenterView;
    }

    @FXML
    private void onExit() {
        System.exit(0);
    }

    @FXML
    private void onShowLogs() {
        showTourLogs();
    }

    @FXML
    private void onShowHome() {
        showHome();
    }

    @FXML
    private void onShowStatistics() {
        showStatistics();
    }

    private void showStatistics() {
        try {
            TourStatisticsViewModel statsViewModel = viewFactory.getTourStatisticsViewModel();
            TourStatisticsView statsView = new TourStatisticsView(statsViewModel);

            // Create a container node for the statistics view
            javafx.scene.layout.AnchorPane statsPane = new javafx.scene.layout.AnchorPane();

            // Use a new Stage to get the Scene, then extract the root
            javafx.stage.Stage dummyStage = new javafx.stage.Stage();
            statsView.show(dummyStage);
            javafx.scene.Scene statsScene = dummyStage.getScene();
            javafx.scene.Parent statsRoot = statsScene.getRoot();
            statsPane.getChildren().add(statsRoot);
            AnchorPane.setTopAnchor(statsRoot, 0.0);
            AnchorPane.setBottomAnchor(statsRoot, 0.0);
            AnchorPane.setLeftAnchor(statsRoot, 0.0);
            AnchorPane.setRightAnchor(statsRoot, 0.0);
            centerContainer.getChildren().clear();
            centerContainer.getChildren().add(statsPane);
            dummyStage.close();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load Statistics: " + e.getMessage()).showAndWait();
        }
    }

    private void showTourLogs() {
        try {
            if (tourLogView == null) {
                var logViewPair = viewFactory.createView("tourlog-view.fxml", TourLogViewModel.class);
                tourLogRoot = logViewPair.root();
                tourLogView = (TourLogView) logViewPair.controller();
                tourLogViewModel = viewFactory.getTourLogViewModel();
                tourLogView.setViewModel(tourLogViewModel);
                
                // Connect tour selection if not already connected
                if (tourListView != null) {
                    tourListView.selectedTourProperty().addListener((obs, oldVal, newVal) -> {
                        tourLogViewModel.setSelectedTour(newVal);
                    });
                    
                    // Check if there are any tours available
                    ListView<com.tourplanner.backend.dto.TourDTO> listView = tourListView.getTourList();
                    if (listView.getItems().isEmpty()) {
                        // No tours available
                        tourLogViewModel.setSelectedTour(null);
                    } else {
                        // Ensure a tour is selected in the ListView
                        if (listView.getSelectionModel().getSelectedItem() == null) {
                            listView.getSelectionModel().selectFirst();
                        }
                        tourLogViewModel.setSelectedTour(listView.getSelectionModel().getSelectedItem());
                    }
                } else {
                    // No tour list view available, try to load all logs
                    tourLogViewModel.loadData();
                }
            }
            
            centerContainer.getChildren().clear();
            AnchorPane.setTopAnchor(tourLogRoot, 0.0);
            AnchorPane.setBottomAnchor(tourLogRoot, 0.0);
            AnchorPane.setLeftAnchor(tourLogRoot, 0.0);
            AnchorPane.setRightAnchor(tourLogRoot, 0.0);
            centerContainer.getChildren().add(tourLogRoot);
            
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load Tour Logs: " + e.getMessage()).showAndWait();
        }
    }

    private void showTourDetails() {
        try {
            if (tourDetailsView == null) {
                var detailsViewPair = viewFactory.createViewWithoutViewModel("tourdetails-view.fxml");
                Node detailsRoot = detailsViewPair.root();
                tourDetailsView = (TourDetailsView) detailsViewPair.controller();
            }
            
            // We use the stored root node
            Node detailsRoot = tourDetailsRoot;
            if (detailsRoot == null) {
                // If the root is null, it is created from the FXML
                var detailsViewPair = viewFactory.createViewWithoutViewModel("tourdetails-view.fxml");
                detailsRoot = detailsViewPair.root();
                tourDetailsRoot = detailsRoot;
                tourDetailsView = (TourDetailsView) detailsViewPair.controller();
            }
            
            centerContainer.getChildren().clear();
            AnchorPane.setTopAnchor(detailsRoot, 0.0);
            AnchorPane.setBottomAnchor(detailsRoot, 0.0);
            AnchorPane.setLeftAnchor(detailsRoot, 0.0);
            AnchorPane.setRightAnchor(detailsRoot, 0.0);
            centerContainer.getChildren().add(detailsRoot);
            
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load Tour Details: " + e.getMessage()).showAndWait();
        }
    }

    private void showHome() {
        centerContainer.getChildren().clear();
        if (defaultCenterView != null) {
            centerContainer.getChildren().add(defaultCenterView);
        }
    }

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About TourPlanner");
        alert.setHeaderText("TourPlanner Application");
        alert.setContentText("A JavaFX application for managing tours and tour logs.\n\n" +
                           "Features:\n" +
                           "• Create and manage tours\n" +
                           "• Add tour logs with statistics\n" +
                           "• Generate PDF reports\n" +
                           "• Search and filter functionality\n" +
                           "• Interactive maps and route planning");
        alert.showAndWait();
    }

    public void show(Stage stage) {
        try {
            // Load the FXML layout, controller is already specified in FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tourplanner/fxml/main-view.fxml"));
            BorderPane root = loader.load();
            
            // Get the controller and set the ViewFactory
            MainView controller = loader.getController();
            controller.setViewFactory(viewFactory);
            
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("TourPlanner");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            // Fallback to simple view if FXML loading fails
            javafx.scene.layout.StackPane fallbackRoot = new javafx.scene.layout.StackPane(new javafx.scene.control.Label("Failed to load main view: " + e.getMessage()));
            Scene scene = new Scene(fallbackRoot, 800, 600);
            stage.setTitle("TourPlanner - Error");
            stage.setScene(scene);
            stage.show();
        }
    }
}
