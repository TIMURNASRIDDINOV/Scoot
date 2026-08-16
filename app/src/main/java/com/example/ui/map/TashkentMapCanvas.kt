package com.example.ui.map

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Ride
import com.example.model.TashkentZone
import com.example.model.TashkentZones
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.model.ZoneType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MapCanvasBg
import com.example.ui.theme.MapParkGreen
import com.example.ui.theme.MapRoadPrimary
import com.example.ui.theme.MapRoadSecondary
import com.example.ui.theme.MapWaterBlue
import com.example.ui.theme.MapZoneRestricted
import com.example.ui.theme.MapZoneSlow
import com.example.ui.theme.ScootCyan
import com.example.ui.theme.ScootGreen
import com.example.ui.theme.ScootPurple
import com.example.ui.theme.ScootRed
import com.example.ui.theme.ScootYellow
import kotlin.math.sqrt

// Center anchor of map projection (Amir Timur Square)
private const val MAP_CENTER_LAT = 41.3111
private const val MAP_CENTER_LNG = 49.2797 // Used as baseline scale
private const val BASE_LAT = 41.3111
private const val BASE_LNG = 69.2797

@Composable
fun TashkentMapCanvas(
    fleet: List<Vehicle>,
    selectedVehicle: Vehicle?,
    userLocation: Pair<Double, Double>,
    activeRide: Ride?,
    showZones: Boolean = true,
    onScooterClick: (Vehicle) -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Zoom and pan state
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Pulsing radar animation for user location
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRadius"
    )

    // Helper conversion from Lat/Lng to Screen Canvas coordinates
    fun latLngToScreen(lat: Double, lng: Double, canvasSize: Size): Offset {
        val centerX = canvasSize.width / 2f + panOffset.x
        val centerY = canvasSize.height / 2f + panOffset.y

        // Scale factor: 1 degree latitude ~ 111 km, in pixels scaled
        val basePixelsPerDegreeLat = canvasSize.height * 14.0f * zoomScale
        val basePixelsPerDegreeLng = canvasSize.width * 11.0f * zoomScale

        val dLng = (lng - BASE_LNG)
        val dLat = (lat - BASE_LAT)

        val x = centerX + (dLng * basePixelsPerDegreeLng).toFloat()
        val y = centerY - (dLat * basePixelsPerDegreeLat).toFloat() // Invert Y for screen coords
        return Offset(x, y)
    }

    Box(modifier = modifier.fillMaxSize().background(MapCanvasBg)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("tashkent_map_canvas")
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.5f)
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-1200f, 1200f),
                            y = (panOffset.y + pan.y).coerceIn(-1200f, 1200f)
                        )
                    }
                }
                .pointerInput(fleet, zoomScale, panOffset) {
                    detectTapGestures { tapOffset ->
                        val hitRadius = 32.dp.toPx()
                        var hitScooter: Vehicle? = null

                        for (vehicle in fleet) {
                            val scooterPos = latLngToScreen(vehicle.latitude, vehicle.longitude, Size(size.width.toFloat(), size.height.toFloat()))
                            val dx = tapOffset.x - scooterPos.x
                            val dy = tapOffset.y - scooterPos.y
                            if (sqrt((dx * dx + dy * dy).toDouble()) <= hitRadius) {
                                hitScooter = vehicle
                                break
                            }
                        }

                        if (hitScooter != null) {
                            onScooterClick(hitScooter)
                        } else {
                            onMapClick()
                        }
                    }
                }
        ) {
            val canvasSize = size

            // 1. Draw Tashkent Geography & Road Grid
            drawTashkentGeography(canvasSize, ::latLngToScreen)

            // 2. Draw Geofence Zones if enabled
            if (showZones) {
                drawGeofenceZones(canvasSize, zoomScale, ::latLngToScreen)
            }

            // 3. Draw Active Ride Route Polyline
            if (activeRide != null && activeRide.routePoints.isNotEmpty()) {
                drawActiveRoutePolyline(activeRide, canvasSize, ::latLngToScreen)
            }

            // 4. Draw Fleet Scooters
            drawFleetScooters(fleet, selectedVehicle, canvasSize, ::latLngToScreen)

            // 5. Draw User / Rider Location with live pulse
            drawUserLocation(userLocation, radarPulse, canvasSize, ::latLngToScreen)
        }

        // Floating Map Controls (Center, Zoom, Zones)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .offset(y = (-40).dp)
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        panOffset = Offset.Zero
                        zoomScale = 1.0f
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = ScootGreen,
                    modifier = Modifier.testTag("map_center_btn")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Center on me")
                }

                SmallFloatingActionButton(
                    onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.5f) },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("map_zoom_in_btn")
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in")
                }

                SmallFloatingActionButton(
                    onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.6f) },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("map_zoom_out_btn")
                ) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out")
                }
            }
        }

        // Map City Badge (Top Left overlay)
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = CircleShape,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 80.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(ScootGreen, CircleShape)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Tashkent Fleet Live",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

