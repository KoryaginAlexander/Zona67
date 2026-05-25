package com.retailstore.presentation.routes

import com.retailstore.domain.repository.ReviewRepository
import com.retailstore.presentation.dto.CreateReviewRequest
import com.retailstore.presentation.dto.ErrorResponse
import com.retailstore.presentation.dto.ReviewResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.reviewRoutes() {
    val reviewRepository by inject<ReviewRepository>()

    route("/products/{productId}/reviews") {
        get {
            val productId = UUID.fromString(call.parameters["productId"] ?: return@get call.respond(HttpStatusCode.BadRequest))
            val reviews = reviewRepository.getReviews(productId)
            call.respond(reviews.map { it.toResponse() })
        }

        authenticate("auth-jwt") {
            post {
                val userId = call.reviewUserId()
                val productId = UUID.fromString(call.parameters["productId"] ?: return@post call.respond(HttpStatusCode.BadRequest))
                if (reviewRepository.hasReviewed(productId, userId)) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse("ALREADY_REVIEWED", "Вы уже оставили отзыв на этот товар", 409))
                    return@post
                }
                val request = call.receive<CreateReviewRequest>()
                if (request.rating !in 1..5) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_RATING", "Оценка должна быть от 1 до 5", 400))
                    return@post
                }
                val review = reviewRepository.addReview(productId, userId, request.rating, request.comment)
                call.respond(HttpStatusCode.Created, review.toResponse())
            }

            delete {
                val userId = call.reviewUserId()
                val productId = UUID.fromString(call.parameters["productId"] ?: return@delete call.respond(HttpStatusCode.BadRequest))
                reviewRepository.deleteReview(productId, userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun com.retailstore.domain.model.Review.toResponse() = ReviewResponse(
    id = id.toString(),
    productId = productId.toString(),
    userId = userId.toString(),
    userName = userName,
    rating = rating,
    comment = comment,
    createdAt = createdAt.toString()
)

private fun ApplicationCall.reviewUserId(): UUID {
    val principal = principal<JWTPrincipal>()!!
    return UUID.fromString(principal.payload.getClaim("userId").asString())
}
