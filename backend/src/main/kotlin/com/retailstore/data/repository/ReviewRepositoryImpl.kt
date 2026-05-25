package com.retailstore.data.repository

import com.retailstore.data.database.tables.ReviewsTable
import com.retailstore.data.database.tables.UsersTable
import com.retailstore.domain.model.Review
import com.retailstore.domain.repository.ReviewRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

class ReviewRepositoryImpl : ReviewRepository {

    private fun ResultRow.toReview() = Review(
        id = this[ReviewsTable.id],
        productId = this[ReviewsTable.productId],
        userId = this[ReviewsTable.userId],
        userName = this[UsersTable.fullName] ?: this[UsersTable.email].substringBefore("@"),
        rating = this[ReviewsTable.rating],
        comment = this[ReviewsTable.comment],
        createdAt = this[ReviewsTable.createdAt]
    )

    override suspend fun getReviews(productId: UUID): List<Review> =
        newSuspendedTransaction {
            (ReviewsTable innerJoin UsersTable)
                .select { ReviewsTable.productId eq productId }
                .orderBy(ReviewsTable.createdAt, SortOrder.DESC)
                .map { it.toReview() }
        }

    override suspend fun addReview(productId: UUID, userId: UUID, rating: Int, comment: String?): Review =
        newSuspendedTransaction {
            val id = UUID.randomUUID()
            ReviewsTable.insert {
                it[ReviewsTable.id] = id
                it[ReviewsTable.productId] = productId
                it[ReviewsTable.userId] = userId
                it[ReviewsTable.rating] = rating
                it[ReviewsTable.comment] = comment
                it[ReviewsTable.createdAt] = LocalDateTime.now()
            }
            (ReviewsTable innerJoin UsersTable)
                .select { ReviewsTable.id eq id }
                .single()
                .toReview()
        }

    override suspend fun deleteReview(productId: UUID, userId: UUID): Boolean =
        newSuspendedTransaction {
            ReviewsTable.deleteWhere {
                (ReviewsTable.productId eq productId) and (ReviewsTable.userId eq userId)
            } > 0
        }

    override suspend fun hasReviewed(productId: UUID, userId: UUID): Boolean =
        newSuspendedTransaction {
            ReviewsTable.select {
                (ReviewsTable.productId eq productId) and (ReviewsTable.userId eq userId)
            }.count() > 0
        }
}
