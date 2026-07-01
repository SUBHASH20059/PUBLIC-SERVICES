package org.psei.services

import org.psei.models.*
import java.time.LocalDateTime
import java.util.*

// ═════════════════════════════════════════════════════════════════════════════
// IDEA VAULT SERVICE
// ═════════════════════════════════════════════════════════════════════════════

class IdeaVaultService {
    private val ideas = mutableMapOf<String, IdeaVault>()
    private val versions = mutableMapOf<String, MutableList<IdeaVersion>>()

    fun createIdea(idea: IdeaVault): IdeaVault {
        val id = UUID.randomUUID().toString()
        val newIdea = idea.copy(id = id, status = IdeaStatus.DRAFT)
        ideas[id] = newIdea

        // Store initial version
        val version = IdeaVersion(
            id = UUID.randomUUID().toString(),
            ideaId = id,
            version = 1,
            content = idea.description,
            changedBy = idea.creatorId,
            hash = computeHash(idea.description)
        )
        versions[id] = mutableListOf(version)

        return newIdea
    }

    fun getIdea(ideaId: String, requesterId: String): IdeaVault? {
        val idea = ideas[ideaId] ?: return null

        // Log access
        if (idea.creatorId == requesterId || idea.isPublic) {
            val updatedIdea = idea.copy(
                accessLogs = idea.accessLogs + AccessLog(
                    accessorId = requesterId,
                    timestamp = LocalDateTime.now().toString(),
                    action = "VIEW"
                )
            )
            ideas[ideaId] = updatedIdea
            return updatedIdea
        }
        return null
    }

    fun registerIdea(ideaId: String, signature: String, fingerprint: String): Boolean {
        val idea = ideas[ideaId] ?: return false
        val updated = idea.copy(
            status = IdeaStatus.REGISTERED,
            digitalSignature = signature,
            publicKeyFingerprint = fingerprint
        )
        ideas[ideaId] = updated
        return true
    }

    fun getVersionHistory(ideaId: String): List<IdeaVersion> = versions[ideaId] ?: emptyList()

    fun updateIdea(ideaId: String, content: String, userId: String, reason: String? = null): Boolean {
        val idea = ideas[ideaId] ?: return false
        if (idea.creatorId != userId) return false

        val newVersion = idea.version + 1
        val version = IdeaVersion(
            id = UUID.randomUUID().toString(),
            ideaId = ideaId,
            version = newVersion,
            content = content,
            changedBy = userId,
            changeReason = reason,
            hash = computeHash(content)
        )

        versions[ideaId]?.add(version)
        ideas[ideaId] = idea.copy(version = newVersion)
        return true
    }

    private fun computeHash(content: String): String =
        content.hashCode().toString()  // TODO: use SHA-256 in production
}

// ═════════════════════════════════════════════════════════════════════════════
// PATENT SERVICE
// ═════════════════════════════════════════════════════════════════════════════

class PatentService {
    private val patents = mutableMapOf<String, PatentApplication>()

    fun filePatent(patent: PatentApplication): PatentApplication {
        val id = UUID.randomUUID().toString()
        val newPatent = patent.copy(id = id, status = PatentStatus.DRAFT)
        patents[id] = newPatent
        return newPatent
    }

    fun submitForExamination(patentId: String, userId: String): Boolean {
        val patent = patents[patentId] ?: return false
        if (patent.applicantId != userId) return false

        patents[patentId] = patent.copy(status = PatentStatus.FILED)
        return true
    }

    fun updateStatus(patentId: String, newStatus: PatentStatus, examinerName: String, findings: String): Boolean {
        val patent = patents[patentId] ?: return false

        val report = ExaminationReport(
            id = UUID.randomUUID().toString(),
            reportDate = LocalDateTime.now().toString(),
            examinerName = examinerName,
            findings = findings
        )

        val updated = patent.copy(
            status = newStatus,
            examinationReports = patent.examinationReports + report
        )
        patents[patentId] = updated
        return true
    }

    fun getPatent(patentId: String): PatentApplication? = patents[patentId]

    fun listPatentsByApplicant(applicantId: String): List<PatentApplication> =
        patents.values.filter { it.applicantId == applicantId }
}

// ═════════════════════════════════════════════════════════════════════════════
// GOVERNMENT SCHEME SERVICE (Keyword matching for now; AI/ML later)
// ═════════════════════════════════════════════════════════════════════════════

class GovernmentSchemeService {
    private val schemes = mutableListOf<GovernmentScheme>()
    private val applications = mutableMapOf<String, SchemeApplication>()

    init {
        seedSchemes()
    }

