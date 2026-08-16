package com.example.model

data class IotTelemetry(
    val vehicleId: String,
    val imei: String,
    val simImsi: String,
    val batteryVoltage: Double = 36.4,
    val batteryTempCelsius: Double = 28.5,
    val motorTempCelsius: Double = 34.0,
    val controllerStatus: String = "NORMAL",
    val gpsHdop: Double = 0.9,
    val satellitesLocked: Int = 14,
    val cellularRssiDbm: Int = -72,
    val networkOperator: String = "Ucell 4G LTE",
    val lockSolenoidState: String = "LOCKED",
    val buzzerActive: Boolean = false,
    val speedLimiterActive: Boolean = true,
    val mqttBrokerConnected: Boolean = true,
    val brokerEndpoint: String = "tls://mqtt.iot.scoot.uz:8883",
    val lastMqttTopic: String = "scoot/tashkent/v1/telemetry"
)
