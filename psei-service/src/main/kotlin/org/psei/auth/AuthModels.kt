package org.psei.auth

import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt

// ─── Request / response DTOs ──────────────────────────────────────────────────

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val role: String = "CITIZEN",       // clients may only request CITIZEN; admin promotes later
    val division: String? = null        // only relevant for OFFICER role
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String,
    val role: String
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class MeResponse(
    val userId: String,
    val email: String,
    val fullName: String,
    val role: String,
    val division: String?
)

// ─── User entity (swap for Exposed DAO in production) ────────────────────────

data class UserRecord(
    val id: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val role: Role,
    val division: String?
)

// ─── In-memory user store  ───────────────────────────────────────────────────
//
// Replace this with an Exposed DAO + PostgreSQL table once the DB layer is wired.
// The interface below makes it trivial to swap implementations.

interface UserRepository {
    fun findByEmail(email: String): UserRecord?
    fun findById(id: String): UserRecord?
    fun save(user: UserRecord): UserRecord
    fun updateRole(userId: String, newRole: Role): UserRecord?
}

object InMemoryUserRepository : UserRepository {

    private val store = mutableMapOf<String, UserRecord>()

    // Seed a SUPER_ADMIN for first-time setup
    init {
        val id = "user-seed-001"
        store[id] = UserRecord(
            id           = id,
            email        = "admin@psei.gov.in",
            passwordHash = BCrypt.hashpw("Admin@1234", BCrypt.gensalt()),
            fullName     = "PSEI System Admin",
            role         = Role.SUPER_ADMIN,
            division     = null
        )
    }

    override fun findByEmail(email: String): UserRecord? =
        store.values.firstOrNull { it.email.equals(email, ignoreCase = true) }

    override fun findById(id: String): UserRecord? = store[id]

    override fun save(user: UserRecord): UserRecord {
        store[user.id] = user
        return user
    }

    override fun updateRole(userId: String, newRole: Role): UserRecord? {
        val existing = store[userId] ?: return null
        val updated  = existing.copy(role = newRole)
        store[userId] = updated
        return updated
    }
}
