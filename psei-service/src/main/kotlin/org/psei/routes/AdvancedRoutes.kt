package org.psei.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.psei.auth.*
import org.psei.ml.*
import org.psei.offline.*
import org.psei.security.*

// ═══ Service instances ════════════════════════════════════════════════════════
private val forgeryDetector    = DocumentForgeryDetectionService()
private val schemeEngine       = SchemeRecommendationEngine()
private val vcService          = VerifiableCredentialService()
private val zkpService         = ZKPAgeProofService()
private val intentRouter       = WhatsAppIntentRouter()
private val botResponder       = WhatsAppBotResponseGenerator(schemeEngine)
private val rateLimiter        = TokenBucketRateLimiter(capacity = 100, refillRatePerSecond = 10)
private val encryptionService  = DocumentEncryptionService()
private val crdtSyncEngine     = CRDTSyncEngine()

fun Routing.registerAdvancedRoutes() {

    // ═══════════════════════════════════════════════════════════════════════
    // OPENAPI v3 SPEC  (machine-readable, served at /openapi.json)
    // ═══════════════════════════════════════════════════════════════════════
    get("/openapi.json") {
        call.respond(HttpStatusCode.OK, openApiSpec())
    }

    get("/docs") {
        call.respondText(
            """
            <!DOCTYPE html><html><head>
            <title>PSEI Authority API</title>
            <meta charset="utf-8"/>
            <script type="module" src="https://unpkg.com/rapidoc/dist/rapidoc-min.js"></script>
            </head><body>
            <rapi-doc spec-url="/openapi.json" theme="dark" show-header="false"
              primary-color="#FF9933" bg-color="#002366" text-color="#ffffff">
            </rapi-doc>
            </body></html>
            """.trimIndent(),
            contentType = ContentType.Text.Html
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // V1 RESOURCE-ORIENTED API  (HATEOAS)
    // ═══════════════════════════════════════════════════════════════════════
    route("/v1") {
        authenticate("jwt") {

            // GET /v1/citizens/{id}/vault
            get("/citizens/{citizenId}/vault") {
                val user = call.requireRole(Role.CITIZEN) ?: return@get
                val citizenId = call.parameters["citizenId"] ?: return@get
                val fields = call.request.queryParameters["fields"]?.split(",") ?: emptyList()

                if (citizenId != user.userId && !user.role.atLeast(Role.OFFICER)) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                    return@get
                }

                call.response.headers.append("X-Cache-Status", "MISS")
                call.response.headers.append("X-RateLimit-Remaining", "99")

                call.respond(mapOf(
                    "citizenId" to citizenId,
                    "documentCount" to 0,
                    "lastUpdated" to java.time.Instant.now().toString(),
                    "fields" to fields,
                    "_links" to mapOf(
                        "self" to "/v1/citizens/$citizenId/vault",
                        "documents" to "/citizen-vault/identity-documents",
                        "certificates" to "/citizen-vault/certificates",
                        "schemes" to "/v1/citizens/$citizenId/eligible-schemes"
                    )
                ))
            }

            // GET /v1/citizens/{id}/eligible-schemes
            get("/citizens/{citizenId}/eligible-schemes") {
                val user = call.requireRole(Role.CITIZEN) ?: return@get
                val citizenId = call.parameters["citizenId"] ?: return@get

                // Rate limit per citizen
                if (!rateLimiter.allow(citizenId)) {
                    call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "Rate limit exceeded"))
                    return@get
                }

                val profileReq = runCatching { call.receive<CitizenProfileRequest>() }.getOrNull()
                val profile = CitizenProfile(
                    citizenId = citizenId,
                    age = profileReq?.age ?: 25,
                    gender = profileReq?.gender ?: "M",
                    state = profileReq?.state ?: "national",
                    annualIncomeINR = profileReq?.annualIncomeINR ?: 3_00_000L,
                    occupation = profileReq?.occupation ?: "GENERAL",
                    educationLevel = profileReq?.educationLevel ?: "SECONDARY",
                    isMSMEOwner = profileReq?.isMSMEOwner ?: false,
                    isStartupFounder = profileReq?.isStartupFounder ?: false
                )

                val algorithm = call.request.queryParameters["matchAlgorithm"] ?: "ensemble"
                val recommendations = schemeEngine.recommend(profile, limit = 10)

                call.respond(mapOf(
                    "citizenId" to citizenId,
                    "algorithm" to algorithm,
                    "count" to recommendations.size,
                    "recommendations" to recommendations,
                    "_links" to mapOf(
                        "apply" to "/psei/schemes/{schemeId}/apply",
                        "search" to "/psei/schemes/search"
                    )
                ))
            }

            // POST /v1/citizens/{id}/verifiable-credentials
            post("/citizens/{citizenId}/verifiable-credentials") {
                val user = call.requireRole(Role.CITIZEN) ?: return@post
                val citizenId = call.parameters["citizenId"] ?: return@post
                val req = runCatching { call.receive<VCIssuanceRequest>() }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                    return@post
                }

                val vc = vcService.issue(
                    citizenDID = "did:psei:$citizenId",
                    claims = req.claims,
                    credentialTypes = req.credentialTypes,
                    expiryDays = req.expiryDays ?: 365L
                )

                call.respond(HttpStatusCode.Created, mapOf(
                    "verifiableCredential" to mapOf(
                        "id" to vc.id,
                        "type" to vc.type,
                        "issuer" to vc.issuer,
                        "issuanceDate" to vc.issuanceDate,
                        "expirationDate" to vc.expirationDate,
                        "credentialSubject" to vc.credentialSubject,
                        "proof" to mapOf(
                            "type" to vc.proof.type,
                            "created" to vc.proof.created,
                            "verificationMethod" to vc.proof.verificationMethod,
                            "proofValue" to vc.proof.proofValue
                        )
                    )
                ))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ML & AI ROUTES
    // ═══════════════════════════════════════════════════════════════════════
    route("/ai") {
        authenticate("jwt") {

            // POST /ai/documents/{id}/analyse-forgery
            post("/documents/{documentId}/analyse-forgery") {
                val user = call.requireRole(Role.OFFICER) ?: return@post
                val documentId = call.parameters["documentId"] ?: return@post

                val metadata = DocumentMetadataFeatures(
                    hasValidWatermark = call.request.queryParameters["watermark"] != "false",
                    fontAnomalyDetected = call.request.queryParameters["fontAnomaly"] == "true",
                    metadataDateMismatch = call.request.queryParameters["dateMismatch"] == "true"
                )

                val result = forgeryDetector.analyseDocument(
                    documentId = documentId,
                    documentBytes = ByteArray(0),  // TODO: load from vault
                    metadata = metadata
                )

                call.respond(mapOf(
                    "documentId" to result.documentId,
                    "verdict" to result.verdict.name,
                    "fraudScore" to result.fraudScore,
                    "confidence" to result.confidence,
                    "signals" to result.signals,
                    "reviewRequired" to result.reviewRequired,
                    "blockedImmediately" to result.blockedImmediately,
                    "analysedAt" to result.analysedAt
                ))
            }

            // POST /ai/schemes/discover (natural language query)
            post("/schemes/discover") {
                val user = call.requireRole(Role.CITIZEN) ?: return@post
                val req = runCatching { call.receive<AISchemeDiscoverRequest>() }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                    return@post
                }

                val profile = CitizenProfile(
                    citizenId = user.userId,
                    age = req.age ?: 25,
                    gender = req.gender ?: "M",
                    state = req.state ?: "national",
                    annualIncomeINR = req.annualIncomeINR ?: 3_00_000L,
                    occupation = req.occupation ?: "GENERAL",
                    educationLevel = req.educationLevel ?: "SECONDARY",
                    isMSMEOwner = req.isMSMEOwner ?: false,
                    isStartupFounder = req.isStartupFounder ?: false
                )

                val results = schemeEngine.recommend(profile, limit = req.limit ?: 10)
                call.respond(mapOf(
                    "query" to (req.context ?: ""),
                    "algorithm" to (req.matchAlgorithm ?: "ensemble"),
                    "count" to results.size,
                    "results" to results
                ))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ZERO-KNOWLEDGE PROOFS
    // ═══════════════════════════════════════════════════════════════════════
    route("/zkp") {
        authenticate("jwt") {

            // POST /zkp/age-proof  — prove age ≥ 18 without revealing birthdate
            post("/age-proof") {
                val user = call.requireRole(Role.CITIZEN) ?: return@post
                val req = runCatching { call.receive<AgeProofRequest>() }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                    return@post
                }

                val birthdateEpoch = java.time.LocalDate.parse(req.birthdateISO).toEpochDay()
                val thresholdEpoch = java.time.LocalDate.parse(req.thresholdDateISO).toEpochDay()

                val proof = zkpService.generateProof(birthdateEpoch, thresholdEpoch)

                call.respond(HttpStatusCode.Created, mapOf(
                    "proofId" to proof.proofId,
                    "commitmentHash" to proof.commitmentHash,
                    "publicInput" to proof.publicInput,
                    "validForMinutes" to proof.validForMinutes,
                    "generatedAt" to proof.generatedAt,
                    "message" to "Proof generated — birthdate never revealed to verifier",
                    "usage" to "Share proofId + commitmentHash with verifier; they call POST /zkp/verify"
                ))
            }

            // POST /zkp/verify
            post("/verify") {
                val req = runCatching { call.receive<ZKPVerifyRequest>() }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                    return@post
                }

                val threshold = java.time.LocalDate.parse(req.thresholdDateISO).toEpochDay()
                val proof = AgeProof(
                    proofId = req.proofId,
                    commitmentHash = req.commitmentHash,
                    publicInput = threshold.toString(),
                    proofBytes = ByteArray(0),
                    verificationKeyHash = "",
                    generatedAt = req.generatedAt,
                    validForMinutes = req.validForMinutes
                )

                val valid = zkpService.verifyProof(proof, threshold)
                call.respond(mapOf(
                    "proofId" to req.proofId,
                    "valid" to valid,
                    "message" to if (valid) "Proof verified: citizen satisfies age threshold"
                    else "Proof invalid or expired"
                ))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // WHATSAPP WEBHOOK  (Meta Business API endpoint)
    // ═══════════════════════════════════════════════════════════════════════
    route("/whatsapp") {

        // GET /whatsapp/webhook — Meta verification handshake
        get("/webhook") {
            val mode = call.request.queryParameters["hub.mode"]
            val token = call.request.queryParameters["hub.verify_token"]
            val challenge = call.request.queryParameters["hub.challenge"]

            val expectedToken = System.getenv("WHATSAPP_VERIFY_TOKEN") ?: "psei-whatsapp-verify"

            if (mode == "subscribe" && token == expectedToken) {
                call.respondText(challenge ?: "", ContentType.Text.Plain)
            } else {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Invalid verify token"))
            }
        }

        // POST /whatsapp/webhook — Incoming messages from citizens
        post("/webhook") {
            val body = runCatching { call.receive<WhatsAppWebhookPayload>() }.getOrElse {
                call.respond(HttpStatusCode.OK, mapOf("status" to "ignored")) // Always 200 to Meta
                return@post
            }

            // Extract first message (simplified — production handles all entries)
            val entry = body.entry.firstOrNull()
            val change = entry?.changes?.firstOrNull()
            val msgObj = change?.value?.messages?.firstOrNull()

            if (msgObj != null) {
                val message = WhatsAppMessage(
                    from = msgObj.from,
                    messageId = msgObj.id,
                    body = msgObj.text?.body ?: ""
                )

                val intent = intentRouter.parse(message)
                val reply = botResponder.generateReply(message, intent)

                // TODO: call Meta Business API to send reply
                // POST https://graph.facebook.com/v18.0/{phone_number_id}/messages
                println("[WhatsApp Bot] From=${message.from} Intent=${intent.intent.name} Reply=${reply.body.take(50)}")
            }

            call.respond(HttpStatusCode.OK, mapOf("status" to "received"))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CRDT OFFLINE SYNC
    // ═══════════════════════════════════════════════════════════════════════
    route("/sync") {
        authenticate("jwt") {

            // POST /sync/push — citizen device pushes pending operations
            post("/push") {
                val user = call.requireRole(Role.CITIZEN) ?: return@post
                val req = runCatching { call.receive<SyncPushRequest>() }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))
                    return@post
                }

                req.operations.forEach { op ->
                    crdtSyncEngine.applyOperation(
                        user.userId,
                        SyncOperation(
                            type = SyncOpType.valueOf(op.type),
                            documentId = op.documentId,
                            vectorClock = op.vectorClock
                        )
                    )
                }

                val newRoot = crdtSyncEngine.getRemoteMerkleRoot(user.userId)
                call.respond(mapOf(
                    "synced" to req.operations.size,
                    "merkleRoot" to newRoot,
                    "timestamp" to java.time.Instant.now().toString()
                ))
            }

            // GET /sync/status — get remote Merkle root for diff check
            get("/status") {
                val user = call.requireRole(Role.CITIZEN) ?: return@get
                val remoteRoot = crdtSyncEngine.getRemoteMerkleRoot(user.userId)
                call.respond(mapOf(
                    "citizenId" to user.userId,
                    "merkleRoot" to remoteRoot,
                    "timestamp" to java.time.Instant.now().toString()
                ))
            }
        }
    }
}

// ─── Request/Response DTOs ───────────────────────────────────────────────────

@Serializable data class CitizenProfileRequest(
    val age: Int? = null,
    val gender: String? = null,
    val state: String? = null,
    val annualIncomeINR: Long? = null,
    val occupation: String? = null,
    val educationLevel: String? = null,
    val isMSMEOwner: Boolean? = null,
    val isStartupFounder: Boolean? = null
)

@Serializable data class VCIssuanceRequest(
    val claims: Map<String, String>,
    val credentialTypes: List<String> = emptyList(),
    val expiryDays: Long? = null
)

@Serializable data class AISchemeDiscoverRequest(
    val context: String? = null,
    val matchAlgorithm: String? = null,
    val limit: Int? = null,
    val age: Int? = null,
    val gender: String? = null,
    val state: String? = null,
    val annualIncomeINR: Long? = null,
    val occupation: String? = null,
    val educationLevel: String? = null,
    val isMSMEOwner: Boolean? = null,
    val isStartupFounder: Boolean? = null
)

@Serializable data class AgeProofRequest(
    val birthdateISO: String,      // "YYYY-MM-DD" — never stored after proof generation
    val thresholdDateISO: String   // Date 18 years ago from today
)

@Serializable data class ZKPVerifyRequest(
    val proofId: String,
    val commitmentHash: String,
    val thresholdDateISO: String,
    val generatedAt: String,
    val validForMinutes: Int
)

@Serializable data class WhatsAppWebhookPayload(
    val `object`: String = "",
    val entry: List<WhatsAppEntry> = emptyList()
)
@Serializable data class WhatsAppEntry(val id: String = "", val changes: List<WhatsAppChange> = emptyList())
@Serializable data class WhatsAppChange(val value: WhatsAppValue = WhatsAppValue(), val field: String = "")
@Serializable data class WhatsAppValue(val messages: List<WhatsAppMsg> = emptyList())
@Serializable data class WhatsAppMsg(val id: String = "", val from: String = "", val text: WhatsAppText? = null, val type: String = "")
@Serializable data class WhatsAppText(val body: String = "")

@Serializable data class SyncPushRequest(val operations: List<SyncOpRequest> = emptyList())
@Serializable data class SyncOpRequest(val type: String, val documentId: String, val vectorClock: Long)

// ─── OpenAPI v3 Spec ─────────────────────────────────────────────────────────
private fun openApiSpec(): Map<String, Any> = mapOf(
    "openapi" to "3.0.3",
    "info" to mapOf(
        "title" to "PSEI Authority API",
        "version" to "2.0.0",
        "description" to "National Digital Trust Platform — India",
        "contact" to mapOf("name" to "PSEI Authority", "url" to "https://github.com/SUBHASH20059/PUBLIC-SERVICES")
    ),
    "servers" to listOf(
        mapOf("url" to "https://api.psei.gov.in/v1", "description" to "Production"),
        mapOf("url" to "https://sandbox-api.psei.gov.in/v1", "description" to "Sandbox (mock HSM, fake Aadhaar)")
    ),
    "security" to listOf(mapOf("BearerAuth" to emptyList<String>())),
    "paths" to mapOf(
        "/citizens/{citizenId}/vault" to mapOf(
            "get" to mapOf(
                "summary" to "Retrieve citizen document vault",
                "tags" to listOf("Vault"),
                "parameters" to listOf(
                    mapOf("name" to "citizenId", "in" to "path", "required" to true),
                    mapOf("name" to "fields", "in" to "query", "schema" to mapOf("type" to "array"))
                ),
                "responses" to mapOf("200" to mapOf("description" to "Vault retrieved"))
            )
        ),
        "/citizens/{citizenId}/eligible-schemes" to mapOf(
            "get" to mapOf("summary" to "AI-powered scheme matching", "tags" to listOf("Schemes"))
        ),
        "/citizens/{citizenId}/verifiable-credentials" to mapOf(
            "post" to mapOf("summary" to "Issue W3C Verifiable Credential", "tags" to listOf("Identity"))
        ),
        "/ai/documents/{documentId}/analyse-forgery" to mapOf(
            "post" to mapOf("summary" to "ML forgery detection (ensemble: CNN + LSTM + XGBoost)", "tags" to listOf("AI"))
        ),
        "/ai/schemes/discover" to mapOf(
            "post" to mapOf("summary" to "Natural language scheme discovery", "tags" to listOf("AI"))
        ),
        "/zkp/age-proof" to mapOf(
            "post" to mapOf("summary" to "Generate ZK proof of age without revealing birthdate", "tags" to listOf("Privacy"))
        ),
        "/zkp/verify" to mapOf(
            "post" to mapOf("summary" to "Verify a ZK age proof", "tags" to listOf("Privacy"))
        ),
        "/whatsapp/webhook" to mapOf(
            "post" to mapOf("summary" to "WhatsApp Bot webhook — multilingual citizen assistant", "tags" to listOf("Bot"))
        ),
        "/sync/push" to mapOf(
            "post" to mapOf("summary" to "CRDT offline sync — push queued operations", "tags" to listOf("Offline"))
        )
    ),
    "components" to mapOf(
        "securitySchemes" to mapOf(
            "BearerAuth" to mapOf("type" to "http", "scheme" to "bearer", "bearerFormat" to "JWT")
        )
    )
)
