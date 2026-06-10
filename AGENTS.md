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
- Backend features live under `backend/src/main/java/com/shootersplatform/backend/<feature>/` with observed `domain`,
  `web`, `infrastructure`, and `usecase` boundaries. Current top-level features include `identity`, `bookings`, and
  `shared`.

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
- Domain tests uses in-memory port implementations
- Use case tests cover orchestration, transaction-level business flows, compound results, and module cooperation. Prefer
  real domain services with in-memory port implementations; use mocks only for external gateways/adapters or narrow
  contract tests.
- Web tests cover main business cases, main business lifecycle, HTTP contracts, security, request/response mapping, and
  exception handling without duplicating the
  full domain test matrix.
- Web tests uses spring integration tests and created api clients that encapsulates communication with web api.

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

CI in @.github/workflows/ci.yml builds/tests the backend, builds/tests/lints the frontend, starts both apps, waits for
`/actuator/health`, then runs Playwright. @.github/workflows/osv-scanner.yml runs recursive OSV scans on pull requests,
merge queue, pushes to `main`, weekly schedule, and manual dispatch.

## Commits And PRs

Recent history uses Conventional Commit-style subjects such as `feat(booking): ...`, `refactor(identity): ...`, and `chore(CI): ...`. Keep PR descriptions tied to the changed feature area and name the commands you ran, especially when skipping e2e or OSV-related checks.

<!-- BEGIN @przeprogramowani/10x-cli -->

## 10xDevs AI Toolkit - Module 2, Lesson 5

Scale the single-change cycle into parallel work with **worktrees, goal-directed delegation, and multi-session
orchestration**:

```
worktree per change -> /goal or your AI coding assistant -p -> PR -> review -> merge
```

The lesson focus is safe throughput: isolated contexts, choosing the right execution mode, and capping parallelism at
review capacity.

### Task Router - Where to start

| Skill                                                   | Use it when                                                                                                                                  |
|---------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| **Code isolation**                                      |                                                                                                                                              |
| `git worktree add`                                      | You need a separate working directory for a parallel change. One change per worktree, one fresh agent context per worktree.                  |
| **Complex changes**                                     |                                                                                                                                              |
| `/10x-implement <change-id> phase <n>`                  | The change has multiple phases, needs manual gates, or benefits from interactive decision-making during execution.                           |
| **Simple changes**                                      |                                                                                                                                              |
| `/goal`                                                 | You have a clear, bounded task and want goal-directed delegation. The agent works autonomously toward the stated goal with a stop condition. |
| `your AI coding assistant -p`                           | You want headless execution for a well-defined task. The Ralph Wiggum loop (run, check, retry) is the universal autonomous pattern.          |
| **Multi-session orchestration**                         |                                                                                                                                              |
| Superset / Conductor / Antigravity / VS Code Agent View | You are running multiple agent sessions in parallel and need visibility, coordination, or session management across them.                    |

### Parallel work rules

- One change per worktree or isolated workspace. One fresh agent context per change.
- Choose interactive `/10x-implement` for complex changes, `/goal` or `your AI coding assistant -p` for simple ones.
- Parallelism is capped by review capacity. More agents without review means more unreviewed code, not higher
  throughput.
- The quality pain from faster shipping is intentional — it bridges into Module 3 testing gates.

### Lesson boundaries

- Do not reteach interactive `/10x-implement` or `/10x-impl-review`; those are Lessons 2 and 3.
- Do not introduce testing strategy here. The quality pain is the motivation for Module 3.
- Worktrees are a mechanism for isolation, not the topic of a full git tutorial.

### Paths used by this lesson

- `context/changes/<change-id>/` - active change folder
- `context/changes/<change-id>/plan.md` - implementation input for any execution mode

Skills must not write to `context/archive/`. Archived changes are immutable; if a resolved target path starts with `context/archive/`, abort with: "This change is archived. Open a new change with `/10x-new` instead."

<!-- END @przeprogramowani/10x-cli -->
