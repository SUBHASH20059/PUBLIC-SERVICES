package org.psei.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.psei.auth.*
import org.psei.models.*
import org.psei.services.*
import java.util.*

private val idApplicationService = IDApplicationService()
private val documentUpdateService = DocumentUpdateService()
private val propertyService = PropertyManagementService()

fun Routing.registerIDApplicationAndPropertyRoutes() {
    route("/citizen-services") {
        authenticate("jwt") {

            // ═══════════════════════════════════════════════════════════════
            // 1. ID APPLICATION WORKFLOW
            // ═══════════════════════════════════════════════════════════════

            route("/id-applications") {

                // List my applications
                get {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val applications = idApplicationService.getApplications(user.userId)
                    call.respond(mapOf(
                        "count" to applications.size,
                        "applications" to applications.map { app ->
                            mapOf(
                                "id" to app.id,
                                "type" to app.applicationType.name,
                                "status" to app.status.name,
                                "applicationDate" to app.applicationDate,
                                "idNumber" to (app.generatedIDNumber ?: "Not yet issued")
                            )
                        }
                    ))
                }

                // Create new ID application
                post {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val req = runCatching { call.receive<IDApplicationCreateRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val applicationType = runCatching { IDApplicationType.valueOf(req.applicationType) }.getOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID type"))

                    val application = idApplicationService.createApplication(
                        user.userId,
                        applicationType,
                        mapOf(
                            "applicantName" to req.applicantName,
                            "applicantEmail" to user.email,
                            "applicantPhone" to req.applicantPhone,
                            "dateOfBirth" to req.dateOfBirth,
                            "gender" to req.gender,
                            "address" to req.address,
                            "city" to req.city,
                            "state" to req.state,
                            "pinCode" to req.pinCode
                        ) + mapOf(
                            "fatherName" to (req.fatherName ?: ""),
                            "motherName" to (req.motherName ?: "")
                        )
                    )

                    call.respond(HttpStatusCode.Created, mapOf(
                        "applicationId" to application.id,
                        "type" to application.applicationType.name,
                        "status" to application.status.name,
                        "message" to "Application created. Next: upload supporting documents"
                    ))
                }

                // Submit application
                post("/{applicationId}/submit") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val appId = call.parameters["applicationId"] ?: return@post
                    val req = runCatching { call.receive<SubmitApplicationRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val success = idApplicationService.submitApplication(appId, user.userId, req.documents)
                    if (success) {
                        call.respond(mapOf(
                            "message" to "Application submitted successfully",
                            "nextStep" to "Biometric collection will be scheduled"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Application not found"))
                    }
                }

                // Get application details
                get("/{applicationId}") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val appId = call.parameters["applicationId"] ?: return@get

                    val app = idApplicationService.getApplication(appId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))

                    if (app.userId != user.userId && user.role != Role.OFFICER) {
                        return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                    }

                    call.respond(app)
                }

                // View application timeline
                get("/{applicationId}/timeline") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val appId = call.parameters["applicationId"] ?: return@get

                    val timeline = idApplicationService.getTimeline(appId)
                    call.respond(mapOf(
                        "applicationId" to appId,
                        "events" to timeline
                    ))
                }

                // OFFICER: Verify application
                put("/{applicationId}/verify") {
                    val user = call.requireRole(Role.OFFICER) ?: return@put
                    val appId = call.parameters["applicationId"] ?: return@put
                    val req = runCatching { call.receive<VerifyApplicationRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@put
                    }

                    val success = idApplicationService.verifyApplication(appId, user.userId, req.approved, req.notes ?: "")
                    if (success) {
                        call.respond(mapOf(
                            "status" to if (req.approved) "APPROVED" else "REJECTED",
                            "message" to if (req.approved) "Application approved. Biometric collection scheduled." 
                                         else "Application rejected: ${req.notes}"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Application not found"))
                    }
                }

                // Collect biometric
                post("/{applicationId}/biometric") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val appId = call.parameters["applicationId"] ?: return@post

                    val success = idApplicationService.collectBiometric(appId, call.request.queryParameters["center"] ?: "Default Center")
                    if (success) {
                        call.respond(mapOf("message" to "Biometric data collected successfully"))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Cannot collect biometric at this stage"))
                    }
                }

                // OFFICER: Issue ID
                post("/{applicationId}/issue-id") {
                    val user = call.requireRole(Role.OFFICER) ?: return@post
                    val appId = call.parameters["applicationId"] ?: return@post
                    val req = runCatching { call.receive<IssueIDRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val success = idApplicationService.issueID(appId, req.idNumber, req.expiryDate)
                    if (success) {
                        call.respond(mapOf(
                            "message" to "ID issued successfully",
                            "idNumber" to req.idNumber,
                            "collectAt" to req.collectionCenter
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Cannot issue ID at this stage"))
                    }
                }

                // Mark ID as collected
                post("/{applicationId}/mark-collected") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val appId = call.parameters["applicationId"] ?: return@post

                    val success = idApplicationService.markCollected(appId)
                    if (success) {
                        call.respond(mapOf("message" to "ID marked as collected"))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid operation"))
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // 2. DOCUMENT UPDATE REQUESTS (Legal Owner Changes)
            // ═══════════════════════════════════════════════════════════════

            route("/document-updates") {

                // Request document update
                post {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val req = runCatching { call.receive<DocumentUpdateRequestCreate>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val updateType = runCatching { DocumentUpdateType.valueOf(req.updateType) }.getOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid update type"))

                    val updateRequest = documentUpdateService.requestUpdate(
                        user.userId,
                        req.documentId,
                        updateType,
                        req.currentValue,
                        req.newValue,
                        req.justificationReason
                    )

                    call.respond(HttpStatusCode.Created, mapOf(
                        "requestId" to updateRequest.id,
                        "status" to updateRequest.status.name,
                        "message" to "Update request created. Next: verify ownership"
                    ))
                }

                // Get my update requests
                get {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val requests = documentUpdateService.getUserUpdateRequests(user.userId)
                    call.respond(mapOf(
                        "count" to requests.size,
                        "requests" to requests
                    ))
                }

                // Send verification code
                post("/{requestId}/send-verification") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val requestId = call.parameters["requestId"] ?: return@post
                    val method = call.request.queryParameters["method"] ?: "SMS"

                    val success = documentUpdateService.sendVerificationCode(requestId, method)
                    if (success) {
                        call.respond(mapOf(
                            "message" to "Verification code sent",
                            "method" to method
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Request not found"))
                    }
                }

                // Verify ownership with code
                post("/{requestId}/verify-ownership") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val requestId = call.parameters["requestId"] ?: return@post
                    val req = runCatching { call.receive<VerifyOwnershipRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val success = documentUpdateService.verifyOwnership(requestId, req.verificationCode, req.method)
                    if (success) {
                        call.respond(mapOf("message" to "Ownership verified. Please upload supporting documents."))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Verification code invalid"))
                    }
                }

                // Upload supporting documents
                post("/{requestId}/upload-documents") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val requestId = call.parameters["requestId"] ?: return@post
                    val req = runCatching { call.receive<UploadDocumentsRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val success = documentUpdateService.addSupportingDocuments(requestId, req.documents)
                    if (success) {
                        call.respond(mapOf(
                            "message" to "Documents uploaded",
                            "nextStep" to "Application submitted for review"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Request not found"))
                    }
                }

                // OFFICER: Approve update
                put("/{requestId}/approve") {
                    val user = call.requireRole(Role.OFFICER) ?: return@put
                    val requestId = call.parameters["requestId"] ?: return@put
                    val req = runCatching { call.receive<ApproveUpdateRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@put
                    }

                    val success = documentUpdateService.approveUpdate(requestId, user.userId, req.notes ?: "")
                    if (success) {
                        call.respond(mapOf("message" to "Update approved. Document will be processed."))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Request not found"))
                    }
                }

                // OFFICER: Reject update
                put("/{requestId}/reject") {
                    val user = call.requireRole(Role.OFFICER) ?: return@put
                    val requestId = call.parameters["requestId"] ?: return@put
                    val req = runCatching { call.receive<RejectUpdateRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@put
                    }

                    val success = documentUpdateService.rejectUpdate(requestId, user.userId, req.reason)
                    if (success) {
                        call.respond(mapOf("message" to "Update rejected: ${req.reason}"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Request not found"))
                    }
                }

                // ADMIN: Process update
                post("/{requestId}/process") {
                    val user = call.requireRole(Role.ADMIN) ?: return@post
                    val requestId = call.parameters["requestId"] ?: return@post

                    val success = documentUpdateService.processUpdate(requestId)
                    if (success) {
                        call.respond(mapOf("message" to "Update processed. Document has been updated."))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Cannot process at this stage"))
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // 3. PROPERTY MANAGEMENT & PROPERTY LAWS
            // ═══════════════════════════════════════════════════════════════

            route("/property") {

                // Register property
                post("/register") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val req = runCatching { call.receive<PropertyRegisterRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val property = PropertyRecord(
                        userId = user.userId,
                        propertyName = req.propertyName,
                        propertyType = PropertyType.valueOf(req.propertyType),
                        ownershipType = PropertyOwnershipType.valueOf(req.ownershipType),
                        address = req.address,
                        city = req.city,
                        state = req.state,
                        pinCode = req.pinCode,
                        surveyNumber = req.surveyNumber,
                        areaInSqFt = req.areaInSqFt,
                        areaInSqMeter = req.areaInSqMeter,
                        owners = listOf(PropertyOwner(
                            userId = user.userId,
                            ownerName = req.ownerName,
                            ownerEmail = user.email,
                            ownershipPercentage = 100.0,
                            ownershipType = req.ownershipType
                        ))
                    )

                    val registered = propertyService.registerProperty(user.userId, property)
                    call.respond(HttpStatusCode.Created, mapOf(
                        "propertyId" to registered.id,
                        "message" to "Property registered"
                    ))
                }

                // Get my properties
                get {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val properties = propertyService.getUserProperties(user.userId)
                    call.respond(mapOf(
                        "count" to properties.size,
                        "properties" to properties
                    ))
                }

                // Get property details
                get("/{propertyId}") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val propId = call.parameters["propertyId"] ?: return@get

                    val property = propertyService.getProperty(propId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Property not found"))

                    call.respond(property)
                }

                // Add co-owner
                post("/{propertyId}/add-co-owner") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val propId = call.parameters["propertyId"] ?: return@post
                    val req = runCatching { call.receive<AddCoOwnerRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val success = propertyService.addCoOwner(propId, req.coOwnerId, req.percentage)
                    if (success) {
                        call.respond(mapOf("message" to "Co-owner added"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Property not found"))
                    }
                }

                // Calculate property tax
                get("/{propertyId}/tax/{financialYear}") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val propId = call.parameters["propertyId"] ?: return@get
                    val year = call.parameters["financialYear"] ?: return@get

                    val calculation = propertyService.calculatePropertyTax(propId, year)
                    call.respond(calculation)
                }

                // Transfer property
                post("/{propertyId}/transfer") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val propId = call.parameters["propertyId"] ?: return@post
                    val req = runCatching { call.receive<PropertyTransferRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val transfer = propertyService.transferProperty(propId, user.userId, req.toOwner, req.amount, req.transferDate)
                    call.respond(HttpStatusCode.Created, mapOf(
                        "transferId" to transfer.id,
                        "status" to transfer.status,
                        "message" to "Transfer initiated"
                    ))
                }

                // File dispute
                post("/{propertyId}/file-dispute") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val propId = call.parameters["propertyId"] ?: return@post
                    val req = runCatching { call.receive<FileDisputeRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val dispute = propertyService.fileDispute(propId, user.userId, req.disputeType, req.description, req.legalBasis)
                    call.respond(HttpStatusCode.Created, mapOf(
                        "disputeId" to dispute.id,
                        "message" to "Dispute filed"
                    ))
                }

                // Get property laws
                get("/laws/search") {
                    val keyword = call.request.queryParameters["q"] ?: ""
                    val laws = propertyService.getPropertyLaws(keyword)
                    call.respond(mapOf(
                        "count" to laws.size,
                        "laws" to laws
                    ))
                }

                // Get specific law
                get("/laws/{lawCode}") {
                    val lawCode = call.parameters["lawCode"] ?: return@get
                    val law = propertyService.getPropertyLaw(lawCode)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Law not found"))

                    call.respond(law)
                }
            }
        }
    }
}

// ─── REQUEST/RESPONSE DTOs ──────────────────────────────────────────────────

@Serializable data class IDApplicationCreateRequest(
    val applicationType: String,
    val applicantName: String,
    val applicantPhone: String,
    val dateOfBirth: String,
    val gender: String,
    val fatherName: String? = null,
    val motherName: String? = null,
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String
)

@Serializable data class SubmitApplicationRequest(
    val documents: List<String>
)

@Serializable data class VerifyApplicationRequest(
    val approved: Boolean,
    val notes: String? = null
)

@Serializable data class IssueIDRequest(
    val idNumber: String,
    val expiryDate: String,
    val collectionCenter: String? = null
)

@Serializable data class DocumentUpdateRequestCreate(
    val documentId: String,
    val updateType: String,
    val currentValue: String,
    val newValue: String,
    val justificationReason: String
)

@Serializable data class VerifyOwnershipRequest(
    val verificationCode: String,
    val method: String
)

@Serializable data class UploadDocumentsRequest(
    val documents: List<String>
)

@Serializable data class ApproveUpdateRequest(
    val notes: String? = null
)

@Serializable data class RejectUpdateRequest(
    val reason: String
)

@Serializable data class PropertyRegisterRequest(
    val propertyName: String,
    val propertyType: String,
    val ownershipType: String,
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val surveyNumber: String,
    val areaInSqFt: Double,
    val areaInSqMeter: Double,
    val ownerName: String
)

@Serializable data class AddCoOwnerRequest(
    val coOwnerId: String,
    val percentage: Double
)

@Serializable data class PropertyTransferRequest(
    val toOwner: String,
    val amount: Long? = null,
    val transferDate: String
)

@Serializable data class FileDisputeRequest(
    val disputeType: String,
    val description: String,
    val legalBasis: String
)
