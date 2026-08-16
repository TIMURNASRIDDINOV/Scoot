package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val vehicleModel: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val startLocationName: String,
    val endLocationName: String?,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val totalCostUzs: Long,
    val status: String,
    val parkingPhotoUri: String?,
    val paymentMethod: String,
    val paymentTransactionId: String?,
    val rating: Int?
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amountUzs: Long,
    val isDebit: Boolean,
    val timestampMillis: Long,
    val paymentMethod: String,
    val referenceId: String
)

@Entity(tableName = "fleet_overrides")
data class FleetOverrideEntity(
    @PrimaryKey val vehicleId: String,
    val isLocked: Boolean,
    val isHeadlightOn: Boolean,
    val batteryPercent: Int,
    val isMaintenance: Boolean,
    val latitude: Double,
    val longitude: Double
)
