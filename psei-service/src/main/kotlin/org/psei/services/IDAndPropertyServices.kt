package org.psei.services

import org.psei.models.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

// ═════════════════════════════════════════════════════════════════════════════
// ID APPLICATION SERVICE
// ═════════════════════════════════════════════════════════════════════════════

class IDApplicationService {
    private val applications = mutableMapOf<String, IDApplication>()
    private val timelines = mutableMapOf<String, MutableList<IDApplicationTimeline>>()

    fun createApplication(userId: String, applicationType: IDApplicationType, 
                         details: Map<String, String>): IDApplication {
        val id = UUID.randomUUID().toString()
        
        val application = IDApplication(
            id = id,
            userId = userId,
            applicationType = applicationType,
            applicantName = details["applicantName"] ?: "",
            applicantEmail = details["applicantEmail"] ?: "",
            applicantPhone = details["applicantPhone"] ?: "",
            dateOfBirth = details["dateOfBirth"] ?: "",
            gender = details["gender"] ?: "",
            fatherName = details["fatherName"],
            motherName = details["motherName"],
            address = details["address"] ?: "",
            city = details["city"] ?: "",
            state = details["state"] ?: "",
            pinCode = details["pinCode"] ?: ""
        )
        
        applications[id] = application
        timelines[id] = mutableListOf(
            IDApplicationTimeline(
                id = UUID.randomUUID().toString(),
                applicationId = id,
                status = "DRAFT",
                event = "Application Created",
                description = "User created new $applicationType application"
            )
        )
        
        return application
    }

    fun submitApplication(applicationId: String, userId: String, documents: List<String>): Boolean {
        val app = applications[applicationId] ?: return false
        if (app.userId != userId) return false
        
        applications[applicationId] = app.copy(
            status = IDApplicationStatus.SUBMITTED,
            submissionDate = LocalDateTime.now().toString(),
            supportingDocuments = documents,
            statusUpdatedAt = LocalDateTime.now().toString()
        )
        
        addTimeline(applicationId, "SUBMITTED", "Application Submitted",
            "Application submitted with ${documents.size} supporting documents")
        
        return true
    }

    fun verifyApplication(applicationId: String, officerId: String, 
                         approved: Boolean, notes: String = ""): Boolean {
        val app = applications[applicationId] ?: return false
        
        applications[applicationId] = app.copy(
            status = if (approved) IDApplicationStatus.APPROVED else IDApplicationStatus.REJECTED,
            verifiedBy = officerId,
            verificationDate = LocalDateTime.now().toString(),
            verificationNotes = notes,
            rejectionReason = if (!approved) notes else null
        )
        
        addTimeline(applicationId, 
            if (approved) "APPROVED" else "REJECTED",
            "Application ${if (approved) "Approved" else "Rejected"}",
            notes
        )
        
        return true
    }

    fun collectBiometric(applicationId: String, center: String): Boolean {
        val app = applications[applicationId] ?: return false
        if (app.status != IDApplicationStatus.APPROVED) return false
        
        applications[applicationId] = app.copy(
            biometricCollectionRequired = true,
            biometricCollectionDate = LocalDateTime.now().toString(),
            biometricCollectionCenter = center,
            biometricStatus = "COLLECTED"
        )
        
        addTimeline(applicationId, "BIOMETRIC_COLLECTED",
            "Biometric Collected", "Biometric data collected at $center")
        
        return true
    }

    fun issueID(applicationId: String, idNumber: String, expiryDate: String): Boolean {
        val app = applications[applicationId] ?: return false
        
        applications[applicationId] = app.copy(
            status = IDApplicationStatus.ISSUED,
            generatedIDNumber = idNumber,
            issuanceDate = LocalDateTime.now().toString(),
            expiryDate = expiryDate,
            trackingNumber = generateTrackingNumber()
        )
        
        addTimeline(applicationId, "ISSUED", "ID Issued",
            "ID $idNumber issued and ready for collection")
        
        return true
    }

    fun markCollected(applicationId: String): Boolean {
        val app = applications[applicationId] ?: return false
        
        applications[applicationId] = app.copy(
            status = IDApplicationStatus.COLLECTED
        )
        
        addTimeline(applicationId, "COLLECTED", "ID Collected",
            "Applicant collected their ID")
        
        return true
    }

    fun getApplication(applicationId: String): IDApplication? = applications[applicationId]

