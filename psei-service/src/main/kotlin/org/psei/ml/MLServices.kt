package org.psei.ml

import kotlinx.coroutines.*
import org.psei.models.*
import java.time.LocalDateTime
import java.util.UUID

// ═════════════════════════════════════════════════════════════════════════════
// DOCUMENT FORGERY DETECTION  (Ensemble: CNN + LSTM + XGBoost)
// ═════════════════════════════════════════════════════════════════════════════

enum class ForgeryVerdict { AUTHENTIC, SUSPICIOUS, FRAUDULENT }

data class ForgeryAnalysisResult(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val verdict: ForgeryVerdict,
    val fraudScore: Float,               // 0.0 (clean) → 1.0 (forged)
    val confidence: Float,
    val analysedAt: String = LocalDateTime.now().toString(),
    val signals: List<ForgerySignal> = emptyList(),
    val reviewRequired: Boolean = false,
    val blockedImmediately: Boolean = false
)

data class ForgerySignal(
    val signalType: String,              // WATERMARK_MISMATCH, FONT_ANOMALY, etc.
    val severity: String,               // LOW, MEDIUM, HIGH, CRITICAL
    val description: String,
    val confidence: Float
)

/**
 * Three-model ensemble forgery detector.
 *
 * Production wiring:
 *   - CNN (ResNet-50): gRPC call to TensorFlow Serving for visual pattern detection
 *   - LSTM: gRPC call to OCR sequence model for text integrity
 *   - XGBoost: REST call to Python feature scorer for tabular metadata
 *
 * This implementation uses deterministic heuristic stubs that produce
 * realistic-looking scores; replace each stub with a real gRPC call.
 */
class DocumentForgeryDetectionService {

    private val fraudEventBus = FraudEventBus()

    suspend fun analyseDocument(
        documentId: String,
        documentBytes: ByteArray,
        metadata: DocumentMetadataFeatures
    ): ForgeryAnalysisResult = coroutineScope {

        // Three models run in parallel
        val cnnScore = async { runCNNModel(documentBytes) }
        val lstmScore = async { runLSTMModel(documentBytes) }
        val xgboostScore = async { runXGBoostModel(metadata) }

        // Soft voting ensemble (weights: CNN 40%, LSTM 30%, XGBoost 30%)
        val fraudScore = (cnnScore.await() * 0.40f) +
                (lstmScore.await() * 0.30f) +
                (xgboostScore.await() * 0.30f)

        val signals = buildSignalList(metadata, fraudScore)

        val verdict = when {
            fraudScore < 0.15f -> ForgeryVerdict.AUTHENTIC
            fraudScore > 0.85f -> ForgeryVerdict.FRAUDULENT
            else -> ForgeryVerdict.SUSPICIOUS
        }

        if (verdict == ForgeryVerdict.FRAUDULENT) {
            fraudEventBus.publish(FraudAlert(documentId, fraudScore,
                "Ensemble model: score $fraudScore exceeds FRAUDULENT threshold 0.85"))
        }

        ForgeryAnalysisResult(
            documentId = documentId,
            verdict = verdict,
            fraudScore = fraudScore,
            confidence = 1f - kotlin.math.abs(fraudScore - 0.5f) * 2f,
            signals = signals,
            reviewRequired = verdict == ForgeryVerdict.SUSPICIOUS,
            blockedImmediately = verdict == ForgeryVerdict.FRAUDULENT
        )
    }

    // ── Model stubs (replace with real gRPC calls in production) ──────────────

    private suspend fun runCNNModel(bytes: ByteArray): Float {
        // TODO: mlClient.predict(modelName="forgery_cnn_resnet50", inputs=imageTensor(bytes))
        delay(1) // simulate I/O
        val entropyScore = bytes.map { it.toInt() and 0xFF }.average().toFloat() / 255f
        return if (entropyScore > 0.6f) 0.08f else if (entropyScore < 0.3f) 0.72f else 0.20f
    }