    fun searchSchemes(query: String): List<GovernmentScheme> {
        val lowerQuery = query.lowercase()
        return schemes.filter { scheme ->
            scheme.name.lowercase().contains(lowerQuery) ||
            scheme.keywords.any { it.lowercase().contains(lowerQuery) } ||
            scheme.description.lowercase().contains(lowerQuery)
        }
    }

    fun matchSchemesForUser(userId: String, sector: String, state: String? = null): List<GovernmentScheme> {
        return schemes.filter { scheme ->
            (state == null || scheme.state == null || scheme.state == state) &&
            scheme.keywords.any { it.lowercase().contains(sector.lowercase()) }
        }
    }

    fun applyForScheme(application: SchemeApplication): SchemeApplication {
        val id = UUID.randomUUID().toString()
        val newApp = application.copy(id = id)
        applications[id] = newApp
        return newApp
    }

    fun getApplications(userId: String): List<SchemeApplication> =
        applications.values.filter { it.userId == userId }

    private fun seedSchemes() {
        val startupsIndia = GovernmentScheme(
            name = "Startup India",
            ministry = "Ministry of Commerce & Industry",
            description = "Offers tax benefits, seed funding, and regulatory support for registered startups",
            eligibilityCriteria = "Indian startup registered < 7 years, turnover < ₹100 crore",
            benefits = listOf("Income tax holiday (3 years)", "No TDS for first 3 years", "Patent support"),
            keywords = listOf("startup", "funding", "tax", "technology"),
            state = null,
            url = "https://www.startupindia.gov.in"
        )

        val msme = GovernmentScheme(
            name = "MSME Sector Support",
            ministry = "Ministry of MSME",
            description = "Credit support, technology upgradation, and cluster development for MSMEs",
            eligibilityCriteria = "Micro/Small/Medium enterprises registered in India",
            benefits = listOf("Priority lending", "Credit guarantee", "Technology subsidy"),
            keywords = listOf("msme", "credit", "small business"),
            state = null,
            url = "https://www.msme.gov.in"
        )

        val aim = GovernmentScheme(
            name = "Atal Innovation Mission",
            ministry = "NITI Aayog",
            description = "Supports innovation across all sectors through Atal Incubation Centers",
            eligibilityCriteria = "Entrepreneurs, students, researchers with innovative ideas",
            benefits = listOf("Mentorship", "Co-working space", "Grant up to ₹20 lakh"),
            keywords = listOf("innovation", "student", "incubation"),
            state = null,
            url = "https://aim.gov.in"
        )

        schemes.addAll(listOf(startupsIndia, msme, aim))
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// BUSINESS REGISTRATION SERVICE
// ═════════════════════════════════════════════════════════════════════════════

class BusinessRegistrationService {
    private val registrations = mutableMapOf<String, BusinessRegistration>()

    fun register(business: BusinessRegistration): BusinessRegistration {
        val id = UUID.randomUUID().toString()
        val newBusiness = business.copy(id = id, status = RegistrationStatus.DRAFT)
        registrations[id] = newBusiness
        return newBusiness
    }

    fun submitForVerification(businessId: String, userId: String): Boolean {
        val business = registrations[businessId] ?: return false
        if (business.ownerId != userId) return false

        registrations[businessId] = business.copy(status = RegistrationStatus.SUBMITTED)
        return true
    }

    fun approveRegistration(businessId: String, certificateUrl: String, gstNumber: String? = null): Boolean {
        val business = registrations[businessId] ?: return false

        registrations[businessId] = business.copy(
            status = RegistrationStatus.APPROVED,
            certificateUrl = certificateUrl,
            gstNumber = gstNumber
        )
        return true
    }

    fun getRegistration(businessId: String): BusinessRegistration? = registrations[businessId]

    fun listByOwner(ownerId: String): List<BusinessRegistration> =
        registrations.values.filter { it.ownerId == ownerId }
}

// ═════════════════════════════════════════════════════════════════════════════
// MENTOR & INVESTOR SERVICE
// ═════════════════════════════════════════════════════════════════════════════

class NetworkService {
    private val mentors = mutableMapOf<String, MentorProfile>()
    private val investors = mutableMapOf<String, InvestorProfile>()
    private val mentorshipRequests = mutableMapOf<String, MentorshipRequest>()
    private val investmentProposals = mutableMapOf<String, InvestmentProposal>()

    fun registerMentor(mentor: MentorProfile): MentorProfile {
        val id = UUID.randomUUID().toString()
        val newMentor = mentor.copy(id = id)
        mentors[id] = newMentor
        return newMentor
    }

    fun searchMentors(expertise: String): List<MentorProfile> =
        mentors.values.filter { m ->
            m.expertise.any { it.lowercase().contains(expertise.lowercase()) } &&
            m.availability != "Unavailable" && m.menteeCount < m.menteeCapacity
        }

    fun requestMentorship(request: MentorshipRequest): MentorshipRequest {
        val id = UUID.randomUUID().toString()
        val newRequest = request.copy(id = id)
        mentorshipRequests[id] = newRequest
        return newRequest
    }

    fun acceptMentorship(requestId: String): Boolean {
        val request = mentorshipRequests[requestId] ?: return false
        mentorshipRequests[requestId] = request.copy(status = MentorshipStatus.ACCEPTED)
        
        mentors[request.mentorId]?.let { mentor ->
            mentors[request.mentorId] = mentor.copy(menteeCount = mentor.menteeCount + 1)
        }
        return true
    }

    fun registerInvestor(investor: InvestorProfile): InvestorProfile {
        val id = UUID.randomUUID().toString()
        val newInvestor = investor.copy(id = id)
        investors[id] = newInvestor
        return newInvestor
    }

    fun searchInvestors(sector: String, fundingNeeded: Long): List<InvestorProfile> =
        investors.values.filter { i ->
            i.focusSectors.any { it.lowercase().contains(sector.lowercase()) } &&
            i.minTicketINR <= fundingNeeded && fundingNeeded <= i.maxTicketINR
        }

    fun proposeInvestment(proposal: InvestmentProposal): InvestmentProposal {
        val id = UUID.randomUUID().toString()
        val newProposal = proposal.copy(id = id)
        investmentProposals[id] = newProposal
        return newProposal
    }

    fun getProposal(proposalId: String): InvestmentProposal? = investmentProposals[proposalId]

    fun listProposalsForInvestor(investorId: String): List<InvestmentProposal> =
        investmentProposals.values.filter { it.investorId == investorId }
}

// ═════════════════════════════════════════════════════════════════════════════
// STUDENT INNOVATION HUB SERVICE
// ═════════════════════════════════════════════════════════════════════════════

class StudentHubService {
    private val projects = mutableMapOf<String, StudentProject>()
    private val grants = mutableMapOf<String, StudentGrant>()

    fun submitProject(project: StudentProject): StudentProject {
        val id = UUID.randomUUID().toString()
        val newProject = project.copy(id = id, status = ProjectStatus.DRAFT)
        projects[id] = newProject
        return newProject
    }

    fun approveProject(projectId: String): Boolean {
        val project = projects[projectId] ?: return false
        projects[projectId] = project.copy(status = ProjectStatus.APPROVED)
        return true
    }

    fun issueGrant(grant: StudentGrant): StudentGrant {
        val id = UUID.randomUUID().toString()
        val newGrant = grant.copy(id = id, status = GrantStatus.APPROVED)
        grants[id] = newGrant
        return newGrant
    }

    fun updateGrantStatus(grantId: String, status: GrantStatus): Boolean {
        val grant = grants[grantId] ?: return false
        grants[grantId] = grant.copy(status = status)
        return true
    }

    fun listProjectsByStudent(studentId: String): List<StudentProject> =
        projects.values.filter { it.studentId == studentId }

    fun listApprovedProjects(): List<StudentProject> =
        projects.values.filter { it.status == ProjectStatus.APPROVED }

    fun getProject(projectId: String): StudentProject? = projects[projectId]
}

// ═════════════════════════════════════════════════════════════════════════════
// AUDIT & NOTIFICATION SERVICE
// ═════════════════════════════════════════════════════════════════════════════

class AuditService {
    private val logs = mutableListOf<AuditLog>()

    fun log(log: AuditLog) {
        logs.add(log.copy(id = UUID.randomUUID().toString()))
    }

    fun getUserLogs(userId: String): List<AuditLog> =
        logs.filter { it.userId == userId }

    fun getEntityLogs(entityId: String): List<AuditLog> =
        logs.filter { it.entityId == entityId }
}

class NotificationService {
    private val notifications = mutableMapOf<String, Notification>()

    fun send(notification: Notification): Notification {
        val id = UUID.randomUUID().toString()
        val newNotification = notification.copy(id = id)
        notifications[id] = newNotification
        return newNotification
    }

    fun getUnread(userId: String): List<Notification> =
        notifications.values.filter { it.recipientId == userId && !it.read }

    fun markAsRead(notificationId: String): Boolean {
        val notification = notifications[notificationId] ?: return false
        notifications[notificationId] = notification.copy(read = true)
        return true
    }
}
