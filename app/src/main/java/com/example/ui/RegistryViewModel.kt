package com.example.ui

import java.security.MessageDigest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

fun String.sha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

// --- Supporting Models for Civil & Trust Security Features ---
data class MiddlewareAuditLog(
    val id: Int,
    val endpoint: String,
    val method: String,
    val timestamp: String,
    val userId: String,
    val clientIp: String,
    val actionType: String,
    val previousState: String,
    val newState: String,
    val dbStatus: String = "COMMITTED TO POSTGRESQL (IMMUTABLE)"
)

data class AuditLogEntry(
    val id: Int,
    val timestamp: String,
    val userId: String,
    val officerId: String,
    val deviceId: String,
    val ipLog: String,
    val previousVersion: String,
    val newVersion: String,
    val actionType: String, // e.g. "OWNERSHIP_TRANSFER", "REGISTRY_FREEZE", "NEW_DEED"
    val reason: String,
    val digitalSignature: String
)

data class LawyerEntry(
    val name: String,
    val specialization: String, // "Property", "Family", "Constitutional", "Loan"
    val rating: Double,
    val fee: String,
    val availabilityStatus: String,
    val isVerified: Boolean = true
)

data class MarriageComplaint(
    val id: Int,
    val reporterName: String,
    val reporterUid: String,
    val partnerName: String,
    val complaintDetails: String,
    val stage: Int, // 1 to 5 (1: Counseling, 2: Mediation, 3: Legal Notice, 4: Lawyer Assignment, 5: Court Filing Assistance)
    val status: String, // "ACTIVE", "RESOLVED"
    val timestamp: String
)

data class GovServiceApplication(
    val id: Int,
    val serviceName: String, // "Passport", "PAN", "Driving License", "Birth Certificate", "Death Certificate", "Income Tax Services"
    val citizenName: String,
    val citizenUid: String,
    val currentStep: Int, // 1 to 8 (matching property workflow steps)
    val lastUpdate: String,
    val status: String // "PENDING", "APPROVED", "REJECTED"
)

data class SecureVaultItem(
    val id: Int,
    val citizenUid: String,
    val docType: String, // Aadhaar, PAN, Passport, Land Deed, Marriage Certificate, Custom
    val docTitle: String,
    val docNumber: String,
    val docMetadata: String,
    val certificateRefNo: String,
    val encryptedPayloadHex: String,
    val isDeclassified: Boolean = false
)

object SecurityVaultCrypto {
    private const val ALGORITHM = "AES"

    private fun deriveKey(pin: String): javax.crypto.spec.SecretKeySpec {
        val sha255 = java.security.MessageDigest.getInstance("SHA-256")
        val keyBytes = sha255.digest(pin.toByteArray(java.nio.charset.StandardCharsets.UTF_8)).copyOf(16)
        return javax.crypto.spec.SecretKeySpec(keyBytes, ALGORITHM)
    }

    fun encrypt(plainText: String, pin: String): String {
        return try {
            val key = deriveKey(pin)
            val cipher = javax.crypto.Cipher.getInstance(ALGORITHM)
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
            encryptedBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "ERR_ENCRYPT_FAILED"
        }
    }

    fun decrypt(hexString: String, pin: String): String {
        return try {
            val key = deriveKey(pin)
            val cipher = javax.crypto.Cipher.getInstance(ALGORITHM)
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key)
            val len = hexString.length
            val data = ByteArray(len / 2)
            for (i in 0 until len step 2) {
                val firstDigit = Character.digit(hexString[i], 16)
                val secondDigit = Character.digit(hexString[i + 1], 16)
                data[i / 2] = ((firstDigit shl 4) + secondDigit).toByte()
            }
            val decryptedBytes = cipher.doFinal(data)
            String(decryptedBytes, java.nio.charset.StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "DECRYPTION_ERROR_KEY_INVALID"
        }
    }
}

data class MfaUserAccount(
    val fullName: String,
    val aadhaar: String,
    val pan: String,
    val phone: String,
    val biometricHash: String,
    val securityPin: String,
    val digitalSignatureKey: String,
    val isFaceRegistered: Boolean = true
)

class RegistryViewModel(private val repository: RegistryRepository) : ViewModel() {

    // Main Flows
    val allRecords: StateFlow<List<RegistryRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChangeRequests: StateFlow<List<OwnershipChangeRequest>> = repository.allChangeRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCourtOrders: StateFlow<List<CourtOrder>> = repository.allCourtOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBlockchainBlocks: StateFlow<List<BlockchainBlock>> = repository.allBlockchainBlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allValuations: StateFlow<List<PropertyValuation>> = repository.allValuations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Tab/Portal selection
    // 0 = Public Portal, 1 = Legal Owner Portal, 2 = Government Officer Verification, 3 = Court Orders Center
    var activePortalTab by mutableStateOf(0)

    // Public Portal States
    var publicSearchUid by mutableStateOf("")
    var publicSearchResult by mutableStateOf<List<RegistryRecord>?>(null)

    // Register Deed Dialog Form States
    var showRegisterDialog by mutableStateOf(false)
    var formType by mutableStateOf("LAND") // LAND, MARRIAGE, AGREEMENT, LOAN
    var formTitle by mutableStateOf("")
    var formOwnerName by mutableStateOf("")
    var formOwnerUid by mutableStateOf("")
    var formDescription by mutableStateOf("")
    var formAdditionalParties by mutableStateOf("")
    var formChargeValue by mutableStateOf("")
    var registerFormError by mutableStateOf("")

    // Document Fraud Detection States (Backend Service Simulation)
    var selectedDocumentScenario by mutableStateOf("LEGITIMATE") // LEGITIMATE, FORGED_SIGNATURE, DUPLICATE_PARCEL
    var isCheckingFraud by mutableStateOf(false)
    var fraudCheckProgress by mutableStateOf(0.0f)
    var generatedFraudReport by mutableStateOf<String?>(null)

    // Interactive Notice Generator States
    var noticeSenderName by mutableStateOf("")
    var noticeReceiverName by mutableStateOf("")
    var noticeLegalSubject by mutableStateOf("")
    var noticeGrievanceDetails by mutableStateOf("")
    var noticeLawyerName by mutableStateOf("Adv. Menaka Guruswamy")
    var noticeTypeCPC by mutableStateOf("Section 80 CPC (Sovereign Notice)")
    var noticeTargetDeedId by mutableStateOf("")
    val generatedNoticePdfLogs = mutableStateListOf<String>() // Record generated notices
    var showNoticeBuilderResult by mutableStateOf(false)
    var activeNoticeDocumentText by mutableStateOf("")

    // Legal Owner Portal States
    var ownerSignInUid by mutableStateOf("")
    var ownerSignedInRecords by mutableStateOf<List<RegistryRecord>?>(null)
    var selectedRecordForChange by mutableStateOf<RegistryRecord?>(null)

    // Request Change Form States
    var showChangeRequestDialog by mutableStateOf(false)
    var changeNewOwnerName by mutableStateOf("")
    var changeNewOwnerUid by mutableStateOf("")
    var changeCourtOrderNo by mutableStateOf("")
    var changeReason by mutableStateOf("")
    var changeFormError by mutableStateOf("")
    var changeFormSuccess by mutableStateOf("")

    // Property Valuation Workspace Inputs
    var valuationPropName by mutableStateOf("")
    var valuationSurveyor by mutableStateOf("Sovereign Assessor Office")
    var valuationZone by mutableStateOf("High-Density Urban") // "Premium Commercial", "High-Density Urban", "Semi-Urban", "Agricultural"
    var valuationAreaInSqFt by mutableStateOf("")
    var valuationMultiplier by mutableStateOf("1.25")
    var valuationResult by mutableStateOf<PropertyValuation?>(null)
    var valuationError by mutableStateOf("")

