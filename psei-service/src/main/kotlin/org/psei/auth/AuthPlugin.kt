package org.psei.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// ─── Install JWT plugin ───────────────────────────────────────────────────────

/**
 * Call this inside Application.module() to activate JWT authentication.
 *
 * Usage in routes:
 *   authenticate("jwt") { ... }
 */
fun Application.configureAuth() {
    install(Authentication) {
        jwt("jwt") {
            realm     = JwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(JwtConfig.secret))
                    .withIssuer(JwtConfig.issuer)
                    .withAudience(JwtConfig.audience)
                    .build()
            )
            validate { credential ->
                // Map the verified JWT into our UserPrincipal
                credential.payload.toUserPrincipal()
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Token is missing or invalid")
                )
            }
        }
    }
}

// ─── Role-guard helpers ───────────────────────────────────────────────────────

/**
 * Require the caller to have AT LEAST [minimumRole].
 * Returns the principal on success; responds 403 and returns null on failure.
 *
 * Example:
 *   post("/ideas") {
 *       val user = call.requireRole(Role.CITIZEN) ?: return@post
 *       ...
 *   }
 */
suspend fun ApplicationCall.requireRole(minimumRole: Role): UserPrincipal? {
    val principal = principal<UserPrincipal>()
    if (principal == null || !principal.role.atLeast(minimumRole)) {
        respond(
            HttpStatusCode.Forbidden,
            mapOf(
                "error"    to "Insufficient permissions",
                "required" to minimumRole.name,
                "yours"    to (principal?.role?.name ?: "none")
            )
        )
        return null
    }
    return principal
}

/**
 * Require the caller to have EXACTLY one of [allowedRoles].
 *
 * Example:
 *   get("/schemes/admin") {
 *       val user = call.requireAnyRole(Role.OFFICER, Role.ADMIN) ?: return@get
 *       ...
 *   }
 */
suspend fun ApplicationCall.requireAnyRole(vararg allowedRoles: Role): UserPrincipal? {
    val principal = principal<UserPrincipal>()
    if (principal == null || principal.role !in allowedRoles) {
        respond(
            HttpStatusCode.Forbidden,
            mapOf(
                "error"    to "Insufficient permissions",
                "required" to allowedRoles.map { it.name },
                "yours"    to (principal?.role?.name ?: "none")
            )
        )
        return null
    }
    return principal
}

/**
 * Route-level DSL shorthand: wrap a block so it only runs for [minimumRole]+.
 *
 * Example:
 *   get("/patents") {
 *       withRole(Role.CITIZEN) { user ->
 *           call.respond(patentService.listFor(user.userId))
 *       }
 *   }
 */
suspend fun ApplicationCall.withRole(
    minimumRole: Role,
    block: suspend ApplicationCall.(UserPrincipal) -> Unit
) {
    val user = requireRole(minimumRole) ?: return
    block(user)
}