    private suspend fun runLSTMModel(bytes: ByteArray): Float {
        // TODO: mlClient.predict(modelName="forgery_lstm_ocr_seq", inputs=ocrFeatures(bytes))
        delay(1)
        return 0.05f  // stub: most OCR sequences are clean
    }

    private suspend fun runXGBoostModel(meta: DocumentMetadataFeatures): Float {
        // TODO: REST POST to Python FastAPI XGBoost microservice
        delay(1)
        var score = 0.0f
        if (!meta.hasValidWatermark)   score += 0.35f
        if (meta.fontAnomalyDetected)  score += 0.20f
        if (meta.metadataDateMismatch) score += 0.25f
        if (!meta.issuingAuthorityKnown) score += 0.15f
        return score.coerceIn(0f, 1f)
    }

    private fun buildSignalList(meta: DocumentMetadataFeatures, score: Float): List<ForgerySignal> {
        val signals = mutableListOf<ForgerySignal>()
        if (!meta.hasValidWatermark)
            signals += ForgerySignal("WATERMARK_MISSING", "HIGH", "Expected watermark not found", 0.90f)
        if (meta.fontAnomalyDetected)
            signals += ForgerySignal("FONT_ANOMALY", "MEDIUM", "Non-standard font detected in official area", 0.70f)
        if (meta.metadataDateMismatch)
            signals += ForgerySignal("METADATA_DATE_MISMATCH", "HIGH", "PDF creation date inconsistent with issue date", 0.85f)
        return signals
    }
}

data class DocumentMetadataFeatures(
    val hasValidWatermark: Boolean = true,
    val fontAnomalyDetected: Boolean = false,
    val metadataDateMismatch: Boolean = false,
    val issuingAuthorityKnown: Boolean = true,
    val pageCount: Int = 1,
    val fileSizeBytes: Long = 0
)

// ═════════════════════════════════════════════════════════════════════════════
// SCHEME RECOMMENDATION ENGINE  (Hybrid: Rule + Collaborative + Content)
// ═════════════════════════════════════════════════════════════════════════════

data class CitizenProfile(
    val citizenId: String,
    val age: Int,
    val gender: String,
    val state: String,
    val annualIncomeINR: Long,
    val occupation: String,          // STUDENT, FARMER, ENTREPRENEUR, EMPLOYEE
    val educationLevel: String,      // PRIMARY, SECONDARY, GRADUATE, POSTGRADUATE
    val hasAadhar: Boolean = true,
    val hasBPLCard: Boolean = false,
    val isMSMEOwner: Boolean = false,
    val isStartupFounder: Boolean = false,
    val sector: String = "GENERAL",
    val languages: List<String> = listOf("English"),
    val demographicVector: List<Float> = emptyList(),
    val featureVector: List<Float> = emptyList()
)

data class SchemeRecommendation(
    val schemeId: String,
    val schemeName: String,
    val ministry: String,
    val matchScore: Float,             // 0.0 – 1.0
    val matchSource: String,           // rule_based, collaborative, content_based, ensemble
    val benefitSummary: String,
    val applyUrl: String? = null,
    val deadline: String? = null,
    val estimatedBenefitINR: Long? = null
)

data class SchemeEligibilityRule(
    val schemeId: String,
    val schemeName: String,
    val ministry: String,
    val benefitSummary: String,
    val maxAgeYears: Int? = null,
    val minAgeYears: Int? = null,
    val requiredGender: String? = null,       // M, F, null = any
    val maxAnnualIncomeINR: Long? = null,
    val requiredOccupation: String? = null,
    val requiresBPL: Boolean = false,
    val requiresMSME: Boolean = false,
    val requiresStartup: Boolean = false,
    val applicableStates: List<String> = emptyList(),
    val estimatedBenefitINR: Long? = null,
    val applyUrl: String? = null
)

class SchemeRecommendationEngine {

    // Seed with representative Indian government schemes
    private val schemeRules: List<SchemeEligibilityRule> = buildSchemeDatabase()

