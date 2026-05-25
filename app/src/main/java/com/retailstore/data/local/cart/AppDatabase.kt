package com.retailstore.data.local.cart

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GuestCartEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun guestCartDao(): GuestCartDao
}
