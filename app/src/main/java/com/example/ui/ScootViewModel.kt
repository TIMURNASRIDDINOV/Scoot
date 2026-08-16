package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RideEntity
import com.example.data.ScootDatabase
import com.example.data.TransactionEntity
import com.example.gateway.IotMqttPacket
import com.example.gateway.MockUzbekPaymentProvider
import com.example.gateway.SimulatedVehicleGateway
import com.example.gateway.StubbedIotGateway
import com.example.gateway.VehicleGateway
import com.example.model.IotTelemetry
import com.example.model.PaymentCard
import com.example.model.PaymentType
import com.example.model.Ride
import com.example.model.RideStatus
import com.example.model.RoutePoint
import com.example.model.TashkentZone
import com.example.model.TashkentZones
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.model.WalletAccount
import com.example.model.ZoneType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

enum class AppMode {
    RIDER,
    FLEET_BACKOFFICE
}

data class UiNotification(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val isError: Boolean = false
)

class ScootViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ScootDatabase.getInstance(application)
    val iotGateway = StubbedIotGateway()
    val vehicleGateway: SimulatedVehicleGateway = SimulatedVehicleGateway(iotGateway, viewModelScope)
    val paymentProvider = MockUzbekPaymentProvider()

    // Mode
    private val _appMode = MutableStateFlow(AppMode.RIDER)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    // Fleet state
    val fleet: StateFlow<List<Vehicle>> = vehicleGateway.observeFleet()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Back-office filters
    private val _fleetFilter = MutableStateFlow<VehicleStatus?>(null)
    val fleetFilter: StateFlow<VehicleStatus?> = _fleetFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredFleet: StateFlow<List<Vehicle>> = combine(fleet, _fleetFilter, _searchQuery) { list, filter, query ->
        list.filter { vehicle ->
            val matchesFilter = filter == null || vehicle.status == filter
            val matchesQuery = query.isEmpty() ||
                    vehicle.id.contains(query, ignoreCase = true) ||
                    vehicle.locationName.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Selected vehicle in rider map or back-office
    private val _selectedVehicle = MutableStateFlow<Vehicle?>(null)
    val selectedVehicle: StateFlow<Vehicle?> = _selectedVehicle.asStateFlow()

    // User GPS location (Tashkent central - near Amir Timur / Sayilgoh)
    private val _userLocation = MutableStateFlow(Pair(41.3118, 69.2785))
    val userLocation: StateFlow<Pair<Double, Double>> = _userLocation.asStateFlow()

    // Active Ride
    private val _activeRide = MutableStateFlow<Ride?>(null)
    val activeRide: StateFlow<Ride?> = _activeRide.asStateFlow()

    // Ride simulation job
    private var rideSimulationJob: Job? = null

    // Wallet & Balance
    private val _wallet = MutableStateFlow(WalletAccount())
    val wallet: StateFlow<WalletAccount> = _wallet.asStateFlow()

    // Notification toast / snackbar
    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    // History from Room
    val pastRides: StateFlow<List<RideEntity>> = db.rideDao().getAllRides()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Live MQTT stream for inspector
    val mqttPackets: StateFlow<List<IotMqttPacket>> = iotGateway.livePacketStream
        .scan(emptyList<IotMqttPacket>()) { list, pkt -> (list + pkt).takeLast(50) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Modal UI states
    val isUnlockDialogVisible = MutableStateFlow(false)
    val isEndRideDialogVisible = MutableStateFlow(false)
    val isWalletDialogVisible = MutableStateFlow(false)
    val isTelemetryDialogVisible = MutableStateFlow(false)
    val telemetryTargetVehicleId = MutableStateFlow<String?>(null)
    val isZonesLayerVisible = MutableStateFlow(true)

    init {
        // Pre-populate with initial welcoming transactions if empty
        viewModelScope.launch {
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    id = "TX-INIT-01",
                    title = "Welcome Bonus Balance",
                    amountUzs = 45000L,
                    isDebit = false,
                    timestampMillis = System.currentTimeMillis() - 3600000 * 24,
                    paymentMethod = "Payme",
                    referenceId = "PAYME-TOPUP-98214"
                )
            )
        }
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun selectVehicle(vehicle: Vehicle?) {
        _selectedVehicle.value = vehicle
    }

    fun setFleetFilter(status: VehicleStatus?) {
        _fleetFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showNotification(message: String, isError: Boolean = false) {
        _notification.value = UiNotification(message = message, isError = isError)
    }

    fun clearNotification() {
        _notification.value = null
    }

    fun getCurrentZone(lat: Double, lng: Double): TashkentZone? {
        return TashkentZones.zones.find { zone ->
            val dist = calculateDistanceMeters(lat, lng, zone.centerLat, zone.centerLng)
            dist <= zone.radiusMeters
        }
    }

    fun startRide(vehicleId: String) {
        val vehicle = vehicleGateway.getVehicle(vehicleId)
        if (vehicle == null) {
            showNotification("Scooter $vehicleId not found", isError = true)
            return
        }

        if (vehicle.status == VehicleStatus.IN_RIDE) {
            showNotification("Scooter is already in ride", isError = true)
            return
        }

        if (vehicle.batteryPercent < 15) {
            showNotification("Battery is too low (<15%). Please choose another scooter.", isError = true)
            return
        }

        viewModelScope.launch {
            val unlockResult = vehicleGateway.unlockVehicle(vehicle.id)
            if (unlockResult.isSuccess) {
                val newRide = Ride(
                    id = "RIDE-UZ-${(10000..99999).random()}",
                    vehicleId = vehicle.id,
                    vehicleModel = vehicle.modelName,
                    startTimeMillis = System.currentTimeMillis(),
                    startLocationName = vehicle.locationName,
                    status = RideStatus.ACTIVE,
                    unlockCostUzs = vehicle.unlockFeeUzs,
                    totalCostUzs = vehicle.unlockFeeUzs,
                    routePoints = listOf(RoutePoint(vehicle.latitude, vehicle.longitude))
                )
                _activeRide.value = newRide
                _selectedVehicle.value = null
                isUnlockDialogVisible.value = false
                showNotification("⚡ ${vehicle.id} Unlocked! Enjoy your ride across Tashkent!")

                // Start simulated movement along Tashkent street nodes
                startRideSimulation(newRide, vehicle)
            } else {
                showNotification("Failed to unlock vehicle: ${unlockResult.exceptionOrNull()?.message}", isError = true)
            }
        }
    }

    private fun startRideSimulation(initialRide: Ride, vehicle: Vehicle) {
        rideSimulationJob?.cancel()
        rideSimulationJob = viewModelScope.launch {
            var currentLat = vehicle.latitude
            var currentLng = vehicle.longitude
            var totalDistMeters = 0.0
            var seconds = 0L
            var pausedSeconds = 0L

            // Tashkent central street wander vector
            var dLat = 0.00008
            var dLng = 0.00006

            while (true) {
                delay(1000)
                val current = _activeRide.value ?: break
                if (current.status == RideStatus.ACTIVE) {
                    seconds++
                    // Speed simulation: 18-23 km/h normal, 12-14 km/h in slow zone
                    val currentZone = getCurrentZone(currentLat, currentLng)
                    val speed = if (currentZone?.type == ZoneType.SLOW_SPEED_15KMH) {
                        Random.nextDouble(12.0, 14.5)
                    } else {
                        Random.nextDouble(18.5, 23.2)
                    }

                    // Delta distance
                    val deltaMeters = (speed * 1000.0) / 3600.0
                    totalDistMeters += deltaMeters

                    // Move coordinates
                    currentLat += dLat
                    currentLng += dLng

                    // Bounce slightly if wandering too far from central Tashkent
                    if (currentLat > 41.3350 || currentLat < 41.2950) dLat = -dLat
                    if (currentLng > 69.2950 || currentLng < 69.2250) dLng = -dLng

                    _userLocation.value = Pair(currentLat, currentLng)
                    vehicleGateway.updateSimulatedPosition(vehicle.id, currentLat, currentLng, deltaMeters / 1000.0)

                    // Cost calculation: 1,000 UZS unlock + 800 UZS/min (prorated per second)
                    val rideFee = ((seconds / 60.0) * vehicle.minuteFeeUzs).toLong()
                    val pauseFee = ((pausedSeconds / 60.0) * vehicle.pauseFeeUzs).toLong()
                    val total = vehicle.unlockFeeUzs + rideFee + pauseFee

                    val updatedPoints = current.routePoints + RoutePoint(currentLat, currentLng)

                    _activeRide.value = current.copy(
                        durationSeconds = seconds,
                        distanceMeters = totalDistMeters,
                        currentSpeedKmh = speed,
                        rideCostUzs = rideFee,
                        pauseCostUzs = pauseFee,
                        totalCostUzs = total,
                        routePoints = updatedPoints
                    )
                } else if (current.status == RideStatus.PAUSED) {
                    pausedSeconds++
                    val rideFee = ((seconds / 60.0) * vehicle.minuteFeeUzs).toLong()
                    val pauseFee = ((pausedSeconds / 60.0) * vehicle.pauseFeeUzs).toLong()
                    val total = vehicle.unlockFeeUzs + rideFee + pauseFee

                    _activeRide.value = current.copy(
                        pausedSeconds = pausedSeconds,
                        currentSpeedKmh = 0.0,
                        rideCostUzs = rideFee,
                        pauseCostUzs = pauseFee,
                        totalCostUzs = total
                    )
                }
            }
        }
    }

    fun pauseRide() {
        val current = _activeRide.value ?: return
        _activeRide.value = current.copy(status = RideStatus.PAUSED)
        showNotification("Ride paused (300 UZS/min). Scooter wheel locked.")
    }

    fun resumeRide() {
        val current = _activeRide.value ?: return
        _activeRide.value = current.copy(status = RideStatus.ACTIVE)
        showNotification("Ride resumed! Have a safe trip.")
    }

    fun toggleRideHeadlight() {
        val current = _activeRide.value ?: return
        val vehicle = vehicleGateway.getVehicle(current.vehicleId) ?: return
        viewModelScope.launch {
            val newState = !vehicle.isHeadlightOn
            vehicleGateway.toggleHeadlight(vehicle.id, newState)
            showNotification(if (newState) "💡 Headlight turned ON" else "Headlight turned OFF")
        }
    }

    fun ringRideBell() {
        val current = _activeRide.value ?: return
        viewModelScope.launch {
            vehicleGateway.sendBuzzer(current.vehicleId)
            showNotification("🔔 Scooter buzzer ringing! (85dB)")
        }
    }

    fun endRide(parkingPhotoUri: String? = null, rating: Int = 5) {
        val current = _activeRide.value ?: return
        rideSimulationJob?.cancel()

        viewModelScope.launch {
            // Lock the vehicle via gateway seam
            vehicleGateway.lockVehicle(current.vehicleId)

            // Current zone check for end of ride
            val endLat = _userLocation.value.first
            val endLng = _userLocation.value.second
            val zone = getCurrentZone(endLat, endLng)
            val endLocationName = zone?.name ?: "Tashkent City Area"

            // Process payment through mock Uzbek payment provider
            val authResult = paymentProvider.authorizeAndCapture(
                amountUzs = current.totalCostUzs,
                paymentType = PaymentType.PAYME,
                card = _wallet.value.cards.firstOrNull(),
                description = "Scoot Ride ${current.id}"
            )

            val completedRide = current.copy(
                endTimeMillis = System.currentTimeMillis(),
                endLocationName = endLocationName,
                status = RideStatus.COMPLETED,
                parkingPhotoUri = parkingPhotoUri ?: "mock_parking_approved.jpg",
                paymentMethod = authResult.paymentGateway,
                paymentTransactionId = authResult.transactionId,
                rating = rating
            )

            // Persist to Room
            db.rideDao().insertRide(
                RideEntity(
                    id = completedRide.id,
                    vehicleId = completedRide.vehicleId,
                    vehicleModel = completedRide.vehicleModel,
                    startTimeMillis = completedRide.startTimeMillis,
                    endTimeMillis = completedRide.endTimeMillis,
                    startLocationName = completedRide.startLocationName,
                    endLocationName = completedRide.endLocationName,
                    durationSeconds = completedRide.durationSeconds,
                    distanceMeters = completedRide.distanceMeters,
                    totalCostUzs = completedRide.totalCostUzs,
                    status = completedRide.status.name,
                    parkingPhotoUri = completedRide.parkingPhotoUri,
                    paymentMethod = completedRide.paymentMethod,
                    paymentTransactionId = completedRide.paymentTransactionId,
                    rating = completedRide.rating
                )
            )

            // Deduct / record transaction
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    id = authResult.transactionId,
                    title = "Ride ${completedRide.vehicleId}",
                    amountUzs = completedRide.totalCostUzs,
                    isDebit = true,
                    timestampMillis = System.currentTimeMillis(),
                    paymentMethod = authResult.paymentGateway,
                    referenceId = authResult.receiptNumber
                )
            )

            _wallet.update {
                it.copy(balanceUzs = (it.balanceUzs - completedRide.totalCostUzs).coerceAtLeast(0L))
            }

            _activeRide.value = null
            isEndRideDialogVisible.value = false
            showNotification("Ride completed! Total: ${formatUzs(completedRide.totalCostUzs)} UZS. Receipt saved.")
        }
    }

    fun topUpWallet(amountUzs: Long, paymentType: PaymentType) {
        viewModelScope.launch {
            val result = paymentProvider.topUpWallet(amountUzs, paymentType, _wallet.value.cards.firstOrNull())
            if (result.success) {
                _wallet.update { it.copy(balanceUzs = it.balanceUzs + amountUzs) }
                db.transactionDao().insertTransaction(
                    TransactionEntity(
                        id = result.transactionId,
                        title = "Wallet Top-up via ${paymentType.name}",
                        amountUzs = amountUzs,
                        isDebit = false,
                        timestampMillis = System.currentTimeMillis(),
                        paymentMethod = paymentType.name,
                        referenceId = result.receiptNumber
                    )
                )
                showNotification("✅ Successfully added ${formatUzs(amountUzs)} UZS to Scoot Wallet!")
            }
        }
    }

    fun redeemPromo(code: String) {
        viewModelScope.launch {
            val res = paymentProvider.applyPromoCode(code)
            if (res.isSuccess) {
                val bonus = res.getOrThrow()
                _wallet.update {
                    it.copy(
                        balanceUzs = it.balanceUzs + bonus,
                        appliedPromos = it.appliedPromos + code.uppercase()
                    )
                }
                db.transactionDao().insertTransaction(
                    TransactionEntity(
                        id = "PROMO-${(1000..9999).random()}",
                        title = "Promo Code: ${code.uppercase()}",
                        amountUzs = bonus,
                        isDebit = false,
                        timestampMillis = System.currentTimeMillis(),
                        paymentMethod = "Scoot Promo",
                        referenceId = "PRM-${code.uppercase()}"
                    )
                )
                showNotification("🎉 Promo applied! +${formatUzs(bonus)} UZS added to balance!")
            } else {
                showNotification("Invalid or expired promo code: $code", isError = true)
            }
        }
    }

    // Back-office remote controls
    fun backOfficeRemoteUnlock(vehicleId: String) {
        viewModelScope.launch {
            vehicleGateway.unlockVehicle(vehicleId)
            showNotification("Remote Unlock sent to $vehicleId")
        }
    }

    fun backOfficeRemoteLock(vehicleId: String) {
        viewModelScope.launch {
            vehicleGateway.lockVehicle(vehicleId)
            showNotification("Remote Lock sent to $vehicleId")
        }
    }

    fun backOfficeBuzzer(vehicleId: String) {
        viewModelScope.launch {
            vehicleGateway.sendBuzzer(vehicleId)
            showNotification("Acoustic Buzzer triggered on $vehicleId")
        }
    }

    fun backOfficeSwapBattery(vehicleId: String) {
        viewModelScope.launch {
            vehicleGateway.swapBattery(vehicleId)
            showNotification("⚡ Battery swapped to 100% on $vehicleId")
        }
    }

    fun backOfficeToggleMaintenance(vehicleId: String, currentMaintenance: Boolean) {
        viewModelScope.launch {
            vehicleGateway.setMaintenance(vehicleId, !currentMaintenance)
            showNotification(if (!currentMaintenance) "$vehicleId marked for MAINTENANCE" else "$vehicleId returned to ACTIVE FLEET")
        }
    }

    fun backOfficeRelocate(vehicleId: String, lat: Double, lng: Double, name: String) {
        viewModelScope.launch {
            vehicleGateway.relocateVehicle(vehicleId, lat, lng, name)
            showNotification("$vehicleId relocated to $name")
        }
    }

    fun openTelemetry(vehicleId: String) {
        telemetryTargetVehicleId.value = vehicleId
        isTelemetryDialogVisible.value = true
    }

    fun formatUzs(amount: Long): String {
        return String.format("%,d", amount).replace(',', ' ')
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
