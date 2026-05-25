package com.retailstore.data.local.cart

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GuestCartDao {
    @Query("SELECT * FROM guest_cart")
    fun getAll(): Flow<List<GuestCartEntity>>

    @Query("SELECT COUNT(*) FROM guest_cart")
    fun getCount(): Flow<Int>

    @Query("SELECT SUM(quantity) FROM guest_cart")
    fun getTotalQuantity(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: GuestCartEntity)

    @Delete
    suspend fun delete(item: GuestCartEntity)

    @Query("DELETE FROM guest_cart")
    suspend fun deleteAll()

    @Query("SELECT * FROM guest_cart WHERE productId = :productId")
    suspend fun findByProductId(productId: String): GuestCartEntity?
}
