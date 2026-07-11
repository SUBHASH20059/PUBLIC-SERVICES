package org.psei.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Central routing configuration.
 *
 * This replaces the inline `routing { }` block that was accidentally removed.
 * All route modules are registered here in order:
 *   1. Health check & API docs (public, no auth)
 *   2. Administrative endpoints
 *   3. Authentication
 *   4. Division routes (Civil Registry, Real Estate, Business, Legal)
 *   5. PSEI core features (Ideas, Patents, Schemes, Business, Network, Student)
 *   6. Document & Certificate routes (Citizen Vault)
 *   7. ID Application & Property routes
 *   8. Advanced routes (ML Pipeline, ZKP, WhatsApp Bot, CRDT, OpenAPI)
 */
fun Application.configureRoutes() {
    routing {

        // ── Health Check ────────────────────────────────────────────────────────
        get("/health") {
            call.respond(mapOf(
                "status" to "ok",
                "service" to "PSEI Authority - National Digital Trust Platform",
                "version" to "3.0.0",
                "timestamp" to java.time.LocalDateTime.now().toString()
            ))
        }

        // ── API Documentation Endpoint ──────────────────────────────────────────
        get("/api-docs") {
            call.respond(mapOf(
                "title" to "PSEI Authority API",
                "version" to "3.0.0",
                "description" to """
                    Comprehensive national platform for:
                    1. Idea Protection & IP Management
                    2. Patent Assistance
                    3. Government Schemes & Subsidies
                    4. Business Registration
                    5. Mentor & Investor Network
                    6. Student Innovation Hub
                    7. Secure Citizen Document Vault with Government Certificates
                    8. Digital Identity & Property Laws
                    9. Machine Learning Pipeline & OpenAPI Schema
                """.trimIndent(),
                "openapi" to "/openapi/schema.json",
                "endpoints" to mapOf(
                    "authentication" to "/auth/**",
                    "ideas" to "/psei/ideas",
                    "patents" to "/psei/patents",
                    "schemes" to "/psei/schemes",
                    "business" to "/psei/business",
                    "network" to "/psei/network",
                    "student" to "/psei/student",
                    "documents" to "/citizen-vault/identity-documents",
                    "certificates" to "/citizen-vault/certificates",
                    "audit" to "/citizen-vault/audit-logs",
                    "id-applications" to "/id-applications/**",
                    "property-laws" to "/property-laws/**",
                    "advanced" to "/advanced/**",
                    "admin" to "/admin/**"
                )
            ))
        }

        // ── Administrative Endpoints ────────────────────────────────────────────
        route("/admin") {
            get("/system-status") {
                call.respond(mapOf(
                    "status" to "operational",
                    "uptime" to "running",
                    "database" to "postgresql",
                    "encryption" to "AES-256-GCM",
                    "lastMaintenance" to java.time.LocalDateTime.now().toString()
                ))
            }
        }

        // ── Register All Route Modules ──────────────────────────────────────────
        registerAuthRoutes()
        registerDivisionRoutes()
        registerPSEIRoutes()
        registerDocumentAndCertificateRoutes()
        registerIDApplicationAndPropertyRoutes()
        registerAdvancedRoutes()
    }
}