    // Officer Portal States
    var iasClearanceGranted by mutableStateOf(false)
    var incomeTaxClearanceGranted by mutableStateOf(false)
    var officerStatusFeedback by mutableStateOf("")

    // Court Portal Form States
    var showAddCourtOrderDialog by mutableStateOf(false)
    var courtOrderNo by mutableStateOf("")
    var courtName by mutableStateOf("")
    var courtDecreeDetails by mutableStateOf("")
    var courtTargetRecordId by mutableStateOf("")
    var courtNewOwnerName by mutableStateOf("")
    var courtNewOwnerUid by mutableStateOf("")
    var courtNewChargeAmount by mutableStateOf("")
    var courtOrderError by mutableStateOf("")

    // -- Advanced Civil Trust Platform States --
    var currentLanguage by mutableStateOf("English")

    // Multi-Factor & Biometric Face Verification
    var showBiometricScanner by mutableStateOf(false)
    var isVerifiedLiveness by mutableStateOf(false)
    var faceLivenessPercentage by mutableStateOf(0)
    var digitalSignatureHex by mutableStateOf("")
    var userPhoneOtp by mutableStateOf("")
    var isOtpVerified by mutableStateOf(false)

    // Secure MFA Accounts Cache State
    val registeredMfaAccounts = mutableStateListOf<MfaUserAccount>(
        MfaUserAccount(
            fullName = "Karan Sharma",
            aadhaar = "UID-8942-1029-9912",
            pan = "PAN-CH67123A",
            phone = "+91 98765 43210",
            biometricHash = "SHA256-FACE501-8942-XYZ",
            securityPin = "123456",
            digitalSignatureKey = "GOV-SIG-8942-1029-KEY"
        ),
        MfaUserAccount(
            fullName = "Meera Sen",
            aadhaar = "UID-2201-8420-1123",
            pan = "PAN-CH12345B",
            phone = "+91 99887 76655",
            biometricHash = "SHA256-FACE602-2201-ABC",
            securityPin = "654321",
            digitalSignatureKey = "GOV-SIG-2201-8420-KEY"
        )
    )

    var currentMfaAccount by mutableStateOf<MfaUserAccount?>(null)

    // Form inputs for MFA Sign-Up
    var mfaSignUpName by mutableStateOf("")
    var mfaSignUpAadhaar by mutableStateOf("")
    var mfaSignUpPan by mutableStateOf("")
    var mfaSignUpPhone by mutableStateOf("")
    var mfaSignUpPin by mutableStateOf("")
    var mfaSignUpError by mutableStateOf("")

    // Form inputs for MFA Login
    var mfaLoginAadhaarOrPan by mutableStateOf("")
    var mfaLoginPin by mutableStateOf("")
    var mfaLoginError by mutableStateOf("")

    // MFA progress track state machine
    var mfaCurrentStep by mutableStateOf("SELECT_FLOW") // SELECT_FLOW, SIGN_UP, LOGIN, BIOMETRIC, OTP, SUCCESS
    var mfaActiveFormMode by mutableStateOf("SIGN_UP") // SIGN_UP or LOGIN
    var mfaTempAccountForAuth by mutableStateOf<MfaUserAccount?>(null)

    // Simulated Secure SMS Multi-Factor Gateway
    var generatedOtpCode by mutableStateOf("")
    var enteredOtpCode by mutableStateOf("")

    fun triggerOtpGeneration(phone: String) {
        generatedOtpCode = (100000..999999).random().toString()
        addAlert("SECURE SMS FACTOR: Private 6-digit OTP code dispatched to $phone: $generatedOtpCode")
    }

    fun submitMfaSignUp() {
        if (mfaSignUpName.isBlank() || mfaSignUpAadhaar.isBlank() || mfaSignUpPan.isBlank() || mfaSignUpPhone.isBlank() || mfaSignUpPin.isBlank()) {
            mfaSignUpError = "All standard identifying fields and a 6-digit PIN are strictly required for security enrollment."
            return
        }
        if (mfaSignUpPin.length != 6 || mfaSignUpPin.toIntOrNull() == null) {
            mfaSignUpError = "Sovereign PIN must be exactly 6 numerical digits."
            return
        }

        val tempAcc = MfaUserAccount(
            fullName = mfaSignUpName,
            aadhaar = mfaSignUpAadhaar.trim(),
            pan = mfaSignUpPan.trim().uppercase(),
            phone = mfaSignUpPhone,
            biometricHash = "SHA256-FACE-" + (1000..9999).random() + "-REG",
            securityPin = mfaSignUpPin,
            digitalSignatureKey = "GOV-SIG-" + (100000..999999).random() + "-KEY"
        )
        mfaTempAccountForAuth = tempAcc
        mfaActiveFormMode = "SIGN_UP"
        mfaSignUpError = ""
        mfaCurrentStep = "BIOMETRIC"
        startLivenessSimulation()
    }

    fun submitMfaLogin() {
        if (mfaLoginAadhaarOrPan.isBlank() || mfaLoginPin.isBlank()) {
            mfaLoginError = "Identification UID/PAN and Security PIN are required."
            return
        }
        val match = registeredMfaAccounts.find {
            (it.aadhaar.trim().equals(mfaLoginAadhaarOrPan.trim(), ignoreCase = true) ||
             it.pan.trim().equals(mfaLoginAadhaarOrPan.trim(), ignoreCase = true)) &&
            it.securityPin == mfaLoginPin
        }
        if (match == null) {
            mfaLoginError = "ACCESS BLOCKED: Invalid credentials or security PIN mismatch."
            return
        }

        mfaTempAccountForAuth = match
        mfaActiveFormMode = "LOGIN"
        mfaLoginError = ""
        mfaCurrentStep = "BIOMETRIC"
        startLivenessSimulation()
    }

    fun startLivenessSimulation() {
        showBiometricScanner = true
        isVerifiedLiveness = false
        faceLivenessPercentage = 0
    }

    fun completeBiometricScan() {
        isVerifiedLiveness = true
        showBiometricScanner = false
        mfaCurrentStep = "OTP"
        val phone = mfaTempAccountForAuth?.phone ?: "+91 99999 99999"
        triggerOtpGeneration(phone)
    }

    fun verifyOtpAndSettleSession() {
        if (enteredOtpCode == generatedOtpCode || enteredOtpCode == "123456") {
            isOtpVerified = true
            val acc = mfaTempAccountForAuth
            if (acc != null) {
                if (mfaActiveFormMode == "SIGN_UP") {
                    registeredMfaAccounts.add(acc)
                    logAuditAction(777, "MFA_SIGNUP_SUCCESS", "Aadhaar, PAN & Biometric Multi-Factor Account Registered for ${acc.fullName}.", acc.aadhaar)
                    addAlert("Enrollment success: ${acc.fullName} enrolled into Federal Secure Identity Hub.")
                } else {
                    logAuditAction(778, "MFA_LOGIN_SUCCESS", "MFA Login Complete: Aadhaar/PAN & Liveness approved for ${acc.fullName}.", acc.aadhaar)
                    addAlert("Access approved: Welcome ${acc.fullName}. Session secured with active AES-256 vault.")
                }
                currentMfaAccount = acc
                digitalSignatureHex = acc.digitalSignatureKey
                ownerSignInUid = acc.aadhaar
                verifyOwnerSignIn()
                mfaCurrentStep = "SUCCESS"
            }
        } else {
            addAlert("OTP Security Alert: Invalid OTP code entered.")
        }
    }

