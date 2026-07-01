package org.psei.pipeline

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

// ═════════════════════════════════════════════════════════════════════════════
// DATA INGESTION LAYER — Kafka Event Streaming
// ═════════════════════════════════════════════════════════════════════════════

enum class KafkaTopic {
    PSEI_RAW_EVENTS,            // All incoming events
    PSEI_DOCUMENTS_PROCESSED,   // Post-OCR document events
    PSEI_USER_SESSIONS,         // Aggregated behavior
    PSEI_FRAUD_ALERTS,          // Real-time fraud scores
    PSEI_ML_FEATURES,           // Engineered features
    PSEI_MODEL_PREDICTIONS,     // Model output events
    PSEI_AUDIT_STREAM,          // Immutable audit trail
    PSEI_SCHEME_MATCHES,        // Scheme recommendation events
    PSEI_RETRAINING_TRIGGERS    // Model drift alerts
}

@Serializable
data class PSEIEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val topic: String,
    val eventType: String,
    val citizenId: String? = null,
    val officerId: String? = null,
    val payload: Map<String, String> = emptyMap(),
    val metadata: EventMetadata = EventMetadata(),
    val timestamp: String = LocalDateTime.now().toString()
)

@Serializable
data class EventMetadata(
    val sourceService: String = "PSEI",
    val version: String = "2.0",
    val traceId: String = UUID.randomUUID().toString(),
    val ipAddress: String? = null,
    val deviceType: String? = null,
    val state: String? = null,      // Indian state for geo-partitioning
    val language: String = "en"
)

/**
 * Event streaming service — wraps Kafka producers/consumers.
 * Production: use kotlinx-coroutines-kafka or spring-kafka client.
 */
class EventStreamingService {
    private val eventLog = mutableListOf<PSEIEvent>()
    private val subscribers = mutableMapOf<String, MutableList<(PSEIEvent) -> Unit>>()

    fun publish(event: PSEIEvent) {
        eventLog.add(event)
        subscribers[event.topic]?.forEach { handler ->
            runCatching { handler(event) }
        }
        // Production: kafkaProducer.send(ProducerRecord(event.topic, event.eventId, event))
    }

    fun subscribe(topic: String, handler: (PSEIEvent) -> Unit) {
        subscribers.getOrPut(topic) { mutableListOf() }.add(handler)
    }

    fun getRecentEvents(topic: String, limit: Int = 100): List<PSEIEvent> =
        eventLog.filter { it.topic == topic }.takeLast(limit)

    fun getEventCount(topic: String): Int = eventLog.count { it.topic == topic }
}

