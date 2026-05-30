package com.retailstore.presentation.routes

import com.retailstore.data.database.tables.ProductImagesTable
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class ImageUploadResponse(val url: String)

fun Route.imageRoutes() {
    route("/images") {
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.payload.getClaim("role").asString() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                val multipart = call.receiveMultipart()
                var imageBytes: ByteArray? = null
                var imageContentType = "image/jpeg"

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem && part.name == "image") {
                        imageContentType = part.contentType?.toString() ?: "image/jpeg"
                        imageBytes = part.streamProvider().readBytes()
                    }
                    part.dispose()
                }

                if (imageBytes == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No image provided"))
                    return@post
                }
                val bytes = imageBytes!!

                val id = UUID.randomUUID()
                newSuspendedTransaction {
                    ProductImagesTable.insert {
                        it[ProductImagesTable.id] = id
                        it[data] = bytes
                        it[ProductImagesTable.contentType] = imageContentType
                        it[createdAt] = LocalDateTime.now()
                    }
                }

                call.respond(HttpStatusCode.Created, ImageUploadResponse("images/$id"))
            }
        }

        get("/{id}") {
            val id = runCatching { UUID.fromString(call.parameters["id"]) }.getOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val row = newSuspendedTransaction {
                ProductImagesTable.select { ProductImagesTable.id eq id }.firstOrNull()
            } ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respondBytes(
                row[ProductImagesTable.data],
                ContentType.parse(row[ProductImagesTable.contentType])
            )
        }
    }
}
