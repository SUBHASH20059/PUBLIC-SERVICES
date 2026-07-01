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
import org.psei.services.SecureDocumentVaultService
import java.util.*

private val documentService = SecureDocumentVaultService()

fun Routing.registerDocumentAndCertificateRoutes() {
    route("/citizen-vault") {
        authenticate("jwt") {

            // ═══════════════════════════════════════════════════════════════
            // 1. IDENTITY DOCUMENTS MANAGEMENT
            // ═══════════════════════════════════════════════════════════════

            route("/identity-documents") {

                // List all identity documents
                get {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val documents = documentService.listIdentityDocuments(user.userId)
                    call.respond(mapOf(
                        "count" to documents.size,
                        "documents" to documents.map { doc ->
                            mapOf(
                                "id" to doc.id,
                                "type" to doc.documentType.name,
                                "number" to doc.documentNumber,
                                "status" to doc.verificationStatus.name,
                                "uploadedAt" to doc.uploadedAt
                            )
                        }
                    ))
                }

                // Upload identity document
                post {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val req = runCatching { call.receive<IdentityDocumentUploadRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    // Validate document type exists
                    val docType = runCatching { IdentityDocumentType.valueOf(req.documentType) }.getOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid document type"))

                    val document = IdentityDocument(
                        userId = user.userId,
                        documentType = docType,
                        documentNumber = req.documentNumber,
                        issueDate = req.issueDate,
                        expiryDate = req.expiryDate,
                        issuingAuthority = req.issuingAuthority,
                        issuer = req.issuer,
                        state = req.state,
                        holderName = req.holderName,
                        dateOfBirth = req.dateOfBirth,
                        address = req.address
                    )

                    // TODO: In production, receive actual file upload and encrypt
                    val uploaded = documentService.uploadIdentityDocument(
                        user.userId,
                        document,
                        req.documentNumber.toByteArray()
                    )

                    call.respond(HttpStatusCode.Created, mapOf(
                        "id" to uploaded.id,
                        "documentType" to uploaded.documentType.name,
                        "status" to uploaded.verificationStatus.name,
                        "message" to "Document uploaded. Awaiting government verification."
                    ))
                }

                // Get specific identity document
                get("/{documentId}") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val documentId = call.parameters["documentId"] ?: return@get

                    val document = documentService.getIdentityDocument(
                        documentId,
                        user.userId,
                        user.role.name
                    ) ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Document not found"))

                    call.respond(document)
                }

                // Download encrypted document (verified access only)
                get("/{documentId}/download") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val documentId = call.parameters["documentId"] ?: return@get

                    val document = documentService.getIdentityDocument(
                        documentId,
                        user.userId,
                        user.role.name
                    ) ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Document not found"))

                    if (document.verificationStatus != VerificationStatus.VERIFIED) {
                        return@get call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Document not verified yet")
                        )
                    }

                    call.respond(mapOf(
                        "downloadUrl" to document.encryptedFileUrl,
                        "encryption" to document.encryptionAlgorithm,
                        "message" to "Document encrypted with AES-256-GCM. Decryption keys in your vault."
                    ))
                }

                // OFFICER+: Verify identity document
                put("/{documentId}/verify") {
                    val user = call.requireRole(Role.OFFICER) ?: return@put
                    val documentId = call.parameters["documentId"] ?: return@put
                    val req = runCatching { call.receive<DocumentVerificationRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@put
                    }

                    val status = VerificationStatus.valueOf(req.status)
                    val success = documentService.verifyIdentityDocument(
                        documentId,
                        user.userId,
                        status,
                        req.notes
                    )

                    if (success) {
                        call.respond(mapOf(
                            "documentId" to documentId,
                            "status" to req.status,
                            "verifiedBy" to user.email,
                            "verifiedAt" to java.time.LocalDateTime.now().toString()
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Document not found"))
                    }
                }

                // Get access logs for a document
                get("/{documentId}/access-logs") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val documentId = call.parameters["documentId"] ?: return@get

                    val logs = documentService.getDocumentAccessLogs(documentId)
                    call.respond(mapOf(
                        "documentId" to documentId,
                        "totalAccesses" to logs.size,
                        "logs" to logs
                    ))
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // 2. GOVERNMENT CERTIFICATES
            // ═══════════════════════════════════════════════════════════════

            route("/certificates") {

                // List all certificates
                get {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val certificates = documentService.listGovernmentCertificates(user.userId)
                    call.respond(mapOf(
                        "count" to certificates.size,
                        "certificates" to certificates.map { cert ->
                            mapOf(
                                "id" to cert.id,
                                "type" to cert.certificateType.name,
                                "number" to cert.certificateNumber,
                                "issuer" to cert.issuingDepartment,
                                "status" to cert.status.name,
                                "expiryDate" to cert.expiryDate
                            )
                        }
                    ))
                }

                // Register government certificate (with signature verification)
                post {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val req = runCatching { call.receive<GovernmentCertificateRegistrationRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val certType = runCatching { GovernmentCertificateType.valueOf(req.certificateType) }.getOrNull()
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid certificate type"))

                    val certificate = GovernmentCertificate(
                        userId = user.userId,
                        certificateType = certType,
                        certificateNumber = req.certificateNumber,
                        issueDate = req.issueDate,
                        expiryDate = req.expiryDate,
                        issuingDepartment = req.issuingDepartment,
                        issuingOffice = req.issuingOffice,
                        holderName = req.holderName,
                        certificateDetails = req.details,
                        fileHash = req.fileHash,
                        encryptedCertificateUrl = "vault://certificates/${UUID.randomUUID()}/encrypted",
                        digitalSignature = req.digitalSignature,
                        issuerPublicKeyFingerprint = req.issuerPublicKeyFingerprint,
                        verificationMethod = req.verificationMethod,
                        qrCodeUrl = req.qrCodeUrl
                    )

                    val registered = documentService.registerGovernmentCertificate(user.userId, certificate)

                    call.respond(HttpStatusCode.Created, mapOf(
                        "id" to registered.id,
                        "type" to registered.certificateType.name,
                        "signatureVerified" to registered.signatureVerified,
                        "status" to registered.status.name,
                        "message" to "Certificate registered and queued for verification with issuing authority"
                    ))
                }

                // Get specific certificate
                get("/{certificateId}") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val certificateId = call.parameters["certificateId"] ?: return@get

                    val certificate = documentService.getGovernmentCertificate(
                        certificateId,
                        user.userId,
                        user.role.name
                    ) ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Certificate not found"))

                    call.respond(certificate)
                }

                // Verify certificate authenticity with issuer
                post("/{certificateId}/verify") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val certificateId = call.parameters["certificateId"] ?: return@post

                    val verification = documentService.verifyCertificateWithIssuer(certificateId, user.userId)

                    call.respond(HttpStatusCode.Created, mapOf(
                        "verificationId" to verification.id,
                        "result" to verification.result.name,
                        "method" to verification.verificationMethod,
                        "verifiedAt" to verification.resultDate,
                        "message" to when (verification.result) {
                            VerificationResult.AUTHENTIC -> "Certificate is authentic and verified"
                            VerificationResult.FORGED -> "Certificate is fraudulent - reported to authorities"
                            VerificationResult.SUSPICIOUS -> "Certificate appears invalid - manual review recommended"
                            VerificationResult.NOT_FOUND -> "Certificate not found in government records"
                            else -> "Verification in progress"
                        }
                    ))
                }

                // Check verification status
                get("/{certificateId}/verification-status") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val certificateId = call.parameters["certificateId"] ?: return@get

                    val status = documentService.getVerificationStatus(certificateId)
                    call.respond(mapOf(
                        "certificateId" to certificateId,
                        "status" to status.name,
                        "isAuthentic" to (status == VerificationResult.AUTHENTIC)
                    ))
                }

                // Share certificate with organization/person
                post("/{certificateId}/share") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val certificateId = call.parameters["certificateId"] ?: return@post
                    val req = runCatching { call.receive<CertificateSharingRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val accessLevel = AccessLevel.valueOf(req.accessLevel ?: "VIEW_ONLY")
                    val share = documentService.shareCertificate(
                        certificateId,
                        user.userId,
                        req.sharedWith,
                        accessLevel,
                        req.purpose
                    )

                    call.respond(HttpStatusCode.Created, mapOf(
                        "shareId" to share.id,
                        "certificateId" to certificateId,
                        "sharedWith" to share.sharedWith,
                        "accessLevel" to share.accessLevel.name,
                        "sharingDate" to share.sharingDate,
                        "expiryDate" to share.expiryDate,
                        "message" to "Certificate shared securely"
                    ))
                }

                // Get shared certificates (for recipient)
                get("/shared-with-me") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val shared = documentService.getSharedCertificates(user.email)
                    call.respond(mapOf(
                        "count" to shared.size,
                        "shared" to shared
                    ))
                }

                // View certificate access logs
                get("/{certificateId}/access-logs") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val certificateId = call.parameters["certificateId"] ?: return@get

                    val logs = documentService.getCertificateAccessLogs(certificateId)
                    call.respond(mapOf(
                        "certificateId" to certificateId,
                        "totalAccesses" to logs.size,
                        "logs" to logs
                    ))
                }

                // Report duplicate/fraudulent certificate
                post("/{certificateId}/report-duplicate") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val certificateId = call.parameters["certificateId"] ?: return@post

                    val success = documentService.reportDuplicateCertificate(certificateId, user.userId)
                    if (success) {
                        call.respond(mapOf(
                            "message" to "Fraudulent certificate reported to authorities",
                            "caseId" to UUID.randomUUID().toString()
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Certificate not found"))
                    }
                }

                // ADMIN: Revoke certificate (if found to be fraudulent)
                put("/{certificateId}/revoke") {
                    val user = call.requireRole(Role.ADMIN) ?: return@put
                    val certificateId = call.parameters["certificateId"] ?: return@put
                    val req = runCatching { call.receive<CertificateRevocationRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@put
                    }

                    val success = documentService.revokeCertificate(certificateId, req.reason)
                    if (success) {
                        call.respond(mapOf(
                            "certificateId" to certificateId,
                            "status" to "REVOKED",
                            "reason" to req.reason,
                            "revokedAt" to java.time.LocalDateTime.now().toString()
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Certificate not found"))
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // 3. DOCUMENT STATISTICS & COMPLIANCE
            // ═══════════════════════════════════════════════════════════════

            route("/statistics") {

                get {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val stats = documentService.getDocumentStatistics(user.userId)
                    call.respond(stats)
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // 4. SECURITY AUDIT LOGS (ADMIN+)
            // ═══════════════════════════════════════════════════════════════

            route("/audit-logs") {

                get {
                    val user = call.requireRole(Role.ADMIN) ?: return@get
                    val userId = call.request.queryParameters["userId"]
                    val audits = documentService.getSecurityAudits(userId, limit = 100)
                    call.respond(mapOf(
                        "count" to audits.size,
                        "logs" to audits
                    ))
                }
            }
        }
    }
}

// ─── REQUEST/RESPONSE DTOs ──────────────────────────────────────────────────

@Serializable
data class IdentityDocumentUploadRequest(
    val documentType: String,
    val documentNumber: String,
    val issueDate: String,
    val expiryDate: String? = null,
    val issuingAuthority: String,
    val issuer: String,
    val state: String? = null,
    val holderName: String,
    val dateOfBirth: String? = null,
    val address: String? = null
)

@Serializable
data class DocumentVerificationRequest(
    val status: String,
    val notes: String? = null
)

@Serializable
data class GovernmentCertificateRegistrationRequest(
    val certificateType: String,
    val certificateNumber: String,
    val issueDate: String,
    val expiryDate: String? = null,
    val issuingDepartment: String,
    val issuingOffice: String,
    val holderName: String,
    val details: Map<String, String>,
    val fileHash: String,
    val digitalSignature: String,
    val issuerPublicKeyFingerprint: String,
    val verificationMethod: String = "ISSUER_API",
    val qrCodeUrl: String? = null
)

@Serializable
data class CertificateSharingRequest(
    val sharedWith: String,
    val accessLevel: String? = "VIEW_ONLY",
    val purpose: String? = null
)

@Serializable
data class CertificateRevocationRequest(
    val reason: String
)
