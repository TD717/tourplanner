package com.tourplanner.ui.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.logging.Logger;
import java.util.logging.Level;

// Base ViewModel class that provides common functionality for all ViewModels.
public abstract class BaseViewModel {
    
    protected final Logger logger = Logger.getLogger(getClass().getName());
    
    // Common properties for all ViewModels
    private final StringProperty title = new SimpleStringProperty("");
    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty("");
    

    public abstract void initialize();
    
    //Clean up resources when the ViewModel is no longer needed.
    public abstract void dispose();
    
    // Load data for the ViewModel.
    public abstract void loadData();
    
    // Refresh the ViewModel data.
    public void refresh() {
        logger.fine("Refreshing ViewModel: " + getClass().getSimpleName());
        loadData();
    }
    
    // Clear any error messages.
    public void clearError() {
        errorMessage.set("");
    }
    
    // Set an error message.
    public void setError(String error) {
        errorMessage.set(error);
    }
    
    // Getters and setters
    public String getTitle() {
        return title.get();
    }
    
    public void setTitle(String title) {
        this.title.set(title);
    }
    
    public StringProperty titleProperty() {
        return title;
    }
    
    public boolean isLoading() {
        return isLoading.get();
    }
    
    public void setLoading(boolean loading) {
        this.isLoading.set(loading);
    }
    
    public BooleanProperty loadingProperty() {
        return isLoading;
    }
    
    public String getErrorMessage() {
        return errorMessage.get();
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage.set(errorMessage);
    }
    
    public StringProperty errorMessageProperty() {
        return errorMessage;
    }
} 