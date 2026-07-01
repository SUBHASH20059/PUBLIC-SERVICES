package org.psei.offline

import kotlinx.coroutines.*
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

// ═════════════════════════════════════════════════════════════════════════════
// CRDT OFFLINE SYNC  (Conflict-free Replicated Data Types)
// ═════════════════════════════════════════════════════════════════════════════

enum class SyncOpType { ADD, UPDATE, DELETE }

data class SyncOperation(
    val id: String = UUID.randomUUID().toString(),
    val type: SyncOpType,
    val documentId: String,
    val payload: ByteArray? = null,          // Encrypted document bytes
    val crdtDelta: Map<String, Any>? = null, // Partial update delta
    val vectorClock: Long = System.currentTimeMillis(),
    val timestamp: String = LocalDateTime.now().toString()
)

data class PendingOperation(
    val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val payload: ByteArray,
    val timestamp: String = LocalDateTime.now().toString(),
    val vectorClock: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

enum class OperationType { UPLOAD, UPDATE, DELETE, VERIFY }

/**
 * Offline-first document vault using Merkle-DAG + LWW (Last-Write-Wins) CRDT.
 *
 * Works in zero-connectivity areas — operations are queued locally and
 * efficiently synced when connectivity returns using Merkle tree diffs.
 *
 * Production: use SQLDelight (Android/iOS) + KTOR sync endpoint with
 * delta-sync. This implements the server-side sync engine.
 */
class CRDTSyncEngine {

    // Merkle root → tree of document hashes
    private val remoteMerkleRoots = mutableMapOf<String, String>()
    private val documentStore = mutableMapOf<String, ByteArray>()
    private val tombstones = mutableSetOf<String>()

    fun getRemoteMerkleRoot(citizenId: String): String =
        remoteMerkleRoots.getOrDefault(citizenId, computeMerkleRoot(emptyList()))

    fun computeDiff(localRoot: String, remoteRoot: String): List<SyncOperation> {
        if (localRoot == remoteRoot) return emptyList()
        // In production: traverse Merkle tree layer by layer,
        // returning only differing subtrees (O(diff) not O(total))
        return emptyList()  // stub — returns empty when roots match
    }

    fun applyOperation(citizenId: String, op: SyncOperation) {
        when (op.type) {
            SyncOpType.ADD    -> op.payload?.let { documentStore[op.documentId] = it }
            SyncOpType.UPDATE -> op.crdtDelta?.let { applyLWWDelta(op.documentId, it) }
            SyncOpType.DELETE -> tombstones.add(op.documentId)
        }
        recomputeMerkleRoot(citizenId)
    }

    private fun applyLWWDelta(documentId: String, delta: Map<String, Any>) {
        // LWW: last writer wins — highest vectorClock value persists
        // In production: compare vector clocks, merge non-conflicting fields
    }

    private fun recomputeMerkleRoot(citizenId: String) {
        val activeIds = documentStore.keys.filter { it !in tombstones }.sorted()
        remoteMerkleRoots[citizenId] = computeMerkleRoot(activeIds)
    }

    private fun computeMerkleRoot(ids: List<String>): String {
        if (ids.isEmpty()) return sha256("empty")
        var root = sha256(ids.first())
        ids.drop(1).forEach { id -> root = sha256("$root|${sha256(id)}") }
        return root
    }

    private fun sha256(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
}

/**
 * Client-side offline vault (runs on citizen's device).
 * Queues operations when offline, syncs when connected.
 */
class OfflineDocumentVault(
    private val citizenId: String,
    private val syncEngine: CRDTSyncEngine = CRDTSyncEngine()
) {
    private val pendingQueue = mutableListOf<PendingOperation>()
    private var localMerkleRoot = "empty"
    private var vectorClock = 0L

    /** Called when network is available — syncs all queued operations. */
    suspend fun syncWhenConnected(): SyncResult = coroutineScope {
        val remoteRoot = syncEngine.getRemoteMerkleRoot(citizenId)

        if (localMerkleRoot == remoteRoot && pendingQueue.isEmpty()) {
            return@coroutineScope SyncResult(synced = 0, conflicts = 0)
        }

        // Upload pending operations
        val uploaded = pendingQueue.mapNotNull { op ->
            runCatching {
                syncEngine.applyOperation(citizenId,
                    SyncOperation(
                        type = when (op.type) {
                            OperationType.UPLOAD -> SyncOpType.ADD
                            OperationType.UPDATE -> SyncOpType.UPDATE
                            OperationType.DELETE -> SyncOpType.DELETE
                            else -> SyncOpType.UPDATE
                        },
                        documentId = UUID.randomUUID().toString(),
                        payload = op.payload,
                        vectorClock = op.vectorClock
                    )
                )
                op
            }.getOrNull()
        }

        pendingQueue.removeAll(uploaded.toSet())
        localMerkleRoot = syncEngine.getRemoteMerkleRoot(citizenId)

        SyncResult(synced = uploaded.size, conflicts = pendingQueue.size - uploaded.size)
    }

    /** Queue a document upload for later sync. */
    fun queueUpload(encryptedDocument: ByteArray): PendingOperation {
        vectorClock++
        return PendingOperation(
            type = OperationType.UPLOAD,
            payload = encryptedDocument,
            vectorClock = vectorClock
        ).also { pendingQueue.add(it) }
    }

    fun pendingCount(): Int = pendingQueue.size
    fun localRoot(): String = localMerkleRoot
}

data class SyncResult(val synced: Int, val conflicts: Int)

// ═════════════════════════════════════════════════════════════════════════════
// WHATSAPP BOT — Multilingual Intent Router
// ═════════════════════════════════════════════════════════════════════════════

enum class BotIntent {
    SCHEME_DISCOVERY,
    DOCUMENT_VAULT,
    IDEA_REGISTER,
    PATENT_FILE,
    BUSINESS_REGISTER,
    PROPERTY_INFO,
    OFFICER_ALERT,
    UNKNOWN
}

data class ParsedIntent(
    val intent: BotIntent,
    val confidence: Float,
    val language: String,
    val extractedEntities: Map<String, String> = emptyMap()
)

data class WhatsAppMessage(
    val from: String,           // Citizen's phone number
    val messageId: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "text"   // text, image, document
)

data class WhatsAppReply(
    val to: String,
    val body: String,
    val buttons: List<QuickReplyButton> = emptyList(),
    val documentUrl: String? = null
)

data class QuickReplyButton(val id: String, val title: String)

/**
 * Multilingual NLP intent router for WhatsApp bot.
 *
 * Supports Hindi, Tamil, Telugu, Kannada, Bengali, Marathi, Gujarati, English.
 * Production: connect to Google Dialogflow CX / AWS Lex / IndicBERT for
 * accurate multilingual NLP. This implementation uses keyword matching
 * as a deterministic fallback.
 */
class WhatsAppIntentRouter {

    // Multilingual keyword patterns per intent
    private val intentPatterns = mapOf(
        BotIntent.SCHEME_DISCOVERY to listOf(
            // English
            "scheme", "benefit", "subsidy", "yojana", "apply for",
            // Hindi
            "स्कीम", "योजना", "सब्सिडी", "लाभ", "आवेदन",
            // Telugu
            "పథకం", "సహాయం", "యోజన",
            // Tamil
            "திட்டம்", "பலன்", "மானியம்"
        ),
        BotIntent.DOCUMENT_VAULT to listOf(
            "document", "certificate", "aadhar", "pan", "passport", "vault", "store",
            "दस्तावेज़", "प्रमाण पत्र", "आधार", "पासपोर्ट",
            "పత్రం", "ஆவணம்"
        ),
        BotIntent.IDEA_REGISTER to listOf(
            "idea", "invention", "protect", "intellectual", "ip",
            "आइडिया", "विचार", "संरक्षण", "बौद्धिक",
            "ఆలోచన", "கருத்து"
        ),
        BotIntent.PATENT_FILE to listOf(
            "patent", "file patent", "ip india", "पेटेंट", "పేటెంట్", "காப்புரிமை"
        ),
        BotIntent.BUSINESS_REGISTER to listOf(
            "business", "company", "register", "gst", "msme", "startup",
            "व्यापार", "कंपनी", "पंजीकरण", "స్టార్టప్", "வணிகம்"
        ),
        BotIntent.PROPERTY_INFO to listOf(
            "property", "land", "house", "plot", "transfer", "sale deed",
            "संपत्ति", "जमीन", "भूमि", "ఆస్తి", "சொத்து"
        ),
        BotIntent.OFFICER_ALERT to listOf(
            "urgent", "fraud", "fake", "report", "alert",
            "धोखा", "अर्जेंट", "शिकायत", "మోసం", "அவசரம்"
        )
    )

    fun parse(message: WhatsAppMessage): ParsedIntent {
        val text = message.body.lowercase()
        val language = detectLanguage(message.body)

        var bestIntent = BotIntent.UNKNOWN
        var bestScore = 0.0f

        intentPatterns.forEach { (intent, keywords) ->
            val matches = keywords.count { kw -> text.contains(kw, ignoreCase = true) }
            val score = matches.toFloat() / keywords.size.toFloat() * 2f
            if (score > bestScore) {
                bestScore = score
                bestIntent = intent
            }
        }

        // Extract entities
        val entities = extractEntities(text)

        return ParsedIntent(
            intent = bestIntent,
            confidence = bestScore.coerceIn(0f, 1f),
            language = language,
            extractedEntities = entities
        )
    }

    private fun detectLanguage(text: String): String {
        return when {
            text.any { it in '\u0900'..'\u097F' } -> "hindi"
            text.any { it in '\u0C00'..'\u0C7F' } -> "telugu"
            text.any { it in '\u0B80'..'\u0BFF' } -> "tamil"
            text.any { it in '\u0C80'..'\u0CFF' } -> "kannada"
            text.any { it in '\u0980'..'\u09FF' } -> "bengali"
            text.any { it in '\u0A80'..'\u0AFF' } -> "gujarati"
            else -> "english"
        }
    }

    private fun extractEntities(text: String): Map<String, String> {
        val entities = mutableMapOf<String, String>()

        // Age extraction
        val ageMatch = Regex("""(\d{1,2})\s*(?:years?|साल|वर्ष)""").find(text)
        ageMatch?.let { entities["age"] = it.groupValues[1] }

        // State extraction (major states)
        val states = listOf("maharashtra", "gujarat", "rajasthan", "up", "mp",
            "karnataka", "telangana", "andhra", "tamilnadu", "kerala",
            "punjab", "haryana", "bihar", "bengal", "odisha")
        states.firstOrNull { text.contains(it) }?.let { entities["state"] = it }

        // Amount extraction
        val amountMatch = Regex("""(\d+)\s*(?:lakh|crore|thousand|₹)""").find(text)
        amountMatch?.let { entities["amount"] = it.groupValues[1] }

        return entities
    }
}

/**
 * WhatsApp Bot Response Generator.
 * Generates appropriate replies for each intent in the user's language.
 */
class WhatsAppBotResponseGenerator(
    private val schemeEngine: org.psei.ml.SchemeRecommendationEngine =
        org.psei.ml.SchemeRecommendationEngine()
) {

    suspend fun generateReply(
        message: WhatsAppMessage,
        intent: ParsedIntent
    ): WhatsAppReply {
        return when (intent.intent) {

            BotIntent.SCHEME_DISCOVERY -> {
                val age = intent.extractedEntities["age"]?.toIntOrNull() ?: 25
                val profile = org.psei.ml.CitizenProfile(
                    citizenId = message.from,
                    age = age,
                    gender = "M",
                    state = intent.extractedEntities["state"] ?: "national",
                    annualIncomeINR = 3_00_000L,
                    occupation = "GENERAL",
                    educationLevel = "SECONDARY"
                )
                val schemes = schemeEngine.recommend(profile, limit = 3)
                val body = if (schemes.isEmpty()) {
                    noSchemesMessage(intent.language)
                } else {
                    schemes.joinToString("\n\n") { s ->
                        "✅ *${s.schemeName}*\n${s.benefitSummary}" +
                                if (s.estimatedBenefitINR != null) "\nBenefit: ₹${formatINR(s.estimatedBenefitINR)}" else ""
                    }
                }
                WhatsAppReply(
                    to = message.from,
                    body = body,
                    buttons = schemes.take(3).map { QuickReplyButton(it.schemeId, "Apply: ${it.schemeName.take(20)}") }
                )
            }

            BotIntent.DOCUMENT_VAULT ->
                WhatsAppReply(
                    to = message.from,
                    body = vaultMessage(intent.language),
                    buttons = listOf(
                        QuickReplyButton("upload_doc", "📄 Upload Document"),
                        QuickReplyButton("view_docs", "🔍 View My Documents"),
                        QuickReplyButton("share_cert", "🔗 Share Certificate")
                    )
                )

            BotIntent.IDEA_REGISTER ->
                WhatsAppReply(
                    to = message.from,
                    body = ideaMessage(intent.language),
                    buttons = listOf(
                        QuickReplyButton("register_idea", "💡 Register Idea"),
                        QuickReplyButton("file_patent", "📋 File Patent"),
                        QuickReplyButton("learn_ip", "ℹ️ Learn About IP")
                    )
                )

            BotIntent.PROPERTY_INFO ->
                WhatsAppReply(
                    to = message.from,
                    body = propertyMessage(intent.language),
                    buttons = listOf(
                        QuickReplyButton("prop_laws", "⚖️ Property Laws"),
                        QuickReplyButton("register_property", "🏠 Register Property"),
                        QuickReplyButton("tax_calc", "🧾 Calculate Tax")
                    )
                )

            else ->
                WhatsAppReply(
                    to = message.from,
                    body = helpMessage(intent.language),
                    buttons = listOf(
                        QuickReplyButton("schemes", "🎯 Schemes for Me"),
                        QuickReplyButton("documents", "📄 My Documents"),
                        QuickReplyButton("ideas", "💡 Protect My Idea"),
                        QuickReplyButton("property", "🏠 Property Help")
                    )
                )
        }
    }

    private fun helpMessage(lang: String) = when (lang) {
        "hindi" -> "🇮🇳 नमस्ते! PSEI Authority में आपका स्वागत है।\nआप क्या जानना चाहते हैं?"
        "telugu" -> "🇮🇳 నమస్కారం! PSEI Authority కి స్వాగతం.\nమీకు ఏమి కావాలి?"
        "tamil" -> "🇮🇳 வணக்கம்! PSEI Authority-க்கு வரவேற்கிறோம்.\nஎன்ன உதவி வேண்டும்?"
        else -> "🇮🇳 Welcome to PSEI Authority — India's National Digital Trust Platform.\nHow can I help you today?"
    }

    private fun noSchemesMessage(lang: String) = when (lang) {
        "hindi" -> "आपके लिए कोई स्कीम नहीं मिली। अधिक जानकारी के लिए अपनी प्रोफ़ाइल पूरी करें।"
        else -> "No matching schemes found. Please complete your profile for better matches."
    }

    private fun vaultMessage(lang: String) = when (lang) {
        "hindi" -> "📁 आपका सुरक्षित दस्तावेज़ वॉल्ट\nAES-256 एन्क्रिप्शन से सुरक्षित\n55+ दस्तावेज़ प्रकार समर्थित"
        else -> "📁 Your Secure Document Vault\nProtected with AES-256-GCM encryption\n55+ Indian document types supported"
    }

    private fun ideaMessage(lang: String) = when (lang) {
        "hindi" -> "💡 अपने विचार को तुरंत सुरक्षित करें!\nडिजिटल हस्ताक्षर के साथ टाइमस्टैम्प\nपेटेंट फाइलिंग में सहायता"
        else -> "💡 Protect your idea instantly!\nTimestamped with digital signature\nFull patent filing assistance available"
    }

    private fun propertyMessage(lang: String) = when (lang) {
        "hindi" -> "🏠 संपत्ति सेवाएं\nHRA 1882 • पंजीकरण अधिनियम 1908\nभूमि हस्तांतरण • विवाद दर्ज करें"
        else -> "🏠 Property Services\nTransfer of Property Act 1882 • Registration Act 1908\nLand Transfer • File Dispute • Tax Calculation"
    }

    private fun formatINR(amount: Long): String = when {
        amount >= 1_00_00_000L -> "${amount / 1_00_00_000L} Crore"
        amount >= 1_00_000L -> "${amount / 1_00_000L} Lakh"
        amount >= 1_000L -> "${amount / 1_000L}K"
        else -> amount.toString()
    }
}
