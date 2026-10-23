# Tatkal Ticket Reservation System

A Java/Spring Boot ticket reservation learning project. It uses PostgreSQL, Redis, and Apache Kafka to demonstrate booking persistence, availability caching, and transaction-triggered events.

The project is a development example, not a production-ready booking service. See the [project setup, API, tests, and limitations](demo/README.md).

## Quick start

Requirements: Java 24, Docker Compose, PostgreSQL, and the environment-based database and HTTP Basic credentials described in the [setup guide](demo/README.md).

From `demo/`, start Kafka with `docker compose up -d --wait`, start PostgreSQL and Redis, provide the required environment variables, then run `./mvnw spring-boot:run` (PowerShell: `./mvnw.cmd spring-boot:run`).

Run the isolated automated tests from `demo/` with `./mvnw test` (PowerShell: `./mvnw.cmd test`).
