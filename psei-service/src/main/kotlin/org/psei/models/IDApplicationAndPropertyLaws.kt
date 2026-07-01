package org.psei.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

// ═════════════════════════════════════════════════════════════════════════════
// 1. IDENTITY DOCUMENT APPLICATIONS (New ID Registration)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
enum class IDApplicationType {
    AADHAR_NEW,                 // New Aadhar card application
    AADHAR_UPDATE,              // Update existing Aadhar
    AADHAR_CORRECTION,          // Correct data in Aadhar
    VOTER_ID_NEW,               // New voter ID application
    VOTER_ID_UPDATE,            // Update voter ID
    PASSPORT_NEW,               // New passport application
    PASSPORT_RENEWAL,           // Passport renewal
    PASSPORT_REISSUE,           // Passport reissue (correction)
    DRIVING_LICENSE_NEW,        // New driving license
    DRIVING_LICENSE_RENEWAL,    // License renewal
    DRIVING_LICENSE_CORRECTION, // Correct driving license details
    STATE_ID_NEW,               // New state ID
    PAN_NEW,                    // New PAN card
    PAN_CORRECTION              // PAN correction
}

@Serializable
enum class IDApplicationStatus {
    DRAFT,              // User created, not submitted
    SUBMITTED,          // Submitted to authorities
    DOCUMENTS_UPLOADED, // Waiting for document upload
    UNDER_VERIFICATION, // Being verified by officer
    APPROVED,           // Application approved
    REJECTED,           // Rejected with reason
    ON_HOLD,            // Pending additional information
    ISSUED,             // ID issued and ready for collection
    COLLECTED           // Applicant collected the ID
}

@Serializable
data class IDApplication(
    val id: String? = null,
    val userId: String,
    val applicationType: IDApplicationType,
    val applicantName: String,
    val applicantEmail: String,
    val applicantPhone: String,
    val dateOfBirth: String,    // YYYY-MM-DD
    val gender: String,         // M, F, O
    val fatherName: String? = null,
    val motherName: String? = null,
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val aadharNumber: String? = null,  // For updates/corrections
    val applicantPhoto: String? = null, // File path/URL
    val applicantSignature: String? = null,
    
    // Supporting documents
    val supportingDocuments: List<String> = emptyList(),  // Document IDs
    val documentsChecklistItems: Map<String, Boolean> = emptyMap(),
    
    // Application details
    val applicationDate: String = LocalDateTime.now().toString(),
    val submissionDate: String? = null,
    val status: IDApplicationStatus = IDApplicationStatus.DRAFT,
    val statusUpdatedAt: String? = null,
    
    // Verification
    val verifiedBy: String? = null,
    val verificationDate: String? = null,
    val verificationNotes: String? = null,
    val rejectionReason: String? = null,
    
    // ID Generation
    val generatedIDNumber: String? = null,
    val issuanceDate: String? = null,
    val expiryDate: String? = null,
    val collectionCenter: String? = null,
    val trackingNumber: String? = null,
    
    // Biometric
    val biometricCollectionRequired: Boolean = true,
    val biometricCollectionDate: String? = null,
    val biometricCollectionCenter: String? = null,
    val biometricStatus: String = "PENDING",  // PENDING, COLLECTED, PROCESSED
    
    // Audit
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String? = null,
    val lastModifiedBy: String? = null
)

@Serializable
data class IDApplicationTimeline(
    val id: String? = null,
    val applicationId: String,
    val timestamp: String = LocalDateTime.now().toString(),
    val status: String,
    val event: String,
    val description: String,
    val updatedBy: String? = null,
    val remarks: String? = null
)

// ═════════════════════════════════════════════════════════════════════════════
// 2. DOCUMENT MODIFICATION & UPDATES (Legal Owner Changes)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
enum class DocumentUpdateType {
    NAME_CHANGE,            // Name change (marriage, court order)
    ADDRESS_CHANGE,         // Change of address
    DATE_CORRECTION,        // Correct date of birth
    MARITAL_STATUS_CHANGE,  // Update marital status
    EMERGENCY_CONTACT_UPDATE, // Change emergency contact
    PHONE_EMAIL_UPDATE,     // Update contact information
    BIOMETRIC_UPDATE,       // Update fingerprints/biometrics
    PHOTOGRAPH_UPDATE,      // Change photograph
    SIGNATURE_UPDATE,       // Update signature
    CORRECTION_OF_DATA,     // Correct any data field
    DUPLICATE_CARD,         // Request duplicate/replacement
    UPGRADE_CATEGORY        // Upgrade driving license category
}