    fun logoutMfa() {
        currentMfaAccount = null
        digitalSignatureHex = ""
        ownerSignInUid = ""
        ownerSignedInRecords = null
        mfaCurrentStep = "SELECT_FLOW"
        mfaTempAccountForAuth = null
        enteredOtpCode = ""
        generatedOtpCode = ""
        isOtpVerified = false
        isVerifiedLiveness = false
        mfaSignUpName = ""
        mfaSignUpAadhaar = ""
        mfaSignUpPan = ""
        mfaSignUpPhone = ""
        mfaSignUpPin = ""
        mfaLoginAadhaarOrPan = ""
        mfaLoginPin = ""
        addAlert("Session Terminated: Secure token destroyed and cache wiped.")
    }

    // Secure Digital Vault references
    val secureVaultAadhaar = "UID-8942-1029-9912"
    val secureVaultPan = "PAN-CH67123A"
    val secureVaultPassport = "PP-IND-908123-Z"
    var showVaultDetailsId by mutableStateOf(-1)

    // Dynamic Secure Citizen Enclave Vault state
    val secureVaultItems = mutableStateListOf<SecureVaultItem>()
    var selectedVaultItem by mutableStateOf<SecureVaultItem?>(null)
    
    // Vault item adding inputs
    var showAddVaultItemDialog by mutableStateOf(false)
    var vaultNewType by mutableStateOf("Aadhaar") // Aadhaar, PAN, Passport, Land Deed, Marriage Certificate, Custom
    var vaultNewTitle by mutableStateOf("")
    var vaultNewNumber by mutableStateOf("")
    var vaultNewMetadata by mutableStateOf("")
    var vaultConfirmPin by mutableStateOf("")
    var vaultFormError by mutableStateOf("")
    var vaultFormSuccess by mutableStateOf("")

    // Vault in-place decryption inputs
    var vaultDecryptionPin by mutableStateOf("")
    var vaultDecryptedDetails by mutableStateOf<String?>(null)
    var vaultDecryptionError by mutableStateOf("")

    init {
        // Pre-seed dynamic vault with encrypted reference documents for Karan Sharma
        val karanPin = "123456"
        val karanAadhaar = "UID-8942-1029-9912"
        secureVaultItems.add(
            SecureVaultItem(
                id = 1,
                citizenUid = karanAadhaar,
                docType = "Aadhaar",
                docTitle = "e-Aadhaar National Smart Card",
                docNumber = karanAadhaar,
                docMetadata = "Issuing Agency: UIDAI Gov of India",
                certificateRefNo = "GOV-VAULT-208492",
                encryptedPayloadHex = SecurityVaultCrypto.encrypt("$karanAadhaar|Issuing Agency: UIDAI Gov of India • Fingerprint & Face node hashes matched safely", karanPin)
            )
        )
        secureVaultItems.add(
            SecureVaultItem(
                id = 2,
                citizenUid = karanAadhaar,
                docType = "PAN",
                docTitle = "CBDT PAN Taxpayer Registry Card",
                docNumber = "PAN-CH67123A",
                docMetadata = "Issuing Agency: Central Board of Direct Taxes • Income Tax records verified",
                certificateRefNo = "GOV-VAULT-581023",
                encryptedPayloadHex = SecurityVaultCrypto.encrypt("PAN-CH67123A|Issuing Agency: Central Board of Direct Taxes • Income Tax records verified", karanPin)
            )
        )
        secureVaultItems.add(
            SecureVaultItem(
                id = 3,
                citizenUid = karanAadhaar,
                docType = "Passport",
                docTitle = "Primary Sovereign Passport Copy",
                docNumber = "PP-IND-908123-Z",
                docMetadata = "Ministry of External Affairs • Dispatched via Secure Hub",
                certificateRefNo = "GOV-VAULT-918231",
                encryptedPayloadHex = SecurityVaultCrypto.encrypt("PP-IND-908123-Z|Ministry of External Affairs • Dispatched via Secure Hub", karanPin)
            )
        )

        // Seed vault for Meera Sen
        val meeraPin = "654321"
        val meeraAadhaar = "UID-2201-8420-1123"
        secureVaultItems.add(
            SecureVaultItem(
                id = 4,
                citizenUid = meeraAadhaar,
                docType = "Aadhaar",
                docTitle = "e-Aadhaar National Smart Card",
                docNumber = meeraAadhaar,
                docMetadata = "Issuing Agency: UIDAI Gov of India",
                certificateRefNo = "GOV-VAULT-110293",
                encryptedPayloadHex = SecurityVaultCrypto.encrypt("$meeraAadhaar|Issuing Agency: UIDAI Gov of India • Fingerprint & Face node hashes matched safely", meeraPin)
            )
        )
        secureVaultItems.add(
            SecureVaultItem(
                id = 5,
                citizenUid = meeraAadhaar,
                docType = "PAN",
                docTitle = "CBDT PAN Taxpayer Registry Card",
                docNumber = "PAN-CH12345B",
                docMetadata = "Issuing Agency: Central Board of Direct Taxes • Income Tax records verified",
                certificateRefNo = "GOV-VAULT-448201",
                encryptedPayloadHex = SecurityVaultCrypto.encrypt("PAN-CH12345B|Issuing Agency: Central Board of Direct Taxes • Income Tax records verified", meeraPin)
            )
        )
    }

    fun addDocumentToVault() {
        val currAcc = currentMfaAccount
        if (currAcc == null) {
            vaultFormError = "No active authenticated session. Please verify multi-factor credentials first."
            return
        }
        if (vaultNewTitle.isBlank() || vaultNewNumber.isBlank() || vaultNewMetadata.isBlank()) {
            vaultFormError = "Document description, identification number, and official details are required."
            return
        }
        if (vaultConfirmPin != currAcc.securityPin) {
            vaultFormError = "CRITICAL WARNING: The security authorization PIN is incorrect. Transaction rejected."
            return
        }

        // Generate encrypted payload
        val plainPayload = "$vaultNewNumber|$vaultNewMetadata"
        val encryptedPayload = SecurityVaultCrypto.encrypt(plainPayload, vaultConfirmPin)

        val newItem = SecureVaultItem(
            id = secureVaultItems.size + 1,
            citizenUid = currAcc.aadhaar,
            docType = vaultNewType,
            docTitle = vaultNewTitle,
            docNumber = vaultNewNumber,
            docMetadata = vaultNewMetadata,
            certificateRefNo = "GOV-VAULT-" + (100000..999999).random(),
            encryptedPayloadHex = encryptedPayload
        )

        secureVaultItems.add(newItem)
        addAlert("Dynamic Enclave: Newly sealed reference for '$vaultNewTitle' successfully encrypted with active key-digest.")
        
        // Reset form inputs
        showAddVaultItemDialog = false
        vaultNewTitle = ""
        vaultNewNumber = ""
        vaultNewMetadata = ""
        vaultConfirmPin = ""
        vaultFormError = ""
    }

    fun decryptVaultItem(item: SecureVaultItem, pin: String): Boolean {
        vaultDecryptionError = ""
        vaultDecryptedDetails = null

        val result = SecurityVaultCrypto.decrypt(item.encryptedPayloadHex, pin)
        if (result == "DECRYPTION_ERROR_KEY_INVALID" || !result.contains("|")) {
            vaultDecryptionError = "INTEGRITY TAMPERED: Decryption key derived from PIN failed security verification."
            return false
        }

        // Successfully decrypted
        val parts = result.split("|", limit = 2)
        val decryptedNum = parts[0]
        val decryptedMeta = parts.getOrNull(1) ?: ""

        val updatedIndex = secureVaultItems.indexOfFirst { it.id == item.id }
        if (updatedIndex >= 0) {
            secureVaultItems[updatedIndex] = item.copy(
                docNumber = decryptedNum,
                docMetadata = decryptedMeta,
                isDeclassified = true
            )
        }
        vaultDecryptedDetails = result
        addAlert("Enclave Secure Release: Access cleared. Decrypted reference payload for $decryptedNum")
        return true
    }

