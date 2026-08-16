package com.example.ui.rider

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.ui.AppMode
import com.example.ui.ScootViewModel
import com.example.ui.map.TashkentMapCanvas
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.ScootCyan
import com.example.ui.theme.ScootGreen
import com.example.ui.theme.ScootYellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RiderScreen(
    viewModel: ScootViewModel,
    modifier: Modifier = Modifier
) {
    val fleet by viewModel.fleet.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val activeRide by viewModel.activeRide.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val pastRides by viewModel.pastRides.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isUnlockVisible by viewModel.isUnlockDialogVisible.collectAsState()
    val isEndRideVisible by viewModel.isEndRideDialogVisible.collectAsState()
    val isWalletVisible by viewModel.isWalletDialogVisible.collectAsState()
    val showZones by viewModel.isZonesLayerVisible.collectAsState()

    val currentZone = viewModel.getCurrentZone(userLocation.first, userLocation.second)

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        // 1. Interactive Tashkent Vector Map
        TashkentMapCanvas(
            fleet = fleet,
            selectedVehicle = selectedVehicle,
            userLocation = userLocation,
            activeRide = activeRide,
            showZones = showZones,
            onScooterClick = { scooter ->
                viewModel.selectVehicle(scooter)
            },
            onMapClick = {
                viewModel.selectVehicle(null)
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Bar Controls: Brand, Wallet Balance, Switch to Back-Office
        RiderTopBar(
            balanceUzs = wallet.balanceUzs,
            showZones = showZones,
            onWalletClick = { viewModel.isWalletDialogVisible.value = true },
            onToggleZones = { viewModel.isZonesLayerVisible.value = !showZones },
            onSwitchToOps = { viewModel.setAppMode(AppMode.FLEET_BACKOFFICE) },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 3. Bottom Panels:
        // A. If Active Ride -> ActiveRideHud
        // B. Else If Selected Scooter -> ScooterDetailCard
        // C. Else -> Default "Scan QR" bottom action bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            if (activeRide != null) {
                ActiveRideHud(
                    ride = activeRide!!,
                    vehicle = fleet.find { it.id == activeRide!!.vehicleId },
                    currentZone = currentZone,
                    onPauseClick = { viewModel.pauseRide() },
                    onResumeClick = { viewModel.resumeRide() },
                    onToggleHeadlight = { viewModel.toggleRideHeadlight() },
                    onRingBell = { viewModel.ringRideBell() },
                    onEndRideClick = { viewModel.isEndRideDialogVisible.value = true }
                )
            } else if (selectedVehicle != null) {
                SelectedScooterCard(
                    vehicle = selectedVehicle!!,
                    onClose = { viewModel.selectVehicle(null) },
                    onRingBell = {
                        viewModel.ringRideBell()
                        viewModel.showNotification("🔔 Locating ${selectedVehicle!!.id}...")
                    },
                    onUnlock = {
                        viewModel.startRide(selectedVehicle!!.id)
                    }
                )
            } else {
                // Default Bottom Bar
                DefaultRiderBottomBar(
                    onScanClick = { viewModel.isUnlockDialogVisible.value = true }
                )
            }
        }

        // Modals
        if (isUnlockVisible) {
            UnlockDialog(
                availableFleet = fleet,
                preSelectedVehicle = selectedVehicle,
                onDismiss = { viewModel.isUnlockDialogVisible.value = false },
                onUnlock = { id -> viewModel.startRide(id) }
            )
        }

        if (isEndRideVisible && activeRide != null) {
            EndRideDialog(
                ride = activeRide!!,
                onDismiss = { viewModel.isEndRideDialogVisible.value = false },
                onConfirmEndRide = { photoUri, rating ->
                    viewModel.endRide(photoUri, rating)
                }
            )
        }

        if (isWalletVisible) {
            WalletDialog(
                wallet = wallet,
                transactions = transactions,
                onDismiss = { viewModel.isWalletDialogVisible.value = false },
                onTopUp = { amount, method -> viewModel.topUpWallet(amount, method) },
                onRedeemPromo = { code -> viewModel.redeemPromo(code) }
            )
        }
    }
}