private fun DrawScope.drawTashkentGeography(
    canvasSize: Size,
    projector: (Double, Double, Size) -> Offset
) {
    // 1. Parks & Green Belts (Tashkent City Park, Amir Timur Park, Navoi Garden)
    // Amir Timur Square Center Circular Garden
    val timurCenter = projector(41.3111, 69.2797, canvasSize)
    drawCircle(
        color = MapParkGreen,
        radius = 50f,
        center = timurCenter
    )
    drawCircle(
        color = Color(0x3300E599),
        radius = 20f,
        center = timurCenter
    )

    // Tashkent City Park Lake & Green grounds
    val tcPark = projector(41.3128, 69.2540, canvasSize)
    drawRoundRect(
        color = MapParkGreen,
        topLeft = Offset(tcPark.x - 70f, tcPark.y - 50f),
        size = Size(140f, 100f),
        cornerRadius = CornerRadius(24f, 24f)
    )
    // Tashkent City Fountain lake
    drawCircle(
        color = MapWaterBlue,
        radius = 28f,
        center = tcPark
    )

    // Magic City Lake & grounds
    val magicCenter = projector(41.3032, 69.2470, canvasSize)
    drawRoundRect(
        color = MapParkGreen,
        topLeft = Offset(magicCenter.x - 60f, magicCenter.y - 45f),
        size = Size(120f, 90f),
        cornerRadius = CornerRadius(20f, 20f)
    )
    drawCircle(
        color = MapWaterBlue,
        radius = 22f,
        center = magicCenter
    )

    // 2. Anhor Canal (Water ribbon running through central Tashkent)
    val canalPath = Path().apply {
        val p1 = projector(41.3320, 69.2610, canvasSize)
        val p2 = projector(41.3210, 69.2640, canvasSize)
        val p3 = projector(41.3140, 69.2635, canvasSize)
        val p4 = projector(41.3050, 69.2590, canvasSize)
        val p5 = projector(41.2980, 69.2530, canvasSize)
        moveTo(p1.x, p1.y)
        cubicTo(p2.x, p2.y, p3.x, p3.y, p4.x, p4.y)
        lineTo(p5.x, p5.y)
    }
    drawPath(
        path = canalPath,
        color = MapWaterBlue,
        style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // 3. Primary Tashkent Arteries (Mustaqillik, Navoi, Amir Timur, Sayilgoh, Shota Rustaveli)
    // Amir Timur Radial Ring
    drawCircle(
        color = MapRoadPrimary,
        radius = 55f,
        center = timurCenter,
        style = Stroke(width = 12f)
    )

    // Mustaqillik Avenue (Northeast from Timur Sq towards Pushkin)
    val mustaqillikEnd = projector(41.3280, 69.3020, canvasSize)
    drawLine(
        color = MapRoadPrimary,
        start = timurCenter,
        end = mustaqillikEnd,
        strokeWidth = 14f,
        cap = StrokeCap.Round
    )

    // Amir Timur Street (North towards Alay Market & Minor)
    val timurNorth = projector(41.3340, 69.2820, canvasSize)
    val timurSouth = projector(41.2980, 69.2780, canvasSize)
    drawLine(
        color = MapRoadPrimary,
        start = timurNorth,
        end = timurSouth,
        strokeWidth = 12f,
        cap = StrokeCap.Round
    )

    // Sayilgoh / Broadway pedestrian avenue (West from Timur Sq to Navoi St)
    val sayilgohWest = projector(41.3145, 69.2690, canvasSize)
    drawLine(
        color = Color(0xFF2E4666),
        start = timurCenter,
        end = sayilgohWest,
        strokeWidth = 16f,
        cap = StrokeCap.Round
    )

    // Navoi Avenue (connecting Broadway past Theatre to Chorsu)
    val chorsuHub = projector(41.3275, 69.2360, canvasSize)
    val navoiEast = projector(41.3090, 69.2720, canvasSize)
    drawLine(
        color = MapRoadPrimary,
        start = sayilgohWest,
        end = chorsuHub,
        strokeWidth = 12f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = MapRoadPrimary,
        start = sayilgohWest,
        end = navoiEast,
        strokeWidth = 10f,
        cap = StrokeCap.Round
    )

    // Islam Karimov Avenue (Connecting Tashkent City, Forum Palace, Kosmonavtlar)
    val karimovEast = projector(41.3060, 69.2840, canvasSize)
    val karimovWest = projector(41.3120, 69.2480, canvasSize)
    drawLine(
        color = MapRoadSecondary,
        start = karimovEast,
        end = karimovWest,
        strokeWidth = 10f,
        cap = StrokeCap.Round
    )

    // Secondary Street Grid lines
    val gridA1 = projector(41.3200, 69.2400, canvasSize)
    val gridA2 = projector(41.3000, 69.2400, canvasSize)
    drawLine(color = MapRoadSecondary, start = gridA1, end = gridA2, strokeWidth = 6f)

    val gridB1 = projector(41.3250, 69.2550, canvasSize)
    val gridB2 = projector(41.2980, 69.2550, canvasSize)
    drawLine(color = MapRoadSecondary, start = gridB1, end = gridB2, strokeWidth = 6f)

    // Draw City Landmark Text Labels natively on canvas
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(220, 26, 28, 24)
            textSize = 24f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        drawText("AMIR TIMUR SQ", timurCenter.x, timurCenter.y - 65f, paint)
        drawText("SAYILGOH (BROADWAY)", sayilgohWest.x - 30f, sayilgohWest.y - 18f, paint)
        drawText("TASHKENT CITY", tcPark.x, tcPark.y - 60f, paint)
        drawText("CHORSU BAZAAR", chorsuHub.x, chorsuHub.y - 40f, paint)
        drawText("NAVOI THEATRE", navoiEast.x, navoiEast.y + 40f, paint)
        drawText("MAGIC CITY", magicCenter.x, magicCenter.y - 50f, paint)
    }
}

