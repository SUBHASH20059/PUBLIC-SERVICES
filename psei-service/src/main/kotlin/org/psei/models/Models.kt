package org.psei.models

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Marriage(
    val id: Long? = null,
    val spouseA: String,
    val spouseB: String,
    val marriageDate: String // ISO date
)

@Serializable
data class Birth(
    val id: Long? = null,
    val childName: String,
    val motherName: String,
    val fatherName: String?,
    val birthDate: String
)

@Serializable
data class Property(
    val id: Long? = null,
    val titleNumber: String,
    val ownerName: String,
    val address: String,
    val areaSqMeters: Double?
)

@Serializable
data class Company(
    val id: Long? = null,
    val companyName: String,
    val registrationNumber: String,
    val incorporationDate: String
)

@Serializable
data class Complaint(
    val id: Long? = null,
    val complainantName: String,
    val against: String,
    val description: String,
    val createdAt: String
)
