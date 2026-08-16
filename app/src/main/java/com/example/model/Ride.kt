package com.example.model

enum class RideStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED
}

data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class Ride(
    val id: String,
    val vehicleId: String,
    val vehicleModel: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val startLocationName: String,
    val endLocationName: String? = null,
    val durationSeconds: Long = 0L,
    val pausedSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeedKmh: Double = 0.0,
    val unlockCostUzs: Long = 1000L,
    val rideCostUzs: Long = 0L,
    val pauseCostUzs: Long = 0L,
    val totalCostUzs: Long = 1000L,
    val status: RideStatus = RideStatus.ACTIVE,
    val parkingPhotoUri: String? = null,
    val paymentMethod: String = "Payme",
    val paymentTransactionId: String? = null,
    val rating: Int? = null,
    val routePoints: List<RoutePoint> = emptyList()
)
