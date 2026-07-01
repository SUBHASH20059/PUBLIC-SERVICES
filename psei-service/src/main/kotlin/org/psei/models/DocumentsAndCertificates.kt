package org.psei.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

// ═════════════════════════════════════════════════════════════════════════════
// 1. ALL INDIAN IDENTITY DOCUMENTS
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
enum class IdentityDocumentType {
    // National IDs
    AADHAR_CARD,                    // 12-digit unique identification
    VOTER_ID,                        // Electoral registration document
    PASSPORT,                        // International travel document
    PAN_CARD,                        // Permanent Account Number for tax
    
    // State IDs
    DRIVING_LICENSE,                 // Road transport authority license
    STATE_ID_CARD,                   // State-issued identity card
    
    // Digital IDs
    AADHAAR_VIRTUAL_ID,              // Temporary masked Aadhar ID
    UPI_ID,                          // Unified Payments Interface ID
    
    // Official Documents
    BIRTH_CERTIFICATE,               // Vital registration document
    MARRIAGE_CERTIFICATE,            // Marriage registration from government
    DEATH_CERTIFICATE,               // Death registration certificate
    DIVORCE_DECREE,                  // Court-issued divorce document
    
    // Residential
    ELECTRICITY_BILL,                // Proof of residence
    WATER_BILL,                      // Proof of residence
    TELEPHONE_BILL,                  // Proof of residence
    GAS_BILL,                        // Proof of residence
    PROPERTY_DEED,                   // Property ownership document
    RENTAL_AGREEMENT,                // Rental property agreement
    
    // Employment & Financial
    EMPLOYMENT_LETTER,               // Company employment verification
    BANK_STATEMENT,                  // Bank account statement
    SALARY_SLIP,                     // Monthly salary document
    FORM_16,                         // Tax deduction certificate from employer
    ITR_RETURN,                      // Income tax return filing
    
    // Education
    SCHOOL_CERTIFICATE,              // 10th/12th pass certificate
    COLLEGE_DIPLOMA,                 // Graduation/Post-graduation diploma
    DEGREE_CERTIFICATE,              // University degree certificate
    SKILL_CERTIFICATE,               // Training/Skill certification
    JEE_SCORECARD,                   // JEE exam scorecard
    NEET_SCORECARD,                  // NEET exam scorecard
    IELTS_SCORE,                     // English proficiency test
    GATE_SCORECARD,                  // Graduate aptitude test
    
    // Medical
    VACCINATION_CERTIFICATE,         // COVID/immunization record
    HEALTH_INSURANCE_CARD,           // Insurance provider card
    MEDICAL_REPORT,                  // Doctor's medical report
    DISABILITY_CERTIFICATE,          // Government disability certificate
    
    // Business & Trade
    GST_CERTIFICATE,                 // Goods & Services Tax registration
    IEC_CERTIFICATE,                 // Import-Export Code
    UDYAM_REGISTRATION,              // MSME registration certificate
    SHOP_ESTABLISHMENT_LICENSE,      // Business license from state
    SHOP_ACT_LICENSE,                // Retail shop registration
    
    // Professional
    CA_MEMBERSHIP,                   // Chartered Accountant membership
    LAWYER_MEMBERSHIP,               // Bar Council membership
    DOCTOR_REGISTRATION,             // Medical Council registration
    ENGINEER_REGISTRATION,           // Engineering Council registration
    
    // Other Government
    RATION_CARD,                     // Food rationing card
    SENIOR_CITIZEN_CARD,             // Government senior citizen card
    BPL_CARD,                        // Below Poverty Line card
    CASTE_CERTIFICATE,               // SC/ST/OBC certificate
    INCOME_CERTIFICATE,              // Government income verification
    DOMICILE_CERTIFICATE,            // State resident certificate
    STUDENT_ID_CARD                  // University/school ID
}

// ─── Core Identity Document ──────────────────────────────────────────────────

