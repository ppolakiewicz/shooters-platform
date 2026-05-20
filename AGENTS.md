# Repository Guidelines

Shooters Platform is a multi-stack training and booking app: Spring Boot backend, Angular frontend, and Playwright e2e workspace. Use @README.md for local startup details and @context/foundation/stack-assessment.md for the current stack assessment.

## Hard Rules

- Do not write to `context/archive/`; archived changes are immutable. Open a new change instead.
- Keep backend schema changes in Flyway migrations under `backend/src/main/resources/db/migration`; do not rely on Hibernate DDL generation.
- Preserve Java null-safety: new backend packages must remain under `@NullMarked` coverage and pass NullAway/JSpecify checks from @backend/build.gradle.

## Project Structure

- `backend/` is a Java 25 Spring Boot 4.0 Gradle application using PostgreSQL, JPA, Flyway, Spring Security, Error Prone, NullAway, Spock, and Testcontainers.
- `frontend/` is an Angular 21 TypeScript 5.9 app with Angular Material, strict template checking, Vitest, and Angular ESLint.
- `e2e/` contains Playwright browser tests; `package.json` workspaces include `frontend` and `e2e`.
- Backend features live under `backend/src/main/java/com/shootersplatform/backend/<feature>/` with observed `domain`, `web`, `infrastructure`, and `usecase` boundaries. Current top-level features include `identity`, `training`, `bookings`, `health`, and `shared`.

## Commands

- `npm install` installs root workspace dependencies.
- `docker compose up -d postgres` starts the local PostgreSQL service used by the backend.
- From `backend/`, run `./gradlew build` on Unix or `.\gradlew.bat build` on Windows for backend compile and tests; use `bootRun` to start the API.
- `npm run frontend:start` serves Angular on `http://localhost:4200` and proxies `/api` to the backend.
- `npm run frontend:build`, `npm run test --workspace frontend`, and `npm run lint --workspace frontend` match the frontend CI checks.
- `npm run --workspace e2e install:browsers` installs Playwright browsers once; `npm run e2e:test` runs e2e tests. Set `E2E_SKIP_WEBSERVER=1` when both apps are already running.

## Style And Naming

@.editorconfig enforces LF, final newline, 2-space default indentation, and 4-space Java/Gradle indentation. Angular selectors must use `app` prefixes: element components in kebab-case and attribute directives in camelCase, as configured in @frontend/eslint.config.js. Keep Angular feature files co-located as `*.component.ts`, `*.component.html`, `*.component.css`, `*.service.ts`, `*.models.ts`, and matching `*.spec.ts` files.

## Testing And CI

CI in @.github/workflows/ci.yml builds/tests the backend, builds/tests/lints the frontend, starts both apps, waits for `/api/health`, then runs Playwright. @.github/workflows/osv-scanner.yml runs recursive OSV scans on pull requests, merge queue, pushes to `main`, weekly schedule, and manual dispatch.

## Commits And PRs

Recent history uses Conventional Commit-style subjects such as `feat(booking): ...`, `refactor(identity): ...`, and `chore(CI): ...`. Keep PR descriptions tied to the changed feature area and name the commands you ran, especially when skipping e2e or OSV-related checks.
