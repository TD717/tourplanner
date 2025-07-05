module com.tourplanner {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // Java SE APIs
    requires java.sql;
    requires java.desktop;
    requires java.logging;
    requires java.net.http;

    // JSON + Logging
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires org.apache.logging.log4j;

    // Spring Boot and Spring Framework
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.data.jpa;
    requires spring.data.commons;

    // JPA
    requires jakarta.persistence;
    requires jakarta.annotation;       // for @PostConstruct, etc.
    requires jakarta.transaction;      // for @Transactional

    // Hibernate (modular JARs needed!)
    requires org.hibernate.orm.core;   // Hibernate core

    // PDF generation (assuming these are modular JARs)
    requires kernel;
    requires layout;
    requires io;

    // Open packages for reflection
    opens com.tourplanner.backend.model;
    opens com.tourplanner.backend.dto;
    opens com.tourplanner to javafx.fxml, spring.core;
    opens com.tourplanner.ui to javafx.fxml;
    opens com.tourplanner.ui.view to javafx.fxml;
    opens com.tourplanner.ui.viewmodel to javafx.fxml;
    opens com.tourplanner.backend.repository to spring.beans;
    opens com.tourplanner.backend.service;

    // Export packages
    exports com.tourplanner;
    exports com.tourplanner.ui;
    exports com.tourplanner.ui.view;
    exports com.tourplanner.ui.viewmodel;
    exports com.tourplanner.backend.model;
    exports com.tourplanner.backend.service;
    exports com.tourplanner.backend.repository;
    exports com.tourplanner.backend.dto;
}
