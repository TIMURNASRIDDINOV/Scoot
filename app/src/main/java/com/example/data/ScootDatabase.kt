package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RideEntity::class, TransactionEntity::class, FleetOverrideEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScootDatabase : RoomDatabase() {
    abstract fun rideDao(): RideDao
    abstract fun transactionDao(): TransactionDao
    abstract fun fleetOverrideDao(): FleetOverrideDao

    companion object {
        @Volatile
        private var INSTANCE: ScootDatabase? = null

        fun getInstance(context: Context): ScootDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScootDatabase::class.java,
                    "scoot_tashkent.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