@Serializable
enum class UpdateApprovalStatus {
    DRAFT,              // User prepared, not submitted
    SUBMITTED,          // Submitted for approval
    UNDER_REVIEW,       // Officer reviewing
    LEGAL_CHECK,        // Legal verification in progress
    EVIDENCE_NEEDED,    // Requesting additional evidence
    APPROVED,           // Approved by authority
    REJECTED,           // Rejected with reason
    PROCESSED,          // Changes processed
    UPDATED              // Document updated and reissued
}

@Serializable
data class DocumentUpdateRequest(
    val id: String? = null,
    val userId: String,
    val documentId: String,                     // Existing document ID
    val documentType: IdentityDocumentType,
    val updateType: DocumentUpdateType,
    val currentValue: String,
    val newValue: String,
    val justificationReason: String,            // Why update is needed
    
    // Supporting Evidence
    val supportingDocuments: List<String> = emptyList(),
    val affidavitUrl: String? = null,           // Notarized affidavit
    val courtOrderUrl: String? = null,          // Court order if applicable
    val marriageCertificateUrl: String? = null, // For name change
    val gazetteNotificationUrl: String? = null, // For official changes
    
    // Verification
    val legalOwnerVerification: Boolean = false, // Verified as legal owner
    val verificationMethod: String? = null,     // Aadhar OTP, Face, Document
    val verifiedAt: String? = null,
    val verifiedBy: String? = null,
    val verificationCode: String? = null,       // OTP or code received
    
    // Status & Approval
    val requestDate: String = LocalDateTime.now().toString(),
    val status: UpdateApprovalStatus = UpdateApprovalStatus.DRAFT,
    val statusHistory: List<UpdateStatusLog> = emptyList(),
    val approvedBy: String? = null,
    val approvalDate: String? = null,
    val rejectionReason: String? = null,
    
    // Processing
    val processingStartedAt: String? = null,
    val processingCompletedAt: String? = null,
    val newDocumentUrl: String? = null,         // Updated document
    val newDocumentHash: String? = null,
    
    // Notification
    val notificationSent: Boolean = false,
    val collectionReadyNotified: Boolean = false,
    val collectionDate: String? = null,
    val collectionCenter: String? = null
)

@Serializable
data class UpdateStatusLog(
    val timestamp: String,
    val status: String,
    val description: String,
    val updatedBy: String? = null
)

// ═════════════════════════════════════════════════════════════════════════════
// 3. PROPERTY LAWS & REAL ESTATE DOCUMENTS
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
enum class PropertyType {
    RESIDENTIAL,        // House, apartment, flat
    COMMERCIAL,         // Shop, office, warehouse
    AGRICULTURAL,       // Farm land, agricultural property
    INDUSTRIAL,         // Factory, mill
    MIXED_USE,          // Combination of uses
    VACANT_LAND         // Unbuilt land
}

@Serializable
enum class PropertyOwnershipType {
    SOLE_OWNER,         // Single person owns
    JOINT_OWNERS,       // Multiple owners (equal share)
    TENANCY_IN_COMMON,  // Multiple owners (different shares)
    COPARCENARY,        // Hindu family property
    TRUST_OWNED,        // Owned by trust
    SOCIETY_OWNED,      // Owned by cooperative society
    GOVERNMENT_LAND     // Government property
}