@Serializable
data class IdentityDocument(
    val id: String? = null,
    val userId: String,
    val documentType: IdentityDocumentType,
    val documentNumber: String,           // e.g., Aadhar: 123456789012
    val issueDate: String,                // YYYY-MM-DD
    val expiryDate: String? = null,       // null if no expiry
    val issuingAuthority: String,         // e.g., "UIDAI", "RTO Gujarat"
    val issuer: String,                   // e.g., "Unique Identification Authority of India"
    val state: String? = null,            // For state-level documents
    val holderName: String,
    val dateOfBirth: String? = null,      // YYYY-MM-DD
    val fatherName: String? = null,       // For certain documents
    val gender: String? = null,           // "M", "F", "O"
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    
    // Security fields
    val uploadedAt: String = LocalDateTime.now().toString(),
    val fileHash: String? = null,         // SHA-256 of original document
    val encryptedFileUrl: String? = null, // Encrypted document storage path
    val encryptionAlgorithm: String = "AES-256-GCM",
    val encryptionKeyVersion: Int = 1,
    val digitalSignature: String? = null, // RSA signature from government
    val signatureVerified: Boolean = false,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val verificationDate: String? = null,
    val verifiedBy: String? = null,       // Officer/System that verified
    val verificationNotes: String? = null,
    val isActive: Boolean = true,
    val accessLogs: List<DocumentAccessLog> = emptyList()
)

enum class VerificationStatus {
    PENDING,        // Awaiting verification
    UNDER_REVIEW,   // Being reviewed by officer
    VERIFIED,       // Successfully verified
    REJECTED,       // Failed verification
    EXPIRED,        // Document has expired
    SUSPENDED       // Temporarily suspended
}

@Serializable
data class DocumentAccessLog(
    val accessorId: String,
    val accessorRole: String,           // CITIZEN, OFFICER, ADMIN
    val timestamp: String,
    val action: String,                 // VIEW, DOWNLOAD, VERIFY, REVOKE
    val ipAddress: String? = null,
    val deviceInfo: String? = null,
    val reason: String? = null
)

// ═════════════════════════════════════════════════════════════════════════════
// 2. GOVERNMENT-ISSUED CERTIFICATES
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
enum class GovernmentCertificateType {
    // Education
    BIRTH_REGISTRATION,               // Vital Statistics registration
    SCHOOL_LEAVING,                   // 10th/12th standard certificate
    COLLEGE_DEGREE,                   // Graduate degree
    PROFESSIONAL_QUALIFICATION,       // Engineering/Medical/Law degree
    
    // Occupational
    CONTRACTOR_LICENSE,               // Government contractor authorization
    HEALTH_WORKER_CERTIFICATE,        // ASHA/ANM certification
    TEACHER_CERTIFICATE,              // Teaching qualification
    POLICE_CLEARANCE,                 // Police verification certificate
    
    // Business
    BUSINESS_REGISTRATION,            // Udyam/MSME registration
    PARTNERSHIP_DEED_REGISTRATION,    // Business partnership certificate
    EXPORT_IMPORT_CODE,               // IEC certificate for trade
    FOOD_SAFETY_LICENSE,              // FSSAI registration
    MEDICINE_LICENSE,                 // Pharma license
    
    // Social & Welfare
    SC_ST_OBC_CERTIFICATE,            // Caste certificate
    INCOME_CERTIFICATE,               // Annual income verification
    WIDOW_PENSION_CERTIFICATE,        // Widow welfare certificate
    DISABILITY_CERTIFICATE,           // PWD certificate with %age
    POVERTY_BPL_CARD,                 // Below poverty line certificate
    
    // Environmental & Compliance
    POLLUTION_CONTROL_BOARD_APPROVAL, // PCB/SPCB NOC
    FACTORY_LICENSE,                  // Factories Act license
    BUILDING_COMPLETION_CERTIFICATE,  // Municipal completion certificate
    
    // Agricultural
    LAND_OWNERSHIP_CERTIFICATE,       // Revenue department certificate
    AGRICULTURAL_LOAN_CERTIFICATE,    // Crop insurance certificate
    ORGANIC_FARMING_CERTIFICATE,      // Organic certification
    
    // Public Utility
    DRIVING_PERMISSION_CERTIFICATE,   // RTO certificate
    VACCINATION_CERTIFICATE,          // Health department certificate
    MARRIAGE_CERTIFICATE,             // Registrar office certificate
    CHARACTER_CERTIFICATE,            // Police character certificate
    
