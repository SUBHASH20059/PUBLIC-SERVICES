package org.psei.validation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for the validation framework.
 * Tests all primitive validators, composite schemas, and PSEI-specific schemas.
 */

class ValidationTest {

    // ─── String Validator Tests ──────────────────────────────────────────────

    @Test
    fun `nonBlank validator rejects empty strings`() {
        val rule = StringRule.NonBlank("Must not be blank")
        assertFalse(rule.validate("").isSuccess)
        assertFalse(rule.validate("   ").isSuccess)
    }

    @Test
    fun `nonBlank validator accepts non-empty strings`() {
        val rule = StringRule.NonBlank("Must not be blank")
        assertTrue(rule.validate("hello").isSuccess)
        assertTrue(rule.validate("a").isSuccess)
    }

    @Test
    fun `minLength validator rejects short strings`() {
        val rule = StringRule.MinLength(3, "Too short")
        assertFalse(rule.validate("ab").isSuccess)
        assertTrue(rule.validate("abc").isSuccess)
        assertTrue(rule.validate("hello").isSuccess)
    }

    @Test
    fun `maxLength validator rejects long strings`() {
        val rule = StringRule.MaxLength(5, "Too long")
        assertFalse(rule.validate("hello1").isSuccess)
        assertTrue(rule.validate("hello").isSuccess)
        assertTrue(rule.validate("hi").isSuccess)
    }

    @Test
    fun `email validator accepts valid emails`() {
        val rule = StringRule.Email("Invalid email")
        assertTrue(rule.validate("user@example.com").isSuccess)
        assertTrue(rule.validate("test.user@domain.org").isSuccess)
    }

    @Test
    fun `email validator rejects invalid emails`() {
        val rule = StringRule.Email("Invalid email")
        assertFalse(rule.validate("not-an-email").isSuccess)
        assertFalse(rule.validate("@domain.com").isSuccess)
        assertFalse(rule.validate("user@").isSuccess)
    }

    @Test
    fun `phone validator accepts valid phone numbers`() {
        val rule = StringRule.Phone("Invalid phone")
        assertTrue(rule.validate("+919876543210").isSuccess)
        assertTrue(rule.validate("919876543210").isSuccess)
    }

    @Test
    fun `pan validator accepts valid PAN numbers`() {
        val rule = StringRule.Pan("Invalid PAN")
        assertTrue(rule.validate("ABCDE1234F").isSuccess)
    }

    @Test
    fun `pan validator rejects invalid PAN numbers`() {
        val rule = StringRule.Pan("Invalid PAN")
        assertFalse(rule.validate("ABCD1234F").isSuccess)
        assertFalse(rule.validate("abcde1234f").isSuccess)
    }

    @Test
    fun `aadhar validator accepts valid Aadhar numbers`() {
        val rule = StringRule.Aadhar("Invalid Aadhar")
        assertTrue(rule.validate("234567890123").isSuccess)
    }

    @Test
    fun `aadhar validator rejects invalid Aadhar numbers`() {
        val rule = StringRule.Aadhar("Invalid Aadhar")
        assertFalse(rule.validate("123456789012").isSuccess)
        assertFalse(rule.validate("12345678901").isSuccess)
    }

    // ─── Number Validator Tests ──────────────────────────────────────────────

    @Test
    fun `positive validator rejects zero and negative`() {
        val rule = NumberRule.Positive("Must be positive")
        assertFalse(rule.validate(0).isSuccess)
        assertFalse(rule.validate(-1).isSuccess)
        assertTrue(rule.validate(1).isSuccess)
    }

    @Test
    fun `range validator checks bounds correctly`() {
        val rule = NumberRule.Range(10, 100, "Out of range")
        assertFalse(rule.validate(9).isSuccess)
        assertFalse(rule.validate(101).isSuccess)
        assertTrue(rule.validate(10).isSuccess)
        assertTrue(rule.validate(100).isSuccess)
        assertTrue(rule.validate(50).isSuccess)
    }

    // ─── Schema Validator Tests ──────────────────────────────────────────────

    @Test
    fun `schema validator returns success for valid data`() {
        val validator = V.schema("test") {
            addStringField("name", V.string.nonBlank(), V.string.minLength(2))
        }

        val result = validator.validateMap(mapOf("name" to "Alice"))
        assertTrue(result.valid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `schema validator returns errors for invalid data`() {
        val validator = V.schema("test") {
            addStringField("name", V.string.nonBlank())
            addStringField("email", V.string.email())
        }

        val result = validator.validateMap(mapOf(
            "name" to "",
            "email" to "invalid"
        ))
        assertFalse(result.valid)
        assertEquals(2, result.errors.size)
    }

    @Test
    fun `schema validator detects missing required fields`() {
        val validator = V.schema("test") {
            addStringField("required", V.string.nonBlank("Field is required"))
        }

        val result = validator.validateMap(mapOf())
        assertFalse(result.valid)
        assertEquals("REQUIRED", result.errors.first().code)
    }

    // ─── PSEI Validation Schema Tests ────────────────────────────────────────

    @Test
    fun `registration schema validates valid registration data`() {
        val result = PseiValidation.registrationSchema.validateMap(mapOf(
            "email" to "user@example.com",
            "password" to "securePassword123",
            "fullName" to "John Doe"
        ))
        assertTrue(result.valid)
    }

    @Test
    fun `registration schema rejects invalid email`() {
        val result = PseiValidation.registrationSchema.validateMap(mapOf(
            "email" to "not-an-email",
            "password" to "securePassword123",
            "fullName" to "John Doe"
        ))
        assertFalse(result.valid)
    }

    @Test
    fun `registration schema rejects short password`() {
        val result = PseiValidation.registrationSchema.validateMap(mapOf(
            "email" to "user@example.com",
            "password" to "short",
            "fullName" to "John Doe"
        ))
        assertFalse(result.valid)
    }

    @Test
    fun `business schema validates valid business data`() {
        val result = PseiValidation.businessSchema.validateMap(mapOf(
            "businessName" to "Tech Solutions Pvt Ltd",
            "businessType" to "PRIVATE_LIMITED",
            "sector" to "Technology",
            "address" to "123 Main St",
            "city" to "Bangalore",
            "state" to "Karnataka",
            "pinCode" to "560001",
            "phone" to "+919876543210",
            "panNumber" to "ABCDE1234F"
        ))
        assertTrue(result.valid)
    }

    @Test
    fun `business schema rejects invalid PAN`() {
        val result = PseiValidation.businessSchema.validateMap(mapOf(
            "businessName" to "Tech Solutions",
            "businessType" to "PRIVATE_LIMITED",
            "sector" to "Technology",
            "address" to "123 Main St",
            "city" to "Bangalore",
            "state" to "Karnataka",
            "pinCode" to "560001",
            "phone" to "+919876543210",
            "panNumber" to "INVALID"
        ))
        assertFalse(result.valid)
    }

    @Test
    fun `idea schema validates valid idea data`() {
        val result = PseiValidation.ideaSchema.validateMap(mapOf(
            "title" to "My Great Idea",
            "description" to "This is a detailed description of my innovative idea",
            "domain" to "Technology"
        ))
        assertTrue(result.valid)
    }

    @Test
    fun `patent schema rejects short abstract`() {
        val result = PseiValidation.patentSchema.validateMap(mapOf(
            "title" to "Invention",
            "abstract" to "too short",
            "claims" to "Claim 1"
        ))
        assertFalse(result.valid)
    }
}