@Serializable
enum class PropertyDocumentType {
    SALE_DEED,              // Property transfer document
    GIFT_DEED,              // Gift of property
    LEASE_DEED,             // Rental agreement
    MORTGAGE_DEED,          // Property as collateral
    PARTITION_DEED,         // Division among co-owners
    POWER_OF_ATTORNEY,      // Authority to act
    WILL,                   // Inheritance document
    MUTATION_DEED,          // Transfer in land records
    REGISTRY_CERTIFICATE,   // Government registration
    SURVEY_PLAN,            // Property boundaries
    TITLE_CLEARANCE,        // Proof of ownership
    ENCUMBRANCE_CERTIFICATE, // No legal claims
    OCCUPANCY_CERTIFICATE,  // Construction completion
    MUNICIPAL_TAX_RECEIPT,  // Property tax paid
    KHATA_CERTIFICATE,      // Land records
    BIS_MARK,              // Boundary marks
    AFFIDAVIT,             // Sworn statement
    NOTARY_AFFIDAVIT,      // Notarized statement
    AGREEMENT_DRAFT        // Pre-registration draft
}

@Serializable
data class PropertyRecord(
    val id: String? = null,
    val userId: String,                    // Current owner
    val propertyName: String,
    val propertyType: PropertyType,
    val ownershipType: PropertyOwnershipType,
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val landmark: String? = null,
    
    // Legal Description
    val surveyNumber: String,              // Government survey reference
    val khataNumber: String? = null,       // Revenue record number
    val areaInSqFt: Double,
    val areaInSqMeter: Double,
    val dimensions: String? = null,        // e.g., "100x150 ft"
    
    // Ownership Details
    val owners: List<PropertyOwner> = emptyList(),
    val coOwners: List<String> = emptyList(),  // Co-owner user IDs
    val sharesPercentage: Map<String, Double> = emptyMap(),
    
    // Documents
    val documents: List<PropertyDocumentType> = emptyList(),
    val documentUrls: Map<String, String> = emptyMap(),
    val documentHashes: Map<String, String> = emptyMap(),
    
    // Financial
    val purchasePrice: Long? = null,       // In INR
    val purchaseDate: String? = null,
    val currentMarketValue: Long? = null,
    val propertyTaxAmount: Long? = null,
    val propertyTaxLastPaid: String? = null,
    
    // Encumbrances
    val mortgageAmount: Long? = null,
    val mortgageHolder: String? = null,
    val mortgageDateFrom: String? = null,
    val mortgageDateTo: String? = null,
    val lienClaims: List<String> = emptyList(),
    
    // Registration
    val registrationNumber: String? = null,
    val registrationDate: String? = null,
    val registeredWithOffice: String? = null,  // Sub-registrar office
    val encumbranceCertificate: String? = null,
    val encumbranceCertificateDate: String? = null,
    
    // Legal Status
    val isClear: Boolean = false,          // No legal claims
    val isDisputedProperty: Boolean = false,
    val disputeDetails: String? = null,
    val courtCaseNumber: String? = null,
    
    // Insurance & Tax
    val insured: Boolean = false,
    val insurancePolicy: String? = null,
    val insuredAmount: Long? = null,
    val gstApplicable: Boolean = false,
    
    // Verification
    val verificationStatus: String = "PENDING",
    val verifiedBy: String? = null,
    val verificationDate: String? = null,
    val verificationNotes: String? = null,
    
    // Audit
    val createdAt: String = LocalDateTime.now().toString(),
    val lastUpdatedAt: String = LocalDateTime.now().toString(),
    val updateHistory: List<PropertyUpdateLog> = emptyList()
)

@Serializable
data class PropertyOwner(
    val userId: String,
    val ownerName: String,
    val ownerEmail: String,
    val aadharNumber: String? = null,
    val panNumber: String? = null,
    val ownershipPercentage: Double = 100.0,
    val ownershipType: String,
    val addedDate: String = LocalDateTime.now().toString()
)

@Serializable
data class PropertyUpdateLog(
    val timestamp: String,
    val fieldChanged: String,
    val oldValue: String,
    val newValue: String,
    val changedBy: String,
    val reason: String? = null
)

@Serializable
data class PropertyTransfer(
    val id: String? = null,
    val propertyId: String,
    val fromOwner: String,                 // Current owner user ID
    val toOwner: String,                   // New owner user ID
    val transferType: String,              // SALE, GIFT, INHERITANCE
    val transferDate: String,              // Transaction date
    val transferAmount: Long? = null,      // Sale price (if applicable)
    val deedNumber: String? = null,
    val registrationNumber: String? = null,
    val registrationDate: String? = null,
    val saleDeeds: List<String> = emptyList(),
    val affidavits: List<String> = emptyList(),
    val stampDutyPaid: Boolean = false,
    val stampDutyAmount: Long? = null,
    val stampDutyReceiptUrl: String? = null,
    val registrationCompleted: Boolean = false,
    val status: String = "PENDING",        // PENDING, IN_PROGRESS, COMPLETED
    val createdAt: String = LocalDateTime.now().toString()
)

