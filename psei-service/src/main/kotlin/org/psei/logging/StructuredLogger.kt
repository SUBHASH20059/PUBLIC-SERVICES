package org.psei.logging

import org.psei.config.AppConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

/**
 * Structured logging utility that wraps SLF4J with MDC (Mapped Diagnostic Context)
 * for request tracing, user identification, and structured log output.
 *
 * Provides:
 *   - Request ID tracking across the entire request lifecycle
 *   - User ID injection when authenticated
 *   - Structured JSON output in production
 *   - Performance timing for slow operations
 *   - Security event logging
 */

object StructuredLogger {

    /**
     * Create a logger for the given class.
     */
    fun logger(name: String): Logger = LoggerFactory.getLogger(name)

    /**
     * Set MDC context for request tracing.
     */
    fun setRequestContext(
        requestId: String,
        method: String,
        path: String,
        userId: String? = null,
        ip: String? = null
    ) {
        MDC.put("requestId", requestId)
        MDC.put("method", method)
        MDC.put("path", path)
        userId?.let { MDC.put("userId", it) }
        ip?.let { MDC.put("ipAddress", it) }
    }

    /**
     * Clear all MDC context.
     */
    fun clearContext() {
        MDC.clear()
    }

    /**
     * Log a security event with full context.
     */
    fun logSecurityEvent(
        event: String,
        details: Map<String, Any?> = emptyMap(),
        severity: String = "INFO"
    ) {
        val logger = logger("SecurityEvents")
        val context = buildString {
            append("event=$event, ")
            append("severity=$severity")
            details.forEach { (k, v) ->
                append(", $k=$v")
            }
        }
        when (severity.uppercase()) {
            "CRITICAL" -> logger.error(context)
            "WARN" -> logger.warn(context)
            else -> logger.info(context)
        }
    }

    /**
     * Log a performance metric.
     */
    fun logPerformance(
        operation: String,
        durationMs: Long,
        userId: String? = null
    ) {
        val logger = logger("Performance")
        val context = buildString {
            append("operation=$operation, ")
            append("duration_ms=$durationMs")
            userId?.let { append(", user_id=$it") }
        }
        when {
            durationMs > 5000 -> logger.warn(context)
            durationMs > 1000 -> logger.info(context)
            else -> logger.debug(context)
        }
    }

    /**
     * Time a block of code and log the result.
     */
    fun <T> timed(operation: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        try {
            return block()
        } finally {
            val duration = System.currentTimeMillis() - start
            logPerformance(operation, duration)
        }
    }

    /**
     * Log API access for audit purposes.
     */
    fun logApiAccess(
        method: String,
        path: String,
        statusCode: Int,
        durationMs: Long,
        userId: String? = null,
        ip: String? = null
    ) {
        val logger = logger("ApiAccess")
        val context = buildString {
            append("method=$method, ")
            append("path=$path, ")
            append("status=$statusCode, ")
            append("duration_ms=$durationMs")
            userId?.let { append(", user_id=$it") }
            ip?.let { append(", ip=$it") }
        }
        logger.info(context)
    }
}
