package org.psei.monitoring

import io.ktor.server.application.*
import io.sentry.Sentry
import io.sentry.SentryOptions
import org.psei.config.AppConfig
import org.slf4j.LoggerFactory

/**
 * Monitoring and observability configuration.
 *
 * Integrates:
 *   - Sentry: Error tracking and performance monitoring
 *   - Structured logging with MDC context
 *   - Health metrics collection
 *
 * In production, configure via environment variables:
 *   SENTRY_DSN=https://your-dsn@sentry.io/project
 */

private val logger = LoggerFactory.getLogger("Monitoring")

fun Application.configureMonitoring() {
    val dsn = AppConfig.sentryDsn

    if (dsn == null || dsn.isBlank()) {
        logger.info("Sentry DSN not configured. Error monitoring disabled.")
        return
    }

    Sentry.init(SentryOptions().apply {
        dsn = dsn
        environment = AppConfig.appEnv
        isDebug = AppConfig.isDevelopment

        // Enable performance monitoring
        tracesSampleRate = if (AppConfig.isProduction) 0.1 else 1.0

        // Capture uncaught exceptions
        isEnableUncaughtExceptionHandler = true

        // Include breadcrumbs
        maxBreadcrumbs = 100

        // Tags for filtering
        setTag("service", "psei-authority")
        setTag("version", AppConfig.appVersion)
    })

    logger.info("Sentry monitoring initialized")
}

/**
 * Shutdown Sentry gracefully.
 */
fun shutdownMonitoring() {
    Sentry.close()
    logger.info("Sentry monitoring shut down")
}