@Serializable
data class PropertyDispute(
    val id: String? = null,
    val propertyId: String,
    val claimant: String,                  // User ID of person making claim
    val defendant: String? = null,         // Respondent user ID
    val disputeType: String,               // OWNERSHIP, BOUNDARY, MORTGAGE
    val description: String,
    val legalBasis: String,                // Which law/act
    val supportingDocuments: List<String> = emptyList(),
    val courtCaseNumber: String? = null,
    val courtName: String? = null,
    val filedDate: String? = null,
    val status: String = "OPEN",           // OPEN, IN_COURT, SETTLED, CLOSED
    val resolution: String? = null,
    val createdAt: String = LocalDateTime.now().toString()
)

// ═════════════════════════════════════════════════════════════════════════════
// 4. PROPERTY LAWS & REGULATIONS DATABASE
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class PropertyLaw(
    val id: String? = null,
    val lawName: String,                   // e.g., "Transfer of Property Act, 1882"
    val lawCode: String,                   // e.g., "TP_ACT_1882"
    val country: String = "India",
    val applicableStates: List<String> = emptyList(),  // null = national
    val description: String,
    val provisions: List<LawProvision> = emptyList(),
    val keywords: List<String> = emptyList(),
    val url: String? = null,
    val lastUpdated: String = LocalDateTime.now().toString()
)

@Serializable
data class LawProvision(
    val sectionNumber: String,             // e.g., "Section 2"
    val title: String,
    val description: String,
    val applicableFor: List<String> = emptyList(),  // SALE, GIFT, INHERITANCE, etc.
    val implications: String? = null,
    val penalties: String? = null
)

@Serializable
data class PropertyTaxCalculation(
    val id: String? = null,
    val propertyId: String,
    val financialYear: String,             // e.g., "2024-2025"
    val propertyValue: Long,
    val taxRate: Double,                   // Percentage
    val calculatedTax: Long,
    val rebate: Long? = null,
    val surcharge: Long? = null,
    val totalTax: Long,
    val paymentStatus: String = "PENDING", // PENDING, PAID, OVERDUE
    val dueDate: String,
    val paidDate: String? = null,
    val receiptNumber: String? = null,
    val nextYear: Boolean = false
)

@Serializable
data class RightOfWay(
    val id: String? = null,
    val propertyId: String,
    val rightType: String,                 // EASEMENT, RIGHT_OF_WAY, WAYLEAVE
    val beneficiary: String,               // Who has the right
    val purpose: String,                   // Reason for the right
    val grantedDate: String,
    val expiryDate: String? = null,
    val document: String? = null,
    val legalDescription: String? = null
)

@Serializable
data class Encumbrance(
    val id: String? = null,
    val propertyId: String,
    val encumbranceType: String,           // MORTGAGE, LIEN, LEASE, CHARGE
    val encumbrancer: String,              // Person/entity holding the claim
    val amount: Long? = null,
    val registrationDate: String,
    val expiryDate: String? = null,
    val documentUrl: String? = null,
    val status: String = "ACTIVE"
)

@Serializable
data class PropertySurveyRecord(
    val id: String? = null,
    val propertyId: String,
    val surveyNumber: String,
    val surveyDate: String,
    val surveyArea: Double,
    val boundaryPoints: List<BoundaryPoint> = emptyList(),
    val mapDocument: String? = null,
    val surveyorName: String,
    val surveyorLicense: String? = null,
    val accuracy: String? = null,
    val notes: String? = null
)

@Serializable
data class BoundaryPoint(
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val marker: String? = null
)

@Serializable
enum class LegalDocumentValidityStatus {
    VALID,           // Document is valid
    EXPIRED,         // Validity period ended
    DISPUTED,        // Under legal challenge
    CANCELLED,       // Cancelled by authority
    SUPERSEDED,      // Replaced by newer document
    PENDING_REVIEW   // Awaiting verification
}
