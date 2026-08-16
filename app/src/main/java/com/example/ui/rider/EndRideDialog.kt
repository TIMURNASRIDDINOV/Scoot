package com.example.ui.rider

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Ride
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.ScootCyan
import com.example.ui.theme.ScootGreen
import com.example.ui.theme.ScootYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndRideDialog(
    ride: Ride,
    onDismiss: () -> Unit,
    onConfirmEndRide: (photoUri: String?, rating: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isPhotoTaken by remember { mutableStateOf(false) }
    var checkKickstand by remember { mutableStateOf(true) }
    var checkSidewalk by remember { mutableStateOf(true) }
    var rating by remember { mutableIntStateOf(5) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F9F2),
        scrimColor = Color.Black.copy(alpha = 0.7f),
        modifier = modifier.testTag("end_ride_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "END RIDE & VERIFY",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1C18),
                    letterSpacing = 0.5.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("end_ride_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1A1C18))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parking Photo Snapshot Simulation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(if (isPhotoTaken) Color(0xFFD7E8CD) else Color.White, RoundedCornerShape(18.dp))
                    .border(
                        BorderStroke(
                            2.dp,
                            DarkSurfaceBorder
                        ),
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { isPhotoTaken = !isPhotoTaken }
                    .testTag("parking_photo_snap_box"),
                contentAlignment = Alignment.Center
            ) {
                if (isPhotoTaken) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF1A1C18),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PARKING PHOTO VERIFIED",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C18)
                        )
                        Text(
                            text = "Scooter upright, sidewalk clear",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424940)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = ScootGreen,
                            shape = CircleShape,
                            border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = Color(0xFF1A1C18),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TAP TO TAKE PARKING PHOTO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C18)
                        )
                        Text(
                            text = "Ensure kickstand is down and scooter is parked safely",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424940)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parking Rules Checklist
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checkKickstand,
                            onCheckedChange = { checkKickstand = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ScootGreen,
                                checkmarkColor = Color(0xFF1A1C18)
                            )
                        )
                        Text(
                            text = "Kickstand engaged, scooter standing upright",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C18)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checkSidewalk,
                            onCheckedChange = { checkSidewalk = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ScootGreen,
                                checkmarkColor = Color(0xFF1A1C18)
                            )
                        )
                        Text(
                            text = "Clear of pedestrian pathways & metro exits",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C18)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Itemized Trip Summary Receipt
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TRIP RECEIPT BREAKDOWN",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ReceiptRow("Vehicle", ride.vehicleId)
                    ReceiptRow("Duration", "${ride.durationSeconds / 60}m ${ride.durationSeconds % 60}s")
                    ReceiptRow("Distance", String.format("%.2f km", ride.distanceMeters / 1000.0))
                    ReceiptRow("Unlock Fee", "1 000 UZS")
                    ReceiptRow("Ride Time (${ride.durationSeconds / 60} min)", "${String.format("%,d", ride.rideCostUzs).replace(',', ' ')} UZS")
                    if (ride.pauseCostUzs > 0) {
                        ReceiptRow("Pause Fee", "${String.format("%,d", ride.pauseCostUzs).replace(',', ' ')} UZS")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.5.dp).background(DarkSurfaceBorder))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL CHARGED",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C18)
                        )
                        Text(
                            text = "${String.format("%,d", ride.totalCostUzs).replace(',', ' ')} UZS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C18)
                        )
                    }

                    Text(
                        text = "Payment: Payme • Instant auto-settlement",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424940),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating Stars
            Text(
                text = "RATE YOUR TASHKENT RIDE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A1C18)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star $i",
                        tint = if (i <= rating) ScootYellow else Color(0xFF94A3B8),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { rating = i }
                            .testTag("rate_star_$i")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lock & Complete Button
            Button(
                onClick = {
                    onConfirmEndRide(
                        if (isPhotoTaken) "parking_verified_scoot.jpg" else null,
                        rating
                    )
                },
                enabled = checkKickstand && checkSidewalk,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScootGreen,
                    contentColor = Color(0xFF1A1C18)
                ),
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_end_ride_final_btn")
            ) {
                Text(
                    text = "LOCK VEHICLE & SETTLE PAYMENT",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF424940))
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = Color(0xFF1A1C18))
    }
}
