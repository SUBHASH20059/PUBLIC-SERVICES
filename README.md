# PSEI Authority

This repository adds a skeleton for the PSEI Authority services: divisions, Kotlin data models, a lightweight Ktor REST scaffold, a Postgres schema (Flyway migration), and an OpenAPI spec.

Structure

- psei-service/
  - src/main/kotlin/org/psei/
    - Application.kt  — Ktor entrypoint
    - models/         — domain data classes (Marriage, Birth, Property, Company, Complaint)
    - routes/         — example endpoints per division
  - resources/
    - application.conf
    - db/migration/V1__init.sql — Flyway migration creating core tables

- docs/openapi.yaml — OpenAPI v3 spec for the skeleton

What I added

- Basic Ktor scaffold with endpoints for each division (GET/POST placeholders).
- Kotlin data classes representing the main entities.
- Flyway SQL migration to create initial tables.
- OpenAPI spec with minimal path definitions.

How to run

1. Install JDK 17+ and Gradle.
2. From psei-service directory: `./gradlew run` (or import into IntelliJ).
3. Ensure Postgres is running and configured in application.conf before enabling DB features.

Next steps I can take

- Wire endpoints to DB (Exposed DAOs), add tests, and document environment variables.
- Open a Pull Request from branch `psei-authority/skeleton` to merge these changes.

