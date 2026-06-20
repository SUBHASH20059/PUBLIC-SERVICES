package org.psei.routes

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import org.psei.models.*
import org.psei.innovation.*

fun Routing.registerPSEIRoutes() {
    route("/psei") {
        // Idea Vault
        route("/ideas") {
            get {
                call.respond(mapOf("items" to emptyList<IdeaRecord>()))
            }
            post {
                // TODO: validate JWT, extract user, sign/version the idea before persisting
                val payload = call.receive<IdeaRecord>()
                call.respond(HttpStatusCode.Created, payload)
            }
            get("/{id}") {
                call.respond(HttpStatusCode.OK, mapOf("item" to null))
            }
        }

        // Patent assistance
        route("/patents") {
            get {
                call.respond(mapOf("items" to emptyList<PatentAssistanceRequest>()))
            }
            post {
                val payload = call.receive<PatentAssistanceRequest>()
                call.respond(HttpStatusCode.Created, payload)
            }
        }

        // Government schemes discovery (stub)
        route("/schemes") {
            get("/search") {
                // TODO: implement scheme matching (keyword or ML-based matcher)
                call.respond(mapOf("matches" to emptyList<SchemeMatch>()))
            }
        }

        // Protection templates (NDA / agreements)
        route("/protection/templates") {
            get {
                call.respond(mapOf("items" to emptyList<TemplateMetadata>()))
            }
            post {
                val payload = call.receive<TemplateMetadata>()
                call.respond(HttpStatusCode.Created, payload)
            }
        }

        // Hub endpoints
        route("/hub") {
            get("/mentors") {
                call.respond(mapOf("items" to emptyList<Mentor>()))
            }
            get("/investors") {
                call.respond(mapOf("items" to emptyList<Investor>()))
            }
            get("/students") {
                call.respond(mapOf("items" to emptyList<StudentProject>()))
            }
        }
    }
}
