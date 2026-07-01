package org.psei.features

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE 1 — AADHAAR BRIDGE 2.0
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class AadhaarBridgeRequest(
    val aadhaarNumber: String? = null,
    val virtualId: String? = null,
    val otpTransactionId: String? = null,
    val otp: String? = null,
    val authType: AadhaarAuthType = AadhaarAuthType.OTP
)

enum class AadhaarAuthType { OTP, BIOMETRIC, OFFLINE_XML, FACE }

@Serializable
data class AadhaarBridgeResult(
    val authenticated: Boolean,
    val maskedAadhaar: String? = null,      // e.g. XXXX-XXXX-1234
    val virtualId: String? = null,
    val name: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val address: String? = null,
    val photo: String? = null,              // Base64 — only with consent
    val txnId: String = UUID.randomUUID().toString(),
    val timestamp: String = LocalDateTime.now().toString()
)

class AadhaarBridgeService {
    private val circuitBreaker = org.psei.security.CircuitBreaker(failureThreshold = 3)

    fun generateOTP(aadhaarOrVirtualId: String): String {
        // TODO: POST https://api.uidai.gov.in/authserver/auth/{version}/{AUA}/{SubAUA}
        return "TXN-${UUID.randomUUID().toString().take(10).uppercase()}"
    }

    fun authenticate(request: AadhaarBridgeRequest): AadhaarBridgeResult {
        return circuitBreaker.execute {
            // TODO: call UIDAI Auth API with PID block encrypted using UIDAI public key
            // For now: simulate successful auth for demo
            AadhaarBridgeResult(
                authenticated = true,
                maskedAadhaar = "XXXX-XXXX-${request.aadhaarNumber?.takeLast(4) ?: "????"}",
                virtualId = "9${UUID.randomUUID().toString().replace("-","").take(15)}",
                name = "Verified Citizen",
                gender = "M",
                dob = "01-01-1990"
            )
        }
    }

    fun generateVirtualId(aadhaarNumber: String): String {
        // TODO: call UIDAI VID generation API
        return "9${UUID.randomUUID().toString().replace("-","").take(15)}"
    }