    fun getApplications(userId: String): List<IDApplication> =
        applications.values.filter { it.userId == userId }

    fun getTimeline(applicationId: String): List<IDApplicationTimeline> =
        timelines[applicationId] ?: emptyList()

    private fun addTimeline(applicationId: String, status: String, event: String, description: String) {
        timelines[applicationId]?.add(
            IDApplicationTimeline(
                id = UUID.randomUUID().toString(),
                applicationId = applicationId,
                status = status,
                event = event,
                description = description
            )
        )
    }

    private fun generateTrackingNumber(): String = "TRK-${UUID.randomUUID().toString().take(8).uppercase()}"
}

// ═════════════════════════════════════════════════════════════════════════════
// DOCUMENT UPDATE SERVICE (Legal Owner Modifications)
// ═════════════════════════════════════════════════════════════════════════════

class DocumentUpdateService {
    private val updateRequests = mutableMapOf<String, DocumentUpdateRequest>()
    private val verificationCodes = mutableMapOf<String, String>()

    fun requestUpdate(userId: String, documentId: String, updateType: DocumentUpdateType,
                     currentValue: String, newValue: String, reason: String): DocumentUpdateRequest {
        val id = UUID.randomUUID().toString()
        
        val request = DocumentUpdateRequest(
            id = id,
            userId = userId,
            documentId = documentId,
            documentType = IdentityDocumentType.AADHAR_CARD, // TODO: get from document
            updateType = updateType,
            currentValue = currentValue,
            newValue = newValue,
            justificationReason = reason
        )
        
        updateRequests[id] = request
        return request
    }

    fun sendVerificationCode(updateRequestId: String, method: String): Boolean {
        val request = updateRequests[updateRequestId] ?: return false
        
        val code = generateVerificationCode()
        verificationCodes[updateRequestId] = code
        
        // TODO: Send via SMS/Email based on method
        println("Verification code sent: $code (in production, send via SMS/Email)")
        
        return true
    }

    fun verifyOwnership(updateRequestId: String, verificationCode: String, verificationMethod: String): Boolean {
        val request = updateRequests[updateRequestId] ?: return false
        val storedCode = verificationCodes[updateRequestId] ?: return false
        
        if (verificationCode != storedCode) return false
        
        updateRequests[updateRequestId] = request.copy(
            legalOwnerVerification = true,
            verificationMethod = verificationMethod,
            verifiedAt = LocalDateTime.now().toString(),
            status = UpdateApprovalStatus.SUBMITTED
        )
        
        verificationCodes.remove(updateRequestId)
        return true
    }

    fun addSupportingDocuments(updateRequestId: String, documents: List<String>): Boolean {
        val request = updateRequests[updateRequestId] ?: return false
        
        updateRequests[updateRequestId] = request.copy(
            supportingDocuments = documents
        )
        
        return true
    }

    fun approveUpdate(updateRequestId: String, officerId: String, notes: String = ""): Boolean {
        val request = updateRequests[updateRequestId] ?: return false
        
        updateRequests[updateRequestId] = request.copy(
            status = UpdateApprovalStatus.APPROVED,
            approvedBy = officerId,
            approvalDate = LocalDateTime.now().toString(),
            statusHistory = request.statusHistory + UpdateStatusLog(
                timestamp = LocalDateTime.now().toString(),
                status = "APPROVED",
                description = "Update approved by officer",
                updatedBy = officerId
            )
        )
        
        return true
    }

    fun rejectUpdate(updateRequestId: String, officerId: String, reason: String): Boolean {
        val request = updateRequests[updateRequestId] ?: return false
        
        updateRequests[updateRequestId] = request.copy(
            status = UpdateApprovalStatus.REJECTED,
            rejectionReason = reason,
            statusHistory = request.statusHistory + UpdateStatusLog(
                timestamp = LocalDateTime.now().toString(),
                status = "REJECTED",
                description = reason,
                updatedBy = officerId
            )
        )
        
        return true
    }

    fun processUpdate(updateRequestId: String): Boolean {
        val request = updateRequests[updateRequestId] ?: return false
        if (request.status != UpdateApprovalStatus.APPROVED) return false
        
        updateRequests[updateRequestId] = request.copy(
            status = UpdateApprovalStatus.UPDATED,
            processingCompletedAt = LocalDateTime.now().toString()
        )
        
        return true
    }

    fun getUpdateRequest(updateRequestId: String): DocumentUpdateRequest? = updateRequests[updateRequestId]

