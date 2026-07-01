package org.psei.auth

import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

/**
 * Encapsulates all authentication business logic.
 * Depends only on [UserRepository], making it fully unit-testable.
 */
class AuthService(private val users: UserRepository = InMemoryUserRepository) {

    // ─── Register ────────────────────────────────────────────────────────────

    sealed class RegisterResult {
        data class Success(val tokens: TokenPair, val user: UserRecord) : RegisterResult()
        data class Error(val message: String) : RegisterResult()
    }

    fun register(req: RegisterRequest): RegisterResult {
        // Validate email format
        if (!req.email.contains("@")) return RegisterResult.Error("Invalid email address")

        // Enforce minimum password strength
        if (req.password.length < 8)
            return RegisterResult.Error("Password must be at least 8 characters")

        // Prevent duplicate accounts
        if (users.findByEmail(req.email) != null)
            return RegisterResult.Error("An account with this email already exists")

        // Public registration is always CITIZEN – admins promote via /auth/roles
        val role = Role.CITIZEN

        val user = UserRecord(
            id           = UUID.randomUUID().toString(),
            email        = req.email.lowercase().trim(),
            passwordHash = BCrypt.hashpw(req.password, BCrypt.gensalt()),
            fullName     = req.fullName.trim(),
            role         = role,
            division     = null
        )
        users.save(user)

        val tokens = TokenService.issueTokenPair(user.id, user.email, user.role)
        return RegisterResult.Success(tokens, user)
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    sealed class LoginResult {
        data class Success(val tokens: TokenPair, val user: UserRecord) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    fun login(req: LoginRequest): LoginResult {
        val user = users.findByEmail(req.email)
            ?: return LoginResult.Error("Invalid email or password")

        if (!BCrypt.checkpw(req.password, user.passwordHash))
            return LoginResult.Error("Invalid email or password")

        val tokens = TokenService.issueTokenPair(user.id, user.email, user.role, user.division)
        return LoginResult.Success(tokens, user)
    }

    // ─── Refresh ─────────────────────────────────────────────────────────────

    sealed class RefreshResult {
        data class Success(val accessToken: String) : RefreshResult()
        data class Error(val message: String) : RefreshResult()
    }

    fun refresh(refreshToken: String): RefreshResult {
        return try {
            val verifier = com.auth0.jwt.JWT
                .require(com.auth0.jwt.algorithms.Algorithm.HMAC256(JwtConfig.secret))
                .withIssuer(JwtConfig.issuer)
                .withAudience(JwtConfig.audience)
                .withClaim("type", "refresh")
                .build()

            val decoded = verifier.verify(refreshToken)
            val userId  = decoded.getClaim(Claims.USER_ID).asString()
                ?: return RefreshResult.Error("Malformed refresh token")

            val user = users.findById(userId)
                ?: return RefreshResult.Error("User no longer exists")

            val newAccessToken = TokenService.generateAccessToken(user.id, user.email, user.role, user.division)
            RefreshResult.Success(newAccessToken)
        } catch (e: Exception) {
            RefreshResult.Error("Refresh token is invalid or expired")
        }
    }

    // ─── Role promotion (admin only) ─────────────────────────────────────────

    sealed class RoleUpdateResult {
        data class Success(val user: UserRecord) : RoleUpdateResult()
        data class Error(val message: String) : RoleUpdateResult()
    }

    fun promoteRole(targetUserId: String, newRole: Role, caller: UserPrincipal): RoleUpdateResult {
        // Only SUPER_ADMIN can assign SUPER_ADMIN; ADMIN can assign up to ADMIN
        if (newRole == Role.SUPER_ADMIN && caller.role != Role.SUPER_ADMIN)
            return RoleUpdateResult.Error("Only SUPER_ADMIN can assign SUPER_ADMIN role")

        if (!caller.role.atLeast(Role.ADMIN))
            return RoleUpdateResult.Error("Insufficient permissions to modify roles")

        val updated = users.updateRole(targetUserId, newRole)
            ?: return RoleUpdateResult.Error("User not found")

        return RoleUpdateResult.Success(updated)
    }
}
