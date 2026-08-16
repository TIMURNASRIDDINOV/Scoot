package com.example.gateway

import com.example.model.IotTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Phase 2 IoT Controller Seam:
 * Handles binary and JSON MQTT payloads to and from physical scooter IoT head-units
 * (e.g. Quectel EC25 LTE / Teltonika TFT100 / Segway IoT v3).
 *
 * This stubbed implementation marks the exact contract and broker hooks where
 * MQTT connection pooling, TLS client certificates, and binary telemetry decoders
 * will land in production Phase 2.
 */
interface IotGateway {
    val brokerHost: String
    val brokerPort: Int
    val isTlsEnabled: Boolean
    val livePacketStream: Flow<IotMqttPacket>

    suspend fun publishCommand(vehicleId: String, command: IotCommand): IotCommandAck
    suspend fun subscribeTelemetry(vehicleId: String): Flow<IotTelemetry>
}

data class IotMqttPacket(
    val id: String,
    val topic: String,
    val payloadJson: String,
    val qos: Int = 1,
    val timestampMillis: Long = System.currentTimeMillis(),
    val direction: PacketDirection = PacketDirection.INBOUND
)

enum class PacketDirection {
    INBOUND,
    OUTBOUND
}

sealed class IotCommand(val name: String) {
    data class Unlock(val speedLimitKmh: Int = 25) : IotCommand("CMD_UNLOCK_SOLENOID")
    object Lock : IotCommand("CMD_ENGAGE_ELECTRONIC_LOCK")
    data class RingBuzzer(val durationSeconds: Int = 3, val frequencyHz: Int = 2400) : IotCommand("CMD_BUZZER_LOCATE")
    data class SetHeadlight(val enabled: Boolean) : IotCommand("CMD_SET_HEADLIGHT")
    data class UpdateSpeedLimit(val limitKmh: Int) : IotCommand("CMD_SET_SPEED_LIMIT")
    data class RequestHeartbeat(val includeGpsHdop: Boolean = true) : IotCommand("CMD_REQ_HEARTBEAT")
}

data class IotCommandAck(
    val commandName: String,
    val vehicleId: String,
    val success: Boolean,
    val latencyMs: Long,
    val rawResponseHex: String
)

/**
 * Stubbed implementation providing live simulated packet logs for the Back-Office demo
 * and testing harness.
 */
class StubbedIotGateway : IotGateway {
    override val brokerHost: String = "mqtt.iot.scoot.uz"
    override val brokerPort: Int = 8883
    override val isTlsEnabled: Boolean = true

    private val _packetStream = MutableSharedFlow<IotMqttPacket>(replay = 20)
    override val livePacketStream: Flow<IotMqttPacket> = _packetStream.asSharedFlow()

    suspend fun recordPacket(topic: String, payload: String, direction: PacketDirection = PacketDirection.INBOUND) {
        val packet = IotMqttPacket(
            id = "pkt-${System.currentTimeMillis()}-${(100..999).random()}",
            topic = topic,
            payloadJson = payload,
            qos = 1,
            direction = direction
        )
        _packetStream.emit(packet)
    }

    override suspend fun publishCommand(vehicleId: String, command: IotCommand): IotCommandAck {
        val topic = "scoot/tashkent/v1/vehicles/$vehicleId/command"
        val payload = """{"cmd":"${command.name}","ts":${System.currentTimeMillis()}}"""
        recordPacket(topic, payload, PacketDirection.OUTBOUND)

        // Phase 2 real MQTT ack simulation
        return IotCommandAck(
            commandName = command.name,
            vehicleId = vehicleId,
            success = true,
            latencyMs = (35L..80L).random(),
            rawResponseHex = "0xAA 0x05 0x${command.name.hashCode().toString(16).take(4)} 0xFF"
        )
    }

    override suspend fun subscribeTelemetry(vehicleId: String): Flow<IotTelemetry> {
        val topic = "scoot/tashkent/v1/vehicles/$vehicleId/telemetry"
        recordPacket(topic, """{"sub":"subscribed","imei":"863920194821039"}""", PacketDirection.OUTBOUND)
        return kotlinx.coroutines.flow.emptyFlow()
    }
}