    fun getUserUpdateRequests(userId: String): List<DocumentUpdateRequest> =
        updateRequests.values.filter { it.userId == userId }

    private fun generateVerificationCode(): String = (100000..999999).random().toString()
}

// ═════════════════════════════════════════════════════════════════════════════
// PROPERTY MANAGEMENT SERVICE
// ═════════════════════════════════════════════════════════════════════════════

class PropertyManagementService {
    private val properties = mutableMapOf<String, PropertyRecord>()
    private val propertyLaws = mutableListOf<PropertyLaw>()
    private val transfers = mutableMapOf<String, PropertyTransfer>()
    private val disputes = mutableMapOf<String, PropertyDispute>()
    private val taxCalculations = mutableMapOf<String, PropertyTaxCalculation>()
    private val encumbrances = mutableMapOf<String, Encumbrance>()

    init {
        seedPropertyLaws()
    }

    fun registerProperty(userId: String, property: PropertyRecord): PropertyRecord {
        val id = UUID.randomUUID().toString()
        
        val newProperty = property.copy(id = id, userId = userId)
        properties[id] = newProperty
        
        return newProperty
    }

    fun getProperty(propertyId: String): PropertyRecord? = properties[propertyId]

    fun getUserProperties(userId: String): List<PropertyRecord> =
        properties.values.filter { it.userId == userId || userId in it.coOwners }

    fun addCoOwner(propertyId: String, newOwnerId: String, percentage: Double): Boolean {
        val property = properties[propertyId] ?: return false
        
        properties[propertyId] = property.copy(
            coOwners = property.coOwners + newOwnerId,
            sharesPercentage = property.sharesPercentage + (newOwnerId to percentage)
        )
        
        return true
    }

    fun transferProperty(propertyId: String, fromOwner: String, toOwner: String, 
                        amount: Long?, transferDate: String): PropertyTransfer {
        val property = properties[propertyId] ?: throw IllegalArgumentException("Property not found")
        
        val transferId = UUID.randomUUID().toString()
        val transfer = PropertyTransfer(
            id = transferId,
            propertyId = propertyId,
            fromOwner = fromOwner,
            toOwner = toOwner,
            transferAmount = amount,
            transferDate = transferDate,
            transferType = if (amount != null) "SALE" else "GIFT"
        )
        
        transfers[transferId] = transfer
        
        return transfer
    }

    fun completeTransfer(transferId: String, registrationNumber: String, 
                        stampDutyPaid: Boolean, stampDutyAmount: Long? = null): Boolean {
        val transfer = transfers[transferId] ?: return false
        
        transfers[transferId] = transfer.copy(
            status = "COMPLETED",
            registrationNumber = registrationNumber,
            registrationDate = LocalDateTime.now().toString(),
            registrationCompleted = true,
            stampDutyPaid = stampDutyPaid,
            stampDutyAmount = stampDutyAmount
        )
        
        // Update property ownership
        val property = properties[transfer.propertyId]
        if (property != null) {
            properties[transfer.propertyId] = property.copy(
                userId = transfer.toOwner,
                coOwners = property.coOwners.filter { it != transfer.fromOwner }
            )
        }
        
        return true
    }

    fun addEncumbrance(propertyId: String, encumbrance: Encumbrance): Encumbrance {
        val id = UUID.randomUUID().toString()
        val newEncumbrance = encumbrance.copy(id = id)
        encumbrances[id] = newEncumbrance
        
        val property = properties[propertyId]
        if (property != null) {
            properties[propertyId] = property.copy(
                mortgageAmount = encumbrance.amount,
                mortgageHolder = encumbrance.encumbrancer
            )
        }
        
        return newEncumbrance
    }

    fun removeEncumbrance(encumbranceId: String): Boolean {
        val encumbrance = encumbrances[encumbranceId] ?: return false
        
        encumbrances[encumbranceId] = encumbrance.copy(status = "RELEASED")
        
        return true
    }

    fun fileDispute(propertyId: String, claimant: String, disputeType: String, 
                   description: String, legalBasis: String): PropertyDispute {
        val id = UUID.randomUUID().toString()
        
        val dispute = PropertyDispute(
            id = id,
            propertyId = propertyId,
            claimant = claimant,
            disputeType = disputeType,
            description = description,
            legalBasis = legalBasis
        )
        
        disputes[id] = dispute
        
        return dispute
    }

