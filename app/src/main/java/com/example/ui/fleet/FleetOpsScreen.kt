package com.example.ui.fleet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.ui.AppMode
import com.example.ui.ScootViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.ScootCyan
import com.example.ui.theme.ScootGreen
import com.example.ui.theme.ScootRed
import com.example.ui.theme.ScootYellow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FleetOpsScreen(
    viewModel: ScootViewModel,
    modifier: Modifier = Modifier
) {
    val fleet by viewModel.fleet.collectAsState()
    val filteredFleet by viewModel.filteredFleet.collectAsState()
    val currentFilter by viewModel.fleetFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val mqttPackets by viewModel.mqttPackets.collectAsState()
    val isTelemetryOpen by viewModel.isTelemetryDialogVisible.collectAsState()
    val targetVehicleId by viewModel.telemetryTargetVehicleId.collectAsState()

    var showMqttInspector by remember { mutableStateOf(false) }

    val totalVehicles = fleet.size
    val availableCount = fleet.count { it.status == VehicleStatus.AVAILABLE }
    val inRideCount = fleet.count { it.status == VehicleStatus.IN_RIDE }
    val lowBatteryCount = fleet.count { it.status == VehicleStatus.LOW_BATTERY || it.batteryPercent < 20 }
    val maintenanceCount = fleet.count { it.status == VehicleStatus.MAINTENANCE }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F2))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header: Title & Switch to Rider Mode
            FleetOpsHeader(
                onSwitchToRider = { viewModel.setAppMode(AppMode.RIDER) },
                onToggleMqtt = { showMqttInspector = !showMqttInspector },
                isMqttOpen = showMqttInspector
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Operational KPI Grid
            KpiSummaryGrid(
                total = totalVehicles,
                available = availableCount,
                inRide = inRideCount,
                lowBattery = lowBatteryCount,
                maintenance = maintenanceCount
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rebalancing Alert Banner
            RebalancingAlertBanner()

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips & Search Bar
            FleetFiltersRow(
                currentFilter = currentFilter,
                onFilterSelected = { viewModel.setFleetFilter(it) },
                searchQuery = searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Vehicle Fleet List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .navigationBarsPadding()
                    .testTag("fleet_ops_list")
            ) {
                items(filteredFleet, key = { it.id }) { vehicle ->
                    FleetVehicleCard(
                        vehicle = vehicle,
                        onUnlock = { viewModel.backOfficeRemoteUnlock(vehicle.id) },
                        onLock = { viewModel.backOfficeRemoteLock(vehicle.id) },
                        onBuzzer = { viewModel.backOfficeBuzzer(vehicle.id) },
                        onSwapBattery = { viewModel.backOfficeSwapBattery(vehicle.id) },
                        onToggleMaintenance = { viewModel.backOfficeToggleMaintenance(vehicle.id, vehicle.status == VehicleStatus.MAINTENANCE) },
                        onRelocate = { lat, lng, name -> viewModel.backOfficeRelocate(vehicle.id, lat, lng, name) },
                        onInspectTelemetry = { viewModel.openTelemetry(vehicle.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // MQTT Phase 2 Broker Inspector Overlay
        if (showMqttInspector) {
            MqttInspectorDrawer(
                packets = mqttPackets,
                onClose = { showMqttInspector = false },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Deep Telemetry Dialog
        if (isTelemetryOpen && targetVehicleId != null) {
            val targetVehicle = fleet.find { it.id == targetVehicleId }
            if (targetVehicle != null) {
                val telemetry = viewModel.vehicleGateway.getIotTelemetry(targetVehicle.id)
                VehicleTelemetryDialog(
                    vehicle = targetVehicle,
                    telemetry = telemetry,
                    onDismiss = { viewModel.isTelemetryDialogVisible.value = false },
                    onSwapBattery = { viewModel.backOfficeSwapBattery(targetVehicle.id) },
                    onBuzzer = { viewModel.backOfficeBuzzer(targetVehicle.id) },
                    onToggleLock = {
                        if (targetVehicle.isLocked) viewModel.backOfficeRemoteUnlock(targetVehicle.id)
                        else viewModel.backOfficeRemoteLock(targetVehicle.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun FleetOpsHeader(
    onSwitchToRider: () -> Unit,
    onToggleMqtt: () -> Unit,
    isMqttOpen: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF1A1C18), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FLEET OPS • TASHKENT HUB",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1C18),
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "Live Telemetry & Remote Vehicle Gateway",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424940)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // MQTT Broker Button
            Surface(
                color = if (isMqttOpen) ScootGreen else Color.White,
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clickable { onToggleMqtt() }
                    .testTag("toggle_mqtt_inspector_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = Color(0xFF1A1C18),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MQTT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                }
            }

            // Switch to Rider App
            Button(
                onClick = onSwitchToRider,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScootGreen,
                    contentColor = Color(0xFF1A1C18)
                ),
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("switch_to_rider_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricScooter,
                    contentDescription = null,
                    tint = Color(0xFF1A1C18),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Rider View",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun KpiSummaryGrid(
    total: Int,
    available: Int,
    inRide: Int,
    lowBattery: Int,
    maintenance: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiCard(
            label = "TOTAL FLEET",
            value = "$total",
            subValue = "100% Online",
            accentColor = Color(0xFF1A1C18),
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label = "AVAILABLE",
            value = "$available",
            subValue = "Ready to ride",
            accentColor = Color(0xFF1A1C18),
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label = "IN RIDE",
            value = "$inRide",
            subValue = "Active trips",
            accentColor = Color(0xFF1A1C18),
            modifier = Modifier.weight(1f)
        )
        KpiCard(
            label = "LOW BATT",
            value = "$lowBattery",
            subValue = "<20% SOC",
            accentColor = if (lowBattery > 0) ScootRed else Color(0xFF424940),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DarkSurfaceBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF424940)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF72796F)
            )
        }
    }
}

@Composable
private fun RebalancingAlertBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = ScootYellow),
        border = BorderStroke(2.dp, DarkSurfaceBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF1A1C18),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Rebalancing Alert: High morning demand near Amir Timur Square. 3 vans dispatched.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1A1C18)
            )
        }
    }
}

@Composable
private fun FleetFiltersRow(
    currentFilter: VehicleStatus?,
    onFilterSelected: (VehicleStatus?) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by ID or Tashkent location...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF424940))
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A1C18),
                unfocusedBorderColor = DarkSurfaceBorder,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                focusedTextColor = Color(0xFF1A1C18),
                unfocusedTextColor = Color(0xFF1A1C18)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fleet_search_bar")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = currentFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("ALL", fontWeight = FontWeight.Black) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScootGreen,
                    selectedLabelColor = Color(0xFF1A1C18),
                    containerColor = Color.White,
                    labelColor = Color(0xFF1A1C18)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = currentFilter == null,
                    borderColor = DarkSurfaceBorder,
                    borderWidth = 2.dp
                ),
                modifier = Modifier.testTag("filter_all")
            )
            FilterChip(
                selected = currentFilter == VehicleStatus.AVAILABLE,
                onClick = { onFilterSelected(VehicleStatus.AVAILABLE) },
                label = { Text("AVAILABLE", fontWeight = FontWeight.Black) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScootGreen,
                    selectedLabelColor = Color(0xFF1A1C18),
                    containerColor = Color.White,
                    labelColor = Color(0xFF1A1C18)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = currentFilter == VehicleStatus.AVAILABLE,
                    borderColor = DarkSurfaceBorder,
                    borderWidth = 2.dp
                ),
                modifier = Modifier.testTag("filter_available")
            )
            FilterChip(
                selected = currentFilter == VehicleStatus.IN_RIDE,
                onClick = { onFilterSelected(VehicleStatus.IN_RIDE) },
                label = { Text("IN RIDE", fontWeight = FontWeight.Black) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScootGreen,
                    selectedLabelColor = Color(0xFF1A1C18),
                    containerColor = Color.White,
                    labelColor = Color(0xFF1A1C18)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = currentFilter == VehicleStatus.IN_RIDE,
                    borderColor = DarkSurfaceBorder,
                    borderWidth = 2.dp
                ),
                modifier = Modifier.testTag("filter_in_ride")
            )
            FilterChip(
                selected = currentFilter == VehicleStatus.LOW_BATTERY,
                onClick = { onFilterSelected(VehicleStatus.LOW_BATTERY) },
                label = { Text("LOW BATT", fontWeight = FontWeight.Black) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScootYellow,
                    selectedLabelColor = Color(0xFF1A1C18),
                    containerColor = Color.White,
                    labelColor = Color(0xFF1A1C18)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = currentFilter == VehicleStatus.LOW_BATTERY,
                    borderColor = DarkSurfaceBorder,
                    borderWidth = 2.dp
                ),
                modifier = Modifier.testTag("filter_low_batt")
            )
        }
    }
}

