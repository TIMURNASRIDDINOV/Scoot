package com.example.ui.rider

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Ride
import com.example.model.RideStatus
import com.example.model.TashkentZone
import com.example.model.Vehicle
import com.example.model.ZoneType
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.ScootCyan
import com.example.ui.theme.ScootGreen
import com.example.ui.theme.ScootRed
import com.example.ui.theme.ScootYellow

@Composable
fun ActiveRideHud(
    ride: Ride,
    vehicle: Vehicle?,
    currentZone: TashkentZone?,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onToggleHeadlight: () -> Unit,
    onRingBell: () -> Unit,
    onEndRideClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPaused = ride.status == RideStatus.PAUSED
    val isSlowZone = currentZone?.type == ZoneType.SLOW_SPEED_15KMH
    val isRestrictedZone = currentZone?.type == ZoneType.NO_PARKING || currentZone?.type == ZoneType.RESTRICTED

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, DarkSurfaceBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .testTag("active_ride_hud")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zone Alert Badge if in special area
                if (currentZone != null) {
                    val badgeColor = when (currentZone.type) {
                        ZoneType.SLOW_SPEED_15KMH -> ScootYellow
                        ZoneType.NO_PARKING, ZoneType.RESTRICTED -> ScootRed
                        else -> ScootGreen
                    }
                    val badgeText = when (currentZone.type) {
                        ZoneType.SLOW_SPEED_15KMH -> "⚡ 15 KM/H SLOW SPEED: ${currentZone.name.uppercase()}"
                        ZoneType.NO_PARKING -> "⚠️ NO-PARKING ZONE: ${currentZone.name.uppercase()}"
                        ZoneType.RESTRICTED -> "🛑 RESTRICTED PERIMETER: ${currentZone.name.uppercase()}"
                        else -> "🟢 RIDING ZONE: ${currentZone.name.uppercase()}"
                    }

                    Surface(
                        color = Color(0xFFF7F9F2),
                        border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isRestrictedZone) Icons.Default.Warning else Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color(0xFF1A1C18),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1A1C18),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Top Status Header: Vehicle ID & Battery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(if (isPaused) ScootYellow else ScootGreen, CircleShape)
                                .border(1.5.dp, DarkSurfaceBorder, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPaused) "RIDE PAUSED" else "ACTIVE RIDE",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C18),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFD7E8CD),
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = ride.vehicleId,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C18),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFF7F9F2),
                        border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔋 ${vehicle?.batteryPercent ?: 85}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C18)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Telemetry Row: Speed Gauge, Duration Timer, Cost
                Surface(
                    color = Color(0xFFF7F9F2),
                    border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speedometer Display
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isPaused) "0" else String.format("%.0f", ride.currentSpeedKmh),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF1A1C18)
                            )
                            Text(
                                text = "KM / H",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF424940)
                            )
                        }

                        // Divider
                        Box(modifier = Modifier.width(2.dp).height(44.dp).background(DarkSurfaceBorder))

                        // Duration
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val mins = ride.durationSeconds / 60
                            val secs = ride.durationSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C18)
                            )
                            Text(
                                text = "DURATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF424940)
                            )
                        }

                        // Divider
                        Box(modifier = Modifier.width(2.dp).height(44.dp).background(DarkSurfaceBorder))

                        // Live Cost in Uzbek Som
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%,d", ride.totalCostUzs).replace(',', ' '),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C18)
                            )
                            Text(
                                text = "UZS COST",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF424940)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Controls Row: Headlight, Buzzer Bell, Pause/Resume, End Ride
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Headlight Toggle
                    Button(
                        onClick = onToggleHeadlight,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (vehicle?.isHeadlightOn == true) ScootGreen else Color(0xFFF7F9F2),
                            contentColor = Color(0xFF1A1C18)
                        ),
                        border = BorderStroke(2.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(52.dp).testTag("ride_headlight_btn")
                    ) {
                        Icon(
                            imageVector = if (vehicle?.isHeadlightOn == true) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Headlight",
                            tint = Color(0xFF1A1C18),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Buzzer Bell
                    Button(
                        onClick = onRingBell,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF7F9F2),
                            contentColor = Color(0xFF1A1C18)
                        ),
                        border = BorderStroke(2.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(52.dp).testTag("ride_bell_btn")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Ring Bell", tint = Color(0xFF1A1C18), modifier = Modifier.size(20.dp))
                    }

                    // Pause / Resume Button
                    Button(
                        onClick = { if (isPaused) onResumeClick() else onPauseClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) ScootGreen else ScootYellow,
                            contentColor = Color(0xFF1A1C18)
                        ),
                        border = BorderStroke(2.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(52.dp).weight(1f).testTag("ride_pause_resume_btn")
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = Color(0xFF1A1C18),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPaused) "RESUME" else "PAUSE",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    // End Ride Button
                    Button(
                        onClick = onEndRideClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScootRed,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(52.dp).weight(1.2f).testTag("ride_end_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "END RIDE",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
