---
project: shooters-platform
assessed_at: 2026-05-19T21:11:42.7117489+02:00
agent_readiness: ready
context_type: brownfield
stack_components:
  language: TypeScript 5.9 and Java 25
  framework: Angular 21.2 frontend and Spring Boot 4.0.5 backend
  build_tool: Angular CLI / @angular/build, Gradle Wrapper, npm workspaces
  test_runner: Vitest, Spock on JUnit Platform, Playwright
  package_manager: npm and Gradle
  ci_provider: GitHub Actions
  deployment_target: Docker Compose local PostgreSQL; no production target detected
gates_passed: 20
gates_failed: 0
---

## Stack Components

`shooters-platform` is an npm workspace rooted at `package.json`, with `frontend` and `e2e` workspaces and Node `>=20.19.0`. Package management is npm, proven by `package-lock.json`.

The frontend is `shooters-platform-frontend`, an Angular 21.2 application using TypeScript 5.9, Angular Material/CDK, RxJS, and Angular CLI builders. `frontend/tsconfig.json` enables `strict`, `noImplicitOverride`, `noImplicitReturns`, `noFallthroughCasesInSwitch`, strict injection parameters, strict input access modifiers, and strict templates.

The backend is a Java 25 Spring Boot 4.0.5 service built with the Gradle Wrapper. `backend/build.gradle` applies the Java, Groovy, Spring Boot, dependency management, Error Prone, and NullAway plugins. The backend uses Spring Web MVC, Spring Security, Spring Data JPA, Bean Validation, Flyway, PostgreSQL, and Bouncy Castle. Source layout follows feature packages such as `identity`, `training`, `bookings`, `health`, and `shared`, with visible `domain`, `web`, `infrastructure`, and `usecase` boundaries.

Testing is split by layer. The frontend uses Angular's unit-test builder with Vitest and jsdom. The backend uses Spock 2.4 on JUnit Platform, Spring Boot test support, Spring Security test support, and Testcontainers. The `e2e` workspace uses Playwright 1.59.1 with a typed `playwright.config.ts`.

CI is GitHub Actions in `.github/workflows/ci.yml`. It installs npm dependencies, builds and tests the backend, builds and tests the frontend, starts both apps, waits on health checks, and runs Playwright e2e tests in the official Playwright container. Local infrastructure is `docker-compose.yml` with PostgreSQL 18.3 and a healthcheck. No production deployment target was detected.

## Quality Gate Assessment

| Component | Typed | Convention | Training Data | Documented | Verdict |
| --- | --- | --- | --- | --- | --- |
| TypeScript frontend language | pass | n/a | n/a | n/a | pass |
| Java backend language | pass | n/a | n/a | n/a | pass |
| Angular frontend framework | n/a | pass | pass | pass | pass |
| Spring Boot backend framework | n/a | pass | pass | pass | pass |
| Angular CLI / @angular/build | n/a | pass | pass | pass | pass |
| Gradle Wrapper | n/a | pass | pass | pass | pass |
| Vitest unit tests | n/a | n/a | pass | pass | pass |
| Spock / JUnit Platform backend tests | n/a | n/a | pass | pass | pass |
| Playwright e2e tests | n/a | n/a | pass | pass | pass |

Legend: `pass` = criterion met, `fail` = criterion missed, `n/a` = not applicable for this component.

### Gate Details

Type safety passes strongly. The frontend is strict TypeScript via `frontend/tsconfig.json`, including strict Angular template checking. The backend is Java 25 and also configures NullAway with JSpecify mode in `backend/build.gradle`; `backend/src/main/java/com/shootersplatform/backend/package-info.java` marks the root package with `@NullMarked`.

Framework convention strength passes. Angular supplies CLI project structure, `src/app`, `app.routes.ts`, component/test naming, and Angular builder conventions in `frontend/angular.json`. Spring Boot supplies application wiring, externalized configuration, auto-configuration, and standard application/test layouts. The backend also has a visible package convention around `domain`, `web`, `infrastructure`, and `usecase`.

Training-data familiarity passes within each language family. Angular is a mainstream TypeScript frontend framework. Spring Boot is a mainstream Java backend framework. Gradle, Vitest, Spock/JUnit, and Playwright are common tools in their respective ecosystems, so agents can usually lean on established idioms.

Documentation passes. Angular's official release documentation shows Angular 21 under support on the assessment date. Spring's official documentation lists Spring Boot 4.0.x as a stable documented line, and Spring announced 4.0.5 as available. Gradle, Vitest, Playwright, and Spock all have official reference or user documentation. Spring Boot 4.0.6 is already listed as the current stable patch line, so this project is one Spring Boot patch behind, but that is not an agent-readiness problem.

## Gaps & Compensation

No quality-gate failures were found. The stack is typed, convention-heavy, mainstream for its ecosystems, and backed by official documentation.

The main improvement opportunity is not a stack gap. `AGENTS.md` currently documents the 10x workflow but not the codebase's own implementation conventions. Adding project-specific instructions would reduce agent rediscovery work, especially around backend package boundaries and Angular feature layout.

### Recommended Instruction File Additions

These additions are optional because the stack already passes the assessment criteria, but they are ready to paste into `AGENTS.md` or `CLAUDE.md`.

```markdown
## Project Stack

This repository is an npm workspace with `frontend` and `e2e`, plus a Java backend in `backend`.

- Frontend: Angular 21, TypeScript 5.9, Angular Material, RxJS.
- Backend: Java 25, Spring Boot 4.0, Gradle Wrapper, Spring Web MVC, Spring Security, Spring Data JPA, Flyway, PostgreSQL.
- Tests: Vitest for Angular unit tests, Spock/JUnit Platform for backend tests, Playwright for e2e tests.
- Local infrastructure: PostgreSQL is provided by `docker-compose.yml`.
```

```markdown
## Backend Conventions

Backend code lives under `backend/src/main/java/com/shootersplatform/backend`.

- Keep domain rules in `domain` packages.
- Keep HTTP controllers, request/response DTOs, and exception handlers in `web` packages.
- Keep JPA entities, Spring Data repositories, and persistence adapters in `infrastructure` packages.
- Keep orchestration that crosses domain boundaries in `usecase` packages.
- Preserve Java null-safety discipline: new backend packages should be covered by `@NullMarked`, and new code must satisfy NullAway.
- Use Flyway migrations in `backend/src/main/resources/db/migration` for schema changes; do not rely on Hibernate DDL generation.
```

```markdown
## Frontend Conventions

Frontend code lives under `frontend/src/app`.

- Keep feature code grouped by domain folder such as `identity`, `training`, `bookings`, and `home`.
- Keep routes in `app.routes.ts`.
- Prefer Angular standalone components and typed services that match existing component/service/spec naming.
- Preserve strict TypeScript and strict Angular template compatibility.
- Add or update `*.spec.ts` tests when changing components, guards, or services.
```

```markdown
## Test Commands

- Full frontend build: `npm run frontend:build`
- Frontend unit tests: `npm run test --workspace frontend`
- Backend build and tests: run `.\gradlew.bat build` from `backend` on Windows, or `./gradlew build` from `backend` on Unix.
- E2E tests: `npm run e2e:test`
```

## Summary

Overall verdict: ready. The strongest agent-readiness signals are strict TypeScript, Java with NullAway, convention-based Angular and Spring Boot structure, and a CI workflow that exercises backend, frontend, and e2e tests.

No stack compensation is required. The recommended next step is `/10x-health-check`, focused on dependency health, test reliability, CI coverage, and any project-specific risks outside the stack-selection criteria.
