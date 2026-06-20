package com.example.network

import retrofit2.http.Body
import retrofit2.http.POST

data class FraudCheckRequest(
    val entityType: String,
    val entityData: String,
    val signature: String? = null
)

data class FraudCheckResponse(
    val isFraudulent: Boolean,
    val confidenceScore: Double,
    val riskLevel: String, // LOW, MEDIUM, HIGH, CRITICAL
    val detectedAnomalies: List<String>,
    val recommendation: String
)

interface FraudDetectionService {
    @POST("v1/fraud/check")
    suspend fun checkForFraud(@Body request: FraudCheckRequest): FraudCheckResponse
}