@Composable
private fun FleetVehicleCard(
    vehicle: Vehicle,
    onUnlock: () -> Unit,
    onLock: () -> Unit,
    onBuzzer: () -> Unit,
    onSwapBattery: () -> Unit,
    onToggleMaintenance: () -> Unit,
    onRelocate: (lat: Double, lng: Double, name: String) -> Unit,
    onInspectTelemetry: () -> Unit
) {
    var isRelocateMenuOpen by remember { mutableStateOf(false) }

    val statusBg = when (vehicle.status) {
        VehicleStatus.AVAILABLE -> ScootGreen
        VehicleStatus.IN_RIDE -> Color(0xFFD7E8CD)
        VehicleStatus.LOW_BATTERY -> ScootYellow
        VehicleStatus.MAINTENANCE -> Color(0xFFE1E3DA)
        VehicleStatus.IMPOUNDED -> ScootRed
        VehicleStatus.RESERVED -> ScootYellow
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DarkSurfaceBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("vehicle_card_${vehicle.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: ID, Status Pill, Battery %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = vehicle.id,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = statusBg,
                        border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = vehicle.status.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C18),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "🔋 ${vehicle.batteryPercent}% (${vehicle.estimatedRangeKm} km)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1C18)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Location
            Text(
                text = "📍 ${vehicle.locationName}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424940)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Action Buttons (Unlock/Lock, Buzzer, Swap 100%, Rebalance, Telemetry)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lock / Unlock Toggle
                Button(
                    onClick = { if (vehicle.isLocked) onUnlock() else onLock() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vehicle.isLocked) ScootGreen else ScootYellow,
                        contentColor = Color(0xFF1A1C18)
                    ),
                    border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.1f).height(40.dp).testTag("card_lock_btn_${vehicle.id}")
                ) {
                    Icon(
                        imageVector = if (vehicle.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF1A1C18),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (vehicle.isLocked) "Unlock" else "Lock", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                // Buzzer
                Button(
                    onClick = onBuzzer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A1C18)
                    ),
                    border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(0.9f).height(40.dp).testTag("card_buzzer_btn_${vehicle.id}")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF1A1C18), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Bell", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                // Swap Battery
                Button(
                    onClick = onSwapBattery,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD7E8CD),
                        contentColor = Color(0xFF1A1C18)
                    ),
                    border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.1f).height(40.dp).testTag("card_swap_btn_${vehicle.id}")
                ) {
                    Text("⚡ 100%", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                // Relocate Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { isRelocateMenuOpen = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1A1C18)
                        ),
                        border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("card_relocate_btn_${vehicle.id}")
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF1A1C18), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Move", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    DropdownMenu(
                        expanded = isRelocateMenuOpen,
                        onDismissRequest = { isRelocateMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Amir Timur Square", fontWeight = FontWeight.Bold) },
                            onClick = {
                                onRelocate(41.3114, 69.2792, "Amir Timur Square (Hub)")
                                isRelocateMenuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sayilgoh (Broadway)", fontWeight = FontWeight.Bold) },
                            onClick = {
                                onRelocate(41.3138, 69.2748, "Sayilgoh Promenade")
                                isRelocateMenuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tashkent City Park", fontWeight = FontWeight.Bold) },
                            onClick = {
                                onRelocate(41.3131, 69.2542, "Tashkent City Park")
                                isRelocateMenuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Chorsu Bazaar", fontWeight = FontWeight.Bold) },
                            onClick = {
                                onRelocate(41.3278, 69.2365, "Chorsu Bazaar Metro")
                                isRelocateMenuOpen = false
                            }
                        )
                    }
                }

                // Telemetry
                Button(
                    onClick = onInspectTelemetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A1C18)
                    ),
                    border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(40.dp).testTag("card_telemetry_btn_${vehicle.id}")
                ) {
                    Icon(Icons.Default.Memory, contentDescription = "IoT Diagnostics", tint = Color(0xFF1A1C18), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun MqttInspectorDrawer(
    packets: List<com.example.gateway.IotMqttPacket>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, DarkSurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .testTag("mqtt_inspector_drawer")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Hub, contentDescription = null, tint = Color(0xFF1A1C18), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Phase 2 IoT MQTT Packet Stream (tls://mqtt.iot.scoot.uz:8883)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1A1C18))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (packets.isEmpty()) {
                    item {
                        Text(
                            text = "Awaiting MQTT packets... Trigger Unlock, Buzzer, or Headlight to inspect live telemetry payloads.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424940)
                        )
                    }
                } else {
                    items(packets.takeLast(10).reversed()) { pkt ->
                        Surface(
                            color = Color(0xFFF7F9F2),
                            border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = pkt.topic,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF1A1C18)
                                    )
                                    Text(
                                        text = if (pkt.direction == com.example.gateway.PacketDirection.OUTBOUND) "PUBLISH (QoS 1)" else "RECEIVED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (pkt.direction == com.example.gateway.PacketDirection.OUTBOUND) ScootYellow else Color(0xFF1A1C18)
                                    )
                                }
                                Text(
                                    text = pkt.payloadJson,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1C18)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
