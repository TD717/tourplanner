package com.tourplanner.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.tourplanner.ui.viewmodel.TourLogViewModel;
import com.tourplanner.ui.viewmodel.TourListViewModel;
import com.tourplanner.ui.view.TourStatisticsView;
import com.tourplanner.ui.view.MainView;
import com.tourplanner.backend.service.TourLogService;
import com.tourplanner.backend.service.RouteService;
import com.tourplanner.ui.viewmodel.TourStatisticsViewModel;
import com.tourplanner.backend.service.TourService;
import com.tourplanner.backend.service.MapService;
import com.tourplanner.backend.service.OpenRouteServicesAPI;
import com.tourplanner.backend.service.ImportExportService;

// ViewFactory responsible for creating and managing JavaFX views using the MVVM pattern.
public class ViewFactory {

    private final Map<String, Object> viewModelCache = new HashMap<>();
    private final Map<String, Stage> stageCache = new HashMap<>();
    private final TourLogService tourLogService;
    private final TourService tourService;
    private final RouteService routeService;
    private final ConfigurableApplicationContext applicationContext;
    private final MapService mapService;
    private final ImportExportService importExportService;
    private TourLogViewModel tourLogViewModel;
    private TourStatisticsViewModel tourStatisticsViewModel;
    private TourStatisticsView tourStatisticsView;

    public ViewFactory(ConfigurableApplicationContext applicationContext, TourLogService tourLogService, TourService tourService, RouteService routeService, ImportExportService importExportService) {
        this.applicationContext = applicationContext;
        this.tourLogService = tourLogService;
        this.tourService = tourService;
        this.routeService = routeService;
        this.importExportService = importExportService;
        this.mapService = applicationContext.getBean(OpenRouteServicesAPI.class);
    }

    // Creates a view without a ViewModel
    public ViewPair createViewWithoutViewModel(String fxmlPath) {
        try {
            // Create FXMLLoader and set controller factory
            String resourcePath = "/com/tourplanner/fxml/" + fxmlPath;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            if (loader.getLocation() == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            // Set controller factory to inject dependencies only
            loader.setControllerFactory(param -> {
                try {
                    // Create controller instance
                    Object controller = param.getDeclaredConstructor().newInstance();
                    // Inject RouteService if controller is TourDetailsView
                    if (controller instanceof com.tourplanner.ui.view.TourDetailsView) {
                        ((com.tourplanner.ui.view.TourDetailsView) controller).setRouteService(routeService);
                    }
                    // Inject MapService if controller is TourListView
                    if (controller instanceof com.tourplanner.ui.view.TourListView) {
                        ((com.tourplanner.ui.view.TourListView) controller).setMapService(mapService);
                        ((com.tourplanner.ui.view.TourListView) controller).setTourLogService(tourLogService);
                        ((com.tourplanner.ui.view.TourListView) controller).setRouteService(routeService);
                        ((com.tourplanner.ui.view.TourListView) controller).setImportExportService(importExportService);
                    }
                    return controller;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create controller", e);
                }
            });
            // Load the FXML first
            Parent root = loader.load();
            return new ViewPair(root, loader.getController());
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load " + fxmlPath, ex);
        }
    }

    // Creates a view with its associated ViewModel following MVVM pattern.
    public <T> ViewPair createView(String fxmlPath, Class<T> viewModelClass) {
        try {
            // Get or create ViewModel
            T viewModel = getOrCreateViewModel(viewModelClass);
            // Create FXMLLoader and set controller factory
            String resourcePath = "/com/tourplanner/fxml/" + fxmlPath;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            if (loader.getLocation() == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            // Set controller factory to inject dependencies only
            loader.setControllerFactory(param -> {
                try {
                    // Create controller instance
                    Object controller = param.getDeclaredConstructor().newInstance();
                    // Inject RouteService if controller is TourDetailsView
                    if (controller instanceof com.tourplanner.ui.view.TourDetailsView) {
                        ((com.tourplanner.ui.view.TourDetailsView) controller).setRouteService(routeService);
                    }
                    // Inject MapService if controller is TourListView
                    if (controller instanceof com.tourplanner.ui.view.TourListView) {
                        ((com.tourplanner.ui.view.TourListView) controller).setMapService(mapService);
                        ((com.tourplanner.ui.view.TourListView) controller).setTourLogService(tourLogService);
                        ((com.tourplanner.ui.view.TourListView) controller).setRouteService(routeService);
                        ((com.tourplanner.ui.view.TourListView) controller).setImportExportService(importExportService);
                    }
                    // Inject TourService if controller is TourLogView
                    if (controller instanceof com.tourplanner.ui.view.TourLogView) {
                        ((com.tourplanner.ui.view.TourLogView) controller).setTourService(tourService);
                    }
                    return controller;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create controller", e);
                }
            });
            // Load the FXML first (this will call initialize() and inject @FXML fields)
            Parent root = loader.load();
            // Inject the ViewModel after FXML fields are available
            Object controller = loader.getController();
            try {
                var setViewModelMethod = controller.getClass()
                        .getMethod("setViewModel", viewModelClass);
                setViewModelMethod.invoke(controller, viewModel);
            } catch (NoSuchMethodException e) {
            }
            return new ViewPair(root, controller);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load " + fxmlPath, ex);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot load " + fxmlPath, ex);
        }
    }


    // Gets or creates a ViewModel from cache or creates manually.

    @SuppressWarnings("unchecked")
    private <T> T getOrCreateViewModel(Class<T> viewModelClass) {
        String key = viewModelClass.getName();
        
        if (viewModelCache.containsKey(key)) {
            return (T) viewModelCache.get(key);
        }
        
        // Create new instance manually with proper dependencies
        try {
            T viewModel;
            if (viewModelClass == TourLogViewModel.class) {
                viewModel = (T) new TourLogViewModel(tourLogService);
            } else if (viewModelClass == TourListViewModel.class) {
                viewModel = (T) new TourListViewModel(tourService);
            } else {
                viewModel = viewModelClass.getDeclaredConstructor().newInstance();
            }
            viewModelCache.put(key, viewModel);
            return viewModel;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create ViewModel: " + viewModelClass.getName(), ex);
        }
    }

    // Returns the TourLog ViewModel
    public TourLogViewModel getTourLogViewModel() {
        if (tourLogViewModel == null) {
            tourLogViewModel = new TourLogViewModel(tourLogService);
            // Connect to statistics view model for automatic updates
            TourStatisticsViewModel statsViewModel = getTourStatisticsViewModel();
            tourLogViewModel.setStatisticsViewModel(statsViewModel);
        }
        return tourLogViewModel;
    }

    // Returns the TourStatistics ViewModel
    public TourStatisticsViewModel getTourStatisticsViewModel() {
        if (tourStatisticsViewModel == null) {
            tourStatisticsViewModel = new TourStatisticsViewModel(tourService, tourLogService);
        }
        return tourStatisticsViewModel;
    }

    // Returns the Main View
    public MainView getMainView() {
        try {
            // Load the FXML layout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tourplanner/fxml/main-view.fxml"));
            loader.load(); // Load the FXML
            
            // Get the controller and inject ViewFactory
            MainView controller = loader.getController();
            controller.setViewFactory(this);
            
            return controller;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MainView", e);
        }
    }

    public record ViewPair(Parent root, Object controller) { }
}
