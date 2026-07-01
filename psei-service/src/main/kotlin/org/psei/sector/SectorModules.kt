package org.psei.sector

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

// ═════════════════════════════════════════════════════════════════════════════
// AGRICULTURE MODULE (400M+ Farmers)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class FarmerProfile(
    val id: String? = null,
    val userId: String,
    val fullName: String,
    val aadhaarMasked: String? = null,
    val pmKisanId: String? = null,
    val state: String,
    val district: String,
    val village: String,
    val pinCode: String,
    val landHoldingAcres: Double,
    val soilHealthCardNumber: String? = null,
    val bankAccountLinked: Boolean = false,
    val registeredAt: String = LocalDateTime.now().toString()
)

@Serializable
data class SoilHealthPassport(
    val id: String? = null,
    val farmerId: String,
    val surveyNumber: String,
    val testDate: String,
    val pH: Double,
    val nitrogen: Double,        // kg/ha
    val phosphorus: Double,
    val potassium: Double,
    val organicCarbon: Double,
    val micronutrients: Map<String, Double> = emptyMap(),
    val recommendedCrops: List<CropRecommendation> = emptyList(),
    val fertilizerDose: String? = null,
    val nextTestDate: String? = null
)

@Serializable
data class CropRecommendation(
    val cropName: String,
    val season: String,          // KHARIF, RABI, ZAID
    val expectedYield: Double,   // quintals/acre
    val marketPrice: Double,     // ₹/quintal (real-time APMC)
    val expectedRevenue: Double,
    val inputCost: Double,
    val netProfit: Double
)

@Serializable
data class MandiPrice(
    val commodity: String,
    val mandeName: String,
    val state: String,
    val arrivals: Double,        // quintals
    val minPrice: Double,        // ₹/quintal
    val maxPrice: Double,
    val modalPrice: Double,
    val priceDate: String,
    val trend: String,           // UP, DOWN, STABLE
    val prediction7Days: Double? = null,   // AI forecast
    val prediction30Days: Double? = null
)

