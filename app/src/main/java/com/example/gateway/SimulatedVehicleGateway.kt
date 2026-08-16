package com.example.gateway

import com.example.model.IotTelemetry
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlin.random.Random

class SimulatedVehicleGateway(
    private val iotGateway: StubbedIotGateway,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : VehicleGateway {

    private val initialFleet = listOf(
        Vehicle(
            id = "SCOOT-7128",
            qrCode = "https://scoot.uz/r/7128",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3114,
            longitude = 69.2792,
            locationName = "Amir Timur Square (East Bay)",
            batteryPercent = 94,
            estimatedRangeKm = 38.5,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 524.3,
            iotSignalDbm = -70
        ),
        Vehicle(
            id = "SCOOT-4092",
            qrCode = "https://scoot.uz/r/4092",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3106,
            longitude = 69.2810,
            locationName = "Hotel Uzbekistan Front plaza",
            batteryPercent = 88,
            estimatedRangeKm = 35.0,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 641.8,
            iotSignalDbm = -74
        ),
        Vehicle(
            id = "SCOOT-5531",
            qrCode = "https://scoot.uz/r/5531",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3138,
            longitude = 69.2748,
            locationName = "Sayilgoh (Broadway) North Arch",
            batteryPercent = 76,
            estimatedRangeKm = 30.2,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 890.1,
            iotSignalDbm = -68
        ),
        Vehicle(
            id = "SCOOT-8219",
            qrCode = "https://scoot.uz/r/8219",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3129,
            longitude = 69.2730,
            locationName = "Sayilgoh Street Café Hub",
            batteryPercent = 65,
            estimatedRangeKm = 26.0,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 1120.4,
            iotSignalDbm = -72
        ),
        Vehicle(
            id = "SCOOT-1940",
            qrCode = "https://scoot.uz/r/1940",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3092,
            longitude = 69.2725,
            locationName = "Alisher Navoi Theatre Fountains",
            batteryPercent = 91,
            estimatedRangeKm = 37.0,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 412.0,
            iotSignalDbm = -75
        ),
        Vehicle(
            id = "SCOOT-6302",
            qrCode = "https://scoot.uz/r/6302",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3131,
            longitude = 69.2542,
            locationName = "Tashkent City Park (Hilton Gate)",
            batteryPercent = 82,
            estimatedRangeKm = 33.0,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 730.5,
            iotSignalDbm = -66
        ),
        Vehicle(
            id = "SCOOT-3844",
            qrCode = "https://scoot.uz/r/3844",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3119,
            longitude = 69.2520,
            locationName = "Tashkent City Musical Fountain",
            batteryPercent = 14,
            estimatedRangeKm = 5.6,
            status = VehicleStatus.LOW_BATTERY,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 1450.2,
            iotSignalDbm = -81
        ),
        Vehicle(
            id = "SCOOT-9104",
            qrCode = "https://scoot.uz/r/9104",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3278,
            longitude = 69.2365,
            locationName = "Chorsu Bazaar Metro Station",
            batteryPercent = 79,
            estimatedRangeKm = 31.5,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 980.6,
            iotSignalDbm = -78
        ),
        Vehicle(
            id = "SCOOT-2275",
            qrCode = "https://scoot.uz/r/2275",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3262,
            longitude = 69.2348,
            locationName = "Kukeldash Madrasah Courtyard",
            batteryPercent = 60,
            estimatedRangeKm = 24.0,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 1205.1,
            iotSignalDbm = -76
        ),
        Vehicle(
            id = "SCOOT-7489",
            qrCode = "https://scoot.uz/r/7489",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3035,
            longitude = 69.2472,
            locationName = "Magic City Main Boulevard",
            batteryPercent = 85,
            estimatedRangeKm = 34.0,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 670.3,
            iotSignalDbm = -69
        ),
        Vehicle(
            id = "SCOOT-1120",
            qrCode = "https://scoot.uz/r/1120",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3075,
            longitude = 69.2840,
            locationName = "Westminster University (WIUT)",
            batteryPercent = 95,
            estimatedRangeKm = 39.0,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 310.8,
            iotSignalDbm = -71
        ),
        Vehicle(
            id = "SCOOT-8833",
            qrCode = "https://scoot.uz/r/8833",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3175,
            longitude = 69.2820,
            locationName = "Oloy (Alay) Bazaar Entrance",
            batteryPercent = 54,
            estimatedRangeKm = 21.6,
            status = VehicleStatus.AVAILABLE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 1540.9,
            iotSignalDbm = -79
        ),
        Vehicle(
            id = "SCOOT-3391",
            qrCode = "https://scoot.uz/r/3391",
            modelName = "Scoot Pro X9 (350W)",
            latitude = 41.3005,
            longitude = 69.2660,
            locationName = "Kosmonavtlar Metro / Intercontinental",
            batteryPercent = 0,
            estimatedRangeKm = 0.0,
            status = VehicleStatus.MAINTENANCE,
            isLocked = true,
            isHeadlightOn = false,
            odometerKm = 2100.4,
            iotSignalDbm = -88
        )
    )

    private val _fleet = MutableStateFlow<List<Vehicle>>(initialFleet)

    init {
        // Start background simulated telemetry beacon
        scope.launch {
            while (true) {
                delay(6000)
                _fleet.update { list ->
                    list.map { v ->
                        v.copy(
                            lastPingTimestamp = System.currentTimeMillis()
                        )
                    }
                }
            }
        }
    }

    override fun observeFleet(): Flow<List<Vehicle>> = _fleet.asStateFlow()

    override fun getVehicle(id: String): Vehicle? {
        val cleanId = id.trim().uppercase()
        return _fleet.value.find {
            it.id.equals(cleanId, ignoreCase = true) ||
            it.id.endsWith(cleanId, ignoreCase = true) ||
            it.qrCode.contains(cleanId, ignoreCase = true)
        }
    }

    override suspend fun unlockVehicle(id: String): Result<Boolean> {
        val target = getVehicle(id) ?: return Result.failure(NoSuchElementException("Scooter $id not found"))
        iotGateway.publishCommand(target.id, IotCommand.Unlock(target.speedLimiterKmh))

        _fleet.update { list ->
            list.map {
                if (it.id == target.id) it.copy(
                    status = VehicleStatus.IN_RIDE,
                    isLocked = false,
                    isHeadlightOn = true
                ) else it
            }
        }
        return Result.success(true)
    }

    override suspend fun lockVehicle(id: String): Result<Boolean> {
        val target = getVehicle(id) ?: return Result.failure(NoSuchElementException("Scooter $id not found"))
        iotGateway.publishCommand(target.id, IotCommand.Lock)

        _fleet.update { list ->
            list.map {
                if (it.id == target.id) it.copy(
                    status = if (it.batteryPercent < 20) VehicleStatus.LOW_BATTERY else VehicleStatus.AVAILABLE,
                    isLocked = true,
                    isHeadlightOn = false
                ) else it
            }
        }
        return Result.success(true)
    }

    override suspend fun sendBuzzer(id: String): Result<Boolean> {
        val target = getVehicle(id) ?: return Result.failure(NoSuchElementException("Scooter $id not found"))
        iotGateway.publishCommand(target.id, IotCommand.RingBuzzer())
        return Result.success(true)
    }

    override suspend fun toggleHeadlight(id: String, on: Boolean): Result<Boolean> {
        val target = getVehicle(id) ?: return Result.failure(NoSuchElementException("Scooter $id not found"))
        iotGateway.publishCommand(target.id, IotCommand.SetHeadlight(on))
        _fleet.update { list ->
            list.map {
                if (it.id == target.id) it.copy(isHeadlightOn = on) else it
            }
        }
        return Result.success(true)
    }

    override suspend fun swapBattery(id: String): Result<Boolean> {
        val target = getVehicle(id) ?: return Result.failure(NoSuchElementException("Scooter $id not found"))
        _fleet.update { list ->
            list.map {
                if (it.id == target.id) it.copy(
                    batteryPercent = 100,
                    estimatedRangeKm = 40.0,
                    status = if (it.status == VehicleStatus.LOW_BATTERY) VehicleStatus.AVAILABLE else it.status
                ) else it
            }
        }
        iotGateway.recordPacket("scoot/tashkent/v1/telemetry", """{"action":"BATTERY_SWAP","vehicleId":"${target.id}","soc":100}""")
        return Result.success(true)
    }

    override suspend fun setMaintenance(id: String, inMaintenance: Boolean): Result<Boolean> {
        val target = getVehicle(id) ?: return Result.failure(NoSuchElementException("Scooter $id not found"))
        _fleet.update { list ->
            list.map {
                if (it.id == target.id) it.copy(
                    status = if (inMaintenance) VehicleStatus.MAINTENANCE else VehicleStatus.AVAILABLE,
                    isLocked = true
                ) else it
            }
        }
        return Result.success(true)
    }

    override suspend fun relocateVehicle(id: String, lat: Double, lng: Double, locationName: String): Result<Boolean> {
        val target = getVehicle(id) ?: return Result.failure(NoSuchElementException("Scooter $id not found"))
        _fleet.update { list ->
            list.map {
                if (it.id == target.id) it.copy(
                    latitude = lat,
                    longitude = lng,
                    locationName = locationName
                ) else it
            }
        }
        iotGateway.recordPacket("scoot/tashkent/v1/telemetry", """{"action":"RELOCATE","lat":$lat,"lng":$lng}""")
        return Result.success(true)
    }

    fun updateSimulatedPosition(id: String, lat: Double, lng: Double, deltaDistanceKm: Double) {
        _fleet.update { list ->
            list.map {
                if (it.id == id) {
                    val newBattery = (it.batteryPercent - 1).coerceAtLeast(5)
                    val newRange = (it.estimatedRangeKm - (deltaDistanceKm * 1.1)).coerceAtLeast(1.0)
                    it.copy(
                        latitude = lat,
                        longitude = lng,
                        batteryPercent = newBattery,
                        estimatedRangeKm = String.format("%.1f", newRange).toDoubleOrNull() ?: newRange,
                        odometerKm = String.format("%.1f", it.odometerKm + deltaDistanceKm).toDoubleOrNull() ?: it.odometerKm
                    )
                } else it
            }
        }
    }

    override fun getIotTelemetry(vehicleId: String): IotTelemetry {
        val v = getVehicle(vehicleId)
        val num = vehicleId.filter { it.isDigit() }.take(4).ifEmpty { "7128" }
        return IotTelemetry(
            vehicleId = vehicleId,
            imei = "86392004918$num",
            simImsi = "43405019382$num",
            batteryVoltage = if (v != null) 32.0 + (v.batteryPercent * 0.1) else 36.4,
            batteryTempCelsius = Random.nextDouble(26.0, 31.5),
            motorTempCelsius = if (v?.status == VehicleStatus.IN_RIDE) 42.0 else 28.0,
            lockSolenoidState = if (v?.isLocked == false) "DISENGAGED (UNLOCKED)" else "ENGAGED (LOCKED)",
            cellularRssiDbm = v?.iotSignalDbm ?: -74,
            networkOperator = "Ucell 4G LTE (Tashkent Cell)",
            speedLimiterActive = true,
            mqttBrokerConnected = true,
            brokerEndpoint = "tls://mqtt.iot.scoot.uz:8883"
        )
    }
}
