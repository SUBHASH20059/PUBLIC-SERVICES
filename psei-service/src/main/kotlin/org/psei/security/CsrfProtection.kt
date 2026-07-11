package org.psei.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.psei.config.AppConfig
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * CSRF (Cross-Site Request Forgery) Protection.
 *
 * Generates and validates CSRF tokens for state-changing requests
 * (POST, PUT, DELETE, PATCH). GET requests are exempt.
 *
 * Tokens are:
 *   - Unique per session
 *   - Time-limited (configurable TTL)
 *   - Cryptographically secure
 */

private val logger = LoggerFactory.getLogger("CsrfProtection")

/**
 * CSRF token store with expiration tracking.
 */
class CsrfTokenStore(private val ttlSeconds: Int = 3600) {
    private val tokens = ConcurrentHashMap<String, CsrfTokenEntry>()

    data class CsrfTokenEntry(val token: String, val createdAt: Long)

    fun generateToken(sessionId: String): String {
        val tokenBytes = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        val token = Base64.getUrlEncoder().encodeToString(tokenBytes)

        // Clean up expired tokens
        cleanup()

        tokens[sessionId] = CsrfTokenEntry(token, System.currentTimeMillis())
        return token
    }

    fun validateToken(sessionId: String, providedToken: String): Boolean {
        val entry = tokens[sessionId] ?: return false

        // Check expiration
        val now = System.currentTimeMillis()
        if (now - entry.createdAt > ttlSeconds * 1000L) {
            tokens.remove(sessionId)
            return false
        }

        // Constant-time comparison to prevent timing attacks
        return constantTimeCompare(entry.token, providedToken)
    }

    fun revokeToken(sessionId: String) {
        tokens.remove(sessionId)
    }

    private fun cleanup() {
        val now = System.currentTimeMillis()
        tokens.entries.removeIf {
            now - it.value.createdAt > ttlSeconds * 1000L
        }
    }

    private fun constantTimeCompare(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}

/**
 * Ktor plugin for CSRF protection.
 */
object CsrfProtection {
    private val tokenStore = CsrfTokenStore(ttlSeconds = 3600)

    /**
     * Generate a CSRF token for a session.
     */
    fun generateToken(sessionId: String): String = tokenStore.generateToken(sessionId)

    /**
     * Validate a CSRF token.
     */
    fun validateToken(sessionId: String, providedToken: String): Boolean =
        tokenStore.validateToken(sessionId, providedToken)

    /**
     * Validate CSRF token from request headers or cookies.
     */
    fun validateRequest(call: ApplicationCall): Boolean {
        // Only validate state-changing methods
        if (call.request.httpMethod in listOf(HttpMethod.Get, HttpMethod.Head, HttpMethod.Options)) {
            return true
        }

        val sessionCookie = call.request.cookies["psei_session"]
        val csrfToken = call.request.header("X-CSRF-Token")
            ?: call.request.cookies["X-CSRF-Token"]
            ?: call.request.header("Csrf-Token")
            ?: call.request.cookies["Csrf-Token"]

        if (sessionCookie == null || csrfToken == null) {
            logger.warn("CSRF token missing for ${call.request.httpMethod} ${call.request.path()}")
            return false
        }

        return tokenStore.validateToken(sessionCookie, csrfToken)
    }

    /**
     * Revokes a CSRF token (e.g., on logout).
     */
    fun revokeToken(sessionId: String) = tokenStore.revokeToken(sessionId)
}
