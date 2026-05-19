---
project: shooters-platform
checked_at: 2026-05-19T22:11:59.5899917+02:00
health_status: needs-attention
context_type: brownfield
language_family: multi
stack_assessment_available: true
checks_run:
  - lockfile
  - dependency_audit
  - outdated_deps
  - test_runner
  - ci_cd
  - configuration
audit_findings:
  critical: 0
  high: 0
  moderate: 0
  low: 0
test_runner_detected: true
ci_provider: GitHub Actions
recommended_fixes: 5
---

## Dependency Health

### Lockfile

Status: present (`package-lock.json`, `backend/gradle.lockfile`)
Package manager: npm workspaces and Gradle Wrapper

The root npm workspace is pinned with `package-lock.json`. The backend uses Gradle dependency locking through `backend/gradle.lockfile`, so both detected package-management surfaces have reproducible dependency state.

### Security Audit

Tool: `npm.cmd audit --json`; Java audit skipped - no built-in Java audit command exists in this health-check dispatch table.
Summary: 0 CRITICAL, 0 HIGH, 0 MODERATE, 0 LOW
Direct vs transitive: npm reported no vulnerabilities across 677 dependencies.

Recommended external Java tool: the repository already has `.github/workflows/osv-scanner.yml`, which runs OSV Scanner recursively on pull requests, merge queue, pushes to `main`, weekly schedule, and manual dispatch.

### Outdated Dependencies

Packages with major version gaps: unable to determine in this run.

`npm.cmd outdated --json` was attempted, but the escalation request was rejected because it would send private workspace package metadata to the external npm registry. The check is recorded as skipped rather than retried through another route.

## Test Suite

Test runner: Vitest, Spock/JUnit Platform, Playwright
Tests found: 34 frontend tests, 81 backend tests, 4 e2e tests discovered
Test execution: passing for frontend and backend; e2e enumeration passing, full e2e execution not attempted in this local health check

Configuration: `frontend/angular.json`, `backend/build.gradle`, `e2e/playwright.config.ts`
Framework: Angular unit-test builder with Vitest 4.1.5, Spock 2.4 on JUnit Platform, Playwright 1.59.1

Verification commands run:

```powershell
npm.cmd run test --workspace frontend
.\gradlew.bat test --no-daemon
npm.cmd exec --workspace e2e -- playwright test --list
```

Results:

- Frontend: 13 test files passed, 34 tests passed.
- Backend: Gradle `test` completed successfully; existing JUnit XML reports show 18 suites, 81 tests, 0 failures, 0 errors.
- E2E: 4 Playwright tests discovered across 3 files.

## CI/CD

Provider: GitHub Actions
Configuration: `.github/workflows/ci.yml`, `.github/workflows/osv-scanner.yml`

| Stage | Status | Notes |
| --- | --- | --- |
| Lint | missing | `frontend/package.json` has `lint`, but CI does not run it. No backend style-only lint task is configured beyond Error Prone and NullAway during compilation. |
| Test | present | CI runs backend tests, frontend tests, and Playwright e2e tests. |
| Build | present | CI runs `backend/gradlew build` and `npm run frontend:build`. |
| Type check | present | Angular build enforces strict TypeScript/template checking; backend compilation enforces Java compilation, Error Prone, and NullAway. |
| Security | present | Separate OSV Scanner workflow runs recursive scans. |

## Configuration

### High severity

No high-severity configuration gaps detected.

### Medium severity

- **Formatter configuration** - `.prettierrc*` or `biome.json` was not found. This matters because agent edits will be more consistent when formatting is explicit. Fix: add a formatter config, for example `npm.cmd install --save-dev prettier` and a root `.prettierrc.json`, or adopt Biome if you want one tool for formatting and linting.
- **CI lint coverage** - CI does not run the existing frontend lint script. This matters because agent edits can pass tests while still violating style or static rules. Fix: add a GitHub Actions step such as `npm run lint --workspace frontend`.

### Low severity

- **Environment template** - `.env.example` or `.env.template` was not found. This matters because agents and new contributors need a stable source for required local variables. Fix: add `.env.example` documenting local PostgreSQL and app variables without secrets.

Present and healthy: `.editorconfig`, `.gitignore`, `AGENTS.md`, strict `frontend/tsconfig.json`, frontend ESLint config, local `docker-compose.yml`, npm lockfile, and Gradle lockfile.

## Stack Assessment Cross-Reference

Stack assessment: `context/foundation/stack-assessment.md`
Agent readiness (from stack-assess): ready

| Quality Gate Gap | Health-Check Finding | Status |
| --- | --- | --- |
| none | Stack assessment found 20 gates passed and 0 failed. | Reinforced |
| project-specific agent instructions recommended | `AGENTS.md` exists, but the prior stack assessment notes it mainly documents the 10x workflow rather than codebase-specific Angular/Spring conventions. | Follow-up |

## Recommended Fixes

### Fix before agent work (Category A)

### 1. Add explicit formatter configuration

**Impact**: Agent-generated changes will be easier to review and less likely to create style churn.
**Severity**: medium
**Effort**: moderate (15-30 min)
**Fix**:

```powershell
npm.cmd install --save-dev prettier
```

Then add a root `.prettierrc.json` and a format script, or choose `biome.json` if you want a combined formatter/linter.

### 2. Run lint in CI

**Impact**: Agents can currently rely on tests and builds, but not on CI-enforced lint feedback.
**Severity**: medium
**Effort**: quick (< 5 min)
**Fix**:

Add this after the frontend test step in `.github/workflows/ci.yml`:

```yaml
- name: Lint frontend
  run: npm run lint --workspace frontend
```

### 3. Decide how to handle dependency staleness checks

**Impact**: The security audit is clean, but this run could not compare installed package versions with latest registry versions without exporting private package metadata.
**Severity**: low
**Effort**: quick (< 5 min)
**Fix**:

Run `npm.cmd outdated --json` only after explicitly approving external registry access, or rely on a trusted internal/dependency-management tool for staleness tracking.

### 4. Add an environment template

**Impact**: Agents and new contributors can infer local setup from `docker-compose.yml`, but an explicit template reduces rediscovery.
**Severity**: low
**Effort**: quick (< 5 min)
**Fix**:

Create `.env.example` with non-secret local defaults and document which values are required by the backend and frontend.

### Addressed in upcoming lessons (Category B)

### Project-specific AI assistant instructions

**Lesson**: [Agent Onboarding: Agents.md, AI Rules i feedback loops (M1L4)](https://platforma.przeprogramowani.pl/external/10xdevs-3/m1-l4)
**What you'll do there**: extend the existing `AGENTS.md` with codebase-specific Angular, Spring Boot, testing, and boundary conventions instead of generating a generic stub now.

## Summary

Health status: needs-attention

The project is in solid operational shape for agent-assisted development: dependencies are locked, npm audit is clean, strict typing is enabled, frontend and backend tests pass, e2e tests are discoverable, and GitHub Actions covers build/test/security. The main gaps are workflow polish rather than blockers: no explicit formatter, no CI lint step, no environment template, and no approved external staleness check for this run.

Next step: address the lightweight Category A fixes above, then proceed to agent onboarding.
