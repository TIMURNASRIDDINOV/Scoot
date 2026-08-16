package com.example.gateway

import com.example.model.IotTelemetry
import com.example.model.Ride
import com.example.model.Vehicle
import kotlinx.coroutines.flow.Flow

/**
 * Core domain seam isolating the fleet management & vehicle control layer.
 * In Phase 1 (current), this is backed by [SimulatedVehicleGateway].
 * In Phase 2, this delegates to [IotGateway] via MQTT / Cellular telemetry.
 */
interface VehicleGateway {
    /**
     * Observe live fleet telemetry and statuses across Tashkent.
     */
    fun observeFleet(): Flow<List<Vehicle>>

    /**
     * Retrieve single vehicle by scooter ID or QR code.
     */
    fun getVehicle(id: String): Vehicle?

    /**
     * Send remote command to unlock the electronic wheel solenoid & power on display.
     */
    suspend fun unlockVehicle(id: String): Result<Boolean>

    /**
     * Send remote command to lock the solenoid, engage anti-theft brake & power off motor.
     */
    suspend fun lockVehicle(id: String): Result<Boolean>

    /**
     * Send command to pulse the 85dB piezoelectric acoustic buzzer for vehicle localization.
     */
    suspend fun sendBuzzer(id: String): Result<Boolean>

    /**
     * Toggle high-output LED front beam & rear taillight.
     */
    suspend fun toggleHeadlight(id: String, on: Boolean): Result<Boolean>

    /**
     * Simulate hot-swap of the 36V 15.6Ah lithium-ion battery pack (fleet maintenance).
     */
    suspend fun swapBattery(id: String): Result<Boolean>

    /**
     * Set operational maintenance flag (marks vehicle unavailable for riders).
     */
    suspend fun setMaintenance(id: String, inMaintenance: Boolean): Result<Boolean>

    /**
     * Relocate / teleport scooter (e.g. back-office fleet rebalancing to hotspot).
     */
    suspend fun relocateVehicle(id: String, lat: Double, lng: Double, locationName: String): Result<Boolean>

    /**
     * Retrieve deep hardware telemetry packet (IMEI, voltage, temperature, GPS HDOP).
     */
    fun getIotTelemetry(vehicleId: String): IotTelemetry
}