private fun DrawScope.drawGeofenceZones(
    canvasSize: Size,
    zoomScale: Float,
    projector: (Double, Double, Size) -> Offset
) {
    for (zone in TashkentZones.zones) {
        val center = projector(zone.centerLat, zone.centerLng, canvasSize)
        val radiusPx = (zone.radiusMeters.toFloat() * 0.28f * zoomScale).coerceAtLeast(35f)

        when (zone.type) {
            ZoneType.SLOW_SPEED_15KMH -> {
                // Amber Slow Speed Zone
                drawCircle(
                    color = MapZoneSlow,
                    radius = radiusPx,
                    center = center
                )
                drawCircle(
                    color = ScootYellow.copy(alpha = 0.8f),
                    radius = radiusPx,
                    center = center,
                    style = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )
            }
            ZoneType.NO_PARKING -> {
                // Red Restricted / No-Parking Zone
                drawCircle(
                    color = MapZoneRestricted,
                    radius = radiusPx,
                    center = center
                )
                drawCircle(
                    color = ScootRed.copy(alpha = 0.8f),
                    radius = radiusPx,
                    center = center,
                    style = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                )
            }
            else -> {
                // Permitted ride zone (subtle green perimeter)
                drawCircle(
                    color = Color(0x0F00E599),
                    radius = radiusPx,
                    center = center
                )
            }
        }
    }
}