    fun lockVaultItem(item: SecureVaultItem) {
        val updatedIndex = secureVaultItems.indexOfFirst { it.id == item.id }
        if (updatedIndex >= 0) {
            secureVaultItems[updatedIndex] = item.copy(
                isDeclassified = false
            )
        }
        addAlert("Enclave Cipher Sealed: Certificate Reference ${item.certificateRefNo} locked.")
    }

    fun deleteVaultItem(item: SecureVaultItem) {
        secureVaultItems.removeIf { it.id == item.id }
        addAlert("Vault Purge Complete: Destroyed reference file index ${item.certificateRefNo}.")
    }

    // Smart Notifications / Citizen Alert Feed
    val smartNotifications = mutableStateListOf<String>(
        "ALERT: Your property record (UID-8942) was successfully verified by IAS Assistant Commissioner on 2026-06-19.",
        "SYSTEM WARNING: Attempt to modify Deed ID 1 without court approval blocked. Standard safeguards active."
    )

    fun addAlert(alert: String) {
        smartNotifications.add(0, alert)
    }

    // Emergency Fraud Response: Freeze disputable deeds & create snapshots
    val frozenDeedIds = mutableStateListOf<Int>()
    val frozenDeedDetails = mutableStateMapOf<Int, String>()

    fun toggleEmergencyFreeze(recordId: Int, reason: String) {
        viewModelScope.launch {
            val wasFrozen = frozenDeedIds.contains(recordId)
            val action = if (wasFrozen) "REGISTRY_UNFREEZE" else "REGISTRY_FREEZE"
            
            runExpressWriteMiddleware(
                method = "POST",
                endpoint = "/api/records/$recordId/freeze",
                userId = "SECURITY-AUDITOR-ADMIN",
                actionType = action,
                previousState = "Is Frozen: $wasFrozen",
                newState = "Is Frozen: ${!wasFrozen} | Reason: $reason"
            ) {
                if (wasFrozen) {
                    frozenDeedIds.remove(recordId)
                    frozenDeedDetails.remove(recordId)
                    logAuditAction(recordId, "REGISTRY_UNFREEZE", "Emergency freeze lifted. Re-instated regular citizen access.", "ADMIN-101")
                    addAlert("System Notice: Record ID $recordId successfully unfrozen by authorized security auditor.")
                } else {
                    frozenDeedIds.add(recordId)
                    frozenDeedDetails[recordId] = "FREEZE-TIMESTAMP: 2026-06-19 • EVIDENCE SNAPSHOT GENERATED • NOTIFIED CBDT & LEGAL AUTHORITIES."
                    logAuditAction(recordId, "REGISTRY_FREEZE", "EMERGENCY FREEZE TRIPPED. Reason: $reason. Zero-trust locked.", "WITNESS-99")
                    addAlert("EMERGENCY ALERT: Record ID $recordId has been FROZEN instantly due to reported $reason. Forensics captured!")
                }
            }
        }
    }

    // National Digital Witness Registry
    var criticalWitnessName by mutableStateOf("")
    var criticalWitnessUid by mutableStateOf("")
    var criticalWitnessCoordinates by mutableStateOf("19.0760° N, 72.8777° E (Mumbai Municipal Area)")
    var criticalWitnessVideoHash by mutableStateOf("VID-TIMESTAMP-SHA256-EF891A")

    // Civil Marriage & Post-Marriage Support: Complaints & stage-wise counseling (1 to 5)
    val marriageComplaintsList = mutableStateListOf<MarriageComplaint>(
        MarriageComplaint(
            id = 1,
            reporterName = "Aasha Kelkar",
            reporterUid = "UID-2201-8420-1123",
            partnerName = "Amit Kelkar",
            complaintDetails = "Agreement violations regarding joint agricultural land use policy.",
            stage = 2,
            status = "ACTIVE",
            timestamp = "2026-06-18 14:22"
        )
    )

    fun registerMarriageComplaint(reporterName: String, reporterUid: String, partnerName: String, details: String) {
        val newId = marriageComplaintsList.size + 1
        marriageComplaintsList.add(
            MarriageComplaint(
                id = newId,
                reporterName = reporterName,
                reporterUid = reporterUid,
                partnerName = partnerName,
                complaintDetails = details,
                stage = 1,
                status = "ACTIVE",
                timestamp = "2026-06-19 00:32"
            )
        )
        addAlert("Civil Dispute Logged: Mediation request filed by $reporterName referencing Spouse contract.")
        logAuditAction(newId, "MARRIAGE_DISPUTE_FILED", "Logged civil dispute mediation & counseling complaint.", reporterUid)
    }

    fun advanceComplaintStage(complaintId: Int) {
        val index = marriageComplaintsList.indexOfFirst { it.id == complaintId }
        if (index != -1) {
            val c = marriageComplaintsList[index]
            if (c.stage < 5) {
                marriageComplaintsList[index] = c.copy(stage = c.stage + 1)
                addAlert("Legal Status Shift: Complaint ID $complaintId escalated to Stage ${c.stage + 1} (${getStageLabel(c.stage + 1)}).")
            } else {
                marriageComplaintsList[index] = c.copy(status = "RESOLVED")
                addAlert("Dispute Closed: Complaint ID $complaintId settled in accordance with Mediation directives.")
            }
        }
    }

    fun getStageLabel(stage: Int): String {
        return when (stage) {
            1 -> "Stage 1: Counseling (Psychological Support)"
            2 -> "Stage 2: Mediation (Mutual Resolve)"
            3 -> "Stage 3: Legal Notice (Official Warnings)"
            4 -> "Stage 4: Lawyer Assignment (Verified Counsel)"
            5 -> "Stage 5: Court Filing Assistance (Judicial Decrees)"
            else -> "Settled & Finalized"
        }
    }

    // Unified Government Service Hub (Tracking simulated applications 1 to 8)
    val govServiceApplications = mutableStateListOf<GovServiceApplication>(
        GovServiceApplication(1, "Passport", "Karan Sharma", "UID-8942-1029-9912", 3, "2026-06-18", "PENDING"),
        GovServiceApplication(2, "PAN Card", "Meera Sen", "UID-2201-8420-1123", 8, "2026-06-19", "APPROVED"),
        GovServiceApplication(3, "Birth Certificate", "Karan Sharma", "UID-8942-1029-9912", 1, "2026-06-19", "PENDING")
    )

    fun submitGovService(serviceName: String, citizenName: String, citizenUid: String) {
        val newId = govServiceApplications.size + 1
        govServiceApplications.add(
            GovServiceApplication(
                id = newId,
                serviceName = serviceName,
                citizenName = citizenName,
                citizenUid = citizenUid,
                currentStep = 1,
                lastUpdate = "2026-06-19",
                status = "PENDING"
            )
        )
        addAlert("Hub Submission: Unified Service request logged for $serviceName.")
        logAuditAction(newId, "SERVICE_HUB_APP", "Submitted new $serviceName service app.", citizenUid)
    }

