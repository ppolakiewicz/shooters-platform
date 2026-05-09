# Shooters Platform Agent Notes

### General info
Always use `caveman` skill for communication.
When working use `grill-me` skill to get more context about changes.
When implementing use `tdd` skill.

## Project Structure

Project is split into three modules:

## Git workflow

All newly created files should be added to the Git stage before finishing the task.

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

Backend tests should be written with Spock specifications in `src/test/groovy`, not JUnit test classes. Spock tests should
use descriptive English labels for `given:`, `when:`, `then:` and `and:` blocks.

Use case logic should be tested with spring boot integration tests.

Web package should be tested with mock MVC that will send request to spring web controller.
Each web controller should have dedicated client in test package that will allow to communicate in tests with given
web resource.

#### Backend implementation notes
Spring configuration classes should live in dedicated `shared/config` subpackages, grouped by concern. For example, clock
configuration belongs in `shared/config/clock`, and security configuration belongs in `shared/config/security`.

Spring Boot 4 splits some auto-configurations into dedicated modules. 
When using JSON/object mapping directly, prefer the Spring Boot 4 Jackson setup and note
that application code may need `tools.jackson.databind.ObjectMapper`.

The domain package should expose domain services for module behavior instead of letting use cases call repositories
directly. In the identity module, use cases call `IdentityService`; that service owns domain rules such as email
normalization, password policy checks, authentication rules, and email uniqueness validation.

Do not add a use case layer when it only forwards calls to a single domain service. Add use cases only when orchestration
between more than one domain service/module or external concern is needed. If a controller calls a single module domain
service directly, put the transaction boundary on that service.

Domain model objects should use domain-specific value objects and enums instead of primitive identifiers, email strings,
or role strings. Convert those domain types to UUIDs, strings, or JSON-friendly DTO shapes at infrastructure and web
boundaries, not inside domain records or services.

Spring Security for the browser SPA uses server-side sessions and CSRF. Angular receives `XSRF-TOKEN` and sends
`X-XSRF-TOKEN` for mutating `/api` requests. Keep session/authentication mechanics in web or shared security
configuration, not in domain code.

Check roles and permissions in the web layer with `@PreAuthorize` on controllers or controller methods. Do not put role
checks in domain services or use cases; they should receive an already-authorized user context and enforce ownership/data
access rules only.

Keep controller request/response records in dedicated files in the module `web` package instead of nesting many records
inside a controller. Package-private records are preferred unless another package must use them.

Backend Java packages are JSpecify null-marked. When adding a new package under `src/main/java`, add a `package-info.java`
with `@NullMarked` unless the package intentionally needs `@NullUnmarked`.

With JSpecify `@NullMarked`, avoid defensive null-checks for parameters and record components that are declared non-null.
Use null-checks only at runtime boundaries or normalization helpers where values can genuinely be absent, and mark those
parameters with `@Nullable` so IDEs and NullAway agree with the check. Keep domain value validation such as ranges,
lengths, and allowed combinations even when null-checks are redundant.

Infrastructure packages that contain JPA entities may use `@NullUnmarked` because Hibernate initializes fields through
reflection. Classes in those packages that implement domain ports must add class-level `@NullMarked` to preserve the
domain interface contract. Apply the same rule to in-memory test implementations of domain ports.

Postgres 18 Docker images should mount persistent data at `/var/lib/postgresql`, not `/var/lib/postgresql/data`.

### Frontend
Angular application that uses angular material components.
Should follow latest angular best practices: standalone components, communication through signals, on push change detection
Each component should have unit tests that validate its expected behaviors.

For Angular 21 forms, prefer Signal Forms from `@angular/forms/signals` for new forms. Keep form models non-null, use
standalone components, Angular Material controls, signal-based state, and OnPush change detection.

Frontend unit tests use Angular's Vitest-based `@angular/build:unit-test` setup. Keep service, guard, and component tests
close to the relevant feature files.

### E2E
Contains end-to-end tests based on playwright to validate main application user paths.
E2E tests should exercise complete user paths through the UI. 
Playwright tests should include short `given:`, `when:`, and `then:` style comments that describe the main scenario
steps.
