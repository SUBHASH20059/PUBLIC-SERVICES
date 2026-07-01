package org.psei.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

// ═════════════════════════════════════════════════════════════════════════════
// 1. IDEA VAULT (Core Innovation Protection)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class IdeaVault(
    val id: String? = null,
    val title: String,
    val description: String,
    val creatorId: String,
    val creatorName: String,
    val creatorEmail: String,
    val domain: String,                    // e.g. "Technology", "Agriculture", "Healthcare"
    val createdAt: String = LocalDateTime.now().toString(),
    val version: Int = 1,
    val status: IdeaStatus = IdeaStatus.DRAFT,
    val digitalSignature: String? = null,  // HMAC or RSA signature
    val publicKeyFingerprint: String? = null,
    val accessLogs: List<AccessLog> = emptyList(),
    val isPublic: Boolean = false
)

enum class IdeaStatus {
    DRAFT, SUBMITTED, APPROVED, REGISTERED, REJECTED
}

@Serializable
data class AccessLog(
    val accessorId: String,
    val timestamp: String,
    val action: String,  // "VIEW", "EDIT", "DOWNLOAD"
    val ipAddress: String? = null
)

// ─── Idea Version ────────────────────────────────────────────────────────────

@Serializable
data class IdeaVersion(
    val id: String? = null,
    val ideaId: String,
    val version: Int,
    val content: String,
    val changedBy: String,
    val changeReason: String? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    val hash: String? = null  // for integrity verification
)

// ═════════════════════════════════════════════════════════════════════════════
// 2. PATENT ASSISTANCE (IP Protection & Filing)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class PatentApplication(
    val id: String? = null,
    val applicantId: String,
    val applicantName: String,
    val applicantEmail: String,
    val title: String,
    val abstract: String,
    val claims: String,
    val drawings: String? = null,
    val filingDate: String = LocalDateTime.now().toString(),
    val status: PatentStatus = PatentStatus.DRAFT,
    val priority: String? = null,
    val internalReference: String? = null,
    val assigneeId: String? = null,
    val publicationNumber: String? = null,
    val examinationReports: List<ExaminationReport> = emptyList()
)

enum class PatentStatus {
    DRAFT, FILED, UNDER_EXAMINATION, APPROVED, REJECTED, ABANDONED, PUBLISHED
}

@Serializable
data class ExaminationReport(
    val id: String? = null,
    val reportDate: String,
    val examinerName: String,
    val findings: String,
    val actionRequired: String? = null
)

// ═════════════════════════════════════════════════════════════════════════════
// 3. GOVERNMENT SCHEMES & SUBSIDIES (Startup India, MSME, AIM)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class GovernmentScheme(
    val id: String? = null,
    val name: String,
    val ministry: String,  // e.g. "Ministry of MSME", "MeitY"
    val description: String,
    val eligibilityCriteria: String,
    val benefits: List<String>,
    val applicationDeadline: String? = null,
    val fundingAmount: Long? = null,  // in INR
    val keywords: List<String>,
    val state: String? = null,  // null = national
    val url: String? = null,
    val contactEmail: String? = null
)

@Serializable
data class SchemeApplication(
    val id: String? = null,
    val userId: String,
    val schemeId: String,
    val schemeName: String,
    val applicationDate: String = LocalDateTime.now().toString(),
    val status: ApplicationStatus = ApplicationStatus.DRAFT,
    val documents: List<String> = emptyList(),
    val notes: String? = null
)

enum class ApplicationStatus {
    DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, WITHDRAWN
}

// ═════════════════════════════════════════════════════════════════════════════
// 4. LEGAL TEMPLATES & PROTECTION
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class LegalTemplate(
    val id: String? = null,
    val name: String,
    val type: TemplateType,
    val description: String,
    val content: String,
    val createdBy: String,
    val createdAt: String = LocalDateTime.now().toString(),
    val jurisdiction: String = "India",  // Applicable law
    val category: String,  // "Startup", "Partnership", "Copyright"
    val tags: List<String> = emptyList(),
    val version: Int = 1,
    val isOfficial: Boolean = false
)

enum class TemplateType {
    NDA, FOUNDER_AGREEMENT, EMPLOYMENT_AGREEMENT, INVESTOR_AGREEMENT,
    CONFIDENTIALITY_AGREEMENT, IP_ASSIGNMENT, SERVICE_AGREEMENT, PARTNERSHIP_DEED
}

@Serializable
data class LegalTemplateDownload(
    val id: String? = null,
    val userId: String,
    val templateId: String,
    val downloadedAt: String = LocalDateTime.now().toString(),
    val customizations: String? = null
)

// ═════════════════════════════════════════════════════════════════════════════
// 5. BUSINESS REGISTRATION
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class BusinessRegistration(
    val id: String? = null,
    val ownerId: String,
    val ownerName: String,
    val businessName: String,
    val businessType: BusinessType,  // Sole Proprietor, Partnership, LLC, Corporation
    val panNumber: String? = null,
    val aadharNumber: String? = null,
    val registrationDate: String = LocalDateTime.now().toString(),
    val status: RegistrationStatus = RegistrationStatus.DRAFT,
    val sector: String,  // "Technology", "Manufacturing", "Service"
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val email: String,
    val phone: String,
    val gstNumber: String? = null,
    val certificateUrl: String? = null
)

enum class BusinessType {
    SOLE_PROPRIETOR, PARTNERSHIP, LLP, PRIVATE_LIMITED, PUBLIC_LIMITED, OPC
}

enum class RegistrationStatus {
    DRAFT, SUBMITTED, UNDER_VERIFICATION, APPROVED, REJECTED, SUSPENDED
}

