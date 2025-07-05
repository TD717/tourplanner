package com.tourplanner.backend.dto;

// Data transfer object for Tour
public class TourDTO {

    private Long id;
    private String name;
    private String description;
    private double distance;
    private String estimatedTime;   // consider java.time.Duration later
    private String transportType;
    private String fromLocation;
    private String toLocation;

    // Empty constructor
    public TourDTO() {
    }

    // Other constructors
    public TourDTO(String name,
                   String description,
                   double distance,
                   String estimatedTime) {
        this(null, name, description, distance, estimatedTime);
    }

    public TourDTO(Long id, String name,
                   String description,
                   double distance,
                   String estimatedTime) {
        this(id, name, description, distance, estimatedTime, null, null, null);
    }

    public TourDTO(Long id, String name,
                   String description,
                   double distance,
                   String estimatedTime,
                   String transportType,
                   String fromLocation,
                   String toLocation) {

        this.id           = id;
        this.name         = name;
        this.description  = description;
        this.distance     = distance;
        this.estimatedTime = estimatedTime;
        this.transportType = transportType;
        this.fromLocation = fromLocation;
        this.toLocation   = toLocation;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getDistance() { return distance; }
    public String getEstimatedTime() { return estimatedTime; }
    public String getTransportType() { return transportType; }
    public String getFromLocation() { return fromLocation; }
    public String getToLocation() { return toLocation; }

    // Setters
    public void setId (Long i)   { id = i; }
    public void setName        (String n) { name = n; }
    public void setDescription (String d) { description = d; }
    public void setDistance    (double d) { distance = d; }
    public void setEstimatedTime(String t){ estimatedTime = t; }
    public void setTransportType(String t){ transportType = t; }
    public void setFromLocation(String f) { fromLocation = f; }
    public void setToLocation  (String t) { toLocation = t; }

    // ListView representation
    @Override public String toString() { return name; }
}
