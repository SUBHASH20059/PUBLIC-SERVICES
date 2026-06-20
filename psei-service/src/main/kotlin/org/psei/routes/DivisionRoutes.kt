package org.psei.routes

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import org.psei.models.*

fun Routing.registerDivisionRoutes() {
    // Civil Affairs
    route("/civil") {
        get("/marriages") {
            call.respond(mapOf("items" to emptyList<Marriage>()))
        }
        post("/marriages") {
            val payload = call.receive<Marriage>()
            call.respond(HttpStatusCode.Created, payload)
        }
        get("/births") {
            call.respond(mapOf("items" to emptyList<Birth>()))
        }
        post("/births") {
            val payload = call.receive<Birth>()
            call.respond(HttpStatusCode.Created, payload)
        }
    }

    // Real Estate
    route("/real-estate") {
        get("/properties") {
            call.respond(mapOf("items" to emptyList<Property>()))
        }
        post("/properties") {
            val payload = call.receive<Property>()
            call.respond(HttpStatusCode.Created, payload)
        }
    }

    // Business & Startup
    route("/business") {
        get("/companies") {
            call.respond(mapOf("items" to emptyList<Company>()))
        }
        post("/companies") {
            val payload = call.receive<Company>()
            call.respond(HttpStatusCode.Created, payload)
        }
    }

    // Legal & Dispute Resolution
    route("/legal") {
        get("/complaints") {
            call.respond(mapOf("items" to emptyList<Complaint>()))
        }
        post("/complaints") {
            val payload = call.receive<Complaint>()
            call.respond(HttpStatusCode.Created, payload)
        }
    }
}
