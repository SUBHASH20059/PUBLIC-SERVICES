package org.psei.docs

import io.ktor.server.application.*
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.dsl.routes.get
import io.github.smiley4.ktorswaggerui.dsl.routes.post
import io.github.smiley4.ktorswaggerui.dsl.routes.put
import io.github.smiley4.ktorswaggerui.dsl.routes.delete
import io.github.smiley4.ktorswaggerui.dsl.routes.route

/**
 * Swagger/OpenAPI documentation configuration.
 *
 * Provides:
 *   - Interactive API documentation at /swagger
 *   - OpenAPI 3.0 specification at /openapi/schema.json
 *   - API Explorer for testing endpoints
 *   - Authentication integration for testing
 *
 * Access:
 *   - Swagger UI: http://localhost:8080/swagger
 *   - OpenAPI JSON: http://localhost:8080/openapi/schema.json
 */

fun Application.configureSwagger() {
    install(SwaggerUI) {
        info {
            title = "PSEI Authority API"
            version = "3.0.0"
            description = """
                Comprehensive National Digital Trust Platform

                ## Core Features
                1. **Idea Protection & IP Management** - Register and protect innovative ideas
                2. **Patent Assistance** - File and track patent applications
                3. **Government Schemes & Subsidies** - Search and apply for government programs
                4. **Business Registration** - Register and manage business entities
                5. **Mentor & Investor Network** - Connect with mentors and investors
                6. **Student Innovation Hub** - Submit and manage student projects
                7. **Secure Citizen Document Vault** - Store and manage government certificates
                8. **Digital Identity** - ID applications and property law management
                9. **Machine Learning Pipeline** - AI-powered scheme matching and fraud detection

                ## Authentication
                All protected endpoints require a Bearer token obtained from /auth/login.

                ## Rate Limiting
                100 requests per 60 seconds per client.

                ## Security
                - JWT-based authentication
                - Role-based access control (CITIZEN, OFFICER, ADMIN, SUPER_ADMIN)
                - XSS and CSRF protection
                - CORS configuration
            """.trimIndent()
        }

        server {
            url = "http://localhost:8080"
            description = "Local Development Server"
        }

        server {
            url = "https://api.psei.gov.in"
            description = "Production Server"
        }

        // Authentication scheme
        security {
            securityScheme("BearerAuth") {
                type = io.github.smiley4.ktorswaggerui.data.SecuritySchemeType.HTTP
                scheme = "bearer"
                bearerFormat = "JWT"
                description = "JWT access token obtained from /auth/login"
            }
            globalSecurityScheme("BearerAuth")
        }

        // JSON format
        json {
            pretty = true
        }

        // Documentation path
        path = "swagger"
        openApiPath = "openapi/schema.json"
    }
}