    fun offlineXMLVerify(xmlData: ByteArray, shareCode: String): AadhaarBridgeResult {
        // TODO: parse Aadhaar Secure QR / Offline XML, verify UIDAI signature
        return AadhaarBridgeResult(authenticated = true, maskedAadhaar = "XXXX-XXXX-0000")
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE 2 — PAN AUTO-LINK (NSDL Verification)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class PANVerificationRequest(val panNumber: String, val fullName: String, val dateOfBirth: String)

@Serializable
data class PANVerificationResult(
    val valid: Boolean,
    val panStatus: String,          // ACTIVE, INACTIVE, FAKE, NOT_FOUND
    val nameMatch: Double,          // 0.0-1.0 fuzzy match score
    val aadhaarLinked: Boolean = false,
    val category: String = "INDIVIDUAL",
    val verifiedAt: String = LocalDateTime.now().toString()
)

class PANVerificationService {
    fun verify(request: PANVerificationRequest): PANVerificationResult {
        // TODO: POST to NSDL e-Gov API / Income Tax Department verification API
        val panRegex = Regex("[A-Z]{5}[0-9]{4}[A-Z]{1}")
        val formatValid = panRegex.matches(request.panNumber.uppercase())
        return PANVerificationResult(
            valid = formatValid,
            panStatus = if (formatValid) "ACTIVE" else "INVALID_FORMAT",
            nameMatch = if (formatValid) 0.95 else 0.0,
            aadhaarLinked = formatValid
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE 3 — DIGILOCKER BIDIRECTIONAL SYNC
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class DigiLockerDocument(
    val uri: String,               // DigiLocker URI
    val name: String,
    val type: String,
    val issuer: String,
    val date: String,
    val validUntil: String? = null
)

@Serializable
data class DigiLockerSyncResult(
    val pulled: List<DigiLockerDocument> = emptyList(),
    val pushed: Int = 0,
    val syncedAt: String = LocalDateTime.now().toString(),
    val nextSyncAt: String = LocalDateTime.now().plusHours(24).toString()
)

class DigiLockerSyncService {
    // OAuth 2.0 + DigiLocker API v2
    fun pullDocuments(accessToken: String): DigiLockerSyncResult {
        // TODO: GET https://api.digitallocker.gov.in/public/oauth2/2/files/issued
        return DigiLockerSyncResult(pulled = emptyList())
    }

    fun pushDocument(accessToken: String, documentUri: String): Boolean {
        // TODO: Push PSEI-verified cert to citizen's DigiLocker
        return true
    }

    fun getAuthUrl(redirectUri: String): String =
        "https://api.digitallocker.gov.in/public/oauth2/1/authorize?" +
        "response_type=code&client_id=PSEI_CLIENT_ID&redirect_uri=$redirectUri&state=${UUID.randomUUID()}"
}

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE 4 — FACE AUTH with Liveness Detection
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class FaceAuthRequest(
    val selfieBase64: String,
    val livenessChallenge: String,    // "blink", "smile", "turn-left", "nod"
    val referenceImageId: String? = null  // Aadhaar photo reference
)

@Serializable
data class FaceAuthResult(
    val authenticated: Boolean,
    val livenessScore: Float,         // 0.0-1.0
    val matchScore: Float,            // 0.0-1.0 (≥0.85 = pass)
    val deepfakeScore: Float,         // 0.0-1.0 (≤0.15 = safe)
    val verdict: String,              // PASSED, FAILED, LIVENESS_FAIL, DEEPFAKE_DETECTED
    val processedAt: String = LocalDateTime.now().toString()
)

class FaceAuthService {
    suspend fun authenticate(request: FaceAuthRequest): FaceAuthResult = coroutineScope {
        val liveness = async { checkLiveness(request.selfieBase64, request.livenessChallenge) }
        val faceMatch = async { matchFace(request.selfieBase64, request.referenceImageId) }
        val deepfake = async { detectDeepfake(request.selfieBase64) }

        val lScore = liveness.await()
        val mScore = faceMatch.await()
        val dScore = deepfake.await()

        val verdict = when {
            dScore > 0.85f -> "DEEPFAKE_DETECTED"
            lScore < 0.70f -> "LIVENESS_FAIL"
            mScore < 0.85f -> "FAILED"
            else -> "PASSED"
        }

        FaceAuthResult(
            authenticated = verdict == "PASSED",
            livenessScore = lScore,
            matchScore = mScore,
            deepfakeScore = dScore,
            verdict = verdict
        )
    }

    private suspend fun checkLiveness(image: String, challenge: String): Float {
        delay(10)
        // TODO: integrate AWS Rekognition / Azure Face / ArcFace for liveness
        return 0.95f
    }

    private suspend fun matchFace(image: String, referenceId: String?): Float {
        delay(10)
        // TODO: compare with Aadhaar photo or stored reference image
        return 0.92f
    }

    private suspend fun detectDeepfake(image: String): Float {
        delay(10)
        // TODO: integrate deepfake detection model (FaceForensics++, DFDC)
        return 0.05f
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE 5 — VOICE BIOMETRIC (for Illiterate Users)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class VoiceAuthRequest(
    val audioBase64: String,          // WAV/OGG, 3-5 seconds
    val languageCode: String = "hi-IN",
    val enrollmentPhrase: String? = null  // for enrollment
)

@Serializable
data class VoiceAuthResult(
    val authenticated: Boolean,
    val speakerScore: Float,
    val transcribedText: String? = null,
    val languageDetected: String? = null,
    val processedAt: String = LocalDateTime.now().toString()
)

class VoiceBiometricService {
    fun enrollVoice(userId: String, request: VoiceAuthRequest): Boolean {
        // TODO: send to speaker recognition model (SpeakerNet / x-vectors)
        return true
    }

    fun authenticate(userId: String, request: VoiceAuthRequest): VoiceAuthResult {
        // TODO: compare voice embedding with enrolled template
        return VoiceAuthResult(authenticated = true, speakerScore = 0.91f,
            transcribedText = "Authentication phrase", languageDetected = request.languageCode)
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE 6 — FAMILY KYC (Household Linking)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class FamilyMember(
    val memberId: String? = null,
    val userId: String,
    val relation: FamilyRelation,
    val fullName: String,
    val dateOfBirth: String? = null,
    val aadhaarMasked: String? = null,
    val linkedAt: String = LocalDateTime.now().toString(),
    val consentGiven: Boolean = false
)

enum class FamilyRelation { SELF, SPOUSE, CHILD, PARENT, SIBLING, GUARDIAN, DEPENDENT }

@Serializable
data class HouseholdProfile(
    val id: String? = null,
    val headOfHousehold: String,
    val members: List<FamilyMember> = emptyList(),
    val address: String,
    val rationCardNumber: String? = null,
    val incomeCategory: String = "GENERAL",  // BPL, EWS, GENERAL
    val createdAt: String = LocalDateTime.now().toString()
)

class FamilyKYCService {
    private val households = mutableMapOf<String, HouseholdProfile>()
    private val memberIndex = mutableMapOf<String, String>()  // userId → householdId

    fun createHousehold(headUserId: String, address: String): HouseholdProfile {
        val id = UUID.randomUUID().toString()
        val household = HouseholdProfile(id = id, headOfHousehold = headUserId, address = address)
        households[id] = household
        memberIndex[headUserId] = id
        return household
    }

    fun addMember(householdId: String, member: FamilyMember): FamilyMember {
        val household = households[householdId] ?: error("Household not found")
        val newMember = member.copy(memberId = UUID.randomUUID().toString())
        households[householdId] = household.copy(members = household.members + newMember)
        memberIndex[member.userId] = householdId
        return newMember
    }

    fun getHousehold(householdId: String): HouseholdProfile? = households[householdId]
    fun getHouseholdByMember(userId: String): HouseholdProfile? =
        memberIndex[userId]?.let { households[it] }
}

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE 7 — DIGITAL DEATH CERTIFICATE & SUCCESSION TRIGGER
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class DeathRegistration(
    val id: String? = null,
    val deceasedUserId: String,
    val deceasedName: String,
    val dateOfDeath: String,
    val placeOfDeath: String,
    val causeOfDeath: String? = null,
    val registeredBy: String,           // Family member userId
    val relation: FamilyRelation,
    val deathCertificateNumber: String? = null,
    val hospitalName: String? = null,
    val doctorName: String? = null,
    val registrationDate: String = LocalDateTime.now().toString(),
    val status: String = "PENDING",     // PENDING, VERIFIED, REGISTERED
    val successionTriggered: Boolean = false,
    val benefitsTerminated: Boolean = false,
    val actions: List<SuccessionAction> = emptyList()
)

@Serializable
data class SuccessionAction(
    val actionType: String,             // BENEFIT_STOP, VAULT_FREEZE, WILL_NOTIFY, TAX_ALERT
    val description: String,
    val completedAt: String? = null,
    val status: String = "PENDING"
)

class DeathRegistrationService {
    private val registrations = mutableMapOf<String, DeathRegistration>()

    fun register(registration: DeathRegistration): DeathRegistration {
        val id = UUID.randomUUID().toString()
        val actions = buildSuccessionActions(registration)
        val newReg = registration.copy(id = id, actions = actions)
        registrations[id] = newReg
        return newReg
    }

    private fun buildSuccessionActions(reg: DeathRegistration): List<SuccessionAction> = listOf(
        SuccessionAction("BENEFIT_STOP",   "Terminate PM-KISAN, pension, and other recurring benefits"),
        SuccessionAction("VAULT_FREEZE",   "Freeze document vault — nominee access only"),
        SuccessionAction("WILL_NOTIFY",    "Notify registered legal heir/nominee"),
        SuccessionAction("PROPERTY_FLAG",  "Flag all registered properties for succession process"),
        SuccessionAction("TAX_ALERT",      "Notify Income Tax department for final return"),
        SuccessionAction("BANK_NOTIFY",    "Alert linked banks for account freeze/transfer"),
        SuccessionAction("VOTER_REMOVE",   "Remove from electoral roll"),
        SuccessionAction("AADHAR_DEACT",   "Initiate Aadhaar deactivation with UIDAI")
    )

    fun getRegistration(id: String): DeathRegistration? = registrations[id]
}

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE 8 — GUARDIAN MODE (Minors, Elderly, Disabled)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class Guardianship(
    val id: String? = null,
    val guardianUserId: String,
    val wardUserId: String,
    val wardName: String,
    val wardType: WardType,
    val guardianshipType: String,       // LEGAL, FAMILY, COURT_APPOINTED
    val courtOrderNumber: String? = null,
    val startDate: String,
    val endDate: String? = null,        // null for permanent
    val permissions: List<String> = listOf("VIEW", "APPLY_SCHEMES", "MANAGE_DOCUMENTS"),
    val restrictions: List<String> = listOf("PROPERTY_TRANSFER", "FINANCIAL_LARGE"),
    val status: String = "ACTIVE",
    val createdAt: String = LocalDateTime.now().toString()
)

enum class WardType { MINOR, ELDERLY, DISABLED, MENTALLY_INCAPACITATED }

class GuardianModeService {
    private val guardianships = mutableMapOf<String, Guardianship>()
    private val wardToGuardian = mutableMapOf<String, String>()

    fun createGuardianship(guardianship: Guardianship): Guardianship {
        val id = UUID.randomUUID().toString()
        val g = guardianship.copy(id = id)
        guardianships[id] = g
        wardToGuardian[guardianship.wardUserId] = guardianship.guardianUserId
        return g
    }

    fun canPerformAction(wardUserId: String, action: String): Boolean {
        val gId = wardToGuardian[wardUserId] ?: return false
        val g = guardianships.values.firstOrNull { it.wardUserId == wardUserId && it.status == "ACTIVE" }
        return g?.permissions?.contains(action) == true && g.restrictions.contains(action).not()
    }

    fun getGuardianship(wardUserId: String): Guardianship? =
        guardianships.values.firstOrNull { it.wardUserId == wardUserId && it.status == "ACTIVE" }
}

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE 9 — NRI/OCI INTEGRATION
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class NRIProfile(
    val id: String? = null,
    val userId: String,
    val nriType: NRIType,
    val countryOfResidence: String,
    val passportNumber: String,
    val ociBCNumber: String? = null,    // OCI card / PIO card number
    val taxResidencyCountry: String,
    val dtaaApplicable: Boolean = false, // Double Tax Avoidance Agreement
    val nreAccountLinked: Boolean = false,
    val nroAccountLinked: Boolean = false,
    val registeredAt: String = LocalDateTime.now().toString()
)

enum class NRIType { NRI, OCI, PIO, FOREIGN_NATIONAL_OF_INDIAN_ORIGIN }

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE 10 — DOCUMENT HEALTH SCORE
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class DocumentHealthReport(
    val userId: String,
    val overallScore: Int,              // 0-100
    val grade: String,                  // A, B, C, D, F
    val totalDocuments: Int,
    val verifiedDocuments: Int,
    val expiredDocuments: Int,
    val expiringIn30Days: Int,
    val missingCriticalDocs: List<String>,
    val recommendations: List<HealthRecommendation>,
    val generatedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class HealthRecommendation(
    val priority: String,               // HIGH, MEDIUM, LOW
    val action: String,
    val documentType: String,
    val daysUntilExpiry: Int? = null,
    val actionUrl: String? = null
)

class DocumentHealthService {
    fun generateHealthReport(userId: String, documents: List<org.psei.models.IdentityDocument>): DocumentHealthReport {
        val today = java.time.LocalDate.now()
        val expired = documents.filter {
            it.expiryDate != null && java.time.LocalDate.parse(it.expiryDate) < today
        }
        val expiringIn30 = documents.filter {
            it.expiryDate != null && run {
                val exp = java.time.LocalDate.parse(it.expiryDate)
                exp >= today && exp <= today.plusDays(30)
            }
        }
        val verified = documents.filter { it.verificationStatus == org.psei.models.VerificationStatus.VERIFIED }

        val criticalDocs = listOf("AADHAR_CARD", "PAN_CARD", "VOTER_ID")
        val presentTypes = documents.map { it.documentType.name }
        val missing = criticalDocs.filter { it !in presentTypes }

        val score = calculateScore(documents.size, verified.size, expired.size, expiringIn30.size, missing.size)

        val recommendations = buildRecommendations(expired, expiringIn30.map { it }, missing)

        return DocumentHealthReport(
            userId = userId,
            overallScore = score,
            grade = when { score >= 90 -> "A"; score >= 75 -> "B"; score >= 60 -> "C"; score >= 40 -> "D"; else -> "F" },
            totalDocuments = documents.size,
            verifiedDocuments = verified.size,
            expiredDocuments = expired.size,
            expiringIn30Days = expiringIn30.size,
            missingCriticalDocs = missing,
            recommendations = recommendations
        )
    }

    private fun calculateScore(total: Int, verified: Int, expired: Int, expiring: Int, missing: Int): Int {
        var score = 50
        score += minOf(25, total * 2)
        score += minOf(20, verified * 3)
        score -= expired * 10
        score -= expiring * 3
        score -= missing * 8
        return score.coerceIn(0, 100)
    }

    private fun buildRecommendations(
        expired: List<org.psei.models.IdentityDocument>,
        expiring: List<org.psei.models.IdentityDocument>,
        missing: List<String>
    ): List<HealthRecommendation> {
        val recs = mutableListOf<HealthRecommendation>()
        missing.forEach { docType ->
            recs += HealthRecommendation("HIGH", "Upload your $docType — critical document missing", docType,
                actionUrl = "/citizen-services/id-applications")
        }
        expired.forEach { doc ->
            recs += HealthRecommendation("HIGH", "Renew your ${doc.documentType.name} — expired!", doc.documentType.name)
        }
        expiring.forEach { doc ->
            val days = doc.expiryDate?.let {
                java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), java.time.LocalDate.parse(it)).toInt()
            } ?: 0
            recs += HealthRecommendation("MEDIUM", "Renew ${doc.documentType.name} soon", doc.documentType.name, days)
        }
        return recs.sortedBy { it.priority }
    }
}
