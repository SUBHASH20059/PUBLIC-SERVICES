package org.psei.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.psei.auth.*
import org.psei.models.*
import org.psei.services.*

// Service instances (TODO: use dependency injection in production)
private val ideaService = IdeaVaultService()
private val patentService = PatentService()
private val schemeService = GovernmentSchemeService()
private val businessService = BusinessRegistrationService()
private val networkService = NetworkService()
private val studentService = StudentHubService()
private val auditService = AuditService()
private val notificationService = NotificationService()

fun Routing.registerPSEIRoutes() {
    route("/psei") {

        // ═══════════════════════════════════════════════════════════════════
        // 1. IDEA VAULT (Protected)
        // ═══════════════════════════════════════════════════════════════════

        route("/ideas") {
            authenticate("jwt") {

                get {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val ideas = if (user.role.atLeast(Role.OFFICER)) {
                        emptyList<IdeaVault>()  // TODO: query all
                    } else {
                        emptyList<IdeaVault>()  // TODO: query user's ideas
                    }
                    call.respond(mapOf("ideas" to ideas))
                }

                post {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val req = runCatching { call.receive<IdeaCreateRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val idea = IdeaVault(
                        title = req.title,
                        description = req.description,
                        creatorId = user.userId,
                        creatorName = user.email,
                        creatorEmail = user.email,
                        domain = req.domain,
                        isPublic = req.isPublic ?: false
                    )

                    val created = ideaService.createIdea(idea)
                    auditService.log(AuditLog(
                        userId = user.userId,
                        action = "CREATE_IDEA",
                        entityType = "IdeaVault",
                        entityId = created.id!!
                    ))

                    call.respond(HttpStatusCode.Created, created)
                }

                get("/{ideaId}") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val ideaId = call.parameters["ideaId"] ?: return@get
                    val idea = ideaService.getIdea(ideaId, user.userId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Idea not found"))
                    call.respond(idea)
                }

                // Register idea with digital signature
                post("/{ideaId}/register") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val ideaId = call.parameters["ideaId"] ?: return@post
                    val req = runCatching { call.receive<SignatureRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid signature"))
                        return@post
                    }

                    val success = ideaService.registerIdea(ideaId, req.signature, req.publicKeyFingerprint)
                    if (success) {
                        call.respond(mapOf("message" to "Idea registered with digital signature"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Idea not found"))
                    }
                }

                // View version history
                get("/{ideaId}/versions") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val ideaId = call.parameters["ideaId"] ?: return@get
                    val versions = ideaService.getVersionHistory(ideaId)
                    call.respond(mapOf("versions" to versions))
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // 2. PATENT ASSISTANCE (Protected)
        // ═══════════════════════════════════════════════════════════════════

        route("/patents") {
            authenticate("jwt") {

                post {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val req = runCatching { call.receive<PatentCreateRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val patent = PatentApplication(
                        applicantId = user.userId,
                        applicantName = user.email,
                        applicantEmail = user.email,
                        title = req.title,
                        abstract = req.abstract,
                        claims = req.claims
                    )

                    val created = patentService.filePatent(patent)
                    auditService.log(AuditLog(
                        userId = user.userId,
                        action = "FILE_PATENT",
                        entityType = "PatentApplication",
                        entityId = created.id!!
                    ))

                    call.respond(HttpStatusCode.Created, created)
                }

                get("/{patentId}") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val patentId = call.parameters["patentId"] ?: return@get
                    val patent = patentService.getPatent(patentId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Patent not found"))
                    
                    if (patent.applicantId != user.userId && !user.role.atLeast(Role.OFFICER)) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                    } else {
                        call.respond(patent)
                    }
                }

                // OFFICER+: update patent status
                put("/{patentId}/status") {
                    val user = call.requireRole(Role.OFFICER) ?: return@put
                    val patentId = call.parameters["patentId"] ?: return@put
                    val req = runCatching { call.receive<PatentStatusRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@put
                    }

                    val success = patentService.updateStatus(
                        patentId,
                        PatentStatus.valueOf(req.status),
                        req.examinerName,
                        req.findings
                    )
                    if (success) {
                        call.respond(mapOf("message" to "Patent status updated"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Patent not found"))
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // 3. GOVERNMENT SCHEMES (Public search, authenticated apply)
        // ═══════════════════════════════════════════════════════════════════

        route("/schemes") {

            // Public search
            get("/search") {
                val query = call.request.queryParameters["q"] ?: ""
                val results = schemeService.searchSchemes(query)
                call.respond(mapOf(
                    "query" to query,
                    "count" to results.size,
                    "schemes" to results
                ))
            }

            // List all schemes
            get {
                // TODO: paginate in production
                call.respond(mapOf("schemes" to emptyList<GovernmentScheme>()))
            }

            authenticate("jwt") {

                // Matched schemes for logged-in user
                get("/matched") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val state = call.request.queryParameters["state"]
                    val sector = call.request.queryParameters["sector"] ?: ""
                    
                    val matched = schemeService.matchSchemesForUser(user.userId, sector, state)
                    call.respond(mapOf("matched" to matched))
                }

                // Apply for scheme
                post("/{schemeId}/apply") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val schemeId = call.parameters["schemeId"] ?: return@post
                    val req = runCatching { call.receive<SchemeApplicationRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val application = SchemeApplication(
                        userId = user.userId,
                        schemeId = schemeId,
                        schemeName = req.schemeName,
                        documents = req.documents ?: emptyList()
                    )

                    val created = schemeService.applyForScheme(application)
                    call.respond(HttpStatusCode.Created, created)
                }

                // List my applications
                get("/my-applications") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val apps = schemeService.getApplications(user.userId)
                    call.respond(mapOf("applications" to apps))
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // 4. BUSINESS REGISTRATION (Protected)
        // ═══════════════════════════════════════════════════════════════════

        route("/business") {
            authenticate("jwt") {

                post {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val req = runCatching { call.receive<BusinessCreateRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val business = BusinessRegistration(
                        ownerId = user.userId,
                        ownerName = user.email,
                        businessName = req.businessName,
                        businessType = BusinessType.valueOf(req.businessType),
                        sector = req.sector,
                        address = req.address,
                        city = req.city,
                        state = req.state,
                        pinCode = req.pinCode,
                        email = user.email,
                        phone = req.phone,
                        panNumber = req.panNumber,
                        aadharNumber = req.aadharNumber
                    )

                    val created = businessService.register(business)
                    call.respond(HttpStatusCode.Created, created)
                }

                get("/{businessId}") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val businessId = call.parameters["businessId"] ?: return@get
                    val business = businessService.getRegistration(businessId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))
                    
                    if (business.ownerId != user.userId && !user.role.atLeast(Role.OFFICER)) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                    } else {
                        call.respond(business)
                    }
                }

                // OFFICER+: approve registration
                put("/{businessId}/approve") {
                    val user = call.requireRole(Role.OFFICER) ?: return@put
                    val businessId = call.parameters["businessId"] ?: return@put
                    val req = runCatching { call.receive<BusinessApprovalRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@put
                    }

                    val success = businessService.approveRegistration(businessId, req.certificateUrl, req.gstNumber)
                    if (success) {
                        call.respond(mapOf("message" to "Business approved"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // 5. MENTOR & INVESTOR NETWORK (Protected)
        // ═══════════════════════════════════════════════════════════════════

        route("/network") {

            route("/mentors") {

                // Public search
                get("/search") {
                    val expertise = call.request.queryParameters["expertise"] ?: ""
                    val mentors = networkService.searchMentors(expertise)
                    call.respond(mapOf("mentors" to mentors))
                }

                authenticate("jwt") {

                    // Register as mentor
                    post {
                        val user = call.requireRole(Role.CITIZEN) ?: return@post
                        val req = runCatching { call.receive<MentorRegisterRequest>() }.getOrElse {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                            return@post
                        }

                        val mentor = MentorProfile(
                            userId = user.userId,
                            name = req.name,
                            email = user.email,
                            expertise = req.expertise,
                            yearsOfExperience = req.yearsOfExperience,
                            bio = req.bio,
                            availability = "Available"
                        )

                        val created = networkService.registerMentor(mentor)
                        call.respond(HttpStatusCode.Created, created)
                    }

                    // Request mentorship
                    post("/{mentorId}/request") {
                        val user = call.requireRole(Role.CITIZEN) ?: return@post
                        val mentorId = call.parameters["mentorId"] ?: return@post

                        val request = MentorshipRequest(
                            menteeId = user.userId,
                            mentorId = mentorId,
                            reason = call.request.queryParameters["reason"] ?: "Seeking mentorship"
                        )

                        val created = networkService.requestMentorship(request)
                        call.respond(HttpStatusCode.Created, created)
                    }
                }
            }

            route("/investors") {

                // Public search
                get("/search") {
                    val sector = call.request.queryParameters["sector"] ?: ""
                    val funding = call.request.queryParameters["funding"]?.toLongOrNull() ?: 0
                    val investors = networkService.searchInvestors(sector, funding)
                    call.respond(mapOf("investors" to investors))
                }

                authenticate("jwt") {

                    // Register as investor
                    post {
                        val user = call.requireRole(Role.CITIZEN) ?: return@post
                        val req = runCatching { call.receive<InvestorRegisterRequest>() }.getOrElse {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                            return@post
                        }

                        val investor = InvestorProfile(
                            userId = user.userId,
                            name = req.name,
                            organizationName = req.organizationName,
                            email = user.email,
                            focusSectors = req.focusSectors,
                            minTicketINR = req.minTicketINR,
                            maxTicketINR = req.maxTicketINR,
                            bio = req.bio
                        )

                        val created = networkService.registerInvestor(investor)
                        call.respond(HttpStatusCode.Created, created)
                    }

                    // Propose investment
                    post("/{investorId}/propose") {
                        val user = call.requireRole(Role.CITIZEN) ?: return@post
                        val investorId = call.parameters["investorId"] ?: return@post
                        val req = runCatching { call.receive<InvestmentProposalRequest>() }.getOrElse {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                            return@post
                        }

                        val proposal = InvestmentProposal(
                            entrepreneurId = user.userId,
                            investorId = investorId,
                            businessName = req.businessName,
                            askAmount = req.askAmount,
                            equity = req.equity,
                            businessPlanUrl = req.businessPlanUrl
                        )

                        val created = networkService.proposeInvestment(proposal)
                        call.respond(HttpStatusCode.Created, created)
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // 6. STUDENT INNOVATION HUB (Protected)
        // ═══════════════════════════════════════════════════════════════════

        route("/student") {
            authenticate("jwt") {

                post("/projects") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@post
                    val req = runCatching { call.receive<StudentProjectRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val project = StudentProject(
                        studentId = user.userId,
                        studentName = req.studentName,
                        studentEmail = user.email,
                        institutionName = req.institutionName,
                        projectTitle = req.projectTitle,
                        projectDescription = req.projectDescription,
                        domain = req.domain,
                        teamMembers = req.teamMembers ?: emptyList(),
                        fundingRequired = req.fundingRequired
                    )

                    val created = studentService.submitProject(project)
                    call.respond(HttpStatusCode.Created, created)
                }

                get("/projects") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val projects = studentService.listProjectsByStudent(user.userId)
                    call.respond(mapOf("projects" to projects))
                }

                // OFFICER+: list approved projects
                get("/approved-projects") {
                    call.requireRole(Role.OFFICER) ?: return@get
                    val projects = studentService.listApprovedProjects()
                    call.respond(mapOf("projects" to projects))
                }

                // OFFICER+: approve project
                put("/projects/{projectId}/approve") {
                    val user = call.requireRole(Role.OFFICER) ?: return@put
                    val projectId = call.parameters["projectId"] ?: return@put

                    val success = studentService.approveProject(projectId)
                    if (success) {
                        call.respond(mapOf("message" to "Project approved"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Project not found"))
                    }
                }

                // ADMIN+: issue grant
                post("/grants") {
                    val user = call.requireRole(Role.ADMIN) ?: return@post
                    val req = runCatching { call.receive<GrantIssuanceRequest>() }.getOrElse {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                        return@post
                    }

                    val grant = StudentGrant(
                        projectId = req.projectId,
                        grantAmount = req.grantAmount,
                        grantType = req.grantType,
                        releaseDate = req.releaseDate
                    )

                    val created = studentService.issueGrant(grant)
                    call.respond(HttpStatusCode.Created, created)
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // 7. NOTIFICATIONS (Protected)
        // ═══════════════════════════════════════════════════════════════════

        route("/notifications") {
            authenticate("jwt") {

                get {
                    val user = call.requireRole(Role.CITIZEN) ?: return@get
                    val unread = notificationService.getUnread(user.userId)
                    call.respond(mapOf("unread" to unread))
                }

                put("/{notificationId}/read") {
                    val user = call.requireRole(Role.CITIZEN) ?: return@put
                    val notificationId = call.parameters["notificationId"] ?: return@put

                    val success = notificationService.markAsRead(notificationId)
                    if (success) {
                        call.respond(mapOf("message" to "Marked as read"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // 8. AUDIT LOGS (ADMIN+)
        // ═══════════════════════════════════════════════════════════════════

        route("/audit") {
            authenticate("jwt") {

                get("/user/{userId}") {
                    val user = call.requireRole(Role.ADMIN) ?: return@get
                    val userId = call.parameters["userId"] ?: return@get
                    val logs = auditService.getUserLogs(userId)
                    call.respond(mapOf("logs" to logs))
                }

                get("/entity/{entityId}") {
                    val user = call.requireRole(Role.ADMIN) ?: return@get
                    val entityId = call.parameters["entityId"] ?: return@get
                    val logs = auditService.getEntityLogs(entityId)
                    call.respond(mapOf("logs" to logs))
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// REQUEST/RESPONSE DTOs
// ═════════════════════════════════════════════════════════════════════════════

@Serializable data class IdeaCreateRequest(
    val title: String,
    val description: String,
    val domain: String,
    val isPublic: Boolean? = false
)

@Serializable data class SignatureRequest(
    val signature: String,
    val publicKeyFingerprint: String
)

@Serializable data class PatentCreateRequest(
    val title: String,
    val abstract: String,
    val claims: String
)

@Serializable data class PatentStatusRequest(
    val status: String,
    val examinerName: String,
    val findings: String
)

@Serializable data class SchemeApplicationRequest(
    val schemeName: String,
    val documents: List<String>? = null
)

@Serializable data class BusinessCreateRequest(
    val businessName: String,
    val businessType: String,
    val sector: String,
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val phone: String,
    val panNumber: String? = null,
    val aadharNumber: String? = null
)

@Serializable data class BusinessApprovalRequest(
    val certificateUrl: String,
    val gstNumber: String? = null
)

@Serializable data class MentorRegisterRequest(
    val name: String,
    val expertise: List<String>,
    val yearsOfExperience: Int,
    val bio: String
)

@Serializable data class InvestorRegisterRequest(
    val name: String,
    val organizationName: String,
    val focusSectors: List<String>,
    val minTicketINR: Long,
    val maxTicketINR: Long,
    val bio: String
)

@Serializable data class InvestmentProposalRequest(
    val businessName: String,
    val askAmount: Long,
    val equity: Double,
    val businessPlanUrl: String? = null
)

@Serializable data class StudentProjectRequest(
    val studentName: String,
    val institutionName: String,
    val projectTitle: String,
    val projectDescription: String,
    val domain: String,
    val teamMembers: List<String>? = null,
    val fundingRequired: Long? = null
)

@Serializable data class GrantIssuanceRequest(
    val projectId: String,
    val grantAmount: Long,
    val grantType: String,
    val releaseDate: String
)
