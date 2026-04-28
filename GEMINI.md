# Project Overview

The `scrapingant4j` project is a Java SDK for interacting with the ScrapingAnt API. It is structured as a multi-module Maven project, consisting of:

*   **`scrapingant4j-core`**: This module provides the core client functionality for the ScrapingAnt API, including request/response DTOs, API interfaces (using Feign), and client options. It is a pure Java library with no Spring-specific dependencies.
*   **`scrapingant4j-spring-boot-starter`**: This module offers a Spring Boot starter for `scrapingant4j-core`, simplifying integration into Spring Boot applications through auto-configuration.

The architecture with a separate core library and a Spring Boot starter is appropriate for this project's purpose. It allows users who are not on Spring Boot to use the core functionality directly, while providing a convenient integration path for Spring Boot users. This design enhances reusability and maintains a clear separation of concerns.

# Building and Running

This project uses Maven for dependency management and building.

*   **Build and Install all modules**:
    ```bash
    mvn clean install
    ```
*   **Run tests for all modules**:
    ```bash
    mvn verify
    ```
*   **Run SonarCloud analysis (requires `SONAR_TOKEN` and `GITHUB_TOKEN` environment variables)**:
    ```bash
    mvn sonar:sonar
    ```

# Development Conventions

*   **Build Tool**: Apache Maven
*   **Language**: Java 21
*   **Code Quality**: SonarCloud for static analysis.
*   **Code Coverage**: JaCoCo for reporting code coverage, integrated with SonarCloud.
*   **Code Formatting**: Spotless Maven Plugin with Google Java Format.
*   **Code Style**: Checkstyle Maven Plugin using `google_checks.xml`.
*   **API Client**: Feign for declarative REST client.
*   **JSON Processing**: Google Gson library.
*   **Dependency Management**: Bill of Materials (BOM) for Spring Boot and Spring Cloud dependencies.
