# TourPlanner Protocol Documentation

This directory contains the comprehensive protocol documentation for the TourPlanner application, including UML diagrams and technical specifications.

## Files Overview

### Main Documentation
- **`../PROTOCOL.md`** - Complete protocol document covering all requirements
- **`README.md`** - This file, explaining the documentation structure

### UML Diagrams (PlantUML)
- **`class-diagram.puml`** - Complete class diagram showing application architecture
- **`search-sequence-diagram.puml`** - Sequence diagram for full-text search functionality
- **`use-case-diagram.puml`** - Use case diagram showing all application features

## How to View UML Diagrams

### Option 1: Online PlantUML Editor
1. Go to [PlantUML Online Editor](http://www.plantuml.com/plantuml/uml/)
2. Copy the content of any `.puml` file
3. Paste it into the editor
4. The diagram will be generated automatically

### Option 2: VS Code Extension
1. Install the "PlantUML" extension in VS Code
2. Open any `.puml` file
3. Press `Alt+Shift+D` to preview the diagram
4. Or right-click and select "Preview Current Diagram"

### Option 3: IntelliJ IDEA Plugin
1. Install the "PlantUML integration" plugin
2. Open any `.puml` file
3. The diagram will be displayed automatically

### Option 4: Command Line (if PlantUML is installed)
```bash
# Install PlantUML (requires Java)
# Then run:
plantuml class-diagram.puml
plantuml search-sequence-diagram.puml
plantuml use-case-diagram.puml
```

## Protocol Sections Covered

The main protocol document (`../PROTOCOL.md`) covers all required sections:

### ✅ Technical Steps and Decisions
- Design decisions and rationale
- Failures encountered and solutions implemented
- Architecture choices and their benefits

### ✅ Application Architecture
- Complete 3-tier architecture description
- Layer contents and functionality
- Detailed class relationships

### ✅ Use Case Documentation
- Primary use cases with detailed flows
- Actor interactions and system responses
- Preconditions and postconditions

### ✅ UI Flow Documentation
- Main application flow diagrams
- Wireframes for key screens
- User interaction patterns

### ✅ UML Diagrams
- **Class Diagram**: Complete application architecture
- **Sequence Diagram**: Full-text search implementation
- **Use Case Diagram**: All application features

### ✅ Library Decisions and Lessons Learned
- Technology stack choices and rationale
- Problems encountered and solutions
- Best practices discovered

### ✅ Design Patterns
- MVVM implementation details
- Repository pattern usage
- Factory pattern application
- DTO pattern implementation

### ✅ Unique Features
- Automatic route calculation
- Interactive map integration
- PDF report generation with maps
- Real-time search with auto-complete
- Comprehensive import/export

### ✅ Time Tracking
- Detailed time breakdown by phase
- Key milestones and achievements
- Lessons from time management

## Key Architecture Highlights

### MVVM Pattern
The application implements a complete MVVM (Model-View-ViewModel) architecture:
- **Views**: JavaFX FXML files with controllers
- **ViewModels**: Business logic and data binding
- **Models**: JPA entities and DTOs

### External API Integration
- **OpenRouteService**: For route calculation and geocoding
- **Async Processing**: Non-blocking UI during API calls
- **Error Handling**: Graceful degradation when APIs are unavailable

### Testing Strategy
- **Integration Tests**: Real services with in-memory H2 database
- **No Mockito**: Avoided due to Java 21 compatibility issues
- **Comprehensive Coverage**: All major functionality tested

## Technology Stack

- **Backend**: Spring Boot 3.3.0, JPA/Hibernate
- **Frontend**: JavaFX 21, FXML
- **Database**: H2 Database (file-based for production)
- **External APIs**: OpenRouteService for routing
- **PDF Generation**: iText 8.0.2
- **JSON Processing**: Jackson 2.15.2

## Getting Started

To understand the application architecture:

1. Start with the **Class Diagram** (`class-diagram.puml`) to see the overall structure
2. Review the **Use Case Diagram** (`use-case-diagram.puml`) to understand features
3. Examine the **Sequence Diagram** (`search-sequence-diagram.puml`) for interaction flows
4. Read the main **Protocol Document** (`../PROTOCOL.md`) for complete details

## Contributing

When updating the documentation:
1. Update the relevant `.puml` files for diagram changes
2. Regenerate diagrams using PlantUML
3. Update the main protocol document to reflect changes
4. Ensure all sections remain comprehensive and accurate 