@Serializable
data class CropInsuranceClaim(
    val id: String? = null,
    val farmerId: String,
    val policyNumber: String,
    val cropName: String,
    val cropSeason: String,
    val affectedArea: Double,
    val damageType: String,      // FLOOD, DROUGHT, PEST, HAILSTORM, CYCLONE
    val damagePercentage: Double,
    val satelliteImageEvidence: String? = null,
    val weatherDataEvidence: String? = null,
    val autoTriggered: Boolean = false,  // if true: satellite+weather auto-claim
    val claimAmount: Double? = null,
    val status: String = "PENDING",
    val filedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class WarehouseReceipt(
    val id: String? = null,
    val farmerId: String,
    val warehouseId: String,
    val warehouseName: String,
    val commodity: String,
    val quantityQtl: Double,
    val gradeQuality: String,
    val depositDate: String,
    val receiptNumber: String,
    val pledgedForLoan: Boolean = false,
    val loanAmount: Double? = null,
    val lenderName: String? = null,
    val expiryDate: String
)

class AgricultureService {
    private val farmers = mutableMapOf<String, FarmerProfile>()
    private val soilPassports = mutableMapOf<String, SoilHealthPassport>()
    private val claims = mutableMapOf<String, CropInsuranceClaim>()
    private val receipts = mutableMapOf<String, WarehouseReceipt>()

    fun registerFarmer(profile: FarmerProfile): FarmerProfile {
        val id = UUID.randomUUID().toString()
        val p = profile.copy(id = id)
        farmers[id] = p
        return p
    }

    fun addSoilHealthPassport(passport: SoilHealthPassport): SoilHealthPassport {
        val id = UUID.randomUUID().toString()
        val p = passport.copy(id = id, recommendedCrops = generateCropRecommendations(passport))
        soilPassports[id] = p
        return p
    }

    fun getMandPrices(commodity: String, state: String): List<MandiPrice> {
        // TODO: integrate eNAM / Agmarknet API
        return listOf(
            MandiPrice(commodity, "Azadpur Mandi", state, 500.0, 1800.0, 2100.0, 1950.0,
                LocalDateTime.now().toString(), "UP", 1980.0, 2050.0)
        )
    }

    fun autoFileCropInsuranceClaim(farmerId: String, cropName: String, damageType: String): CropInsuranceClaim {
        val claim = CropInsuranceClaim(
            farmerId = farmerId, policyNumber = "PMFBY-AUTO-${UUID.randomUUID().toString().take(8).uppercase()}",
            cropName = cropName, cropSeason = "KHARIF", affectedArea = 2.5,
            damageType = damageType, damagePercentage = 75.0,
            satelliteImageEvidence = "sentinel-2://image-${UUID.randomUUID()}",
            weatherDataEvidence = "imd://data-${UUID.randomUUID()}",
            autoTriggered = true, claimAmount = 35000.0
        )
        val id = UUID.randomUUID().toString()
        val c = claim.copy(id = id)
        claims[id] = c
        return c
    }

    fun createWarehouseReceipt(receipt: WarehouseReceipt): WarehouseReceipt {
        val id = UUID.randomUUID().toString()
        val r = receipt.copy(id = id, receiptNumber = "WHR-${UUID.randomUUID().toString().take(8).uppercase()}")
        receipts[id] = r
        return r
    }

    fun pledgeReceiptForLoan(receiptId: String, lenderName: String, loanAmount: Double): Boolean {
        val receipt = receipts[receiptId] ?: return false
        receipts[receiptId] = receipt.copy(pledgedForLoan = true, lenderName = lenderName, loanAmount = loanAmount)
        return true
    }

    private fun generateCropRecommendations(passport: SoilHealthPassport): List<CropRecommendation> {
        val recommendations = mutableListOf<CropRecommendation>()
        if (passport.pH in 6.0..7.5 && passport.nitrogen > 200) {
            recommendations += CropRecommendation("Wheat", "RABI", 20.0, 2200.0, 44000.0, 15000.0, 29000.0)
            recommendations += CropRecommendation("Mustard", "RABI", 8.0, 5000.0, 40000.0, 12000.0, 28000.0)
        }
        if (passport.pH in 5.5..7.0) {
            recommendations += CropRecommendation("Rice", "KHARIF", 25.0, 1800.0, 45000.0, 18000.0, 27000.0)
        }
        return recommendations
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// EDUCATION MODULE (300M+ Students)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class SkillPassport(
    val id: String? = null,
    val userId: String,
    val studentName: String,
    val credentials: List<MicroCredential> = emptyList(),
    val nsdcLinked: Boolean = false,
    val abcId: String? = null,      // Academic Bank of Credits
    val totalCredits: Int = 0,
    val employabilityScore: Int = 0,
    val updatedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class MicroCredential(
    val id: String? = null,
    val credentialName: String,
    val issuerInstitution: String,
    val issuerType: String,         // UGC, AICTE, NSDC, INDUSTRY, PSEI
    val skillCategory: String,
    val credits: Int,
    val level: String,              // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    val completedDate: String,
    val expiryDate: String? = null,
    val verificationUrl: String? = null,
    val blockchainHash: String? = null
)

@Serializable
data class ScholarshipMatch(
    val scholarshipId: String,
    val name: String,
    val provider: String,
    val amount: Long,               // INR per year
    val deadline: String,
    val eligibilityMet: Boolean,
    val matchScore: Double,         // 0.0-1.0
    val requirements: List<String>,
    val applyUrl: String? = null
)

@Serializable
data class PlacementPrediction(
    val studentId: String,
    val currentCGPA: Double,
    val skillVector: List<String>,
    val predictedSalaryRange: Pair<Long, Long>,   // INR/year (min, max)
    val topJobRoles: List<String>,
    val skillGaps: List<SkillGap>,
    val recommendedCourses: List<String>,
    val placementProbability: Double,
    val generatedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class SkillGap(val skill: String, val currentLevel: String, val requiredLevel: String, val priority: String)

class EducationService {
    private val skillPassports = mutableMapOf<String, SkillPassport>()

    fun createSkillPassport(userId: String, studentName: String): SkillPassport {
        val id = UUID.randomUUID().toString()
        val passport = SkillPassport(id = id, userId = userId, studentName = studentName,
            abcId = "ABC-${UUID.randomUUID().toString().take(12).uppercase()}")
        skillPassports[id] = passport
        return passport
    }

    fun addCredential(passportId: String, credential: MicroCredential): MicroCredential {
        val passport = skillPassports[passportId] ?: error("Skill passport not found")
        val c = credential.copy(id = UUID.randomUUID().toString())
        val updated = passport.copy(
            credentials = passport.credentials + c,
            totalCredits = passport.totalCredits + credential.credits
        )
        skillPassports[passportId] = updated
        return c
    }

    fun matchScholarships(cgpa: Double, income: Long, category: String, state: String): List<ScholarshipMatch> {
        // TODO: integrate National Scholarship Portal API
        val scholarships = mutableListOf<ScholarshipMatch>()
        if (cgpa >= 6.0 && income <= 600000) {
            scholarships += ScholarshipMatch(
                "NSP-001", "National Scholarship (OBC)", "Ministry of Social Justice",
                12000L, "31-Oct-2025", true, 0.95,
                listOf("CGPA ≥ 6.0", "Family income ≤ ₹6 lakh", "OBC category"),
                "https://scholarships.gov.in"
            )
        }
        if (cgpa >= 7.5) {
            scholarships += ScholarshipMatch(
                "NSP-002", "Central Sector Scholarship", "Ministry of Education",
                20000L, "31-Oct-2025", true, 0.90,
                listOf("Top 20% in class XII", "Family income ≤ ₹8 lakh"),
                "https://scholarships.gov.in"
            )
        }
        return scholarships.sortedByDescending { it.matchScore }
    }

    fun predictPlacement(studentId: String, cgpa: Double, skills: List<String>): PlacementPrediction {
        val gaps = mutableListOf<SkillGap>()
        if ("Data Structures" !in skills) gaps += SkillGap("Data Structures", "NONE", "INTERMEDIATE", "HIGH")
        if ("System Design" !in skills) gaps += SkillGap("System Design", "NONE", "ADVANCED", "HIGH")
        if ("SQL" !in skills) gaps += SkillGap("SQL", "NONE", "INTERMEDIATE", "MEDIUM")

        val baseSalary = (cgpa * 200000).toLong()
        return PlacementPrediction(
            studentId = studentId,
            currentCGPA = cgpa,
            skillVector = skills,
            predictedSalaryRange = Pair(baseSalary, baseSalary * 2),
            topJobRoles = listOf("Software Engineer", "Data Analyst", "Product Manager").take(if (cgpa >= 8.0) 3 else 2),
            skillGaps = gaps,
            recommendedCourses = gaps.map { "Master ${it.skill} on NPTEL / Coursera" },
            placementProbability = minOf(0.95, cgpa / 10.0 + 0.15)
        )
    }

    fun detectFakeInstitution(institutionName: String, affiliationNumber: String): Boolean {
        // TODO: query UGC + AICTE + NAAC database
        return institutionName.isNotEmpty() && affiliationNumber.isNotEmpty()
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// HEALTHCARE MODULE (1.4B Citizens)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class ABHAProfile(
    val id: String? = null,
    val userId: String,
    val abhaNumber: String,         // 14-digit ABHA number
    val abhaAddress: String,        // @abdm
    val linkedHealthFacilities: List<String> = emptyList(),
    val healthRecords: List<HealthRecord> = emptyList(),
    val consentGiven: Boolean = false,
    val createdAt: String = LocalDateTime.now().toString()
)

@Serializable
data class HealthRecord(
    val id: String? = null,
    val abhaId: String,
    val recordType: String,         // PRESCRIPTION, LAB_REPORT, DISCHARGE_SUMMARY
    val facilityName: String,
    val doctorName: String? = null,
    val date: String,
    val diagnosis: String? = null,
    val medications: List<String> = emptyList(),
    val encryptedFileUrl: String? = null
)

@Serializable
data class VaccinationRecord(
    val id: String? = null,
    val userId: String,
    val vaccineName: String,
    val vaccineType: String,        // ROUTINE, COVID19, TRAVEL
    val doseNumber: Int,
    val totalDoses: Int,
    val administeredAt: String,
    val facilityName: String,
    val batchNumber: String? = null,
    val nextDoseDate: String? = null,
    val certificateUrl: String? = null,
    val cowinCertId: String? = null  // CoWIN certificate integration
)

@Serializable
data class OrganDonorConsent(
    val id: String? = null,
    val userId: String,
    val organsConsented: List<String>,   // HEART, LIVER, KIDNEY, CORNEA, LUNGS, ALL
    val consentDate: String = LocalDateTime.now().toString(),
    val witnessName: String? = null,
    val familyNotified: Boolean = false,
    val registeredWithNOTTO: Boolean = false,  // National Organ & Tissue Transplant Organisation
    val donorCardUrl: String? = null
)

@Serializable
data class BloodInventory(
    val bloodBankId: String,
    val bloodBankName: String,
    val city: String,
    val state: String,
    val inventory: Map<String, Int>,   // blood type → units available
    val contactPhone: String,
    val lastUpdated: String = LocalDateTime.now().toString()
)

class HealthcareService {
    private val abhaProfiles = mutableMapOf<String, ABHAProfile>()
    private val vaccinations = mutableMapOf<String, MutableList<VaccinationRecord>>()
    private val organDonors = mutableMapOf<String, OrganDonorConsent>()
    private val bloodInventory = mutableMapOf<String, BloodInventory>()

    fun createABHAProfile(userId: String, abhaNumber: String): ABHAProfile {
        val id = UUID.randomUUID().toString()
        val profile = ABHAProfile(id = id, userId = userId, abhaNumber = abhaNumber,
            abhaAddress = "${abhaNumber}@abdm")
        abhaProfiles[id] = profile
        return profile
    }

    fun addVaccinationRecord(userId: String, record: VaccinationRecord): VaccinationRecord {
        val r = record.copy(id = UUID.randomUUID().toString())
        vaccinations.getOrPut(userId) { mutableListOf() }.add(r)
        // Check if booster reminder needed
        if (record.nextDoseDate != null) {
            println("[ALERT] Vaccination booster reminder scheduled for $userId on ${record.nextDoseDate}")
        }
        return r
    }

    fun registerOrganDonor(consent: OrganDonorConsent): OrganDonorConsent {
        val c = consent.copy(id = UUID.randomUUID().toString(), registeredWithNOTTO = true,
            donorCardUrl = "vault://organ-donor-card/${consent.userId}.pdf")
        organDonors[consent.userId] = c
        return c
    }

    fun searchBloodBanks(bloodType: String, city: String): List<BloodInventory> {
        // TODO: integrate eRaktkosh API / State blood bank APIs
        return listOf(
            BloodInventory("BB001", "AIIMS Blood Bank", city, "Delhi",
                mapOf("A+" to 12, "B+" to 8, "O+" to 20, bloodType to 5), "+91-11-26588500")
        )
    }

    fun getVaccinationHistory(userId: String): List<VaccinationRecord> =
        vaccinations[userId] ?: emptyList()
}

// ═════════════════════════════════════════════════════════════════════════════
// BUSINESS & MSME MODULE (63M MSMEs)
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class GSTComplianceReport(
    val gstNumber: String,
    val legalName: String,
    val filingStatus: Map<String, String>,  // period → FILED/PENDING/LATE
    val totalTaxPaid: Long,
    val itcAvailable: Long,          // Input Tax Credit
    val pendingReturns: List<String>,
    val mismatches: List<GSTMismatch>,
    val complianceScore: Int,        // 0-100
    val generatedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class GSTMismatch(
    val period: String,
    val invoiceNumber: String,
    val supplierGST: String,
    val reportedAmount: Double,
    val supplierReportedAmount: Double,
    val difference: Double,
    val severity: String           // LOW, MEDIUM, HIGH
)

@Serializable
data class TenderMatch(
    val tenderId: String,
    val title: String,
    val department: String,
    val estimatedValue: Long,
    val deadline: String,
    val platform: String,          // GEM, CPPP, STATE
    val matchScore: Double,
    val eligibilityStatus: String, // ELIGIBLE, PARTIAL, NOT_ELIGIBLE
    val applyUrl: String
)

@Serializable
data class ExportReadinessScore(
    val businessId: String,
    val overallScore: Int,         // 0-100
    val grade: String,
    val completedRequirements: List<String>,
    val missingRequirements: List<ExportRequirement>,
    val estimatedTimeToReady: String,
    val estimatedCost: Long
)

@Serializable
data class ExportRequirement(
    val requirement: String,
    val authority: String,
    val estimatedDays: Int,
    val estimatedCostINR: Long,
    val applicationUrl: String? = null
)

class MSMEBusinessService {
    private val gstReports = mutableMapOf<String, GSTComplianceReport>()
    private val tenderMatches = mutableMapOf<String, List<TenderMatch>>()

    fun generateGSTComplianceReport(gstNumber: String): GSTComplianceReport {
        // TODO: integrate GST Network API / GSTIN verification API
        return GSTComplianceReport(
            gstNumber = gstNumber,
            legalName = "Sample Business Pvt Ltd",
            filingStatus = mapOf("2024-Q4" to "FILED", "2025-Q1" to "PENDING"),
            totalTaxPaid = 1_50_000L,
            itcAvailable = 25_000L,
            pendingReturns = listOf("GSTR-3B for 2025-Q1", "GSTR-1 for March 2025"),
            mismatches = emptyList(),
            complianceScore = 78
        )
    }

    fun matchTenders(businessId: String, msmeCategory: String, sector: String): List<TenderMatch> {
        // TODO: integrate GeM API + CPPP portal
        return listOf(
            TenderMatch("GEM-2025-001", "Supply of IT Equipment", "Ministry of Education",
                50_00_000L, "30-Jul-2025", "GEM", 0.87, "ELIGIBLE",
                "https://gem.gov.in/tender/GEM-2025-001"),
            TenderMatch("CPPP-2025-042", "Software Development Services", "NIC",
                25_00_000L, "15-Aug-2025", "CPPP", 0.72, "ELIGIBLE",
                "https://cppp.nic.in/tender/042")
        )
    }

    fun assessExportReadiness(businessId: String, targetMarket: String): ExportReadinessScore {
        val missing = listOf(
            ExportRequirement("IEC Certificate", "DGFT", 7, 500, "https://dgft.gov.in"),
            ExportRequirement("APEDA Registration", "APEDA", 15, 5000, "https://apeda.gov.in"),
            ExportRequirement("Quality Certification", "BIS/ISO", 90, 50000, null)
        )
        return ExportReadinessScore(
            businessId = businessId,
            overallScore = 45,
            grade = "C",
            completedRequirements = listOf("GST Registration", "MSME Registration", "Bank Account"),
            missingRequirements = missing,
            estimatedTimeToReady = "3-4 months",
            estimatedCost = missing.sumOf { it.estimatedCostINR }
        )
    }

    fun detectPatentableInnovation(businessDescription: String, processes: List<String>): List<String> {
        // TODO: ML-based patent mining from business process descriptions
        val patentable = mutableListOf<String>()
        if (processes.any { it.contains("novel", ignoreCase = true) || it.contains("unique", ignoreCase = true) })
            patentable.add("Novel business process — may qualify for patent under Patents Act 1970, Section 3(k) review")
        if (processes.any { it.contains("machine learning", ignoreCase = true) || it.contains("AI", ignoreCase = true) })
            patentable.add("AI/ML implementation — review for software patent eligibility (technical effect doctrine)")
        return patentable
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// GOVERNANCE MODULE — Officer Tools
// ═════════════════════════════════════════════════════════════════════════════

@Serializable
data class OfficerWorkload(
    val officerId: String,
    val officerName: String,
    val department: String,
    val pendingCases: Int,
    val avgResolutionDays: Double,
    val expertiseAreas: List<String>,
    val currentCapacity: Int,       // 0-100 (percentage full)
    val satisfactionScore: Double   // Citizen NPS 0-10
)

@Serializable
data class CitizenSatisfactionPulse(
    val id: String? = null,
    val citizenId: String,
    val serviceType: String,
    val officerId: String? = null,
    val rating: Int,                // 1-5
    val feedback: String? = null,
    val wouldRecommend: Boolean,
    val submittedAt: String = LocalDateTime.now().toString()
)

@Serializable
data class RTIRequest(
    val id: String? = null,
    val applicantId: String,
    val department: String,
    val subject: String,
    val requestDetails: String,
    val filedDate: String = LocalDateTime.now().toString(),
    val status: String = "FILED",   // FILED, TRANSFERRED, RESPONDED, APPEALED
    val responseDeadline: String,   // 30 working days
    val responseUrl: String? = null,
    val firstAppealFiled: Boolean = false
)

@Serializable
data class RevenueIntelligenceAlert(
    val id: String? = null,
    val alertType: String,          // PROPERTY_TAX_EVASION, BENAMI, UNDER_VALUATION
    val propertyId: String,
    val currentOwner: String,
    val suspiciousIndicators: List<String>,
    val estimatedEvasionINR: Long,
    val riskScore: Double,
    val generatedAt: String = LocalDateTime.now().toString()
)

class GovernanceService {
    private val workloads = mutableMapOf<String, OfficerWorkload>()
    private val satisfactionPulses = mutableListOf<CitizenSatisfactionPulse>()
    private val rtiRequests = mutableMapOf<String, RTIRequest>()
    private val revenueAlerts = mutableListOf<RevenueIntelligenceAlert>()

    fun balanceWorkload(departments: List<String>): Map<String, List<String>> {
        // AI-based workload distribution
        val distribution = mutableMapOf<String, MutableList<String>>()
        departments.forEach { dept ->
            distribution[dept] = mutableListOf("Case auto-routed to least-loaded officer")
        }
        return distribution
    }

    fun submitSatisfactionPulse(pulse: CitizenSatisfactionPulse): CitizenSatisfactionPulse {
        val p = pulse.copy(id = UUID.randomUUID().toString())
        satisfactionPulses.add(p)
        if (pulse.rating <= 2) {
            println("[ALERT] Low satisfaction (${pulse.rating}/5) for officer ${pulse.officerId} — escalation triggered")
        }
        return p
    }

    fun fileRTIRequest(request: RTIRequest): RTIRequest {
        val id = UUID.randomUUID().toString()
        val deadline = java.time.LocalDate.now().plusDays(30).toString()
        val r = request.copy(id = id, responseDeadline = deadline)
        rtiRequests[id] = r
        return r
    }

    fun getDepartmentSatisfactionScore(department: String): Double =
        satisfactionPulses.filter { it.serviceType.contains(department, ignoreCase = true) }
            .takeIf { it.isNotEmpty() }
            ?.map { it.rating.toDouble() }
            ?.average() ?: 0.0

    fun detectPropertyTaxEvasion(propertyId: String, registeredValue: Long, marketValue: Long): RevenueIntelligenceAlert? {
        val undervaluation = marketValue - registeredValue
        if (undervaluation > marketValue * 0.30) {
            val alert = RevenueIntelligenceAlert(
                id = UUID.randomUUID().toString(),
                alertType = "UNDER_VALUATION",
                propertyId = propertyId,
                currentOwner = "Unknown",
                suspiciousIndicators = listOf(
                    "Registered value ₹${registeredValue/100000}L vs market value ₹${marketValue/100000}L",
                    "Undervaluation of ${((undervaluation.toDouble()/marketValue)*100).toInt()}%"
                ),
                estimatedEvasionINR = (undervaluation * 0.08).toLong(),  // 8% stamp duty on difference
                riskScore = minOf(1.0, undervaluation.toDouble() / marketValue)
            )
            revenueAlerts.add(alert)
            return alert
        }
        return null
    }
}
