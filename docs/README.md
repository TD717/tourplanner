TourPlanner Project Protocol
Made by: Tedi Dobi, Luca Bonavetti
The TourPlanner application is an application built using JavaFX for Frontend and Spring Boot for Backend. It allows a user to add/delete/edit tours, generate and view tour logs, import/export CSV data and generate PDF reports. Additionally, it provides a statistics dashboard, which shows analytics and insights for tours.

Highlights
•	Unique Feature: Statistics Dashboard
•	Architecture: MVVM pattern with 3-tier architecture
•	Technology: JavaFX + Spring Boot + PostgreSQL Database
•	Development Time: 68 hours total

Technical Steps
•	Architecture Pattern: MVVM (Model-View-ViewModel)
•	Database: PostgreSQL Database
o	Easy setup and fast development
o	Portable application deployment

•	Framework Stack: Spring Boot + JavaFX
o	Spring Boot provides excellent dependency injection and data access
o	JavaFX offers desktop UI features
o	Good integration capabilities

•	External API Integration: OpenRouteService for route calculation and geocoding.

Application Architecture
3-Tier Architecture Overview: The application follows a clear 3-tier architecture with separation of concerns:
•	Presentation:
o	Includes JavaFX Views, ViewModels and ViewFactory
o	Provides UI logic data binding and user interaction
•	Business:
o	Includes services, DTOs and external API
o	Provides business logic, data transformation and external integration
•	Data
o	Includes repositories, entities and database
o	Provides data persistence, data access and storage

Layer Details
•	Presentation Layer
o	Views: JavaFX FXML files with controllers
o	ViewModel: Business logic, data binding and state management
o	ViewFactory: Dependency injection and view creation
•	Business Layer
o	Services: Business logic, data transformation and external API integration
o	DTOs: Data transfer objects for layer communication
o	External APIs: Route calculation, geocoding and PDF generation

•	Data Layer
o	Repositories: Data access using Spring Data JPA
o	Entities: JPA entities representing database tables
o	Database: Database with JPA/Hibernate ORM

Design Patterns Implemented
•	MVVM Pattern
o	View: FXML files with controllers
o	ViewModel: Business logic and data binding
o	Model: JPA entities and DTOs

•	Repository Pattern
o	Spring Data JPA repositories

•	Factory Pattern
o	Implementation of a ViewFactory
o	Dependency injection

•	DTOs
o	TourDTO and TourLogDTO
o	Decoupling from entities

Technology Stack
•	Backend Framework: Spring Boot
•	Frontend Framework: JavaFX
•	Database: PostgreSQL
•	ORM: JPA/Hibernate
•	PDF Generation: iText
•	JSON Processing: Jackson
•	External API: OpenRouteService

Unique Feature: Statistics Dashboard
The Statistics Dashboard is the unique feature that we developed for our project. It provides analytics and insights for tour planning and performance analysis.
•	Comprehensive data analytics
•	Total tours and logs: Overview of all tour data
•	Most popular tour: Automatic identification based on log count
•	Real-Time dashboard
o	Automatic updates: Statistics update when data changes
o	Professional layout: Clean dashboard design
o	Quick insights: Immediate access to metrics

Time Tracking
•	Planning & Design
o	13 hours
o	Requirements analysis, architecture design and technology selection
•	Backend Development
o	23 hours
o	Entity design, repository implementation and service layer development
•	Frontend Development
o	26 hours
o	JavaFX UI development, FXML design, ViewModel implementation
•	External API Integration
o	10 hours
o	OpenRouteService integration, geocoding, route calculation |
•	Testing
o	10 hours
o	Unit tests, integration tests, test configuration
•	Documentation
o	5 hours
o	Code documentation, protocol writing, UML diagram |
•	Bug fixing
o	8 hours
o	Performance optimization, UI improvements |
•	Total
o	95 hours
o	Complete application development

Lessons from this project
•	External API integration: Took longer than expected due to lack of experience
•	UI Development: JavaFX had a steep learning curve, but was manageable
•	Spring Boot: Spring Boot was initially hard to implement, but there are a lot of resources available