    // Government Schemes
    STARTUP_RECOGNITION,              // Department of Promotion of Industry
    PATENT_GRANT_CERTIFICATE,         // IP India patent grant
    TRADEMARK_REGISTRATION,           // Trademark certificate from IP India
    GEOGRAPHICAL_INDICATION,          // GI registration certificate
    DESIGN_REGISTRATION                // Design patent certificate
}

@Serializable
data class GovernmentCertificate(
    val id: String? = null,
    val userId: String,
    val certificateType: GovernmentCertificateType,
    val certificateNumber: String,         // Government's reference number
    val issueDate: String,                 // YYYY-MM-DD
    val expiryDate: String? = null,        // null if permanent
    val issuingDepartment: String,         // e.g., "UIDAI", "IP India", "Income Tax Dept"
    val issuingOffice: String,             // e.g., "Hyderabad Regional Office"
    val issuingOfficer: String? = null,    // Officer's name who signed
    val holderName: String,
    val certificateDetails: Map<String, String>,  // Custom fields per certificate type
    
    // Security & Verification
    val fileHash: String,                  // SHA-256 of original document
    val encryptedCertificateUrl: String,   // Encrypted storage path
    val encryptionAlgorithm: String = "AES-256-GCM",
    val digitalSignature: String,          // RSA-4096 signature from issuing authority
    val signatureAlgorithm: String = "RSA-SHA256",
    val issuerPublicKeyFingerprint: String, // Issuer's public key hash
    val blockchainHash: String? = null,    // Optional blockchain verification
    val qrCodeUrl: String? = null,         // Government QR code for verification
    
    // Certificate Status
    val status: CertificateStatus = CertificateStatus.ACTIVE,
    val verifiedAt: String = LocalDateTime.now().toString(),
    val verificationMethod: String,        // "BLOCKCHAIN", "ISSUER_API", "MANUAL"
    val accessLogs: List<CertificateAccessLog> = emptyList(),
    val revokedAt: String? = null,
    val revocationReason: String? = null,
    val duplicateReports: List<String> = emptyList()  // IDs of reported duplicates
)

enum class CertificateStatus {
    ACTIVE,         // Valid and usable
    EXPIRED,        // Validity period ended
    REVOKED,        // Canceled by issuer
    SUSPENDED,      // Temporarily unavailable
    ISSUED,         // Recently issued, not yet activated
    LOST,           // Reported as lost by holder
    DUPLICATE       // Reported as duplicate/fraudulent
}

@Serializable
data class CertificateAccessLog(
    val accessorId: String,
    val accessorRole: String,              // CITIZEN, OFFICER, VERIFIER, ADMIN
    val timestamp: String,
    val action: String,                    // VIEW, SHARE, VERIFY, EXPORT
    val verificationResult: String? = null,// "GENUINE", "FRAUDULENT", "PENDING"
    val ipAddress: String? = null,
    val organization: String? = null       // Org that verified/accessed
)

// ─── Certificate Sharing & Verification ───────────────────────────────────────

@Serializable
data class CertificateShare(
    val id: String? = null,
    val certificateId: String,
    val ownerId: String,
    val sharedWith: String,                // Recipient's email or org code
    val sharingDate: String = LocalDateTime.now().toString(),
    val expiryDate: String? = null,        // Share validity period
    val accessLevel: AccessLevel = AccessLevel.VIEW_ONLY,
    val purpose: String? = null            // e.g., "Job Application", "Loan Processing"
)

enum class AccessLevel {
    VIEW_ONLY,      // Can only view
    DOWNLOAD,       // Can download copy
    SHARE,          // Can share further
    VERIFY          // Can verify authenticity
}

@Serializable
data class CertificateVerification(
    val id: String? = null,
    val certificateId: String,
    val requestedBy: String,               // User requesting verification
    val requestDate: String = LocalDateTime.now().toString(),
    val verificationMethod: String,        // "ISSUER_API", "BLOCKCHAIN", "MANUAL"
    val result: VerificationResult = VerificationResult.PENDING,
    val resultDate: String? = null,
    val verifiedBy: String? = null,        // Verifying authority
    val notes: String? = null,
    val blockchainTxHash: String? = null   // If verified on blockchain
)

enum class VerificationResult {
    PENDING,        // Awaiting verification
    AUTHENTIC,      // Verified as genuine
    SUSPICIOUS,     // Appears fraudulent
    FORGED,         // Confirmed fake
    NOT_FOUND       // Not in government records
}

