package org.psei.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date

/**
 * Centralised JWT configuration.
 *
 * In production, pull these values from environment variables or a secrets manager:
 *   JWT_SECRET, JWT_ISSUER, JWT_AUDIENCE, JWT_EXPIRY_MS
 */
object JwtConfig {
    val secret: String   = System.getenv("JWT_SECRET")   ?: "psei-dev-secret-change-in-prod"
    val issuer: String   = System.getenv("JWT_ISSUER")   ?: "psei-authority"
    val audience: String = System.getenv("JWT_AUDIENCE") ?: "psei-clients"
    val expiryMs: Long   = System.getenv("JWT_EXPIRY_MS")?.toLong() ?: (24 * 60 * 60 * 1000L) // 24h

    val algorithm: Algorithm = Algorithm.HMAC256(secret)
    val realm: String = "PSEI Authority API"
}

// ─── Role definitions ────────────────────────────────────────────────────────

/**
 * All roles recognised by the platform.
 *
 * CITIZEN     – authenticated end-user (read own data, submit ideas/patents)
 * OFFICER     – government officer (read all records in their division)
 * ADMIN       – full read/write across all divisions
 * SUPER_ADMIN – platform management (user/role administration)
 */
enum class Role {
    CITIZEN,
    OFFICER,
    ADMIN,
    SUPER_ADMIN;

    /** Returns true when this role's authority is ≥ [required]. */
    fun atLeast(required: Role): Boolean =
        this.ordinal >= required.ordinal
}

// ─── Claim names ─────────────────────────────────────────────────────────────

object Claims {
    const val USER_ID  = "userId"
    const val EMAIL    = "email"
    const val ROLE     = "role"
    const val DIVISION = "division" // optional: which government division the officer belongs to
}

// ─── Token service ───────────────────────────────────────────────────────────

data class TokenPair(val accessToken: String, val refreshToken: String)

object TokenService {

    /**
     * Generate a signed access token containing userId, email, and role.
     */
    fun generateAccessToken(userId: String, email: String, role: Role, division: String? = null): String {
        return JWT.create()
            .withIssuer(JwtConfig.issuer)
            .withAudience(JwtConfig.audience)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + JwtConfig.expiryMs))
            .withClaim(Claims.USER_ID, userId)
            .withClaim(Claims.EMAIL, email)
            .withClaim(Claims.ROLE, role.name)
            .apply { if (division != null) withClaim(Claims.DIVISION, division) }
            .sign(JwtConfig.algorithm)
    }

    /**
     * Generate a long-lived refresh token (7 days).
     * Store its hash server-side to enable revocation.
     */
    fun generateRefreshToken(userId: String): String {
        return JWT.create()
            .withIssuer(JwtConfig.issuer)
            .withAudience(JwtConfig.audience)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L))
            .withClaim(Claims.USER_ID, userId)
            .withClaim("type", "refresh")
            .sign(JwtConfig.algorithm)
    }

    /** Convenience: issue both tokens at once. */
    fun issueTokenPair(userId: String, email: String, role: Role, division: String? = null): TokenPair =
        TokenPair(
            accessToken  = generateAccessToken(userId, email, role, division),
            refreshToken = generateRefreshToken(userId)
        )
}

// ─── Principal ───────────────────────────────────────────────────────────────

/**
 * The authenticated caller attached to every verified request.
 * Accessible via  call.principal<UserPrincipal>()  inside protected routes.
 */
data class UserPrincipal(
    val userId: String,
    val email: String,
    val role: Role,
    val division: String?
) : io.ktor.server.auth.Principal

fun DecodedJWT.toUserPrincipal(): UserPrincipal? {
    val userId   = getClaim(Claims.USER_ID).asString()  ?: return null
    val email    = getClaim(Claims.EMAIL).asString()    ?: return null
    val roleName = getClaim(Claims.ROLE).asString()     ?: return null
    val role     = runCatching { Role.valueOf(roleName) }.getOrNull() ?: return null
    val division = getClaim(Claims.DIVISION).asString() // nullable
    return UserPrincipal(userId, email, role, division)
}