// ═════════════════════════════════════════════════════════════════════════════
// 6. MENTOR & INVESTOR NETWORK
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class MentorProfile(
    val id: String? = null,
    val userId: String,
    val name: String,
    val email: String,
    val expertise: List<String>,  // "Kotlin", "Product Strategy", "B2B Sales"
    val yearsOfExperience: Int,
    val bio: String,
    val linkedinUrl: String? = null,
    val availability: String,  // "Available", "Limited", "Unavailable"
    val menteeCapacity: Int = 5,
    val rating: Double = 0.0,
    val menteeCount: Int = 0,
    val verified: Boolean = false
)

@Serializable
data class InvestorProfile(
    val id: String? = null,
    val userId: String,
    val name: String,
    val organizationName: String,
    val email: String,
    val focusSectors: List<String>,  // "AI", "Fintech", "Healthtech"
    val minTicketINR: Long,
    val maxTicketINR: Long,
    val bio: String,
    val previousInvestments: List<String> = emptyList(),
    val verified: Boolean = false,
    val websiteUrl: String? = null
)

@Serializable
data class MentorshipRequest(
    val id: String? = null,
    val menteeId: String,
    val mentorId: String,
    val requestDate: String = LocalDateTime.now().toString(),
    val status: MentorshipStatus = MentorshipStatus.PENDING,
    val reason: String
)

enum class MentorshipStatus {
    PENDING, ACCEPTED, REJECTED, ACTIVE, COMPLETED
}

@Serializable
data class InvestmentProposal(
    val id: String? = null,
    val entrepreneurId: String,
    val investorId: String,
    val businessName: String,
    val askAmount: Long,  // in INR
    val equity: Double,
    val proposalDate: String = LocalDateTime.now().toString(),
    val status: ProposalStatus = ProposalStatus.SUBMITTED,
    val businessPlanUrl: String? = null
)

enum class ProposalStatus {
    SUBMITTED, UNDER_REVIEW, SHORTLISTED, MEETING_SCHEDULED, REJECTED, FUNDED
}

// ═════════════════════════════════════════════════════════════════════════════
// 7. STUDENT INNOVATION HUB
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class StudentProject(
    val id: String? = null,
    val studentId: String,
    val studentName: String,
    val studentEmail: String,
    val institutionName: String,
    val registrationNumber: String? = null,
    val projectTitle: String,
    val projectDescription: String,
    val domain: String,  // "AI/ML", "Robotics", "Healthcare", "Sustainability"
    val teamMembers: List<String> = emptyList(),
    val submissionDate: String = LocalDateTime.now().toString(),
    val status: ProjectStatus = ProjectStatus.DRAFT,
    val documentUrl: String? = null,
    val prototypeUrl: String? = null,
    val fundingRequired: Long? = null  // in INR
)

enum class ProjectStatus {
    DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, INCUBATED, COMMERCIALIZED
}

@Serializable
data class StudentGrant(
    val id: String? = null,
    val projectId: String,
    val grantAmount: Long,  // in INR
    val grantType: String,  // "SEED", "DEVELOPMENT", "COMMERCIALIZATION"
    val releaseDate: String,
    val milestones: List<Milestone> = emptyList(),
    val status: GrantStatus = GrantStatus.APPROVED
)

@Serializable
data class Milestone(
    val name: String,
    val dueDate: String,
    val completed: Boolean = false
)

enum class GrantStatus {
    APPROVED, DISBURSED, IN_PROGRESS, COMPLETED, SUSPENDED
}

// ═════════════════════════════════════════════════════════════════════════════
// 8. CITIZEN DOCUMENT VAULT
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class CitizenDocument(
    val id: String? = null,
    val userId: String,
    val documentType: DocumentType,  // "Aadhar", "PAN", "Birth Certificate"
    val documentName: String,
    val referenceNumber: String? = null,  // Aadhar, PAN, etc.
    val issueDate: String? = null,
    val expiryDate: String? = null,
    val issuingAuthority: String? = null,
    val fileUrl: String? = null,
    val encryptionKey: String? = null,
    val uploadedAt: String = LocalDateTime.now().toString(),
    val isVerified: Boolean = false
)

enum class DocumentType {
    AADHAR, PAN, PASSPORT, VOTER_ID, DRIVING_LICENSE, BIRTH_CERTIFICATE,
    MARRIAGE_CERTIFICATE, EDUCATION_CERTIFICATE, GST_CERTIFICATE, BANK_STATEMENT
}

// ═════════════════════════════════════════════════════════════════════════════
// 9. AUDIT & LOGGING
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class AuditLog(
    val id: String? = null,
    val userId: String,
    val action: String,  // "CREATE_IDEA", "FILE_PATENT", "UPDATE_BUSINESS"
    val entityType: String,  // "IdeaVault", "PatentApplication", "BusinessRegistration"
    val entityId: String,
    val changes: Map<String, Any>? = null,
    val ipAddress: String? = null,
    val timestamp: String = LocalDateTime.now().toString(),
    val status: String = "SUCCESS"  // "SUCCESS", "FAILED"
)

// ═════════════════════════════════════════════════════════════════════════════
// 10. NOTIFICATIONS
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class Notification(
    val id: String? = null,
    val recipientId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val relatedEntityId: String? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    val read: Boolean = false,
    val actionUrl: String? = null
)

enum class NotificationType {
    IDEA_REGISTERED, PATENT_STATUS_UPDATED, SCHEME_MATCHED, MENTOR_REQUEST,
    INVESTMENT_PROPOSAL, GRANT_APPROVED, DOCUMENT_VERIFIED, ALERT
}
