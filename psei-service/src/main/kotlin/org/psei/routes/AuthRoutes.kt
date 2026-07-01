package org.psei.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.psei.auth.*

private val authService = AuthService()

/**
 * Registers all /auth/* endpoints.
 *
 * Public endpoints  (no token needed):
 *   POST /auth/register   – create a new CITIZEN account
 *   POST /auth/login      – exchange credentials for tokens
 *   POST /auth/refresh    – exchange a refresh token for a new access token
 *
 * Protected endpoints  (Bearer token required):
 *   GET  /auth/me                            – return the caller's profile
 *   PUT  /auth/roles/{userId}                – promote a user's role (ADMIN+)
 */
fun Routing.registerAuthRoutes() {
    route("/auth") {

        // ── POST /auth/register ───────────────────────────────────────────────
        post("/register") {
            val req = runCatching { call.receive<RegisterRequest>() }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body"))
                return@post
            }

            when (val result = authService.register(req)) {
                is AuthService.RegisterResult.Success -> {
                    call.respond(
                        HttpStatusCode.Created,
                        AuthResponse(
                            accessToken  = result.tokens.accessToken,
                            refreshToken = result.tokens.refreshToken,
                            userId       = result.user.id,
                            email        = result.user.email,
                            role         = result.user.role.name
                        )
                    )
                }
                is AuthService.RegisterResult.Error ->
                    call.respond(HttpStatusCode.UnprocessableEntity, mapOf("error" to result.message))
            }
        }

        // ── POST /auth/login ──────────────────────────────────────────────────
        post("/login") {
            val req = runCatching { call.receive<LoginRequest>() }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body"))
                return@post
            }

            when (val result = authService.login(req)) {
                is AuthService.LoginResult.Success -> {
                    call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(
                            accessToken  = result.tokens.accessToken,
                            refreshToken = result.tokens.refreshToken,
                            userId       = result.user.id,
                            email        = result.user.email,
                            role         = result.user.role.name
                        )
                    )
                }
                is AuthService.LoginResult.Error ->
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to result.message))
            }
        }

        // ── POST /auth/refresh ────────────────────────────────────────────────
        post("/refresh") {
            val req = runCatching { call.receive<RefreshRequest>() }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body"))
                return@post
            }

            when (val result = authService.refresh(req.refreshToken)) {
                is AuthService.RefreshResult.Success ->
                    call.respond(HttpStatusCode.OK, mapOf("accessToken" to result.accessToken))
                is AuthService.RefreshResult.Error ->
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to result.message))
            }
        }

        // ── Protected routes ──────────────────────────────────────────────────
        authenticate("jwt") {

            // GET /auth/me
            get("/me") {
                val user = call.requireRole(Role.CITIZEN) ?: return@get
                call.respond(
                    HttpStatusCode.OK,
                    MeResponse(
                        userId   = user.userId,
                        email    = user.email,
                        fullName = "(from DB)",   // TODO: load full name from UserRepository
                        role     = user.role.name,
                        division = user.division
                    )
                )
            }

            // PUT /auth/roles/{userId}  – promote a user's role (ADMIN+ only)
            put("/roles/{userId}") {
                val caller = call.requireRole(Role.ADMIN) ?: return@put

                val targetUserId = call.parameters["userId"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing userId"))

                val body = runCatching { call.receive<RoleUpdateRequest>() }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body"))
                    return@put
                }

                val newRole = runCatching { Role.valueOf(body.role) }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Unknown role: ${body.role}"))
                    return@put
                }

                when (val result = authService.promoteRole(targetUserId, newRole, caller)) {
                    is AuthService.RoleUpdateResult.Success ->
                        call.respond(HttpStatusCode.OK, mapOf(
                            "userId"  to result.user.id,
                            "newRole" to result.user.role.name
                        ))
                    is AuthService.RoleUpdateResult.Error ->
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to result.message))
                }
            }
        }
    }
}

@Serializable
private data class RoleUpdateRequest(val role: String)
