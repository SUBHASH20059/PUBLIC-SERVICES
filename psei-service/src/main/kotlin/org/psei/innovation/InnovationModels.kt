package org.psei.innovation

import kotlinx.serialization.Serializable

@Serializable
data class IdeaRecord(
    val id: Long? = null,
    val title: String,
    val description: String,
    val creatorId: String,
    val createdAt: String, // ISO datetime
    val version: Int = 1,
    val signature: String? = null, // digital signature / proof
    val publicKeyFingerprint: String? = null
)

@Serializable
data class IdeaAccessLog(
    val id: Long? = null,
    val ideaId: Long,
    val accessorId: String,
    val accessAt: String,
    val action: String // VIEW / DOWNLOAD / VERIFY
)

@Serializable
data class PatentAssistanceRequest(
    val id: Long? = null,
    val ideaId: Long,
    val requesterId: String,
    val status: String = "PENDING", // PENDING / IN_REVIEW / SUBMITTED / COMPLETED
    val priorArtNotes: String? = null,
    val createdAt: String
)

@Serializable
data class SchemeMatch(
    val id: Long? = null,
    val userId: String,
    val matchedSchemes: List<String> = emptyList(),
    val criteria: Map<String, String> = emptyMap(),
    val matchedAt: String
)

@Serializable
data class TemplateMetadata(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val fields: List<String> = emptyList(),
    val createdAt: String
)

@Serializable
data class Mentor(
    val id: Long? = null,
    val name: String,
    val expertise: List<String> = emptyList(),
    val contact: String? = null
)

@Serializable
data class Investor(
    val id: Long? = null,
    val name: String,
    val stageFocus: List<String> = emptyList(),
    val contact: String? = null
)

@Serializable
data class StudentProject(
    val id: Long? = null,
    val title: String,
    val studentId: String,
    val supervisor: String? = null,
    val createdAt: String
)
