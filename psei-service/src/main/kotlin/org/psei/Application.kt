package org.psei

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.*
import org.psei.auth.configureAuth
import org.psei.config.AppConfig
import org.psei.database.startDatabase
import org.psei.database.stopDatabase
import org.psei.docs.configureSwagger
import org.psei.jobs.startJobScheduler
import org.psei.monitoring.configureMonitoring
import org.psei.monitoring.shutdownMonitoring
import org.psei.routes.configureRoutes
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = AppConfig.port, host = AppConfig.host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // ── Security Headers (Helmet-equivalent) ──────────────────────────────────
    install(DefaultHeaders) {
        header("X-Content-Type-Options", "nosniff")
        header("X-Frame-Options", "DENY")
        header("X-XSS-Protection", "1; mode=block")
        header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
        header("Referrer-Policy", "strict-origin-when-cross-origin")
        header("Content-Security-Policy", "default-src 'self'")
        header("X-Permitted-Cross-Domain-Policies", "none")
    }

    // ── CORS (Cross-Origin Resource Sharing) ──────────────────────────────────
    install(CORS) {
        AppConfig.allowedOrigins.forEach { allowHost(it) }
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.XForwardedFor)
    }

    // ── Structured Logging (Pino/Winston equivalent) ──────────────────────────
    install(CallLogging) {
        level = Level.INFO
        mdc("requestId") { "${System.currentTimeMillis()}-${java.util.UUID.randomUUID().toString().take(8)}" }
    }

    // ── Rate Limiting ─────────────────────────────────────────────────────────
    install(RateLimit) {
        global {
            rateLimiter(limit = AppConfig.rateLimitRequests, refillPeriod = AppConfig.rateLimitPeriodSeconds.seconds)
        }
    }

    // ── Content Negotiation ───────────────────────────────────────────────────
    install(ContentNegotiation) {
        json()
    }

    // ── Status Pages (Error Handling) ─────────────────────────────────────────
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            org.slf4j.LoggerFactory.getLogger("ErrorHandling")
                .error("Unhandled exception: ${cause.message}", cause)
            call.respond(BadGateway, mapOf(
                "error" to "Internal server error",
                "timestamp" to java.time.LocalDateTime.now().toString()
            ))
        }
        status(HttpStatusCode.NotFound) { call, statusCode ->
            call.respond(statusCode, mapOf(
                "error" to "Resource not found",
                "path" to call.request.path()
            ))
        }
        status(HttpStatusCode.MethodNotAllowed) { call, statusCode ->
            call.respond(statusCode, mapOf(
                "error" to "Method not allowed",
                "path" to call.request.path()
            ))
        }
    }

    // ── Swagger/OpenAPI Documentation ─────────────────────────────────────────
    configureSwagger()

    // ── Monitoring (Sentry) ───────────────────────────────────────────────────
    configureMonitoring()

    // ── Authentication ────────────────────────────────────────────────────────
    configureAuth()

    // ── Routes ────────────────────────────────────────────────────────────────
    configureRoutes()

    // ── Start Background Jobs ─────────────────────────────────────────────────
    startJobScheduler()

    // ── Initialize Database (non-blocking) ────────────────────────────────────
    runCatching { startDatabase() }.onFailure {
        org.slf4j.LoggerFactory.getLogger("Application")
            .warn("Database initialization failed. Running with in-memory data. Error: ${it.message}")
    }

    // ── Startup Log ───────────────────────────────────────────────────────────
    val logger = org.slf4j.LoggerFactory.getLogger("Application")
    logger.info("PSEI Authority v${AppConfig.appVersion} starting on ${AppConfig.host}:${AppConfig.port}")
    logger.info("Environment: ${AppConfig.appEnv}")
    logger.info("Storage Provider: ${AppConfig.storageProvider}")
    logger.info("Jobs Enabled: ${AppConfig.jobsEnabled}")
    logger.info("Swagger UI: http://${AppConfig.host}:${AppConfig.port}/swagger")
    logger.info("OpenAPI Schema: http://${AppConfig.host}:${AppConfig.port}/openapi/schema.json")

    // ── Shutdown Hook ─────────────────────────────────────────────────────────
    environment.monitor.subscribe(ApplicationStopping) {
        logger.info("Application shutting down...")
        stopJobScheduler()
        stopDatabase()
        shutdownMonitoring()
        logger.info("Application stopped")
    }
}
