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

## 10xDevs AI Toolkit — Module 1, Lesson 3

Scaffold the project for the stack you picked in Lesson 2, with the **bootstrap chain**:

```
(/10x-init  →  /10x-shape  →  /10x-prd)  →  /10x-tech-stack-selector  →  /10x-bootstrapper
```

The PRD chain ships from Lesson 1 and the tech-stack-selector ships from Lesson 2 — both re-included in this lesson so you can fix the PRD or swap the stack mid-flight. `/10x-bootstrapper` is the lesson's main topic. The chain ends here in v1; a future Lesson 4 will set up agent context (`AGENTS.md`).

### Task Router — Where to start

| Skill | Use it when |
| --- | --- |
| **Bootstrap (lesson focus)** | |
| `/10x-bootstrapper` | You have a hand-off at `context/foundation/tech-stack.md` (written by `/10x-tech-stack-selector`) and you are ready to scaffold the project into the current directory. The skill reads the hand-off, looks up the chosen card in the starter registry, runs its CLI through one of three cwd strategies (scaffold into a temp directory then move files up; scaffold directly into the current directory; clone a starter repo without keeping its git history), preserves `context/` always, sidelines other clashes as `.scaffold` siblings, runs a light pre-scaffold recency check and a deeper post-scaffold audit, and writes a verification log to `context/changes/bootstrap-verification/verification.md`. Use AFTER `/10x-tech-stack-selector`. |
| **Re-run upstream if needed** | |
| `/10x-init` / `/10x-shape` / `/10x-prd` / `/10x-tech-stack-selector` | Bundled so you can fix the PRD or swap the stack mid-flight. If `/10x-bootstrapper` surfaces a registry-drift refusal or you change your mind on the starter, re-run `/10x-tech-stack-selector` to regenerate `tech-stack.md` and re-invoke. |

### How the chain hands off

- `/10x-tech-stack-selector` (Lesson 2) writes `context/foundation/tech-stack.md` with a 4-key frontmatter (`starter_id`, `package_manager`, `project_name`, `hints`) plus a one-paragraph `## Why this stack` body.
- `/10x-bootstrapper` reads that file FULLY (no fallback to conversation history). If it is absent, the skill refuses with a one-sentence redirect to `/10x-tech-stack-selector` and stops — no inline mini-handoff, no standalone-mode in v1.
- The chosen `starter_id` is looked up in `/skills/10x-tech-stack-selector/references/starter-registry.yaml`. The skill consumes that registry; it does not own it. A CI validator (`scripts/validate-starter-registry-sync.mjs`) prevents bootstrapper from referencing a `starter_id` absent from the registry.
- The skill writes `context/changes/bootstrap-verification/verification.md` as the audit-trail log for the run. Schema in `/skills/10x-bootstrapper/references/verification-log-schema.md`.

### What bootstrapper captures (and what it does NOT)

- **Captured (v1)**: scaffold via the chosen card's `cmd_template` (CLI delegation, not inline file generation), three cwd strategies dispatched from `bootstrapper-config.yaml` (`subdir-then-move`, `native-cwd`, `git-clone`), strict conflict policy producing `.scaffold` siblings + always preserving `context/`, two verification slots (light pre-scaffold recency check + deep post-scaffold language-aware audit), severity-tiered audit summary, full verification log on disk.
- **NOT captured in v1 (deliberate)**: `AGENTS.md` generation (deferred to a future Lesson 4 — "Memory Architecture"); per-starter cert-element placement overlays (live with the future agent-context skill, not here); CI workflow files; AI-as-bridge fallback for stacks outside the registry (deferred to v2 — in v1 chain-mode tech-stack-selector already gates on the registry, so the case cannot arise); standalone-mode where the user names a stack inline without a hand-off (deferred to v2); compensation actions for `bootstrapper_confidence: best-effort` or `quality_override: true` (surfaced in conversation but no automated follow-up — that, too, is the future memory-architecture skill's job).

### The conflict policy

When the skill moves files from a temp scaffold directory up into your current working directory, it applies a strict matrix:

- **`context/**`** — anything the scaffold tried to write under `context/` is **dropped**. Your `context/` is the source of truth for the bootstrap chain (PRD, tech-stack hand-off, plans, frames) and is never overwritten.
- **`.gitignore`** — append-merged: your existing lines stay in order, then the scaffold's lines are de-duped against your set and appended with a separator comment. Git's ignore semantics are additive, so combining is safe.
- **`package.json`, `README.md`, `AGENTS.md`, root-level `*.md`** — your existing file wins; the scaffold's copy lands as `<filename>.scaffold` sibling. You can `diff README.md README.md.scaffold` to see what the starter shipped vs what you had.
- **Anything else** — moves silently if no conflict, sidelined as `<filename>.scaffold` if there is one. The matrix never deletes user files.

For the `git-clone` strategy (10x-astro-starter and similar): the cloned `.git/` is deleted before move-up, so the upstream starter's history does not leak into your repo. You initialise your own history afterwards (`git init`).

### Verification log

Every run writes `context/changes/bootstrap-verification/verification.md`. Sections:

- **`## Hand-off`** — verbatim copy of the tech-stack.md frontmatter and `## Why this stack` body.
- **`## Pre-scaffold verification`** — recency findings table (npm package version + `time.modified` for JS starters; GitHub `pushed_at` for any starter with a GitHub `docs_url`).
- **`## Scaffold log`** — the resolved CLI invocation, exit code, files moved, conflicts surfaced as `.scaffold` siblings, `.gitignore` handling.
- **`## Post-scaffold audit`** — full per-language audit output (`npm audit --json` for JS, `pip-audit` for Python, `cargo audit` for Rust, etc.). Severity-tiered: CRITICAL and HIGH surfaced inline in chat, MODERATE and LOW log-only. Direct-vs-transitive split where the tool supports it.
- **`## Hints recorded but not acted on`** — every hint from the hand-off bootstrapper read but did not act on in v1. Audit-trail completeness for the future memory-architecture skill.
- **`## Next steps`** — pointer text. v1 names "your project is scaffolded and verified — happy hacking" and flags the future Lesson 4 skill as the next chain link.

The folder (`context/changes/bootstrap-verification/`) deliberately has no `change.md`. Bootstrap runs are one-shot artifacts, not tracked workflow changes — the folder hosts the log and nothing else. Re-runs apply a warn-and-confirm guard before overwriting; the escape hatch is `verification-v2.md` (and so on).

### Foundation paths used by this lesson

- `context/foundation/tech-stack.md` — input (from Lesson 2)
- `context/changes/bootstrap-verification/verification.md` — output (the audit-trail log)
- `context/foundation/lessons.md` — recurring rules & pitfalls
- `docs/reference/contract-surfaces.md` — load-bearing names registry

### Universal language

The shipped skill carries no 10xDevs / cohort / certification references. The post-scaffold audit dispatches by `language_family` against a small lookup table; cohorts whose stack lands in `java`, `php`, `dart`, or a multi-language combination see a "no built-in audit tool for this ecosystem" log line and a recommended external tool, not a fake "0 findings" record.

Skills must not write to `context/archive/`. Archived changes are immutable; if a resolved target path starts with `context/archive/`, abort with: "This change is archived. Open a new change with `/10x-new` instead."

<!-- END @przeprogramowani/10x-cli -->