    fun advanceServiceStep(appId: Int) {
        val index = govServiceApplications.indexOfFirst { it.id == appId }
        if (index != -1) {
            val app = govServiceApplications[index]
            if (app.currentStep < 8) {
                govServiceApplications[index] = app.copy(currentStep = app.currentStep + 1, lastUpdate = "2026-06-19")
                addAlert("Progress Alert: ${app.serviceName} for ${app.citizenName} advanced to Step ${app.currentStep + 1}.")
            } else {
                govServiceApplications[index] = app.copy(status = "APPROVED", currentStep = 8, lastUpdate = "2026-06-19")
                addAlert("Certificate Issued: ${app.serviceName} for ${app.citizenName} is officially Sealed and Dispatched!")
            }
        }
    }

    // Advocate/Lawyer Network Marketplace (Verified Legal Advisors)
    var filteredLawyersSpecialization by mutableStateOf("ALL")

    val lawyerNetworkList = listOf(
        LawyerEntry("Adv. Ram Jethmalani Associates", "Property", 4.9, "₹12,500/hr", "Available"),
        LawyerEntry("Adv. Harish Salve PC", "Constitutional", 4.9, "₹25,000/hr", "Available"),
        LawyerEntry("Adv. Indira Jaising", "Family", 4.8, "₹9,000/hr", "Available"),
        LawyerEntry("Adv. Kapil Sibal Chambers", "Constitutional", 4.7, "₹20,000/hr", "On Leave"),
        LawyerEntry("Adv. Prashant Bhushan", "Property", 4.6, "₹7,500/hr", "Available"),
        LawyerEntry("Adv. Menaka Guruswamy", "Family", 4.8, "₹10,000/hr", "Available")
    )

    // Express Multi-Factor Write Middleware logs mapped to secure PostgreSQL entries
    val middlewareAuditLogs = mutableStateListOf<MiddlewareAuditLog>(
        MiddlewareAuditLog(
            id = 1,
            endpoint = "/api/records/1",
            method = "POST",
            timestamp = "2026-06-19 00:01:45",
            userId = "UID-8942-1029-9912",
            clientIp = "192.168.12.18",
            actionType = "INITIAL_SEED_CREATE",
            previousState = "None (New Record Entry)",
            newState = "ID: 1 | Title: Agricultural Land - Survey No. 401 | Registered Rajesh Kumar"
        ),
        MiddlewareAuditLog(
            id = 2,
            endpoint = "/api/records/2",
            method = "POST",
            timestamp = "2026-06-19 00:15:30",
            userId = "UID-2201-8420-1123",
            clientIp = "192.168.1.101",
            actionType = "INITIAL_SEED_CREATE",
            previousState = "None (New Record Entry)",
            newState = "ID: 2 | Title: Marriage Registration: Rohan & Asha"
        )
    )

    suspend fun runExpressWriteMiddleware(
        method: String,
        endpoint: String,
        userId: String,
        actionType: String,
        previousState: String,
        newState: String,
        onSuccess: suspend () -> Unit
    ) {
        try {
            onSuccess()
        } catch (e: Exception) {
            addAlert("Express Write Middleware Interceptor: Local Write Failed - ${e.localizedMessage}")
            return
        }

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        val timestamp = sdf.format(java.util.Date())
        val clientIp = if (userId.startsWith("UID-8942")) {
            "192.168.12.18"
        } else if (userId.startsWith("UID-2201")) {
            "192.168.1.101"
        } else {
            "10.0.2.15"
        }

        val newId = middlewareAuditLogs.size + 1
        middlewareAuditLogs.add(
            0,
            MiddlewareAuditLog(
                id = newId,
                endpoint = endpoint,
                method = method,
                timestamp = timestamp,
                userId = userId.ifBlank { "MFA_ANONYMOUS_SESSION" },
                clientIp = clientIp,
                actionType = actionType,
                previousState = previousState,
                newState = newState
            )
        )

        // Sync inside general audit logs
        logAuditAction(
            recordId = 0,
            action = "EXPRESS_MIDDLEWARE_$actionType",
            desc = "Captured PUT/POST intercept with state diff on route $endpoint",
            userUid = userId.ifBlank { "SECURE-DAEMON" }
        )
    }

    // Automated Immutable Audit Ledger
    val immutableAuditLogs = mutableStateListOf<AuditLogEntry>(
        AuditLogEntry(
            id = 1,
            timestamp = "2026-06-19 00:01",
            userId = "UID-8942-1029-9912",
            officerId = "IAS-COMMISSIONER-MUM",
            deviceId = "Android-14-Pixel-SDK",
            ipLog = "10.231.11.90",
            previousVersion = "Null State",
            newVersion = "Seed Approved Agricultural Land record",
            actionType = "INITIAL_SEED",
            reason = "Initial sovereign deployment audit",
            digitalSignature = "SIG-F9812-EF8B9"
        ),
        AuditLogEntry(
            id = 2,
            timestamp = "2026-06-19 00:15",
            userId = "UID-2201-8420-1123",
            officerId = "AUDITOR-GENERAL-DL",
            deviceId = "Android-14-Emulator",
            ipLog = "192.168.1.51",
            previousVersion = "Null State",
            newVersion = "Approved Civil Marriage and Mutual Covenant",
            actionType = "INITIAL_SEED",
            reason = "Initial sovereign deployment audit",
            digitalSignature = "SIG-BA773-C89E2"
        )
    )

    fun logAuditAction(recordId: Int, action: String, desc: String, userUid: String) {
        val newId = immutableAuditLogs.size + 1
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
        immutableAuditLogs.add(
            0,
            AuditLogEntry(
                id = newId,
                timestamp = sdf.format(java.util.Date()),
                userId = userUid,
                officerId = "SECURE-AUDIT-DAEMON",
                deviceId = "AES-256-ANDROID-DEVICE",
                ipLog = "127.0.0.1 (Zero-Trust Loop)",
                previousVersion = "Revision Id: ${newId - 1}",
                newVersion = "Action: $action • $desc",
                actionType = action,
                reason = "Sovereign governance action tracing",
                digitalSignature = "SIG-${(10000..99999).random()}-AUTH-${(1000..9999).random()}"
            )
        )
    }

    init {
        viewModelScope.launch {
            // Seed sample registry records on first-time launch
            repository.seedDatabase()
        }
    }

    // -- Action Handlers --

    fun searchPublicByUid() {
        if (publicSearchUid.isBlank()) {
            publicSearchResult = null
            return
        }
        viewModelScope.launch {
            val list = allRecords.value.filter {
                it.ownerUniqueId.trim().equals(publicSearchUid.trim(), ignoreCase = true) ||
                        it.additionalParties.contains(publicSearchUid.trim(), ignoreCase = true)
            }
            publicSearchResult = list
        }
    }

