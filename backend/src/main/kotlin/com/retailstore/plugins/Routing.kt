package com.retailstore.plugins

import com.retailstore.presentation.dto.ErrorResponse
import com.retailstore.presentation.routes.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("INTERNAL_ERROR", cause.message ?: "Unknown error", 500)
            )
        }
    }

    routing {
        get("/docs") {
            val html = Application::class.java.getResourceAsStream("/openapi/swagger-ui.html")
                ?.bufferedReader()?.readText()
                ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respondText(html, ContentType.Text.Html)
        }
        get("/openapi.yaml") {
            val yaml = Application::class.java.getResourceAsStream("/openapi/documentation.yaml")
                ?.bufferedReader()?.readText()
                ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respondText(yaml, ContentType.Text.Plain)
        }

        route("/api/v1") {
            authRoutes()
            userRoutes()
            categoryRoutes()
            productRoutes()
            cartRoutes()
            wishlistRoutes()
            orderRoutes()
        }
    }
}