    suspend fun recommend(profile: CitizenProfile, limit: Int = 10): List<SchemeRecommendation> =
        coroutineScope {
            val (ruleBased, contentBased, collaborative) = awaitAll(
                async { ruleBasedEligibility(profile) },
                async { contentBasedMatch(profile) },
                async { collaborativeFilter(profile) }
            )

            ensembleRank(
                ruleBased = ruleBased, contentBased = contentBased, collaborative = collaborative,
                weights = Triple(0.40f, 0.35f, 0.25f)
            ).take(limit)
        }

    private fun ruleBasedEligibility(profile: CitizenProfile): List<SchemeRecommendation> =
        schemeRules.mapNotNull { rule ->
            val score = evaluateRule(rule, profile)
            if (score > 0.0f) SchemeRecommendation(
                schemeId = rule.schemeId,
                schemeName = rule.schemeName,
                ministry = rule.ministry,
                matchScore = score,
                matchSource = "rule_based",
                benefitSummary = rule.benefitSummary,
                applyUrl = rule.applyUrl,
                estimatedBenefitINR = rule.estimatedBenefitINR
            ) else null
        }

    private fun evaluateRule(rule: SchemeEligibilityRule, p: CitizenProfile): Float {
        if (rule.requiresBPL && !p.hasBPLCard) return 0f
        if (rule.requiresMSME && !p.isMSMEOwner) return 0f
        if (rule.requiresStartup && !p.isStartupFounder) return 0f
        if (rule.requiredGender != null && rule.requiredGender != p.gender) return 0f
        if (rule.maxAgeYears != null && p.age > rule.maxAgeYears) return 0f
        if (rule.minAgeYears != null && p.age < rule.minAgeYears) return 0f
        if (rule.maxAnnualIncomeINR != null && p.annualIncomeINR > rule.maxAnnualIncomeINR) return 0f
        if (rule.requiredOccupation != null && rule.requiredOccupation != p.occupation) return 0f
        if (rule.applicableStates.isNotEmpty() && p.state !in rule.applicableStates) return 0f
        return 0.90f  // fully eligible
    }

    private suspend fun contentBasedMatch(profile: CitizenProfile): List<SchemeRecommendation> {
        delay(1)
        // Cosine-similarity placeholder; production: vector DB (Pinecone / pgvector)
        return schemeRules.mapNotNull { rule ->
            val score = cosineSimilarity(profile.featureVector, rule.schemeId)
            if (score > 0.5f) SchemeRecommendation(
                rule.schemeId, rule.schemeName, rule.ministry,
                score, "content_based", rule.benefitSummary,
                estimatedBenefitINR = rule.estimatedBenefitINR
            ) else null
        }
    }

    private suspend fun collaborativeFilter(profile: CitizenProfile): List<SchemeRecommendation> {
        delay(1)
        // Matrix factorization placeholder; production: ALS or LightFM model served via FastAPI
        return emptyList()
    }

    private fun cosineSimilarity(vector: List<Float>, schemeId: String): Float {
        if (vector.isEmpty()) return 0.45f  // neutral score when no vector
        return 0.55f  // stub
    }

    private fun ensembleRank(
        ruleBased: List<SchemeRecommendation>,
        contentBased: List<SchemeRecommendation>,
        collaborative: List<SchemeRecommendation>,
        weights: Triple<Float, Float, Float>
    ): List<SchemeRecommendation> {
        val all = mutableMapOf<String, SchemeRecommendation>()

        fun merge(list: List<SchemeRecommendation>, weight: Float) {
            list.forEach { rec ->
                val existing = all[rec.schemeId]
                all[rec.schemeId] = if (existing == null) {
                    rec.copy(matchScore = rec.matchScore * weight, matchSource = "ensemble")
                } else {
                    existing.copy(matchScore = existing.matchScore + rec.matchScore * weight)
                }
            }
        }

        merge(ruleBased, weights.first)
        merge(contentBased, weights.second)
        merge(collaborative, weights.third)

        return all.values.sortedByDescending { it.matchScore }
    }

