# QGuard Demo Scheduler

A production-oriented modular-monolith demo scheduler for QGuard. It pairs a Spring Boot API with a React/Vite booking UI, PostgreSQL persistence, Flyway migrations, Mailpit email testing, and ICS invitations.

## Run locally

1. Copy `.env.example` to `.env` and replace `APP_PUBLIC_BASE_URL` when exposing the app.
2. Start dependencies: `docker compose up -d postgres mailpit`.
3. Run API: `cd backend && mvn spring-boot:run` (use `mvn -s ../work/maven-settings-clean.xml test` in this sandbox if Maven cannot use its normal cache).
4. Run UI: `cd frontend && npm install && npm run dev`.

The API is available at `http://localhost:8080`; Swagger UI is `/swagger-ui/index.html`; Mailpit is `http://localhost:8025`.

## Architecture

The backend is organized by responsibility: web DTO/controller, application services, persistence entities/repositories, and infrastructure adapters (mail and calendar). The database owns the final reservation guarantee: active bookings have a non-null `slot_key` with a unique constraint. A cancellation releases that key; a reschedule updates it in one transaction. This makes concurrent attempts for the same fixed 30-minute slot resolve with exactly one successful insert/update.

All persisted schedule timestamps are UTC instants. Working hours are interpreted in the configured business zone, and availability responses are rendered into the caller's requested IANA timezone.

## API

- `GET /api/v1/availability?date=2026-08-26&timezone=Asia/Kolkata`
- `POST /api/v1/bookings`
- `GET /api/v1/bookings/manage/{token}`
- `POST /api/v1/bookings/manage/{token}/reschedule`
- `POST /api/v1/bookings/manage/{token}/cancel`

## Tests

Run `cd backend && mvn test`. Tests include scheduling/timezone unit coverage, endpoint validation, cancellation and a concurrent reservation test (H2-compatible schema; PostgreSQL's unique constraint is also exercised in deployment).

## Deployment

The API `Dockerfile` is production-ready for container platforms. `fly.toml` gives a minimal Fly.io service definition; configure `DATABASE_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_SENDER`, and `APP_PUBLIC_BASE_URL` as platform secrets. Use managed PostgreSQL and a transactional email provider outside development. Maven is deliberately pinned to Flyway 10.20.1 and configured to use Maven Central only; this avoids Flyway 11's parent POM repository list, which includes a GitHub Packages repository unnecessary for PostgreSQL.
