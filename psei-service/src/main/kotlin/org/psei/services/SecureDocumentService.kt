package org.psei.services

import org.psei.models.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

// ═════════════════════════════════════════════════════════════════════════════
// SECURE DOCUMENT VAULT SERVICE
// ═════════════════════════════════════════════════════════════════════════════

class SecureDocumentVaultService {
    private val vaults = mutableMapOf<String, SecureDocumentVault>()
    private val identityDocs = mutableMapOf<String, IdentityDocument>()
    private val govCerts = mutableMapOf<String, GovernmentCertificate>()
    private val encryptionKeys = mutableMapOf<String, EncryptionKey>()
    private val accessLogs = mutableMapOf<String, MutableList<DocumentAccessLog>>()
    private val certAccessLogs = mutableMapOf<String, MutableList<CertificateAccessLog>>()
    private val verifications = mutableMapOf<String, CertificateVerification>()
    private val shares = mutableMapOf<String, CertificateShare>()
    private val complianceRecords = mutableListOf<DocumentComplianceRecord>()
    private val securityAudits = mutableListOf<SecurityAudit>()

    // ─── Create/Manage Vaults ────────────────────────────────────────────────

    fun createVault(userId: String, vaultName: String, description: String? = null): SecureDocumentVault {
        val id = UUID.randomUUID().toString()
        val vault = SecureDocumentVault(
            id = id,
            userId = userId,
            vaultName = vaultName,
            description = description
        )
        vaults[id] = vault
        logSecurityAudit(null, "VAULT_CREATED", mapOf("vaultId" to id, "userId" to userId), "INFO")
        return vault
    }

    fun getVault(vaultId: String, userId: String): SecureDocumentVault? {
        val vault = vaults[vaultId] ?: return null
        if (vault.userId != userId) return null
        return vault
    }

    // ─── Identity Document Management ────────────────────────────────────────

    fun uploadIdentityDocument(
        userId: String,
        document: IdentityDocument,
        fileContent: ByteArray
    ): IdentityDocument {
        val id = UUID.randomUUID().toString()
        val fileHash = computeSHA256(fileContent)
        
        // Generate encryption key for this document
        val encKey = generateEncryptionKey(userId)
        val encryptedFile = encryptDocument(fileContent, encKey)
        
        val newDoc = document.copy(
            id = id,
            fileHash = fileHash,
            encryptedFileUrl = "vault://documents/$id/encrypted",
            verificationStatus = VerificationStatus.PENDING,
            uploadedAt = LocalDateTime.now().toString()
        )
        
        identityDocs[id] = newDoc
        accessLogs[id] = mutableListOf()
        
        logSecurityAudit(
            userId = userId,
            auditType = "DOCUMENT_UPLOAD",
            details = mapOf(
                "documentId" to id,
                "documentType" to document.documentType.name,
                "fileHash" to fileHash
            ),
            severity = "INFO"
        )
        
        return newDoc
    }

    fun getIdentityDocument(documentId: String, userId: String, accessorRole: String): IdentityDocument? {
        val doc = identityDocs[documentId] ?: return null
        
        // Access control: owner or government officer
        if (doc.userId != userId && accessorRole != "OFFICER" && accessorRole != "ADMIN") {
            logSecurityAudit(
                userId = userId,
                auditType = "UNAUTHORIZED_ACCESS_ATTEMPT",
                details = mapOf("documentId" to documentId),
                severity = "WARNING"
            )
            return null
        }
        
        // Log access
        logDocumentAccess(
            documentId = documentId,
            accessorId = userId,
            accessorRole = accessorRole,
            action = "VIEW"
        )
        
        return doc
    }

