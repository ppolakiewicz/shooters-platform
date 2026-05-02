# Shooters Platform Agent Notes

## Project Structure

Project is split into three modules:

### Backend
Backend is Spring Boot 4 Gradle application build with Java 25 and Postgres database.
For maintaining database Flyway migrations are used.
Backend is build as set of individual modules that use Domain Driven Design approach.
Each module can contain below main parts:
- domain package - where plain Java business logic is kept in hexagonal architecture with ports
- infrastructure package - where domain adapters are kept that provide connection to expected infrastructure components
- use case package - where logic that need to integrate different modules is kept
- web package - where module spring web controllers and DTOs are kept

#### Testing strategy
Domain logic should be tested using in memory implementation of port interfaces. Those in memory implementations should be
using hash map to store information and provide assertion methods to be used in tests. No mocking is allowed.

Use case logic should be tested with spring boot integration tests.

Web package should be tested with mock MVC that will send request to spring web controller.
Each web controller should have dedicated client in test package that will allow to communicate in tests with given
web resource.

### Frontend
Angular application that uses angular material components.
Should follow latest angular best practices: standalone components, communication through signals, on push change detection
Each component should have unit tests that validate its expected behaviors.

### E2E
Contains end-to-end tests based on playwright to validate main application user paths.
