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

## Backend Module Pattern

Use the current `bookings` backend shape as the reference pattern for new backend work and for larger changes in
existing modules. When touching an existing module that does not follow this structure yet, complete the requested
change locally and include a clear structural alignment proposal for refactoring the whole module; do not perform a
broad module-wide refactor without explicit approval.

Target package shapes:

```text
<feature>.web -> <feature>.usecase -> <feature>.<submodule>.domain
                                    -> <feature>.<submodule>.infrastructure
```

For simpler modules without submodules:

```text
<feature>.web -> <feature>.usecase -> <feature>.domain
                                    -> <feature>.infrastructure
```

Boundary rules:

- Controllers live in `web` and call use cases, not domain services or repositories.
- Use cases are the application boundary, transaction boundary, and orchestration layer. Write operations and
  cross-domain flows should start there; read-only use cases may use read-only transactions when useful.
- Keep one public use case class per business operation, such as `CreateReservationUseCase`; do not create broad facade
  classes such as `BookingUseCases`.
- Use cases call only domain services. They must not inject repositories directly. If a read model is needed, add a
  dedicated domain service for that read path.
- Domain services expose business capabilities of their own model, not persistence mechanics.
- Domain submodules do not orchestrate each other and do not call each other's services or repositories. They may share
  stable value/domain types when practical, such as `UserId`, `TermId`, `EmailAddress`, or `Location`.
- Cross-module flows belong in `usecase`, for example "reservation or waitlist entry", waitlist promotion, notification
  dispatch decisions, and identity registration during booking.
- Use cases may return domain types for simple single-model results. Create dedicated `*Result` classes for compound
  orchestration results. HTTP DTOs stay in `web`.
- Document any deviation from these boundaries in the final response or change documentation, including the reason and
  the proposed path back to the standard.

Testing rules:

- Domain tests cover rules of one model or submodule without Spring and without orchestration across other submodules.
- Use case tests cover orchestration, transaction-level business flows, compound results, and module cooperation. Prefer
  real domain services with in-memory port implementations; use mocks only for external gateways/adapters or narrow
  contract tests.
- Web tests cover HTTP contracts, security, request/response mapping, and exception handling without duplicating the
  full domain test matrix.

Backend quality rules:

- Every new backend production package must be under `@NullMarked` coverage with a `package-info.java`.
- Schema changes must be expressed through Flyway migrations in `backend/src/main/resources/db/migration`; do not use
  Hibernate DDL generation for schema control.

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

<!-- BEGIN @przeprogramowani/10x-cli -->

## 10xDevs AI Toolkit - Module 2, Lesson 1

Move from sprint-zero setup to project orchestration with the **roadmap chain**:

```
(Module 1 foundation docs) -> /10x-roadmap -> backlog-ready roadmap items
```

`/10x-roadmap` is the lesson focus. `/10x-new` is intentionally introduced in Module 2, Lesson 2, when a selected
roadmap item becomes an implementation change folder.

### Task Router - Where to start

| Skill                                                                                                                   | Use it when                                                                                                                                                                                                                                                                                                                                                                                |
|-------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Roadmap (lesson focus)**                                                                                              |                                                                                                                                                                                                                                                                                                                                                                                            |
| `/10x-roadmap`                                                                                                          | You have `context/foundation/prd.md` and a scaffolded project baseline, and you need a vertical-first MVP roadmap. The skill reads the PRD, inspects the code baseline, uses available foundation docs such as `tech-stack.md`, `infrastructure.md`, and `deploy-plan.md`, then writes `context/foundation/roadmap.md`. Use it BEFORE creating per-change folders or implementation plans. |
| **Re-run upstream if needed**                                                                                           |                                                                                                                                                                                                                                                                                                                                                                                            |
| `/10x-shape` / `/10x-prd` / `/10x-tech-stack-selector` / `/10x-bootstrapper` / `/10x-agents-md` / `/10x-infra-research` | Bundled from Module 1 so foundation contracts can be fixed before roadmap sequencing. If roadmap generation exposes a PRD gap, repair the PRD before pretending the backlog is ready.                                                                                                                                                                                                      |

### How the chain hands off

- `/10x-roadmap` bridges product and implementation. It does not choose frameworks, design schemas, or write a
  per-change implementation plan.
- The output is `context/foundation/roadmap.md`: ordered milestones, vertical slices, bounded foundations, dependencies,
  unknowns, risk, and backlog handoff fields.
- Roadmap items should receive stable human-readable identifiers in backlog tools. The actual
  `context/changes/<change-id>/` folder is created in Lesson 2 with `/10x-new`.

### Roadmap boundaries

- Default to vertical slices: user-visible outcomes that cross UI, data, business logic, and integrations.
- Horizontal work is allowed only as a bounded enabler that names the downstream vertical milestone it unlocks.
- Avoid orphan horizontal work such as "build the whole database", "build all API endpoints", or "design the whole UI"
  before the first user-visible flow.
- Roadmap is not a calendar estimate. Do not invent dates, story points, or sprint velocity unless the user explicitly
  asks for a separate planning artifact.

### Foundation paths used by this lesson

- `context/foundation/prd.md` - input
- `context/foundation/tech-stack.md` - optional input
- `context/foundation/infrastructure.md` - optional input
- `context/deployment/deploy-plan.md` - optional input
- `context/foundation/roadmap.md` - output
- `context/foundation/lessons.md` - recurring rules and pitfalls
- `docs/reference/contract-surfaces.md` - load-bearing names registry

Skills must not write to `context/archive/`. Archived changes are immutable; if a resolved target path starts with `context/archive/`, abort with: "This change is archived. Open a new change with `/10x-new` instead."

<!-- END @przeprogramowani/10x-cli -->
