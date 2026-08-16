package com.example.model

enum class VehicleStatus {
    AVAILABLE,
    IN_RIDE,
    RESERVED,
    LOW_BATTERY,
    MAINTENANCE,
    IMPOUNDED
}

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val addressName: String = "Tashkent City Center"
)

data class Vehicle(
    val id: String,                         // e.g. "SCOOT-7128"
    val qrCode: String,                     // e.g. "https://scoot.uz/r/7128"
    val modelName: String = "Scoot Pro X9",
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val batteryPercent: Int,
    val estimatedRangeKm: Double,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val isLocked: Boolean = true,
    val isHeadlightOn: Boolean = false,
    val speedLimiterKmh: Int = 25,
    val unlockFeeUzs: Long = 1000L,
    val minuteFeeUzs: Long = 800L,
    val pauseFeeUzs: Long = 300L,
    val iotSignalDbm: Int = -74,             // 4G LTE signal
    val odometerKm: Double = 840.2,
    val firmwareVersion: String = "v2.5.4-TK",
    val lastPingTimestamp: Long = System.currentTimeMillis()
)