    fun verifyIdentityDocument(
        documentId: String,
        verifiedBy: String,
        status: VerificationStatus,
        notes: String? = null
    ): Boolean {
        val doc = identityDocs[documentId] ?: return false
        
        identityDocs[documentId] = doc.copy(
            verificationStatus = status,
            verificationDate = LocalDateTime.now().toString(),
            verifiedBy = verifiedBy,
            verificationNotes = notes
        )
        
        logSecurityAudit(
            userId = verifiedBy,
            auditType = "DOCUMENT_VERIFIED",
            details = mapOf(
                "documentId" to documentId,
                "status" to status.name,
                "notes" to (notes ?: "")
            ),
            severity = "INFO"
        )
        
        return true
    }

    fun listIdentityDocuments(userId: String): List<IdentityDocument> =
        identityDocs.values.filter { it.userId == userId }

    // ─── Government Certificate Management ────────────────────────────────────

    fun registerGovernmentCertificate(
        userId: String,
        certificate: GovernmentCertificate
    ): GovernmentCertificate {
        val id = UUID.randomUUID().toString()
        
        // Validate digital signature
        val signatureValid = verifyGovernmentSignature(
            certificate.digitalSignature,
            certificate.issuerPublicKeyFingerprint,
            certificate.fileHash
        )
        
        val newCert = certificate.copy(
            id = id,
            signatureVerified = signatureValid
        )
        
        govCerts[id] = newCert
        certAccessLogs[id] = mutableListOf()
        
        logSecurityAudit(
            userId = userId,
            auditType = "CERTIFICATE_REGISTERED",
            details = mapOf(
                "certificateId" to id,
                "type" to certificate.certificateType.name,
                "number" to certificate.certificateNumber,
                "signatureValid" to signatureValid
            ),
            severity = "INFO"
        )
        
        return newCert
    }

    fun getGovernmentCertificate(
        certificateId: String,
        userId: String,
        accessorRole: String
    ): GovernmentCertificate? {
        val cert = govCerts[certificateId] ?: return null
        
        // Access control
        if (cert.userId != userId && accessorRole !in listOf("OFFICER", "ADMIN")) {
            logSecurityAudit(
                userId = userId,
                auditType = "UNAUTHORIZED_CERT_ACCESS",
                details = mapOf("certificateId" to certificateId),
                severity = "WARNING"
            )
            return null
        }
        
        logCertificateAccess(
            certificateId = certificateId,
            accessorId = userId,
            accessorRole = accessorRole,
            action = "VIEW"
        )
        
        return cert
    }

    fun listGovernmentCertificates(userId: String): List<GovernmentCertificate> =
        govCerts.values.filter { it.userId == userId && it.status == CertificateStatus.ACTIVE }

    // ─── Certificate Verification (Against Government Records) ─────────────────

    fun verifyCertificateWithIssuer(
        certificateId: String,
        userId: String
    ): CertificateVerification {
        val cert = govCerts[certificateId] ?: return CertificateVerification(
            certificateId = certificateId,
            requestedBy = userId,
            result = VerificationResult.NOT_FOUND
        )
        
        val verification = CertificateVerification(
            id = UUID.randomUUID().toString(),
            certificateId = certificateId,
            requestedBy = userId,
            verificationMethod = "ISSUER_API",
            result = VerificationResult.PENDING
        )
        
        verifications[verification.id!!] = verification
        
        // TODO: Call government issuer's verification API
        // For now, mark as authentic if signature is valid
        val result = if (cert.signatureVerified) {
            VerificationResult.AUTHENTIC
        } else {
            VerificationResult.SUSPICIOUS
        }
        
        verifications[verification.id!!] = verification.copy(
            result = result,
            resultDate = LocalDateTime.now().toString(),
            verifiedBy = "SYSTEM"
        )
        
        logCertificateAccess(
            certificateId = certificateId,
            accessorId = userId,
            accessorRole = "CITIZEN",
            action = "VERIFY",
            verificationResult = result.name
        )
        
        return verifications[verification.id!!]!!
    }

