package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {
    @Query("SELECT * FROM rides ORDER BY startTimeMillis DESC")
    fun getAllRides(): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE id = :id LIMIT 1")
    suspend fun getRideById(id: String): RideEntity?

    @Query("SELECT * FROM rides WHERE status = 'ACTIVE' OR status = 'PAUSED' LIMIT 1")
    fun getActiveRide(): Flow<RideEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideEntity)

    @Update
    suspend fun updateRide(ride: RideEntity)

    @Query("DELETE FROM rides")
    suspend fun clearRides()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}

@Dao
interface FleetOverrideDao {
    @Query("SELECT * FROM fleet_overrides")
    fun getAllOverrides(): Flow<List<FleetOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverride(override: FleetOverrideEntity)
}