// ═════════════════════════════════════════════════════════════════════════════
// FEATURE ENGINEERING LAYER
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class CitizenFeatureVector(
    val citizenId: String,
    val demographicFeatures: DemographicFeatures,
    val behavioralFeatures: BehavioralFeatures,
    val documentFeatures: DocumentFeatures,
    val geospatialFeatures: GeospatialFeatures,
    val temporalFeatures: TemporalFeatures,
    val computedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class DemographicFeatures(
    val ageGroup: String,               // 18-25, 26-35, 36-50, 51+
    val gender: String,
    val stateCode: String,
    val urbanRural: String,             // URBAN, SEMI_URBAN, RURAL
    val incomeDecile: Int,              // 1-10
    val educationLevel: Int,            // 0-5 ordinal
    val occupationType: String,
    val casteCategory: String,          // GENERAL, OBC, SC, ST, EWS
    val maritalStatus: String,
    val familySize: Int
)

@Serializable
data class BehavioralFeatures(
    val sessionCount30Days: Int,
    val avgSessionDurationMin: Double,
    val featuresUsed: List<String>,
    val schemeApplicationCount: Int,
    val documentUploadCount: Int,
    val loginFrequency: String,         // DAILY, WEEKLY, MONTHLY, RARE
    val preferredLanguage: String,
    val preferredChannel: String,       // APP, WHATSAPP, WEB, KIOSK
    val abandonedApplications: Int,
    val completedWorkflows: Int
)

@Serializable
data class DocumentFeatures(
    val totalDocuments: Int,
    val verifiedDocuments: Int,
    val documentTypes: List<String>,
    val oldestDocumentYear: Int,
    val avgDocumentAge: Double,         // years
    val hasAadhar: Boolean,
    val hasPAN: Boolean,
    val hasPassport: Boolean,
    val documentHealthScore: Int
)

@Serializable
data class GeospatialFeatures(
    val stateCode: String,
    val districtCode: String,
    val pinCode: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val nearestCSCDistanceKm: Double? = null,   // Common Service Centre
    val internetPenetration: Double? = null,    // District-level stat
    val floodProneZone: Boolean = false,
    val droughtProneZone: Boolean = false
)

@Serializable
data class TemporalFeatures(
    val registrationMonthsSince: Int,
    val lastLoginDaysSince: Int,
    val lastDocumentUploadDaysSince: Int,
    val lastSchemeApplicationDaysSince: Int,
    val isRenewPeriod: Boolean,             // upcoming document expiries
    val isSchemeDeadlinePeriod: Boolean,    // scheme deadlines in 30 days
    val agriculturalSeason: String          // KHARIF, RABI, ZAID
)

/**
 * Feature engineering pipeline.
 * Production: Apache Spark batch + Feast feature store for online serving.
 */
class FeatureEngineeringPipeline(
    private val eventStream: EventStreamingService
) {
    private val featureCache = mutableMapOf<String, CitizenFeatureVector>()

    fun computeFeatures(citizenId: String, rawProfile: Map<String, Any>): CitizenFeatureVector {
        val features = CitizenFeatureVector(
            citizenId = citizenId,
            demographicFeatures = extractDemographic(rawProfile),
            behavioralFeatures = extractBehavioral(citizenId),
            documentFeatures = extractDocumentFeatures(rawProfile),
            geospatialFeatures = extractGeospatial(rawProfile),
            temporalFeatures = extractTemporal(citizenId)
        )
        featureCache[citizenId] = features

        // Publish feature event for downstream models
        eventStream.publish(PSEIEvent(
            topic = KafkaTopic.PSEI_ML_FEATURES.name,
            eventType = "CITIZEN_FEATURES_COMPUTED",
            citizenId = citizenId,
            payload = mapOf("featureVersion" to "2.0", "featureCount" to "50")
        ))

        return features
    }

    fun getFeatures(citizenId: String): CitizenFeatureVector? = featureCache[citizenId]

    private fun extractDemographic(profile: Map<String, Any>): DemographicFeatures =
        DemographicFeatures(
            ageGroup = categorizeAge((profile["age"] as? Int) ?: 25),
            gender = (profile["gender"] as? String) ?: "U",
            stateCode = (profile["state"] as? String) ?: "DL",
            urbanRural = (profile["urbanRural"] as? String) ?: "URBAN",
            incomeDecile = calculateIncomeDecile((profile["income"] as? Long) ?: 300000L),
            educationLevel = (profile["educationLevel"] as? Int) ?: 2,
            occupationType = (profile["occupation"] as? String) ?: "GENERAL",
            casteCategory = (profile["caste"] as? String) ?: "GENERAL",
            maritalStatus = (profile["marital"] as? String) ?: "SINGLE",
            familySize = (profile["familySize"] as? Int) ?: 4
        )

    private fun extractBehavioral(citizenId: String): BehavioralFeatures {
        val events = eventStream.getRecentEvents(KafkaTopic.PSEI_USER_SESSIONS.name, 1000)
            .filter { it.citizenId == citizenId }
        return BehavioralFeatures(
            sessionCount30Days = events.size,
            avgSessionDurationMin = 8.5,
            featuresUsed = events.mapNotNull { it.payload["feature"] }.distinct(),
            schemeApplicationCount = events.count { it.eventType == "SCHEME_APPLY" },
            documentUploadCount = events.count { it.eventType == "DOCUMENT_UPLOAD" },
            loginFrequency = if (events.size > 20) "DAILY" else if (events.size > 8) "WEEKLY" else "MONTHLY",
            preferredLanguage = "hi",
            preferredChannel = "WHATSAPP",
            abandonedApplications = 1,
            completedWorkflows = events.count { it.eventType == "WORKFLOW_COMPLETE" }
        )
    }

    private fun extractDocumentFeatures(profile: Map<String, Any>): DocumentFeatures =
        DocumentFeatures(
            totalDocuments = (profile["docCount"] as? Int) ?: 3,
            verifiedDocuments = (profile["verifiedDocs"] as? Int) ?: 2,
            documentTypes = listOf("AADHAR_CARD", "PAN_CARD"),
            oldestDocumentYear = 2015,
            avgDocumentAge = 4.5,
            hasAadhar = true, hasPAN = true, hasPassport = false,
            documentHealthScore = 72
        )

    private fun extractGeospatial(profile: Map<String, Any>): GeospatialFeatures =
        GeospatialFeatures(
            stateCode = (profile["state"] as? String) ?: "DL",
            districtCode = (profile["district"] as? String) ?: "DL001",
            pinCode = (profile["pinCode"] as? String) ?: "110001",
            nearestCSCDistanceKm = 2.5,
            internetPenetration = 0.78
        )

    private fun extractTemporal(citizenId: String): TemporalFeatures =
        TemporalFeatures(
            registrationMonthsSince = 8,
            lastLoginDaysSince = 2,
            lastDocumentUploadDaysSince = 15,
            lastSchemeApplicationDaysSince = 45,
            isRenewPeriod = false,
            isSchemeDeadlinePeriod = true,
            agriculturalSeason = "KHARIF"
        )

    private fun categorizeAge(age: Int): String = when {
        age < 18 -> "under-18"; age < 26 -> "18-25"; age < 36 -> "26-35"
        age < 51 -> "36-50"; else -> "51+"
    }

    private fun calculateIncomeDecile(income: Long): Int = when {
        income < 50000 -> 1; income < 100000 -> 2; income < 150000 -> 3
        income < 250000 -> 4; income < 350000 -> 5; income < 500000 -> 6
        income < 700000 -> 7; income < 1000000 -> 8; income < 2000000 -> 9; else -> 10
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// MODEL SERVING — Triton-style routing (Real-time, Batch, Edge)
// ═════════════════════════════════════════════════════════════════════════════

enum class InferenceMode { REAL_TIME, BATCH, EDGE }

@Serializable
data class ModelPrediction(
    val predictionId: String = UUID.randomUUID().toString(),
    val modelName: String,
    val modelVersion: String,
    val citizenId: String? = null,
    val inputHash: String,
    val output: Map<String, Any>,
    val confidence: Float,
    val inferenceMode: String,
    val latencyMs: Long,
    val timestamp: String = LocalDateTime.now().toString()
)

@Serializable
data class ModelMetrics(
    val modelName: String,
    val version: String,
    val accuracy: Double,
    val precision: Double,
    val recall: Double,
    val f1Score: Double,
    val latencyP50Ms: Long,
    val latencyP99Ms: Long,
    val predictionCount: Long,
    val driftScore: Double,         // 0.0 = no drift, 1.0 = full drift
    val lastEvaluated: String = LocalDateTime.now().toString()
)

/**
 * Model serving router — routes requests to real-time, batch, or edge inference.
 * Production: NVIDIA Triton Inference Server + KServe + Ray Serve.
 */
class ModelServingService(private val eventStream: EventStreamingService) {
    private val modelRegistry = mutableMapOf<String, ModelMetrics>()
    private val predictionLog = mutableListOf<ModelPrediction>()

    init {
        // Register all PSEI models
        registerModel(ModelMetrics("forgery_detection_v2", "2.1.0", 0.94, 0.91, 0.96, 0.935, 45, 250, 0, 0.05))
        registerModel(ModelMetrics("scheme_recommendation_v1", "1.3.0", 0.87, 0.85, 0.89, 0.87, 20, 80, 0, 0.03))
        registerModel(ModelMetrics("intent_classification_v1", "1.0.0", 0.92, 0.90, 0.93, 0.915, 15, 60, 0, 0.02))
        registerModel(ModelMetrics("anomaly_detection_v1", "1.1.0", 0.89, 0.88, 0.90, 0.89, 25, 100, 0, 0.04))
        registerModel(ModelMetrics("eligibility_scoring_v1", "2.0.0", 0.95, 0.94, 0.96, 0.95, 10, 40, 0, 0.01))
    }

    fun registerModel(metrics: ModelMetrics) {
        modelRegistry[metrics.modelName] = metrics
    }

    suspend fun predict(
        modelName: String,
        citizenId: String?,
        input: Map<String, Any>,
        mode: InferenceMode = InferenceMode.REAL_TIME
    ): ModelPrediction = coroutineScope {
        val startMs = System.currentTimeMillis()

        val metrics = modelRegistry[modelName] ?: throw IllegalArgumentException("Model $modelName not registered")

        // Check for model drift — trigger retraining if needed
        if (metrics.driftScore > 0.20) {
            eventStream.publish(PSEIEvent(
                topic = KafkaTopic.PSEI_RETRAINING_TRIGGERS.name,
                eventType = "DRIFT_DETECTED",
                payload = mapOf("model" to modelName, "driftScore" to metrics.driftScore.toString())
            ))
        }

        // Route based on inference mode
        val output = when (mode) {
            InferenceMode.REAL_TIME -> inferRealTime(modelName, input)
            InferenceMode.BATCH -> inferBatch(modelName, listOf(input)).first()
            InferenceMode.EDGE -> inferEdge(modelName, input)
        }

        val latency = System.currentTimeMillis() - startMs

        val prediction = ModelPrediction(
            modelName = modelName,
            modelVersion = metrics.version,
            citizenId = citizenId,
            inputHash = input.hashCode().toString(),
            output = output,
            confidence = (output["confidence"] as? Float) ?: 0.85f,
            inferenceMode = mode.name,
            latencyMs = latency
        )

        predictionLog.add(prediction)
        eventStream.publish(PSEIEvent(
            topic = KafkaTopic.PSEI_MODEL_PREDICTIONS.name,
            eventType = "PREDICTION_MADE",
            citizenId = citizenId,
            payload = mapOf("model" to modelName, "confidence" to prediction.confidence.toString())
        ))

        // Increment prediction count
        modelRegistry[modelName] = metrics.copy(predictionCount = metrics.predictionCount + 1)

        prediction
    }

    private suspend fun inferRealTime(modelName: String, input: Map<String, Any>): Map<String, Any> {
        delay(20) // Simulate gRPC call to Triton Inference Server
        // TODO: grpcChannel.newStub(GRPCInferenceServiceGrpc).modelInfer(...)
        return mapOf("result" to "AUTHENTIC", "confidence" to 0.94f, "processingMs" to 18)
    }

    private suspend fun inferBatch(modelName: String, inputs: List<Map<String, Any>>): List<Map<String, Any>> {
        delay(100) // Simulate batch inference
        return inputs.map { mapOf("result" to "PROCESSED", "confidence" to 0.91f) }
    }

    private suspend fun inferEdge(modelName: String, input: Map<String, Any>): Map<String, Any> {
        delay(5) // Edge model — ultra-fast local inference (TFLite / ONNX)
        return mapOf("result" to "EDGE_PROCESSED", "confidence" to 0.88f, "model" to "tflite_optimized")
    }

    fun getModelMetrics(modelName: String): ModelMetrics? = modelRegistry[modelName]
    fun getAllModels(): List<ModelMetrics> = modelRegistry.values.toList()
}

// ═════════════════════════════════════════════════════════════════════════════
// MODEL MONITORING — Drift Detection + A/B Testing
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class DriftReport(
    val modelName: String,
    val driftType: String,          // DATA_DRIFT, CONCEPT_DRIFT, LABEL_DRIFT
    val psiScore: Double,           // Population Stability Index (>0.25 = significant drift)
    val ksStatistic: Double,        // Kolmogorov-Smirnov test statistic
    val alertLevel: String,         // INFO, WARNING, CRITICAL
    val affectedFeatures: List<String>,
    val recommendation: String,
    val detectedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class ABTestConfig(
    val testId: String = UUID.randomUUID().toString(),
    val modelA: String,             // Control (current)
    val modelB: String,             // Treatment (new)
    val trafficSplitPercent: Int = 20,  // % going to model B
    val startDate: String = LocalDateTime.now().toString(),
    val endDate: String? = null,
    val primaryMetric: String,      // ACCURACY, LATENCY, CITIZEN_SATISFACTION
    val minimumSampleSize: Int = 10000,
    val status: String = "RUNNING"
)

class ModelMonitoringService(private val eventStream: EventStreamingService) {
    private val driftReports = mutableListOf<DriftReport>()
    private val abTests = mutableMapOf<String, ABTestConfig>()
    private val shadowModels = mutableMapOf<String, String>()  // prodModel → shadowModel

    fun detectDrift(modelName: String, referenceData: List<Float>, currentData: List<Float>): DriftReport {
        // Population Stability Index (PSI)
        val psi = computePSI(referenceData, currentData)
        // Kolmogorov-Smirnov test
        val ks = computeKS(referenceData, currentData)

        val alertLevel = when {
            psi > 0.25 || ks > 0.20 -> "CRITICAL"
            psi > 0.10 || ks > 0.10 -> "WARNING"
            else -> "INFO"
        }

        val report = DriftReport(
            modelName = modelName,
            driftType = if (psi > 0.25) "DATA_DRIFT" else "CONCEPT_DRIFT",
            psiScore = psi,
            ksStatistic = ks,
            alertLevel = alertLevel,
            affectedFeatures = listOf("income_decile", "education_level", "state_code"),
            recommendation = when (alertLevel) {
                "CRITICAL" -> "Immediate retraining required — suspend model and use fallback"
                "WARNING"  -> "Schedule retraining within 72 hours, monitor closely"
                else       -> "Monitor at next scheduled evaluation"
            }
        )

        driftReports.add(report)

        if (alertLevel == "CRITICAL") {
            eventStream.publish(PSEIEvent(
                topic = KafkaTopic.PSEI_RETRAINING_TRIGGERS.name,
                eventType = "CRITICAL_DRIFT",
                payload = mapOf("model" to modelName, "psi" to psi.toString(), "ks" to ks.toString())
            ))
        }

        return report
    }

    fun createABTest(config: ABTestConfig): ABTestConfig {
        abTests[config.testId] = config
        return config
    }

    fun routeABRequest(testId: String, requestId: String): String {
        val config = abTests[testId] ?: return "CONTROL"
        val hash = requestId.hashCode().let { if (it < 0) -it else it }
        return if (hash % 100 < config.trafficSplitPercent) "TREATMENT" else "CONTROL"
    }

    fun enableShadowMode(productionModel: String, shadowModel: String) {
        shadowModels[productionModel] = shadowModel
    }

    fun getLatestDriftReports(modelName: String): List<DriftReport> =
        driftReports.filter { it.modelName == modelName }.takeLast(10)

    private fun computePSI(reference: List<Float>, current: List<Float>): Double {
        if (reference.isEmpty() || current.isEmpty()) return 0.0
        // Simplified 10-bucket PSI calculation
        val buckets = 10
        val refHist = histogram(reference, buckets)
        val curHist = histogram(current, buckets)
        return refHist.zip(curHist).sumOf { (ref, cur) ->
            val r = (ref + 0.0001); val c = (cur + 0.0001)
            (c - r) * Math.log(c / r)
        }
    }

    private fun computeKS(reference: List<Float>, current: List<Float>): Double {
        val refSorted = reference.sorted()
        val curSorted = current.sorted()
        var maxDiff = 0.0
        refSorted.forEachIndexed { i, v ->
            val refCDF = (i + 1).toDouble() / refSorted.size
            val curCDF = curSorted.count { it <= v }.toDouble() / curSorted.size
            maxDiff = maxOf(maxDiff, Math.abs(refCDF - curCDF))
        }
        return maxDiff
    }

    private fun histogram(data: List<Float>, buckets: Int): List<Double> {
        if (data.isEmpty()) return List(buckets) { 0.0 }
        val min = data.min(); val max = data.max()
        val width = (max - min) / buckets
        val counts = IntArray(buckets)
        data.forEach { v ->
            val bucket = minOf(((v - min) / width).toInt(), buckets - 1)
            counts[bucket]++
        }
        return counts.map { it.toDouble() / data.size }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FEEDBACK & RETRAINING LOOP
// ═════════════════════════════════════════════════════════════════════════════

enum class FeedbackType { CITIZEN_RATING, OFFICER_OVERRIDE, AUTOMATED_METRIC, EXTERNAL_AUDIT }

@Serializable
data class ModelFeedback(
    val id: String = UUID.randomUUID().toString(),
    val predictionId: String,
    val modelName: String,
    val feedbackType: FeedbackType,
    val isCorrect: Boolean,
    val correctLabel: String? = null,    // Ground truth if known
    val feedbackScore: Int? = null,      // 1-5 citizen rating
    val officerComment: String? = null,
    val source: String,                  // CITIZEN, OFFICER, AUTOMATED, AUDIT
    val submittedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class RetrainingJob(
    val id: String = UUID.randomUUID().toString(),
    val modelName: String,
    val triggerType: String,        // DRIFT, SCHEDULE, MANUAL, ACCURACY_DROP
    val trainingDataFrom: String,
    val trainingDataTo: String,
    val hyperparameters: Map<String, String> = emptyMap(),
    val status: String = "QUEUED",  // QUEUED, RUNNING, COMPLETED, FAILED
    val createdAt: String = LocalDateTime.now().toString(),
    val completedAt: String? = null,
    val newModelAccuracy: Double? = null,
    val promotedToProduction: Boolean = false
)

class FeedbackAndRetrainingService(private val eventStream: EventStreamingService) {
    private val feedbackStore = mutableListOf<ModelFeedback>()
    private val retrainingJobs = mutableMapOf<String, RetrainingJob>()
    private val feedbackThresholds = mapOf(
        "forgery_detection_v2" to 0.90,
        "scheme_recommendation_v1" to 0.85,
        "eligibility_scoring_v1" to 0.92
    )

    fun submitFeedback(feedback: ModelFeedback): ModelFeedback {
        feedbackStore.add(feedback)

        // Check if accuracy has dropped below threshold
        val recentFeedback = feedbackStore.filter { it.modelName == feedback.modelName }.takeLast(100)
        if (recentFeedback.size >= 50) {
            val accuracy = recentFeedback.count { it.isCorrect }.toDouble() / recentFeedback.size
            val threshold = feedbackThresholds[feedback.modelName] ?: 0.85

            if (accuracy < threshold) {
                triggerRetraining(feedback.modelName, "ACCURACY_DROP",
                    mapOf("currentAccuracy" to accuracy.toString(), "threshold" to threshold.toString()))
            }
        }

        return feedback
    }

    fun triggerRetraining(modelName: String, reason: String, params: Map<String, String> = emptyMap()): RetrainingJob {
        val job = RetrainingJob(
            modelName = modelName,
            triggerType = reason,
            trainingDataFrom = java.time.LocalDate.now().minusDays(90).toString(),
            trainingDataTo = java.time.LocalDate.now().toString(),
            hyperparameters = params
        )
        retrainingJobs[job.id] = job

        eventStream.publish(PSEIEvent(
            topic = KafkaTopic.PSEI_RETRAINING_TRIGGERS.name,
            eventType = "RETRAINING_JOB_CREATED",
            payload = mapOf("jobId" to job.id, "model" to modelName, "reason" to reason)
        ))

        // Production: submit to Kubeflow Pipelines / MLflow
        println("[ML PIPELINE] Retraining job ${job.id} queued for $modelName — reason: $reason")

        return job
    }

    fun getFeedbackStats(modelName: String): Map<String, Any> {
        val feedback = feedbackStore.filter { it.modelName == modelName }
        return mapOf(
            "totalFeedback" to feedback.size,
            "accuracyFromFeedback" to if (feedback.isEmpty()) 0.0 else feedback.count { it.isCorrect }.toDouble() / feedback.size,
            "byType" to feedback.groupBy { it.feedbackType.name }.mapValues { it.value.size },
            "recentJobs" to retrainingJobs.values.filter { it.modelName == modelName }.size
        )
    }

    fun getRetrainingJobs(modelName: String? = null): List<RetrainingJob> =
        if (modelName == null) retrainingJobs.values.toList()
        else retrainingJobs.values.filter { it.modelName == modelName }
}

// ═════════════════════════════════════════════════════════════════════════════
// ANOMALY DETECTION — Unusual Access Patterns
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class AnomalyEvent(
    val id: String = UUID.randomUUID().toString(),
    val citizenId: String,
    val anomalyType: String,
    val severity: String,           // LOW, MEDIUM, HIGH, CRITICAL
    val description: String,
    val evidence: Map<String, String> = emptyMap(),
    val autoBlocked: Boolean = false,
    val requiresMFA: Boolean = false,
    val detectedAt: String = LocalDateTime.now().toString()
)

class AnomalyDetectionService(private val eventStream: EventStreamingService) {
    private val loginHistory = mutableMapOf<String, MutableList<LoginEvent>>()
    private val anomalies = mutableListOf<AnomalyEvent>()

    data class LoginEvent(
        val timestamp: Long, val ipAddress: String, val country: String,
        val device: String, val success: Boolean
    )

    fun recordLogin(citizenId: String, ip: String, country: String, device: String, success: Boolean) {
        loginHistory.getOrPut(citizenId) { mutableListOf() }.add(
            LoginEvent(System.currentTimeMillis(), ip, country, device, success)
        )
        detectAnomalies(citizenId)
    }

    private fun detectAnomalies(citizenId: String) {
        val history = loginHistory[citizenId] ?: return
        val recent = history.takeLast(20)

        // Detect: impossible travel (login from two different countries within 1 hour)
        if (recent.size >= 2) {
            val last = recent[recent.size - 1]
            val prev = recent[recent.size - 2]
            if (last.country != prev.country &&
                (last.timestamp - prev.timestamp) < 3600_000) {
                createAnomaly(citizenId, "IMPOSSIBLE_TRAVEL", "HIGH",
                    "Login from ${prev.country} and ${last.country} within 1 hour",
                    mapOf("fromCountry" to prev.country, "toCountry" to last.country), requiresMFA = true)
            }
        }

        // Detect: brute force (5+ failed logins in 5 minutes)
        val recentFailed = recent.filter { !it.success &&
                System.currentTimeMillis() - it.timestamp < 300_000 }
        if (recentFailed.size >= 5) {
            createAnomaly(citizenId, "BRUTE_FORCE", "CRITICAL",
                "${recentFailed.size} failed login attempts in 5 minutes",
                mapOf("attemptCount" to recentFailed.size.toString()), autoBlocked = true)
        }

        // Detect: new device in unfamiliar location
        val knownDevices = history.dropLast(1).map { it.device }.toSet()
        if (recent.last().device !in knownDevices && recent.size > 5) {
            createAnomaly(citizenId, "NEW_DEVICE", "MEDIUM",
                "Login from unrecognized device ${recent.last().device}",
                mapOf("device" to recent.last().device), requiresMFA = true)
        }
    }

    private fun createAnomaly(
        citizenId: String, type: String, severity: String, description: String,
        evidence: Map<String, String>, autoBlocked: Boolean = false, requiresMFA: Boolean = false
    ) {
        val anomaly = AnomalyEvent(
            citizenId = citizenId, anomalyType = type, severity = severity,
            description = description, evidence = evidence,
            autoBlocked = autoBlocked, requiresMFA = requiresMFA
        )
        anomalies.add(anomaly)
        eventStream.publish(PSEIEvent(
            topic = KafkaTopic.PSEI_FRAUD_ALERTS.name,
            eventType = "ANOMALY_DETECTED",
            citizenId = citizenId,
            payload = mapOf("type" to type, "severity" to severity)
        ))
    }

    fun getAnomalies(citizenId: String): List<AnomalyEvent> =
        anomalies.filter { it.citizenId == citizenId }
}
