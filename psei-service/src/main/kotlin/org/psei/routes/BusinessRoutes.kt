package org.psei.routes

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import org.psei.business.*

fun Routing.registerBusinessRoutes() {
    route("/dashboard") {
        // Summary for logged-in user: list linked businesses and quick stats
        get("/summary") {
            // TODO: require JWT and query role_assignments to return only authorized businesses
            call.respond(mapOf("businesses" to emptyList<Business>(), "summary" to mapOf<String, Any>()))
        }

        // Multi-business management
        get("/multi") {
            call.respond(mapOf("items" to emptyList<Business>()))
        }

        // Visualization data for ownership graphs
        get("/visualization/{businessId}") {
            call.respond(mapOf("nodes" to emptyList<Map<String,Any>>(), "edges" to emptyList<Map<String,Any>>()))
        }
    }

    route("/businesses") {
        get {
            // Admin-level listing or for debugging; in production restrict this
            call.respond(mapOf("items" to emptyList<Business>()))
        }

        post {
            // Create business (TODO: auth + validation + store in DB)
            val payload = call.receive<Business>()
            call.respond(HttpStatusCode.Created, payload)
        }

        get("/{id}") {
            // Return business profile if caller is authorized
            call.respond(HttpStatusCode.OK, mapOf("business" to null))
        }

        route("/{id}/ownerships") {
            get {
                call.respond(mapOf("ownerships" to emptyList<Ownership>()))
            }
        }

        route("/{id}/documents") {
            get {
                call.respond(mapOf("documents" to emptyList<DocumentMetadata>()))
            }
            post {
                // Upload placeholder
                call.respond(HttpStatusCode.Created, mapOf("uploaded" to true))
            }
            get("/download/{docId}") {
                // TODO: stream document with access checks and audit logging
                call.respond(HttpStatusCode.OK, mapOf("download" to false))
            }
        }

        route("/{id}/tax") {
            get {
                call.respond(HttpStatusCode.OK, mapOf("tax" to null))
            }
        }
    }
}