    fun getVerificationStatus(certificateId: String): VerificationResult {
        val cert = govCerts[certificateId] ?: return VerificationResult.NOT_FOUND
        
        return when {
            cert.status == CertificateStatus.REVOKED -> VerificationResult.FORGED
            cert.status == CertificateStatus.DUPLICATE -> VerificationResult.FORGED
            cert.signatureVerified -> VerificationResult.AUTHENTIC
            else -> VerificationResult.SUSPICIOUS
        }
    }

    // ─── Certificate Sharing ──────────────────────────────────────────────────

    fun shareCertificate(
        certificateId: String,
        ownerId: String,
        sharedWith: String,
        accessLevel: AccessLevel = AccessLevel.VIEW_ONLY,
        purpose: String? = null
    ): CertificateShare {
        val cert = govCerts[certificateId] ?: throw IllegalArgumentException("Certificate not found")
        if (cert.userId != ownerId) throw IllegalArgumentException("Not certificate owner")
        
        val share = CertificateShare(
            id = UUID.randomUUID().toString(),
            certificateId = certificateId,
            ownerId = ownerId,
            sharedWith = sharedWith,
            accessLevel = accessLevel,
            purpose = purpose
        )
        
        shares[share.id!!] = share
        
        logSecurityAudit(
            userId = ownerId,
            auditType = "CERTIFICATE_SHARED",
            details = mapOf(
                "certificateId" to certificateId,
                "sharedWith" to sharedWith,
                "accessLevel" to accessLevel.name,
                "purpose" to (purpose ?: "")
            ),
            severity = "INFO"
        )
        
        return share
    }

    fun getSharedCertificates(recipientEmail: String): List<CertificateShare> =
        shares.values.filter { it.sharedWith == recipientEmail }

    // ─── Encryption & Security ───────────────────────────────────────────────

    private fun generateEncryptionKey(userId: String): ByteArray {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val key = keyGen.generateKey()
        
        val encKey = EncryptionKey(
            id = UUID.randomUUID().toString(),
            userId = userId,
            keyVersion = (encryptionKeys.values.maxOfOrNull { it.keyVersion } ?: 0) + 1,
            isActive = true
        )
        
        encryptionKeys[encKey.id!!] = encKey
        return key.encoded
    }

    private fun encryptDocument(data: ByteArray, key: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key, 0, key.size, "AES")
        
