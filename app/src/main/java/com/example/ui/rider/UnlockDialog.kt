package com.example.ui.rider

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.ScootCyan
import com.example.ui.theme.ScootGreen
import com.example.ui.theme.ScootYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockDialog(
    availableFleet: List<Vehicle>,
    preSelectedVehicle: Vehicle?,
    onDismiss: () -> Unit,
    onUnlock: (vehicleId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var codeInput by remember { mutableStateOf(preSelectedVehicle?.id ?: "") }
    var isFlashlightOn by remember { mutableStateOf(false) }

    // Laser scan line animation
    val infiniteTransition = rememberInfiniteTransition(label = "ScannerLaser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserY"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F9F2),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = null,
        modifier = modifier.testTag("unlock_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color(0xFF1A1C18),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SCAN TO UNLOCK",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18),
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("unlock_close_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1A1C18))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulated Camera QR Viewfinder
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(BorderStroke(2.dp, DarkSurfaceBorder), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Viewfinder Corner Reticle & Laser Canvas
                Canvas(modifier = Modifier.size(190.dp)) {
                    val w = size.width
                    val h = size.height
                    val cornerLen = 28f
                    val stroke = 6f
                    val cornerColor = Color(0xFF1A1C18)

                    // Top Left Corner
                    drawLine(cornerColor, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth = stroke, cap = StrokeCap.Square)
                    drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cornerLen), strokeWidth = stroke, cap = StrokeCap.Square)

                    // Top Right Corner
                    drawLine(cornerColor, Offset(w, 0f), Offset(w - cornerLen, 0f), strokeWidth = stroke, cap = StrokeCap.Square)
                    drawLine(cornerColor, Offset(w, 0f), Offset(w, cornerLen), strokeWidth = stroke, cap = StrokeCap.Square)

                    // Bottom Left Corner
                    drawLine(cornerColor, Offset(0f, h), Offset(cornerLen, h), strokeWidth = stroke, cap = StrokeCap.Square)
                    drawLine(cornerColor, Offset(0f, h), Offset(0f, h - cornerLen), strokeWidth = stroke, cap = StrokeCap.Square)

                    // Bottom Right Corner
                    drawLine(cornerColor, Offset(w, h), Offset(w - cornerLen, h), strokeWidth = stroke, cap = StrokeCap.Square)
                    drawLine(cornerColor, Offset(w, h), Offset(w, h - cornerLen), strokeWidth = stroke, cap = StrokeCap.Square)

                    // Laser Scanning Bar
                    val laserY = h * laserPosition
                    drawLine(
                        color = Color(0xFF1A1C18),
                        start = Offset(10f, laserY),
                        end = Offset(w - 10f, laserY),
                        strokeWidth = 4f,
                        cap = StrokeCap.Square
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricScooter,
                        contentDescription = null,
                        tint = Color(0xFF1A1C18).copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "POINT CAMERA AT HANDLEBAR QR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18),
                        textAlign = TextAlign.Center
                    )
                }

                // Flashlight Toggle Button
                Surface(
                    color = if (isFlashlightOn) ScootYellow else Color(0xFFF7F9F2),
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clickable { isFlashlightOn = !isFlashlightOn }
                ) {
                    Icon(
                        imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight",
                        tint = Color(0xFF1A1C18),
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Pick Nearby Scooters in Tashkent
            Text(
                text = "OR CHOOSE A NEARBY SCOOTER:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF424940),
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 6.dp)
            )

            val available = availableFleet.filter { it.status == VehicleStatus.AVAILABLE }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(available.take(5)) { vehicle ->
                    val isSelected = codeInput.equals(vehicle.id, ignoreCase = true)
                    Surface(
                        color = if (isSelected) ScootGreen else Color.White,
                        border = BorderStroke(2.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { codeInput = vehicle.id }
                            .testTag("quick_scooter_${vehicle.id}")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = vehicle.id,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C18)
                            )
                            Text(
                                text = "🔋 ${vehicle.batteryPercent}% • ${vehicle.locationName.take(14)}...",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF424940)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Direct ID code text field
            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it.uppercase() },
                label = { Text("SCOOTER ID / CODE", fontWeight = FontWeight.Bold) },
                placeholder = { Text("e.g. SCOOT-7128 or 7128") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (codeInput.isNotBlank()) onUnlock(codeInput)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1A1C18),
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedLabelColor = Color(0xFF1A1C18),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF1A1C18),
                    unfocusedTextColor = Color(0xFF1A1C18)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("scooter_id_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Unlock & NFC Quick Tap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Simulated NFC Tap button
                Button(
                    onClick = {
                        val firstAvail = availableFleet.firstOrNull { it.status == VehicleStatus.AVAILABLE }
                        if (firstAvail != null) {
                            codeInput = firstAvail.id
                            onUnlock(firstAvail.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A1C18)
                    ),
                    border = BorderStroke(2.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(54.dp).testTag("nfc_tap_btn")
                ) {
                    Icon(Icons.Default.Nfc, contentDescription = null, tint = Color(0xFF1A1C18), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NFC TAP", fontWeight = FontWeight.Black)
                }

                // Unlock & Ride Main Button
                Button(
                    onClick = {
                        if (codeInput.isNotBlank()) {
                            onUnlock(codeInput)
                        }
                    },
                    enabled = codeInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScootGreen,
                        contentColor = Color(0xFF1A1C18)
                    ),
                    border = BorderStroke(2.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(54.dp).weight(1f).testTag("confirm_unlock_btn")
                ) {
                    Text(
                        text = "UNLOCK & RIDE",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
