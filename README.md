# Shooters Platform

Repository layout:

- `backend` - Spring Boot Gradle application with PostgreSQL, JPA, Flyway, and Testcontainers.
- `frontend` - Angular application that calls the backend through `/api`.
- `e2e` - Playwright end-to-end tests for the browser flow.

## Prerequisites

- Java 25.
- Node.js 20.19+, 22.12+, or 24+ for Angular 21.
- Docker for the local PostgreSQL database.

## Backend

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the Spring Boot application:

```bash
cd backend
./gradlew bootRun
```

The backend exposes readiness at `GET http://localhost:8080/actuator/health`.

## Frontend

Install JavaScript dependencies:

```bash
npm install
```

Run Angular:

```bash
npm run frontend:start
```

The frontend runs on `http://localhost:4200` and proxies `/api` to the backend.

## End-to-End Tests

Install Playwright browsers once:

```bash
npm run --workspace e2e install:browsers
```

Run tests:

```bash
npm run e2e:test
```

Set `E2E_SKIP_WEBSERVER=1` when the frontend is already running.