@Composable
private fun RiderTopBar(
    balanceUzs: Long,
    showZones: Boolean,
    onWalletClick: () -> Unit,
    onToggleZones: () -> Unit,
    onSwitchToOps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bold App Brand Logo Pill
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, DarkSurfaceBorder),
            shadowElevation = 4.dp,
            modifier = Modifier.padding(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Surface(
                    color = ScootGreen,
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricScooter,
                        contentDescription = "Scoot",
                        tint = Color(0xFF1A1C18),
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SCOOT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1C18),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    color = Color(0xFFD7E8CD),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    Text(
                        text = "TAS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Actions: Zones toggle, Wallet Balance Badge, Switch to Back-Office
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zones Layer Toggle
            Surface(
                color = if (showZones) ScootYellow else Color.White,
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = CircleShape,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .clickable { onToggleZones() }
                    .testTag("toggle_zones_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Zones",
                    tint = Color(0xFF1A1C18),
                    modifier = Modifier.padding(10.dp).size(20.dp)
                )
            }

            // Wallet Balance Badge
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .clickable { onWalletClick() }
                    .testTag("rider_wallet_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color(0xFF1A1C18),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${String.format("%,d", balanceUzs).replace(',', ' ')} UZS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                }
            }

            // Switch to Back-Office Fleet Panel
            Surface(
                color = Color(0xFFD7E8CD),
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .clickable { onSwitchToOps() }
                    .testTag("switch_to_ops_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Color(0xFF1A1C18),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OPS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultRiderBottomBar(
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DarkSurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "READY TO RIDE?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1C18),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "1 000 UZS unlock • 800 UZS / min",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF424940)
                )
            }

            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScootGreen,
                    contentColor = Color(0xFF1A1C18)
                ),
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(52.dp).testTag("main_scan_qr_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color(0xFF1A1C18),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SCAN QR",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun SelectedScooterCard(
    vehicle: Vehicle,
    onClose: () -> Unit,
    onRingBell: () -> Unit,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .testTag("selected_scooter_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header: ID, Model, Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = ScootGreen,
                            shape = CircleShape,
                            border = BorderStroke(2.dp, DarkSurfaceBorder),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricScooter,
                                contentDescription = null,
                                tint = Color(0xFF1A1C18),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = vehicle.id,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1A1C18)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFD7E8CD),
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = vehicle.status.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1A1C18),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = vehicle.modelName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF424940),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFF7F9F2),
                        border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        IconButton(onClick = onClose, modifier = Modifier.testTag("close_selected_scooter_btn")) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1A1C18))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location in Tashkent
                Surface(
                    color = Color(0xFFF7F9F2),
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF1A1C18),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = vehicle.locationName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C18)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Specs Badges: Battery %, Range, Pricing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Battery
                    Surface(
                        color = Color(0xFFF7F9F2),
                        border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🔋 ${vehicle.batteryPercent}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C18)
                            )
                            Text(
                                text = "BATTERY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF424940)
                            )
                        }
                    }

                    // Range
                    Surface(
                        color = Color(0xFFF7F9F2),
                        border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${vehicle.estimatedRangeKm} KM",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C18)
                            )
                            Text(
                                text = "EST. RANGE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF424940)
                            )
                        }
                    }

                    // Rates
                    Surface(
                        color = Color(0xFFF7F9F2),
                        border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "800 UZS/M",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C18)
                            )
                            Text(
                                text = "+1K UNLOCK",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF424940)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Ring Bell & Unlock & Ride
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRingBell,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF7F9F2),
                            contentColor = Color(0xFF1A1C18)
                        ),
                        border = BorderStroke(2.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(54.dp).testTag("ring_bell_btn")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF1A1C18), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BELL", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }

                    Button(
                        onClick = onUnlock,
                        enabled = vehicle.status == VehicleStatus.AVAILABLE,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScootGreen,
                            contentColor = Color(0xFF1A1C18)
                        ),
                        border = BorderStroke(2.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(54.dp).weight(1f).testTag("unlock_selected_scooter_btn")
                    ) {
                        Text(
                            text = if (vehicle.status == VehicleStatus.AVAILABLE) "UNLOCK & RIDE" else "UNAVAILABLE",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
