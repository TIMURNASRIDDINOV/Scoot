package com.example.ui.fleet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IotTelemetry
import com.example.model.Vehicle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.ScootCyan
import com.example.ui.theme.ScootGreen
import com.example.ui.theme.ScootYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleTelemetryDialog(
    vehicle: Vehicle,
    telemetry: IotTelemetry,
    onDismiss: () -> Unit,
    onSwapBattery: () -> Unit,
    onBuzzer: () -> Unit,
    onToggleLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F9F2),
        scrimColor = Color.Black.copy(alpha = 0.7f),
        modifier = modifier.testTag("vehicle_telemetry_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = ScootGreen,
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = Color(0xFF1A1C18),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "IOT TELEMETRY: ${vehicle.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C18),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Quectel EC25 LTE • ${telemetry.networkOperator}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424940)
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("telemetry_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1A1C18))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controller Health & Battery Diagnostics
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HARDWARE & POWER STATE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TelemetryItem("Battery SOC", "${vehicle.batteryPercent}% (${telemetry.batteryVoltage} V)")
                    TelemetryItem("Estimated Range", "${vehicle.estimatedRangeKm} km")
                    TelemetryItem("Battery Temp", "${String.format("%.1f", telemetry.batteryTempCelsius)} °C")
                    TelemetryItem("Motor Temp", "${String.format("%.1f", telemetry.motorTempCelsius)} °C")
                    TelemetryItem("Lock Solenoid", telemetry.lockSolenoidState)
                    TelemetryItem("Firmware", vehicle.firmwareVersion)
                    TelemetryItem("Odometer Total", "${vehicle.odometerKm} km")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cellular & GPS GNSS Telemetry
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "IOT CONNECTIVITY & GPS GNSS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TelemetryItem("Modem IMEI", telemetry.imei)
                    TelemetryItem("SIM IMSI", telemetry.simImsi)
                    TelemetryItem("Signal RSSI", "${telemetry.cellularRssiDbm} dBm (Good)")
                    TelemetryItem("GPS Coordinates", "${String.format("%.5f", vehicle.latitude)}, ${String.format("%.5f", vehicle.longitude)}")
                    TelemetryItem("GPS HDOP Accuracy", "±${telemetry.gpsHdop}m (14 Satellites)")
                    TelemetryItem("MQTT Status", "CONNECTED (tls://8883)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Remote Actions from Back-Office
            Text(
                text = "REMOTE OPS CONTROL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A1C18)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBuzzer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A1C18)
                    ),
                    border = BorderStroke(2.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("diag_buzzer_btn")
                ) {
                    Text("🔔 BUZZER", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                Button(
                    onClick = onSwapBattery,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD7E8CD),
                        contentColor = Color(0xFF1A1C18)
                    ),
                    border = BorderStroke(2.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1.2f).height(48.dp).testTag("diag_swap_btn")
                ) {
                    Text("⚡ SWAP 100%", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                Button(
                    onClick = onToggleLock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vehicle.isLocked) ScootGreen else ScootYellow,
                        contentColor = Color(0xFF1A1C18)
                    ),
                    border = BorderStroke(2.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1.2f).height(48.dp).testTag("diag_lock_toggle_btn")
                ) {
                    Text(if (vehicle.isLocked) "🔓 UNLOCK" else "🔒 LOCK", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun TelemetryItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF424940))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF1A1C18)
        )
    }
}