        // Generate random IV
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec)
        
        val cipherText = cipher.doFinal(data)
        return iv + cipherText
    }

    fun decryptDocument(encryptedData: ByteArray, key: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key, 0, key.size, "AES")
        
        val iv = encryptedData.copyOfRange(0, 12)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec)
        
        return cipher.doFinal(encryptedData, 12, encryptedData.size - 12)
    }

    private fun computeSHA256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02x".format(it) }
    }

    private fun verifyGovernmentSignature(
        signature: String,
        issuerFingerprint: String,
        contentHash: String
    ): Boolean {
        // TODO: Implement actual RSA signature verification with government's public key
        // For now, basic validation
        return signature.isNotEmpty() && issuerFingerprint.isNotEmpty() && contentHash.isNotEmpty()
    }

    // ─── Access Logging ──────────────────────────────────────────────────────

    private fun logDocumentAccess(
        documentId: String,
        accessorId: String,
        accessorRole: String,
        action: String,
        reason: String? = null
    ) {
        val log = DocumentAccessLog(
            accessorId = accessorId,
            accessorRole = accessorRole,
            timestamp = LocalDateTime.now().toString(),
            action = action,
            reason = reason
        )
        
        accessLogs.computeIfAbsent(documentId) { mutableListOf() }.add(log)
    }

    private fun logCertificateAccess(
        certificateId: String,
        accessorId: String,
        accessorRole: String,
        action: String,
        verificationResult: String? = null,
        organization: String? = null
    ) {
        val log = CertificateAccessLog(
            accessorId = accessorId,
            accessorRole = accessorRole,
            timestamp = LocalDateTime.now().toString(),
            action = action,
            verificationResult = verificationResult,
            organization = organization
        )
        
        certAccessLogs.computeIfAbsent(certificateId) { mutableListOf() }.add(log)
    }

    // ─── Compliance & Audit ──────────────────────────────────────────────────

    fun checkCompliance(documentId: String, checkType: String, checkedBy: String): DocumentComplianceRecord {
        val record = DocumentComplianceRecord(
            id = UUID.randomUUID().toString(),
            documentId = documentId,
            complianceType = checkType,
            checkedBy = checkedBy,
            status = ComplianceStatus.COMPLIANT  // TODO: implement actual checks
        )
        
        complianceRecords.add(record)
        
        logSecurityAudit(
            userId = checkedBy,
            auditType = "COMPLIANCE_CHECK",
            details = mapOf(
                "documentId" to documentId,
                "checkType" to checkType,
                "status" to record.status.name
            ),
            severity = "INFO"
        )
        
        return record
    }

    private fun logSecurityAudit(
        userId: String? = null,
        auditType: String,
        details: Map<String, Any>,
        severity: String
    ) {
        val audit = SecurityAudit(
            id = UUID.randomUUID().toString(),
            userId = userId,
            auditType = auditType,
            details = details,
            severity = severity
        )
        
        securityAudits.add(audit)
    }

    fun getSecurityAudits(userId: String? = null, limit: Int = 100): List<SecurityAudit> {
        return if (userId != null) {
            securityAudits.filter { it.userId == userId }.takeLast(limit)
        } else {
            securityAudits.takeLast(limit)
        }
    }

    fun getDocumentAccessLogs(documentId: String): List<DocumentAccessLog> =
        accessLogs[documentId] ?: emptyList()

    fun getCertificateAccessLogs(certificateId: String): List<CertificateAccessLog> =
        certAccessLogs[certificateId] ?: emptyList()

    // ─── Certificate Revocation & Reporting ──────────────────────────────────

    fun revokeCertificate(certificateId: String, reason: String): Boolean {
        val cert = govCerts[certificateId] ?: return false
        
        govCerts[certificateId] = cert.copy(
            status = CertificateStatus.REVOKED,
            revokedAt = LocalDateTime.now().toString(),
            revocationReason = reason
        )
        
        logSecurityAudit(
            auditType = "CERTIFICATE_REVOKED",
            details = mapOf(
                "certificateId" to certificateId,
                "reason" to reason
            ),
            severity = "WARNING"
        )
        
        return true
    }

    fun reportDuplicateCertificate(certificateId: String, reporterUserId: String): Boolean {
        val cert = govCerts[certificateId] ?: return false
        
        govCerts[certificateId] = cert.copy(
            status = CertificateStatus.DUPLICATE,
            duplicateReports = cert.duplicateReports + reporterUserId
        )
        
        logSecurityAudit(
            userId = reporterUserId,
            auditType = "DUPLICATE_REPORTED",
            details = mapOf("certificateId" to certificateId),
            severity = "WARNING"
        )
        
        return true
    }

    // ─── Statistics & Reporting ──────────────────────────────────────────────

    fun getDocumentStatistics(userId: String): Map<String, Any> {
        val userDocs = identityDocs.values.filter { it.userId == userId }
        val userCerts = govCerts.values.filter { it.userId == userId }
        
        return mapOf(
            "totalDocuments" to userDocs.size,
            "verifiedDocuments" to userDocs.count { it.verificationStatus == VerificationStatus.VERIFIED },
            "pendingDocuments" to userDocs.count { it.verificationStatus == VerificationStatus.PENDING },
            "totalCertificates" to userCerts.size,
            "activeCertificates" to userCerts.count { it.status == CertificateStatus.ACTIVE },
            "expiredCertificates" to userCerts.count { it.status == CertificateStatus.EXPIRED }
        )
    }
}