private fun DrawScope.drawActiveRoutePolyline(
    ride: Ride,
    canvasSize: Size,
    projector: (Double, Double, Size) -> Offset
) {
    if (ride.routePoints.size < 2) return

    val path = Path()
    val first = projector(ride.routePoints.first().latitude, ride.routePoints.first().longitude, canvasSize)
    path.moveTo(first.x, first.y)

    for (i in 1 until ride.routePoints.size) {
        val pt = projector(ride.routePoints[i].latitude, ride.routePoints[i].longitude, canvasSize)
        path.lineTo(pt.x, pt.y)
    }

    // Outer glow
    drawPath(
        path = path,
        color = ScootCyan.copy(alpha = 0.3f),
        style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    // Core line
    drawPath(
        path = path,
        color = ScootCyan,
        style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawFleetScooters(
    fleet: List<Vehicle>,
    selectedVehicle: Vehicle?,
    canvasSize: Size,
    projector: (Double, Double, Size) -> Offset
) {
    for (vehicle in fleet) {
        val pos = projector(vehicle.latitude, vehicle.longitude, canvasSize)
        val isSelected = selectedVehicle?.id == vehicle.id

        val pinColor = when (vehicle.status) {
            VehicleStatus.AVAILABLE -> ScootGreen
            VehicleStatus.IN_RIDE -> ScootCyan
            VehicleStatus.LOW_BATTERY -> ScootYellow
            VehicleStatus.MAINTENANCE -> Color(0xFF94A3B8)
            VehicleStatus.IMPOUNDED -> ScootRed
            VehicleStatus.RESERVED -> ScootPurple
        }

        // Target highlight if selected
        if (isSelected) {
            drawCircle(
                color = pinColor.copy(alpha = 0.4f),
                radius = 36f,
                center = pos
            )
            drawCircle(
                color = Color(0xFF1A1C18),
                radius = 28f,
                center = pos,
                style = Stroke(width = 4f)
            )
        }

        // Scooter Pin Body (Bold Neo-brutalist pill with thick black border)
        drawCircle(
            color = Color(0xFF1A1C18),
            radius = 20f,
            center = pos
        )
        drawCircle(
            color = pinColor,
            radius = 16f,
            center = pos
        )

        // Inner battery indicator arc (0 to 360 deg)
        val sweepAngle = (vehicle.batteryPercent / 100f) * 360f
        drawArc(
            color = Color(0xFF1A1C18),
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(pos.x - 16f, pos.y - 16f),
            size = Size(32f, 32f),
            style = Stroke(width = 3.5f)
        )

        // Draw Mini Battery % inside pin in bold black text
        drawContext.canvas.nativeCanvas.apply {
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#1A1C18")
                textSize = 14f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            drawText("${vehicle.batteryPercent}", pos.x, pos.y + 5f, textPaint)
        }
    }
}

private fun DrawScope.drawUserLocation(
    userLocation: Pair<Double, Double>,
    radarPulse: Float,
    canvasSize: Size,
    projector: (Double, Double, Size) -> Offset
) {
    val pos = projector(userLocation.first, userLocation.second, canvasSize)

    // Expanding concentric radar wave
    val pulseRadius = 16f + (radarPulse * 45f)
    val pulseAlpha = (1f - radarPulse).coerceIn(0f, 1f) * 0.5f

    drawCircle(
        color = ScootGreen.copy(alpha = pulseAlpha),
        radius = pulseRadius,
        center = pos
    )

    // User black outer ring
    drawCircle(
        color = Color(0xFF1A1C18),
        radius = 16f,
        center = pos
    )

    // User white inner ring
    drawCircle(
        color = Color.White,
        radius = 12f,
        center = pos
    )

    // Center solid volt GPS dot
    drawCircle(
        color = ScootGreen,
        radius = 8f,
        center = pos
    )
}