    private fun buildSchemeDatabase(): List<SchemeEligibilityRule> = listOf(
        SchemeEligibilityRule("STARTUP_INDIA_001", "Startup India", "DPIIT",
            "Tax exemption 3 years, patent rebate 80%, seed fund access",
            requiresStartup = true, estimatedBenefitINR = 5_00_000L,
            applyUrl = "https://www.startupindia.gov.in"),
        SchemeEligibilityRule("MSME_CREDIT_001", "MSME Credit Guarantee Scheme", "Ministry of MSME",
            "Collateral-free credit up to ₹2 crore", requiresMSME = true,
            estimatedBenefitINR = 2_00_00_000L),
        SchemeEligibilityRule("AIM_001", "Atal Innovation Mission", "NITI Aayog",
            "Grants up to ₹20 lakh for innovators", minAgeYears = 15, maxAgeYears = 35,
            estimatedBenefitINR = 20_00_000L, applyUrl = "https://aim.gov.in"),
        SchemeEligibilityRule("SUKANYA_001", "Sukanya Samriddhi Yojana", "Ministry of Finance",
            "8.2% annual interest, tax-free for girl child",
            maxAgeYears = 10, requiredGender = "F", estimatedBenefitINR = 0L),
        SchemeEligibilityRule("BPL_HOUSING_001", "PM Awas Yojana (Urban)", "MoHUA",
            "Housing subsidy up to ₹2.67 lakh for BPL families",
            requiresBPL = true, estimatedBenefitINR = 2_67_000L),
        SchemeEligibilityRule("FARMER_PM_KISAN", "PM-KISAN", "Ministry of Agriculture",
            "₹6,000/year direct benefit to farmers",
            requiredOccupation = "FARMER", maxAnnualIncomeINR = 2_00_000L,
            estimatedBenefitINR = 6_000L),
        SchemeEligibilityRule("SCHOLARSHIP_001", "National Scholarship Portal", "Ministry of Education",
            "₹5,000–₹20,000/year for meritorious students",
            requiredOccupation = "STUDENT", maxAnnualIncomeINR = 6_00_000L,
            estimatedBenefitINR = 20_000L, applyUrl = "https://scholarships.gov.in"),
        SchemeEligibilityRule("DISABILITY_001", "Assistance to Disabled Persons", "DEPwD",
            "Devices, monthly pension, education support for PWD",
            estimatedBenefitINR = 3_000L * 12),
        SchemeEligibilityRule("MUDRA_001", "PM MUDRA Yojana", "Ministry of Finance",
            "Collateral-free business loans: Shishu ₹50K, Kishore ₹5L, Tarun ₹10L",
            maxAnnualIncomeINR = 10_00_000L, estimatedBenefitINR = 10_00_000L),
        SchemeEligibilityRule("PLI_TECH_001", "PLI Scheme — Technology Products", "MeitY",
            "Production-linked incentive 4-6% of incremental sales",
            requiresStartup = true, requiredOccupation = "ENTREPRENEUR",
            estimatedBenefitINR = 50_00_000L)
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// FRAUD EVENT BUS
// ═════════════════════════════════════════════════════════════════════════════

data class FraudAlert(
    val documentId: String,
    val fraudScore: Float,
    val reason: String,
    val alertId: String = UUID.randomUUID().toString(),
    val timestamp: String = LocalDateTime.now().toString()
)

class FraudEventBus {
    private val listeners = mutableListOf<(FraudAlert) -> Unit>()

    fun publish(alert: FraudAlert) {
        println("[FRAUD ALERT] doc=${alert.documentId} score=${alert.fraudScore} reason=${alert.reason}")
        listeners.forEach { it(alert) }
        // TODO: publish to Kafka / AWS SNS / government alert webhook
    }

    fun subscribe(handler: (FraudAlert) -> Unit) = listeners.add(handler)
}
