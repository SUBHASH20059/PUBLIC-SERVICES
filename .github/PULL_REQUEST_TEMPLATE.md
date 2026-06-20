---
name: "Add PSEI Authority skeleton"
about: "Adds Ktor scaffold, Kotlin models, Flyway migration, and OpenAPI spec for PSEI Authority"

---

This PR adds a skeleton implementation for the PSEI Authority services. Contents:

- Ktor-based service scaffold at psei-service/
- Kotlin domain models (Marriage, Birth, Property, Company, Complaint)
- Flyway V1 migration to create core tables (marriage, birth, property, company, complaint, tax_record)
- Minimal OpenAPI v3 spec in docs/openapi.yaml
- README with run instructions and next steps

What is intentionally omitted:
- Database wiring (Exposed DAOs) and Flyway runtime integration
- Tests and CI workflows

How to test locally:
1. Configure database settings in psei-service/src/main/resources/application.conf
2. cd psei-service && ./gradlew run
3. Use curl or Postman to call GET /civil/marriages and POST /civil/marriages

