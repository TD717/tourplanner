package com.tourplanner;

import com.tourplanner.backend.service.TourLogService;
import com.tourplanner.backend.service.TourService;
import com.tourplanner.backend.service.RouteService;
import com.tourplanner.backend.service.ImportExportService;
import com.tourplanner.ui.ViewFactory;
import com.tourplanner.ui.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class MainApp extends Application {
    private ConfigurableApplicationContext springContext;
    private ViewFactory viewFactory;

    @Override
    public void init() {
        // Use SpringApplicationBuilder to load application.properties
        springContext = new SpringApplicationBuilder(SpringConfig.class).run();
        TourLogService tourLogService = springContext.getBean(TourLogService.class);
        TourService tourService = springContext.getBean(TourService.class);
        RouteService routeService = springContext.getBean(RouteService.class);
        ImportExportService importExportService = springContext.getBean(ImportExportService.class);
        viewFactory = new ViewFactory(springContext, tourLogService, tourService, routeService, importExportService);
    }

    @Override
    public void start(Stage primaryStage) {
        MainView mainView = viewFactory.getMainView();
        mainView.show(primaryStage);
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
