package org.psei

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.psei.auth.configureAuth
import org.psei.routes.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {

    // ── Serialisation ─────────────────────────────────────────────────────────
    install(ContentNegotiation) { json() }

    // ── JWT Authentication ────────────────────────────────────────────────────
    configureAuth()

    // ── Global error handling ─────────────────────────────────────────────────
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "An unexpected error occurred", "message" to (cause.message ?: ""))
            )
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Route not found"))
        }
        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Authentication required"))
        }
        status(HttpStatusCode.Forbidden) { call, _ ->
            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
        }
    }

    // ── Routes ────────────────────────────────────────────────────────────────
    routing {
        // Health check
        get("/health") {
            call.respond(mapOf(
                "status" to "ok",
                "service" to "PSEI Authority - National Digital Trust Platform",
                "version" to "2.0.0",
                "timestamp" to java.time.LocalDateTime.now().toString()
            ))
        }

        // API Documentation
        get("/api-docs") {
            call.respond(mapOf(
                "title" to "PSEI Authority API",
                "version" to "2.0.0",
                "description" to """
                    Comprehensive national platform for:
                    1. Idea Protection & IP Management
                    2. Patent Assistance
                    3. Government Schemes & Subsidies
                    4. Business Registration
                    5. Mentor & Investor Network
                    6. Student Innovation Hub
                    7. Secure Citizen Document Vault with Government Certificates
                """.trimIndent(),
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
                    "audit" to "/citizen-vault/audit-logs"
                )
            ))
        }

        // ════════════════════════════════════════════════════════════════════
        // CORE PSEI FEATURES
        // ════════════════════════════════════════════════════════════════════

        registerAuthRoutes()                     // Authentication & Role Management
        registerPSEIRoutes()                     // Ideas, Patents, Schemes, Business
        registerDocumentAndCertificateRoutes()   // Secure Vault & Government Certs
        registerIDApplicationAndPropertyRoutes() // ID Applications, Property Laws
        registerAdvancedRoutes()                 // ML, ZKP, WhatsApp Bot, CRDT, OpenAPI

        // ════════════════════════════════════════════════════════════════════
        // ADMINISTRATIVE ENDPOINTS
        // ════════════════════════════════════════════════════════════════════

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
    }
}