    fun calculatePropertyTax(propertyId: String, financialYear: String): PropertyTaxCalculation {
        val property = properties[propertyId] ?: throw IllegalArgumentException("Property not found")
        val propertyValue = property.currentMarketValue ?: property.purchasePrice ?: 0
        val taxRate = 0.05  // 5% for example
        
        val calculatedTax = (propertyValue * taxRate).toLong()
        
        val calculation = PropertyTaxCalculation(
            id = UUID.randomUUID().toString(),
            propertyId = propertyId,
            financialYear = financialYear,
            propertyValue = propertyValue,
            taxRate = taxRate,
            calculatedTax = calculatedTax,
            totalTax = calculatedTax,
            dueDate = LocalDate.now().plusMonths(3).toString()
        )
        
        taxCalculations[calculation.id!!] = calculation
        
        return calculation
    }

    fun getTaxCalculations(propertyId: String): List<PropertyTaxCalculation> =
        taxCalculations.values.filter { it.propertyId == propertyId }

    fun getPropertyLaws(keyword: String = ""): List<PropertyLaw> {
        return if (keyword.isEmpty()) {
            propertyLaws
        } else {
            propertyLaws.filter { law ->
                law.lawName.contains(keyword, ignoreCase = true) ||
                law.keywords.any { it.contains(keyword, ignoreCase = true) }
            }
        }
    }

    fun getPropertyLaw(lawCode: String): PropertyLaw? =
        propertyLaws.find { it.lawCode == lawCode }

    fun getEncumbrances(propertyId: String): List<Encumbrance> =
        encumbrances.values.filter { it.propertyId == propertyId && it.status == "ACTIVE" }

    fun getDisputes(propertyId: String): List<PropertyDispute> =
        disputes.values.filter { it.propertyId == propertyId }

    private fun seedPropertyLaws() {
        propertyLaws.addAll(listOf(
            PropertyLaw(
                lawName = "Transfer of Property Act, 1882",
                lawCode = "TP_ACT_1882",
                description = "Act governing the transfer of property in India",
                provisions = listOf(
                    LawProvision(
                        sectionNumber = "Section 2(d)",
                        title = "Property Definition",
                        description = "Property includes movable and immovable property"
                    ),
                    LawProvision(
                        sectionNumber = "Section 5",
                        title = "Transfer of Property",
                        description = "An act by which a living person conveys property"
                    )
                ),
                keywords = listOf("sale", "gift", "lease", "transfer", "property")
            ),
            PropertyLaw(
                lawName = "Registration Act, 1908",
                lawCode = "REG_ACT_1908",
                description = "Act regulating the registration of deeds in India",
                provisions = listOf(
                    LawProvision(
                        sectionNumber = "Section 17",
                        title = "Registrable Documents",
                        description = "Documents that must be registered"
                    ),
                    LawProvision(
                        sectionNumber = "Section 58",
                        title = "Certified Copy",
                        description = "Official copy of registered document"
                    )
                ),
                keywords = listOf("registration", "deed", "document", "certified copy")
            ),
            PropertyLaw(
                lawName = "Land Acquisition Act, 1894",
                lawCode = "LA_ACT_1894",
                description = "Act regulating acquisition of land for public purpose",
                provisions = listOf(
                    LawProvision(
                        sectionNumber = "Section 4",
                        title = "Declaration of Public Purpose",
                        description = "Process for declaring land needed for public purpose"
                    )
                ),
                keywords = listOf("acquisition", "public purpose", "compensation")
            ),
            PropertyLaw(
                lawName = "Easement Act, 1882",
                lawCode = "EASE_ACT_1882",
                description = "Act defining rights of way and easements",
                provisions = listOf(
                    LawProvision(
                        sectionNumber = "Section 3",
                        title = "Easement Defined",
                        description = "Right to use another's property for specific purposes"
                    )
                ),
                keywords = listOf("easement", "right of way", "access")
            ),
            PropertyLaw(
                lawName = "Hindu Succession Act, 1956",
                lawCode = "HS_ACT_1956",
                description = "Act governing succession of property in Hindu families",
                provisions = listOf(
                    LawProvision(
                        sectionNumber = "Section 6",
                        title = "Coparcenary Property",
                        description = "Property held in common by family members"
                    )
                ),
                keywords = listOf("inheritance", "succession", "coparcenary", "hindu")
            )
        ))
    }
}
