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

Start PostgreSQL together with pgAdmin:

```bash
docker compose up -d postgres pgadmin
```

pgAdmin is available at `http://localhost:5050`:

- login: `admin@shooters.local`
- password: `shooters`
- preconfigured server: `Shooters Platform (local)`

Run the Spring Boot application:

```bash
cd backend
./gradlew bootRun
```

The backend exposes readiness at `GET http://localhost:8080/actuator/health`.

### Granting the organizer role

Normal registration grants only the `USER` role. To grant an existing account the additional `ORGANIZER` role, run:

```sql
insert into user_account_roles (user_account_id, role_name)
select id, 'ORGANIZER'
from user_accounts
where email = 'organizer@example.com'
on conflict do nothing;
```

Replace the example email with the normalized email address of the target account. The command is idempotent.

To create a local organizer account, start PostgreSQL and the backend, then run:

```powershell
.\scripts\create-organizer.ps1 `
  -Email organizer@example.com `
  -Username Organizer `
  -Password 'correct horse battery'
```

The script registers a missing account through the application API so the password is validated and hashed normally.
It then idempotently grants both `USER` and `ORGANIZER`. For an existing email, credentials remain unchanged and only
the roles are ensured.

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