// ═════════════════════════════════════════════════════════════════════════════
// 3. SECURE DOCUMENT VAULT
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class SecureDocumentVault(
    val id: String? = null,
    val userId: String,
    val vaultName: String,                 // e.g., "Personal Documents", "Business Files"
    val description: String? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    val lastModified: String = LocalDateTime.now().toString(),
    val documents: List<String> = emptyList(),  // Document IDs
    val certificates: List<String> = emptyList(), // Certificate IDs
    val totalSize: Long = 0,               // In bytes
    val maxSize: Long = 5_368_709_120,    // 5GB default
    val encryptionEnabled: Boolean = true,
    val twoFactorRequired: Boolean = false,
    val autoBackupEnabled: Boolean = true,
    val backupFrequency: String = "DAILY",
    val accessLogs: List<String> = emptyList()  // Access log IDs
)

@Serializable
data class DocumentBackup(
    val id: String? = null,
    val vaultId: String,
    val backupTime: String = LocalDateTime.now().toString(),
    val documentCount: Int,
    val totalSize: Long,
    val backupLocation: String,            // Cloud storage path
    val encryptionKey: String? = null,
    val checksumHash: String,              // Integrity verification
    val restorable: Boolean = true
)

// ═════════════════════════════════════════════════════════════════════════════
// 4. DIGITAL SIGNATURES & VERIFICATION
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class DigitalSignature(
    val id: String? = null,
    val documentId: String,
    val signatureType: SignatureType,
    val signedBy: String,                  // User ID
    val signedAt: String = LocalDateTime.now().toString(),
    val algorithm: String = "RSA-4096",
    val signatureValue: String,            // Hex-encoded signature
    val signerCertificate: String? = null, // X.509 certificate chain
    val timestamp: String,                 // Trusted timestamp
    val chainOfCustody: List<String> = emptyList()  // Previous signatories
)

enum class SignatureType {
    USER_SIGNATURE,        // Individual signing
    OFFICER_SIGNATURE,     // Government officer
    NOTARY_SIGNATURE,      // Notarized document
    MULTI_SIGNATURE,       // Multiple signers required
    TIME_STAMPED           // Legally timed signature
}

@Serializable
data class SignatureVerification(
    val id: String? = null,
    val signatureId: String,
    val verifiedAt: String = LocalDateTime.now().toString(),
    val isValid: Boolean,
    val validationMethod: String,
    val certificateChainValid: Boolean,
    val timestampValid: Boolean,
    val notes: String? = null
)

// ═════════════════════════════════════════════════════════════════════════════
// 5. ENCRYPTION & KEY MANAGEMENT
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class EncryptionKey(
    val id: String? = null,
    val userId: String,
    val keyVersion: Int,
    val algorithm: String = "AES-256-GCM",
    val keySize: Int = 256,
    val createdAt: String = LocalDateTime.now().toString(),
    val rotatedAt: String? = null,
    val expiresAt: String? = null,
    val isActive: Boolean = true,
    val keyDerivationFunction: String = "PBKDF2",
    val saltHash: String? = null,
    val usageCount: Long = 0
)

// ═════════════════════════════════════════════════════════════════════════════
// 6. COMPLIANCE & AUDIT
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class DocumentComplianceRecord(
    val id: String? = null,
    val documentId: String,
    val complianceType: String,            // "DATA_PROTECTION", "GDPR", "IDENTITY_VERIFICATION"
    val checkedAt: String = LocalDateTime.now().toString(),
    val checkedBy: String,
    val status: ComplianceStatus = ComplianceStatus.COMPLIANT,
    val notes: String? = null,
    val nextAuditDate: String? = null
)

enum class ComplianceStatus {
    COMPLIANT,      // Meets all requirements
    NON_COMPLIANT,  // Violates standards
    UNDER_REVIEW,   // Being assessed
    REMEDIATION     // Corrective action needed
}

@Serializable
data class SecurityAudit(
    val id: String? = null,
    val userId: String? = null,            // null for system-level audits
    val auditType: String,                 // "ACCESS", "ENCRYPTION", "MODIFICATION"
    val timestamp: String = LocalDateTime.now().toString(),
    val details: Map<String, Any>,
    val severity: String,                  // "INFO", "WARNING", "CRITICAL"
    val actionTaken: String? = null
)
