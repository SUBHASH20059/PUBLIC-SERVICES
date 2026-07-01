package org.psei.auth

import kotlin.test.*

class AuthServiceTest {

    private lateinit var service: AuthService

    @BeforeTest
    fun setup() {
        // Use a fresh in-memory store for each test
        service = AuthService(InMemoryUserRepository.also {
            // InMemoryUserRepository is an object; tests share state.
            // In production replace with a proper test double.
        })
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    fun `register succeeds with valid input`() {
        val result = service.register(
            RegisterRequest("alice@test.com", "Secret123", "Alice")
        )
        assertIs<AuthService.RegisterResult.Success>(result)
        assertEquals("alice@test.com", (result as AuthService.RegisterResult.Success).user.email)
        assertEquals(Role.CITIZEN, result.user.role)
    }

    @Test
    fun `register fails with short password`() {
        val result = service.register(
            RegisterRequest("bob@test.com", "short", "Bob")
        )
        assertIs<AuthService.RegisterResult.Error>(result)
        assertTrue((result as AuthService.RegisterResult.Error).message.contains("8 characters"))
    }

    @Test
    fun `register fails with invalid email`() {
        val result = service.register(
            RegisterRequest("not-an-email", "ValidPass1", "Charlie")
        )
        assertIs<AuthService.RegisterResult.Error>(result)
    }

    @Test
    fun `register fails when email already exists`() {
        service.register(RegisterRequest("dave@test.com", "ValidPass1", "Dave"))
        val result = service.register(RegisterRequest("dave@test.com", "AnotherPass1", "Dave2"))
        assertIs<AuthService.RegisterResult.Error>(result)
        assertTrue((result as AuthService.RegisterResult.Error).message.contains("already exists"))
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    fun `login succeeds with correct credentials`() {
        service.register(RegisterRequest("eve@test.com", "EvePass123", "Eve"))
        val result = service.login(LoginRequest("eve@test.com", "EvePass123"))
        assertIs<AuthService.LoginResult.Success>(result)
    }

    @Test
    fun `login fails with wrong password`() {
        service.register(RegisterRequest("frank@test.com", "FrankPass1", "Frank"))
        val result = service.login(LoginRequest("frank@test.com", "WrongPassword"))
        assertIs<AuthService.LoginResult.Error>(result)
    }

    @Test
    fun `login fails for unknown email`() {
        val result = service.login(LoginRequest("ghost@test.com", "AnyPass123"))
        assertIs<AuthService.LoginResult.Error>(result)
    }

    // ── Role promotion ────────────────────────────────────────────────────────

    @Test
    fun `admin can promote citizen to officer`() {
        val reg = service.register(RegisterRequest("target@test.com", "Target123", "Target"))
        val user = (reg as AuthService.RegisterResult.Success).user

        val adminPrincipal = UserPrincipal("admin-id", "admin@psei.gov.in", Role.ADMIN, null)
        val result = service.promoteRole(user.id, Role.OFFICER, adminPrincipal)
        assertIs<AuthService.RoleUpdateResult.Success>(result)
        assertEquals(Role.OFFICER, (result as AuthService.RoleUpdateResult.Success).user.role)
    }

    @Test
    fun `citizen cannot promote roles`() {
        val citizenPrincipal = UserPrincipal("c-id", "citizen@test.com", Role.CITIZEN, null)
        val result = service.promoteRole("any-id", Role.OFFICER, citizenPrincipal)
        assertIs<AuthService.RoleUpdateResult.Error>(result)
    }

    @Test
    fun `only super admin can assign super admin role`() {
        val adminPrincipal = UserPrincipal("a-id", "admin@psei.gov.in", Role.ADMIN, null)
        val result = service.promoteRole("any-id", Role.SUPER_ADMIN, adminPrincipal)
        assertIs<AuthService.RoleUpdateResult.Error>(result)
    }
}
