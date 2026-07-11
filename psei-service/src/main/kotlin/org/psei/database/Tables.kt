package org.psei.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Exposed ORM table definitions for all PSEI domain entities.
 *
 * These tables mirror the in-memory data structures defined in Models.kt
 * and AllModels.kt, providing persistent storage via PostgreSQL.
 *
 * Table Naming Convention:
 *   - Plural lowercase with underscores
 *   - Primary key: id (UUID)
 *   - Timestamps: createdAt, updatedAt
 */

// ─── Authentication & Users ──────────────────────────────────────────────────

object UsersTable : Table("users") {
    val id = uuid("id").primaryKey()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 255)
    val role = varchar("role", 50) // CITIZEN, OFFICER, ADMIN, SUPER_ADMIN
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object SessionsTable : Table("sessions") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id").references(UsersTable.id)
    val token = varchar("token", 500)
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at")
}

// ─── PSEI: Ideas ─────────────────────────────────────────────────────────────

object IdeasTable : Table("ideas") {
    val id = uuid("id").primaryKey()
    val title = varchar("title", 200)
    val description = text("description")
    val domain = varchar("domain", 100)
    val userId = uuid("user_id").references(UsersTable.id)
    val status = varchar("status", 50).default("SUBMITTED") // SUBMITTED, REVIEWING, APPROVED, REJECTED
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── PSEI: Patents ───────────────────────────────────────────────────────────

object PatentsTable : Table("patents") {
    val id = uuid("id").primaryKey()
    val title = varchar("title", 255)
    val abstract = text("abstract")
    val claims = text("claims")
    val applicantId = uuid("applicant_id").references(UsersTable.id)
    val status = varchar("status", 50).default("DRAFT") // DRAFT, FILED, EXAMINING, GRANTED, REJECTED
    val patentNumber = varchar("patent_number", 100).nullable()
    val filingDate = datetime("filing_date").nullable()
    val expiryDate = datetime("expiry_date").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── PSEI: Schemes ───────────────────────────────────────────────────────────

object SchemesTable : Table("schemes") {
    val id = uuid("id").primaryKey()
    val name = varchar("name", 255)
    val description = text("description")
    val category = varchar("category", 100)
    val ministry = varchar("ministry", 255)
    val eligibilityCriteria = text("eligibility_criteria").nullable()
    val applicationUrl = varchar("application_url", 500).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object SchemeApplicationsTable : Table("scheme_applications") {
    val id = uuid("id").primaryKey()
    val schemeId = uuid("scheme_id").references(SchemesTable.id)
    val applicantId = uuid("applicant_id").references(UsersTable.id)
    val status = varchar("status", 50).default("PENDING") // PENDING, APPROVED, REJECTED, IN_PROGRESS
    val submittedData = text("submitted_data").nullable() // JSON
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── PSEI: Business Registration ─────────────────────────────────────────────

object BusinessesTable : Table("businesses") {
    val id = uuid("id").primaryKey()
    val businessName = varchar("business_name", 255)
    val businessType = varchar("business_type", 100)
    val sector = varchar("sector", 100)
    val address = varchar("address", 500)
    val city = varchar("city", 100)
    val state = varchar("state", 100)
    val pinCode = varchar("pin_code", 6)
    val phone = varchar("phone", 20)
    val panNumber = varchar("pan_number", 10)
    val ownerId = uuid("owner_id").references(UsersTable.id)
    val status = varchar("status", 50).default("ACTIVE")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── PSEI: Mentor & Investor Network ─────────────────────────────────────────

object MentorsTable : Table("mentors") {
    val id = uuid("id").primaryKey()
    val name = varchar("name", 255)
    val expertise = varchar("expertise", 255)
    val bio = text("bio")
    val userId = uuid("user_id").references(UsersTable.id).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object InvestorsTable : Table("investors") {
    val id = uuid("id").primaryKey()
    val name = varchar("name", 255)
    val organizationName = varchar("organization_name", 255)
    val bio = text("bio")
    val userId = uuid("user_id").references(UsersTable.id).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object ConnectionsTable : Table("connections") {
    val id = uuid("id").primaryKey()
    val studentId = uuid("student_id").references(UsersTable.id)
    val mentorId = uuid("mentor_id").references(MentorsTable.id).nullable()
    val investorId = uuid("investor_id").references(InvestorsTable.id).nullable()
    val status = varchar("status", 50).default("PENDING") // PENDING, CONNECTED, ACTIVE
    val createdAt = datetime("created_at")
}

// ─── PSEI: Student Innovation Hub ────────────────────────────────────────────

object StudentProjectsTable : Table("student_projects") {
    val id = uuid("id").primaryKey()
    val studentName = varchar("student_name", 255)
    val institutionName = varchar("institution_name", 255)
    val projectTitle = varchar("project_title", 200)
    val projectDescription = text("project_description")
    val domain = varchar("domain", 100)
    val studentId = uuid("student_id").references(UsersTable.id)
    val status = varchar("status", 50).default("SUBMITTED")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object GrantsTable : Table("grants") {
    val id = uuid("id").primaryKey()
    val projectId = uuid("project_id").references(StudentProjectsTable.id)
    val grantAmount = varchar("grant_amount", 50)
    val grantType = varchar("grant_type", 100)
    val releaseDate = varchar("release_date", 100)
    val status = varchar("status", 50).default("PENDING")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── Citizen Vault: Identity Documents ───────────────────────────────────────

object IdentityDocumentsTable : Table("identity_documents") {
    val id = uuid("id").primaryKey()
    val documentType = varchar("document_type", 100) // AADHAR, PAN, PASSPORT, VOTER_ID, etc.
    val documentNumber = varchar("document_number", 100)
    val encryptedData = text("encrypted_data")
    val ownerId = uuid("owner_id").references(UsersTable.id)
    val verificationStatus = varchar("verification_status", 50).default("PENDING")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── Citizen Vault: Certificates ─────────────────────────────────────────────

object CertificatesTable : Table("certificates") {
    val id = uuid("id").primaryKey()
    val certificateType = varchar("certificate_type", 100) // BIRTH, MARRIAGE, PROPERTY, etc.
    val certificateNumber = varchar("certificate_number", 100)
    val encryptedData = text("encrypted_data")
    val ownerId = uuid("owner_id").references(UsersTable.id)
    val issuingAuthority = varchar("issuing_authority", 255)
    val issueDate = varchar("issue_date", 100).nullable()
    val expiryDate = varchar("expiry_date", 100).nullable()
    val status = varchar("status", 50).default("ACTIVE")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// ─── Audit Logs ──────────────────────────────────────────────────────────────

object AuditLogsTable : Table("audit_logs") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id").references(UsersTable.id).nullable()
    val action = varchar("action", 100)
    val resource = varchar("resource", 255)
    val resourceId = varchar("resource_id", 100).nullable()
    val ipAddress = varchar("ip_address", 45).nullable()
    val userAgent = varchar("user_agent", 500).nullable()
    val details = text("details").nullable()
    val createdAt = datetime("created_at")
}
