# Tatkal Ticket Reservation System

A Java 24 and Spring Boot REST API demonstrating ticket reservations, PostgreSQL persistence, Redis availability caching, and booking events with Apache Kafka. This repository is a learning project; it is not a production-ready reservation service.

## Features

- Create and cancel bookings.
- Check cached seat availability and suggest an available seat.
- Publish booking events after the database transaction commits.
- Use a row lock when checking a requested seat during booking.
- Require HTTP Basic authentication for API endpoints.

## Requirements

- Java 24
- Docker Desktop or Docker Engine with Compose
- PostgreSQL with a database named `flashy`
- Maven Wrapper (included)

The application connects to Redis on `localhost:6379` and Kafka on `localhost:9092` when run directly on the host. PostgreSQL, Redis, and Kafka must be running before startup.

## Configuration

Provide these environment variables through your shell or a secret manager. Do not commit local values:

- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_SECURITY_USERNAME`
- `APP_SECURITY_PASSWORD`

`SPRING_DATASOURCE_URL` defaults to `jdbc:postgresql://localhost:5432/flashy`. Hibernate defaults to schema validation; use `SPRING_JPA_HIBERNATE_DDL_AUTO=update` only for a disposable development database. This project does not yet include database migrations.

Demo seed data and demo load-test endpoints are disabled by default. Set `APP_DEMO_DATA_ENABLED=true` only for a development database.

## Start dependencies

From this directory, start the single-node Kafka broker:

```powershell
docker compose up -d --wait
```

The Compose file starts Kafka only. Start PostgreSQL and Redis separately, or provide reachable services. Kafka advertises `localhost:9092` for host applications and `host.docker.internal:29092` for an app container on Docker Desktop.

## Run on the host

Set the required environment variables, then run:

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens on port `8080`. Authenticated requests use HTTP Basic credentials configured through `APP_SECURITY_USERNAME` and `APP_SECURITY_PASSWORD`.

## Build and run with Docker

Build the JAR and image:

```powershell
.\mvnw.cmd -DskipTests package
docker build -t tatkal-booking:1.0 .
```

Run the app container after setting the required environment variables on the host:

```powershell
docker run -d --name tatkal-app -p 8081:8080 `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/flashy `
  -e SPRING_DATASOURCE_USERNAME `
  -e SPRING_DATASOURCE_PASSWORD `
  -e APP_SECURITY_USERNAME `
  -e APP_SECURITY_PASSWORD `
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:29092 `
  -e SPRING_DATA_REDIS_HOST=host.docker.internal `
  tatkal-booking:1.0
```

The container listens on host port `8081`. These instructions use the host's PostgreSQL and Redis ports; they do not create or migrate a database.

## API

All endpoints require HTTP Basic authentication. Replace sample IDs with records in your database.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/bookings` | List bookings |
| POST | `/api/bookings` | Create a booking |
| POST | `/api/bookings/available-seat` | Suggest a seat; it does not reserve it |
| GET | `/api/bookings/availability?journeyId=1&seatId=11` | Check availability |
| PUT | `/api/bookings/{id}/cancel` | Cancel a booking |
| GET | `/api/users` | List users |
| POST | `/api/users` | Create a user |

Example booking body:

```json
{
  "userId": 1,
  "journeyId": 1,
  "seatId": 11
}
```

## Tests

Run the suite with:

```powershell
.\mvnw.cmd test
```

Tests use an in-memory H2 database and mocks for Kafka and Redis; they do not need or write to shared services. The suite covers booking creation, a concurrent duplicate-booking attempt, seat suggestion, validation/error responses, persistence, and authenticated access. Cancellation and PostgreSQL-specific concurrency still need dedicated tests.

## Known limitations

- Kafka uses a single plaintext broker for local development.
- Booking events are sent asynchronously after commit; a durable outbox/retry mechanism is not implemented, so an event can be lost if Kafka delivery fails after the booking commits.
- There is no database migration tool or database-level partial unique constraint for active bookings.
- HTTP Basic is a simple development access control, not a full user identity or token system.
- Large list endpoints return entity graphs and should be paginated before handling substantial data volumes.