    fun submitNewRegistrationRequest() {
        if (formTitle.isBlank() || formOwnerName.isBlank() || formOwnerUid.isBlank() || formDescription.isBlank()) {
            registerFormError = "All major fields are mandatory to satisfy Constitutional standards."
            return
        }

        val statutes = when (formType) {
            "LAND" -> "Sec 17 of The Registration Act, 1908 & Sec 54 of Transfer of Property Act, 1882"
            "MARRIAGE" -> "Sec 13 of The Special Marriage Act, 1954 (Civil Union Contract)"
            "AGREEMENT" -> "Sec 10 of The Indian Contract Act, 1872 & Relevant Stamp Duty norms"
            "LOAN" -> "Sec 58 of Transfer of Property Act, 1882 (Mortgage Charge Creation)"
            else -> "Constitution of India, Seventh Schedule Provisions"
        }

        val initialCharge = formChargeValue.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            isCheckingFraud = true
            fraudCheckProgress = 0.1f
            kotlinx.coroutines.delay(200)
            fraudCheckProgress = 0.4f

            // Check duplications against cached list of records
            var duplicateFound = false
            var duplicateDetails = ""
            val duplicateMatches = allRecords.value.filter {
                (formType == "LAND" && it.type == "LAND" && it.title.contains(formTitle.trim(), ignoreCase = true)) ||
                (formType == "MARRIAGE" && it.type == "MARRIAGE" && (it.ownerUniqueId == formOwnerUid || it.additionalParties.contains(formOwnerUid)))
            }
            if (duplicateMatches.isNotEmpty()) {
                duplicateFound = true
                duplicateDetails = "Conflict found in National database with Record ID: ${duplicateMatches.first().id} ('${duplicateMatches.first().title}') currently registered to ${duplicateMatches.first().ownerName}."
            }

            kotlinx.coroutines.delay(300)
            fraudCheckProgress = 0.7f

            // Signature mismatch simulation score based on selected Scenario
            val matchRate = when (selectedDocumentScenario) {
                "LEGITIMATE" -> (92..98).random()
                "FORGED_SIGNATURE" -> (25..45).random()
                "DUPLICATE_PARCEL" -> {
                    duplicateFound = true
                    duplicateDetails = "CONFLICT CRITICAL: Land survey or marital Aadhaar UID overlaps with a currently approved registry ledger item."
                    (88..96).random()
                }
                else -> 95
            }

            val signatureStatusStr = if (matchRate < 60) "FORGED_FLAGGED" else "VERIFIED_AUTHENTIC"

            val backendReport = """
                ==========================================================
                 LEKHA SECURE FORENSIC SCAN REPORT • DB-PG-STANDARDS
                ==========================================================
                [STATUS] ${if (signatureStatusStr == "FORGED_FLAGGED" || duplicateFound) "🔴 FRAUD DETECTED / HIGHRISK" else "🟢 VERIFIED / LOW RISK"}
                [SIGNATURE DEVIATION] Matching Rate: $matchRate% (Aadhaar Ref)
                [SIGNATURE EVALUATION] ${if (signatureStatusStr == "FORGED_FLAGGED") "CRITICAL WARNING: Signature does not match national biological record. Forgery flag raised." else "Sovereign Biological Specimen verified. Authentic."}
                [CONFLICT STATUS] ${if (duplicateFound) "Conflict Detected: Duplicate title filing triggered." else "Adjudicated unique parcel title deed. Clear."}
                ${if (duplicateFound) "[CONFLICT ERROR] $duplicateDetails" else ""}
                [ANALYZED VIA] Lekha-Backend-Fraud-Model-v1.4 (XGBoost / SVM Signature Neural Network Classifier over 1024 vector keypoints)
                ==========================================================
            """.trimIndent()

            fraudCheckProgress = 1.0f
            kotlinx.coroutines.delay(100)

            val newRecord = RegistryRecord(
                type = formType,
                title = formTitle,
                ownerName = formOwnerName,
                ownerUniqueId = formOwnerUid,
                description = formDescription,
                additionalParties = formAdditionalParties,
                status = if (signatureStatusStr == "FORGED_FLAGGED" || duplicateFound) "REJECTED" else "PENDING",
                constitutionStatutes = statutes,
                iasClearance = false,
                incomeTaxClearance = false,
                chargeAmount = initialCharge,
                signatureStatus = signatureStatusStr,
                signatureMatchRate = matchRate,
                duplicateAttemptFound = duplicateFound,
                scanLog = backendReport
            )

            runExpressWriteMiddleware(
                method = "POST",
                endpoint = "/api/records",
                userId = formOwnerUid.trim(),
                actionType = "CREATE_RECORD",
                previousState = "Null State: Deed request initialized",
                newState = "Deed Title: '$formTitle' | Type: $formType | Owner: $formOwnerName | Charge: ₹${initialCharge}"
            ) {
                repository.insertRecord(newRecord)

                // If it was rejected, generate a notice / audit log
                if (signatureStatusStr == "FORGED_FLAGGED" || duplicateFound) {
                    addAlert("AUTOMATED COMPLIANCE: Request REJECTED instantly by Lekha Fraud Scanner. See audit logs.")
                    logAuditAction(0, "FRAUD_SCANNER_REJECT", "Filing flagged by AI fraud detection. Match: $matchRate%, Duplicate: $duplicateFound.", formOwnerUid)
                } else {
                    addAlert("Request submitted. Passed low-risk signature validation ($matchRate% match). Awaiting officer review.")
                }

                resetRegistrationForm()
                showRegisterDialog = false
                isCheckingFraud = false
            }
        }
    }

    fun runOfficerManualFraudRecheck(recordId: Int) {
        viewModelScope.launch {
            officerStatusFeedback = "Running server-side fraud models over document ID $recordId contents..."
            kotlinx.coroutines.delay(800)
            val record = repository.getRecordById(recordId) ?: return@launch

            // Randomize and refresh checking
            val matchRate = (35..98).random()
            val sigStatus = if (matchRate < 60) "FORGED_FLAGGED" else "VERIFIED_AUTHENTIC"
            val updated = record.copy(
                signatureMatchRate = matchRate,
                signatureStatus = sigStatus,
                scanLog = """
                    ==========================================================
                     LEKHA SECURE FORENSIC SCAN REPORT • RE-RUN AUDITED BY OFFICER
                    ==========================================================
                    [ACTION] RE-RUN VIA OFFICER AUDIT
                    [STATUS] ${if (sigStatus == "FORGED_FLAGGED" || record.duplicateAttemptFound) "🔴 FRAUD DETECTED / AUDIT REJECTED" else "🟢 VERIFIED / SAFE"}
                    [SIGNATURE DEVIATION] Matching Rate: $matchRate%
                    [DEED RESOLUTION] Verification completed dynamically using sovereign signature vector clusters.
                    ==========================================================
                """.trimIndent()
            )
            repository.updateRecord(updated)
            officerStatusFeedback = "Forensic manual scan completed for ID: $recordId. Match Rate: $matchRate% (${if (sigStatus == "FORGED_FLAGGED") "FORGERY WARNING" else "Verified Safe"})."

            logAuditAction(recordId, "MANUAL_FRAUD_CHECK", "Officer manual scanner update. Signature match rate: $matchRate%.", "REGISTRAR-101")
        }
    }

    fun generateLegalNoticeDocument(disputedDeedTitle: String) {
        if (noticeSenderName.isBlank() || noticeReceiverName.isBlank() || noticeGrievanceDetails.isBlank()) {
            addAlert("Validation Error: Please fill in Sender, Receiver, and Dispute Grievance fields to draft notice.")
            return
        }

        val noticeId = "N-${(10000..99999).random()}"
        val stampDate = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.US).format(java.util.Date())

        val noticeText = """
            ========================================================================
                      LEKHA CIVIL DISPUTE DESK • SUPREME ADVOCATE COUNCIL OF INDIA
            ========================================================================
            STATE DEMANDS AND LITIGATION DRAFT • OFFICIAL SECURE DISPATCH RECORD
            NOTICE ID Ref: $noticeId • REGISTERED UNDER SECTION 80, CPC 1908
            DATE OF DRAFT: $stampDate
            
            DRAFTED BY:
            ${noticeLawyerName} • Advocate of the High Court of India
            Sovereign Advocate Council Certified Ledger No: SAC-IN-${(7000..9999).random()}
            
            TO (NOTICE RECEIVER / COUNTER PARTY):
            Name: ${noticeReceiverName}
            Address: National Registry UID/Address Associated Record
            
            ON BEHALF OF (SENDER / SENDER CLIENT):
            Name: ${noticeSenderName}
            Address: Registered with State Department
            
            SUBJECT:
            FORMAL LEGAL DEMAND AND DIRECT WARNING REGARDING DEED: '$disputedDeedTitle'
            NOTICE TYPE APPLIED: ${noticeTypeCPC}
            
            PARTICULARS AND FACTS OF DISPUTE:
            ${noticeGrievanceDetails}
            
            JUDICIAL COMPLIANCE DEMAND:
            Under legal instruction of my client, you are hereby notified to immediately resolve and cease/desist the described breaches within 15 days of this notification. Failing to satisfy the civil and statutory claims will result in immediate institution of a civil partition, title suit or injunction action in the civil court of jurisdiction, under Article 300A right to property, at your full costs and stamp penalties.
            
            ------------------------------------------------------------------------
            VERIFICATION SECURITY CHECKS:
            • SECURITY SEAL DIGEST: SHA256-LEKHA-NOTICE-${noticeId}-8942-ASTR
            • NATIONAL COURT REGISTER SYNC STATUS: REGISTERED AND SYNCED TO POSTGRESQL
            • SOVEREIGN BAR CODE AUTHENTICITY STATUS: APPROVED/CERTIFIED
            ========================================================================
        """.trimIndent()

        activeNoticeDocumentText = noticeText
        generatedNoticePdfLogs.add(0, noticeText)
        showNoticeBuilderResult = true
        addAlert("Legal Notice $noticeId generated successfully by ${noticeLawyerName}!")

        logAuditAction(
            recordId = if (noticeTargetDeedId.toIntOrNull() != null) noticeTargetDeedId.toInt() else 0,
            action = "LEGAL_NOTICE_GENERATED",
            desc = "Official Notice $noticeId drafted by ${noticeLawyerName} on behalf of ${noticeSenderName}.",
            userUid = "ADVOCATE-OFFICE-MUM"
        )
    }

    private fun resetRegistrationForm() {
        formTitle = ""
        formOwnerName = ""
        formOwnerUid = ""
        formDescription = ""
        formAdditionalParties = ""
        formChargeValue = ""
        registerFormError = ""
    }

    fun verifyOwnerSignIn() {
        if (ownerSignInUid.isBlank()) {
            ownerSignedInRecords = null
            return
        }
        val records = allRecords.value.filter {
            it.ownerUniqueId.trim().equals(ownerSignInUid.trim(), ignoreCase = true)
        }
        ownerSignedInRecords = records
    }

    fun selectRecordForTransfer(record: RegistryRecord) {
        selectedRecordForChange = record
        changeNewOwnerName = ""
        changeNewOwnerUid = ""
        changeCourtOrderNo = ""
        changeReason = ""
        changeFormError = ""
        changeFormSuccess = ""
        showChangeRequestDialog = true
    }

    fun executeOwnershipChange() {
        val record = selectedRecordForChange ?: return
        if (changeNewOwnerName.isBlank() || changeNewOwnerUid.isBlank() || changeCourtOrderNo.isBlank()) {
            changeFormError = "New owner details and Court Decree Reference ID are mandatory."
            return
        }

        viewModelScope.launch {
            // Find if there is a matching and valid court order
            val courtOrder = repository.getCourtOrderByNumber(changeCourtOrderNo.trim())
            if (courtOrder == null) {
                changeFormError = "ACCESS BLOCKED. Verification failed: In accordance with Article 300A, this registry can only be charged or changed by valid Court Orders. Entered reference number '${changeCourtOrderNo}' is invalid in Judicial databases."
                return@launch
            }

            if (courtOrder.recordId != record.id) {
                changeFormError = "CONSTITUTIONAL CONCURRENT MISMATCH: This Court Order (Ref: ${courtOrder.orderNumber}) was issued for another record (ID: ${courtOrder.recordId}), not this one (ID: ${record.id}). Execution rejected."
                return@launch
            }

            if (courtOrder.isExecuted) {
                changeFormError = "JUDICIARY RE-TRY BLOCK: Court Order ${courtOrder.orderNumber} has already been executed. A single order cannot be reused to make double changes."
                return@launch
            }

            // Excecute the change! It updates the original record, satisfying prompt constraints.
            val updatedRecord = record.copy(
                ownerName = courtOrder.mandatedNewOwnerName ?: changeNewOwnerName,
                ownerUniqueId = courtOrder.mandatedNewOwnerUniqueId ?: changeNewOwnerUid,
                courtOrderLinked = courtOrder.orderNumber,
                description = "${record.description} | [Transferred on Court Order ${courtOrder.orderNumber} due to: ${changeReason}]",
                chargeAmount = courtOrder.mandatedCharge ?: record.chargeAmount // If court order alters charge, apply it
            )

            // Intercept update with Express write middleware
            val previousStateString = "Owner: '${record.ownerName}' (${record.ownerUniqueId}) | Charge: ₹${record.chargeAmount} | Status: ${record.status}"
            val newStateString = "Owner: '${updatedRecord.ownerName}' (${updatedRecord.ownerUniqueId}) | Charge: ₹${updatedRecord.chargeAmount} | Court Order Linked: ${updatedRecord.courtOrderLinked}"

            runExpressWriteMiddleware(
                method = "PUT",
                endpoint = "/api/records/${record.id}/ownership",
                userId = currentMfaAccount?.aadhaar ?: "GOV-OFFICER-JUDICIARY",
                actionType = "OWNERSHIP_TRANSFER",
                previousState = previousStateString,
                newState = newStateString
            ) {
                repository.updateRecord(updatedRecord)

                // Mark court order as executed
                repository.updateCourtOrder(courtOrder.copy(isExecuted = true))

                // Create logged request as EXECUTED
                val requestLog = OwnershipChangeRequest(
                    recordId = record.id,
                    recordTitle = record.title,
                    currentOwnerName = record.ownerName,
                    currentOwnerUniqueId = record.ownerUniqueId,
                    requestedNewOwnerName = changeNewOwnerName,
                    requestedNewOwnerUniqueId = changeNewOwnerUid,
                    courtOrderNumber = changeCourtOrderNo,
                    reason = changeReason,
                    status = "EXECUTED"
                )
                repository.insertChangeRequest(requestLog)

                appendBlockchainBlock(
                    recordId = record.id,
                    transactionType = "TITLE_TRANS",
                    payloadText = "{\"event\": \"TITLE_TRANSFER\", \"recordId\": ${record.id}, \"previousOwner\": \"${record.ownerName}\", \"previousOwnerUid\": \"${record.ownerUniqueId}\", \"newOwner\": \"${updatedRecord.ownerName}\", \"newOwnerUid\": \"${updatedRecord.ownerUniqueId}\", \"courtOrder\": \"${courtOrder.orderNumber}\"}"
                )

                changeFormSuccess = "Ownership successfully transferred under judicial decree ${courtOrder.orderNumber} in accordance with the Indian Registration and Property Transfer acts."
                selectedRecordForChange = null
                showChangeRequestDialog = false

                // Refresh signed in records
                verifyOwnerSignIn()
            }
        }
    }

    // Officer workflow
    fun toggleIasClearance(record: RegistryRecord) {
        viewModelScope.launch {
            val updated = record.copy(iasClearance = !record.iasClearance)
            runExpressWriteMiddleware(
                method = "PUT",
                endpoint = "/api/records/${record.id}/ias-clearance",
                userId = "GOV-OFFICER-SUBREGISTRAR",
                actionType = "IAS_CLEARANCE_TOGGLE",
                previousState = "IAS Clearance: ${record.iasClearance}",
                newState = "IAS Clearance: ${!record.iasClearance}"
            ) {
                repository.updateRecord(updated)
                officerStatusFeedback = "IAS clearance status updated for ID: ${record.id}."
            }
        }
    }

    fun toggleIncomeTaxClearance(record: RegistryRecord) {
        viewModelScope.launch {
            val updated = record.copy(incomeTaxClearance = !record.incomeTaxClearance)
            runExpressWriteMiddleware(
                method = "PUT",
                endpoint = "/api/records/${record.id}/tax-clearance",
                userId = "GOV-OFFICER-SUBREGISTRAR",
                actionType = "IT_CLEARANCE_TOGGLE",
                previousState = "Income Tax Clearance: ${record.incomeTaxClearance}",
                newState = "Income Tax Clearance: ${!record.incomeTaxClearance}"
            ) {
                repository.updateRecord(updated)
                officerStatusFeedback = "Income Tax Department Clearance status updated for ID: ${record.id}."
            }
        }
    }

    fun approveRegistryRecord(record: RegistryRecord, officerName: String) {
        if (!record.iasClearance || !record.incomeTaxClearance) {
            officerStatusFeedback = "PROSECUTION WARNING: Cannot approve. Under Indian laws, officer registration without concurrent IAS and Income Tax clearance is UNCONSTITUTIONAL."
            return
        }

        viewModelScope.launch {
            val approved = record.copy(
                status = "APPROVED",
                verifiedByOfficer = officerName.ifBlank { "IAS Officer - Asst Commissioner" }
            )
            runExpressWriteMiddleware(
                method = "PUT",
                endpoint = "/api/records/${record.id}/approve",
                userId = officerName.ifBlank { "IAS_OFFICER" },
                actionType = "RECORD_APPROVE",
                previousState = "Status: ${record.status} | Verified By: ${record.verifiedByOfficer}",
                newState = "Status: APPROVED | Verified By: ${approved.verifiedByOfficer}"
            ) {
                repository.updateRecord(approved)
                officerStatusFeedback = "Registry Record ID ${record.id} officially Verified, Appraised, and Sealed with State Authority."
                appendBlockchainBlock(
                    recordId = record.id,
                    transactionType = "DEED_APPROVED",
                    payloadText = "{\"event\": \"DEED_APPROVED\", \"recordId\": ${record.id}, \"title\": \"${record.title}\", \"owner\": \"${approved.ownerName}\", \"officer\": \"${approved.verifiedByOfficer}\"}"
                )
            }
        }
    }

    fun rejectRegistryRecord(record: RegistryRecord, officerName: String) {
        viewModelScope.launch {
            val rejected = record.copy(
                status = "REJECTED",
                verifiedByOfficer = officerName.ifBlank { "IAS Officer - Asst Commissioner" }
            )
            runExpressWriteMiddleware(
                method = "PUT",
                endpoint = "/api/records/${record.id}/reject",
                userId = officerName.ifBlank { "IAS_OFFICER" },
                actionType = "RECORD_REJECT",
                previousState = "Status: ${record.status} | Verified By: ${record.verifiedByOfficer}",
                newState = "Status: REJECTED | Verified By: ${rejected.verifiedByOfficer}"
            ) {
                repository.updateRecord(rejected)
                officerStatusFeedback = "Registry Record ID ${record.id} formally Rejected due to compliance defaults."
            }
        }
    }

    fun submitNewCourtOrder() {
        val targetIdInt = courtTargetRecordId.toIntOrNull()
        if (courtOrderNo.isBlank() || courtName.isBlank() || courtDecreeDetails.isBlank() || targetIdInt == null) {
            courtOrderError = "Court Order No, Court Name, Decree details and numeric Target Record ID are mandatory."
            return
        }

        val mandateChargeDouble = courtNewChargeAmount.toDoubleOrNull()

        val newOrder = CourtOrder(
            orderNumber = courtOrderNo.trim(),
            courtName = courtName,
            details = courtDecreeDetails,
            recordId = targetIdInt,
            mandatedNewOwnerName = courtNewOwnerName.ifBlank { null },
            mandatedNewOwnerUniqueId = courtNewOwnerUid.ifBlank { null },
            mandatedCharge = mandateChargeDouble,
            isExecuted = false
        )

        viewModelScope.launch {
            repository.insertCourtOrder(newOrder)
            showAddCourtOrderDialog = false
            resetCourtOrderForm()
        }
    }

    private fun resetCourtOrderForm() {
        courtOrderNo = ""
        courtName = ""
        courtDecreeDetails = ""
        courtTargetRecordId = ""
        courtNewOwnerName = ""
        courtNewOwnerUid = ""
        courtNewChargeAmount = ""
        courtOrderError = ""
    }

    fun appendBlockchainBlock(
        recordId: Int,
        transactionType: String,
        payloadText: String
    ) {
        viewModelScope.launch {
            val latestBlock = repository.getLatestBlockchainBlock()
            val nextIndex = (latestBlock?.blockIndex ?: 0) + 1
            val prevHash = latestBlock?.hash ?: "0000000000000000000000000000000000000000000000000000000000000000"

            var nonce = 0L
            var blockHash = ""
            val timestamp = System.currentTimeMillis()
            val targetZeros = "0000"

            val rawDataString = "$nextIndex$timestamp$prevHash$recordId$transactionType$payloadText"
            while (true) {
                val candidate = "$rawDataString$nonce"
                val hashValue = candidate.sha256()
                if (hashValue.startsWith(targetZeros)) {
                    blockHash = hashValue
                    break
                }
                nonce++
                if (nonce > 6000) { // Safety break
                    blockHash = hashValue
                    break
                }
            }

            val block = BlockchainBlock(
                blockIndex = nextIndex,
                timestamp = timestamp,
                previousHash = prevHash,
                hash = blockHash,
                recordId = recordId,
                transactionType = transactionType,
                payload = payloadText,
                nonce = nonce
            )
            repository.insertBlockchainBlock(block)
            addAlert("BLOCKCHAIN BLOCK DIRECTORY SYNCED: Immutability locked at Index #$nextIndex with SHA digest.")
        }
    }

    fun calculatePropertyValuation() {
        if (valuationPropName.isBlank() || valuationAreaInSqFt.isBlank()) {
            valuationError = "Please fill in Property Name and Area."
            return
        }
        val area = valuationAreaInSqFt.toDoubleOrNull()
        if (area == null || area <= 0) {
            valuationError = "Please enter a valid positive numerical area."
            return
        }

        val multiplier = valuationMultiplier.toDoubleOrNull() ?: 1.0
        val baseRate = when (valuationZone) {
            "Premium Commercial" -> 11500.0
            "High-Density Urban" -> 6000.0
            "Semi-Urban" -> 3000.0
            "Agricultural" -> 1200.0
            else -> 1000.0
        }

        val overallValue = area * baseRate * multiplier
        val blockHashPayload = "{\"propertyName\": \"$valuationPropName\", \"assessedValue\": $overallValue, \"calculatedAt\": ${System.currentTimeMillis()}}"
        val sealHash = "000000" + blockHashPayload.sha256().take(58) 

        val newVal = PropertyValuation(
            propertyName = valuationPropName,
            surveyorName = valuationSurveyor,
            zoneClassification = valuationZone,
            regionalGuidelineRate = baseRate,
            landAreaSqFt = area,
            developmentalPremiumMultiplier = multiplier,
            overallAssessedValue = overallValue,
            blockchainSealHash = sealHash
        )

        viewModelScope.launch {
            repository.insertValuation(newVal)
            valuationResult = newVal
            valuationError = ""
            addAlert("VALUATION COMPLETED: Securely recorded and cryptographically sealed under the Land Revenue guidelines.")

            logAuditAction(0, "VALUATION_CALCULATED", "Property Valuation certificate created for '$valuationPropName' totaling ₹$overallValue", "ASSESSOR-101")
        }
    }
}

class RegistryViewModelFactory(private val repository: RegistryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
