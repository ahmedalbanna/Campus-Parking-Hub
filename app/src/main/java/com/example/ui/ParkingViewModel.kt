package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppLanguage { EN, AR }
enum class UserRole { USER, SECURITY }

class ParkingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ParkingDatabase.getDatabase(application)
    private val repository = ParkingRepository(database.parkingDao())

    // App Global States
    private val _currentLanguage = MutableStateFlow(AppLanguage.EN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.USER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Active User State
    private val _currentUserId = MutableStateFlow("U101")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Selected Zone State
    private val _selectedZoneId = MutableStateFlow("ZONE_A")
    val selectedZoneId: StateFlow<String> = _selectedZoneId.asStateFlow()

    // Success / Error Message banner State
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _arUiMessage = MutableStateFlow<String?>(null)
    val arUiMessage: StateFlow<String?> = _arUiMessage.asStateFlow()

    // Room DB Flow States
    val zones: StateFlow<List<Zone>> = repository.allZones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val slots: StateFlow<List<ParkingSlot>> = repository.allSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reservations: StateFlow<List<Reservation>> = repository.allReservations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val violations: StateFlow<List<Violation>> = repository.allViolations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val permits: StateFlow<List<Permit>> = repository.allPermits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<ReportSnapshot>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived states filtered by current user
    val currentUserVehicles: StateFlow<List<Vehicle>> = combine(_currentUserId, repository.allVehicles) { userId, vehicles ->
        vehicles.filter { it.ownerId == userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserReservations: StateFlow<List<Reservation>> = combine(_currentUserId, repository.allReservations) { userId, res ->
        res.filter { it.userId == userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserPermits: StateFlow<List<Permit>> = combine(_currentUserId, repository.allPermits) { userId, p ->
        p.filter { it.userId == userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserPayments: StateFlow<List<Payment>> = combine(_currentUserId, repository.allPayments) { userId, p ->
        p.filter { it.userId == userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        // Set user simulation contexts depending on role selection
        if (role == UserRole.SECURITY) {
            _currentUserId.value = "S505"
        } else {
            _currentUserId.value = "U101"
        }
    }

    fun switchUserSimulation(userId: String) {
        _currentUserId.value = userId
    }

    fun selectZone(zoneId: String) {
        _selectedZoneId.value = zoneId
    }

    fun clearMessage() {
        _uiMessage.value = null
        _arUiMessage.value = null
    }

    private fun postMessage(en: String, ar: String) {
        _uiMessage.value = en
        _arUiMessage.value = ar
    }

    // --- Action implementations ---

    // 1. Reserve slot
    fun reserveSlot(slotId: String, zoneId: String, vehiclePlate: String, durationHours: Int) {
        viewModelScope.launch {
            val success = repository.makeReservation(
                userId = _currentUserId.value,
                vehiclePlate = vehiclePlate,
                slotId = slotId,
                zoneId = zoneId,
                durationHours = durationHours,
                feePerHour = 5.0
            )
            if (success) {
                postMessage(
                    "Reserved Parking Slot $slotId successfully for $durationHours hours!",
                    "تم حجز موقف السيارات $slotId بنجاح لمدة $durationHours ساعات!"
                )
            } else {
                postMessage(
                    "Parking Slot $slotId is already occupied or unavailable.",
                    "موقف السيارات $slotId مشغول حالياً أو غير متاح."
                )
            }
        }
    }

    // 2. Extend parking time
    fun extendParking(reservationId: Int, additionalHours: Int) {
        viewModelScope.launch {
            val success = repository.extendReservation(reservationId, additionalHours, 5.0)
            if (success) {
                postMessage(
                    "Extended parking reservation by $additionalHours hours. Please complete extra fee payment.",
                    "تم تمديد فترة الحجز بمقدار $additionalHours ساعات. يرجى سداد الرسوم الإضافية."
                )
            } else {
                postMessage(
                    "Failed to extend reservation. It may not be active.",
                    "فشل تمديد حجز الموقف. قد لا يكون الحجز نشطاً."
                )
            }
        }
    }

    // 3. Register vehicle and apply for permit
    fun addVehicleAndApplyPermit(plate: String, model: String, permitType: String) {
        viewModelScope.launch {
            if (plate.isBlank() || model.isBlank()) {
                postMessage("Plate number and vehicle model cannot be empty.", "لا يمكن أن يكون رقم اللوح أو طراز المركبة فارغاً.")
                return@launch
            }

            // Register Vehicle
            val newVehicle = Vehicle(ownerId = _currentUserId.value, licensePlate = plate, model = model)
            repository.insertVehicle(newVehicle)

            // Submit permit application automatically approved for simulation
            val now = System.currentTimeMillis()
            val expiry = now + (30L * 24L * 3600_000L) // 30 days
            val permit = Permit(
                userId = _currentUserId.value,
                licensePlate = plate,
                permitType = permitType,
                issueDate = now,
                expiryDate = expiry,
                isValid = true
            )
            repository.insertPermit(permit)

            postMessage(
                "Registered vehicle and active permit issued successfully for plate: $plate",
                "تم تسجيل المركبة وإصدار التصريح بنجاح للوحة: $plate"
            )
        }
    }

    // 4. Pay parking fee / violation fine
    fun payFee(referenceId: Int, type: String, amount: Double, method: String) {
        viewModelScope.launch {
            val success = repository.processItemPayment(
                userId = _currentUserId.value,
                amount = amount,
                type = type,
                referenceId = referenceId,
                method = method
            )
            if (success) {
                postMessage(
                    "Paid $$amount successfully via $method!",
                    "تم دفع $$amount بنجاح عبر $method!"
                )
            } else {
                postMessage("Payment failed. Please try again.", "عملية الدفع فشلت.يرجى المحاولة مرة أخرى.")
            }
        }
    }

    // 5. Security: Register Check-In (Vehicle Entry)
    fun securityCheckIn(slotId: String, plate: String) {
        viewModelScope.launch {
            if (plate.isBlank()) {
                postMessage("License plate is required for Entry registration.", "رقم اللوحة مطلوب لتسجيل الدخول.")
                return@launch
            }
            val success = repository.registerVehicleEntry(slotId, plate)
            if (success) {
                // Verify permit automatically
                val permit = repository.verifyPermit(plate)
                if (permit == null) {
                    postMessage(
                        "Checked-in vehicle $plate in Slot $slotId. WARNING: This vehicle does not have a valid campus permit!",
                        "تم تسجيل دخول المركبة $plate للموقف $slotId. تحذير: هذه المركبة ليس لديها تصريح جامعي صالح!"
                    )
                } else {
                    postMessage(
                        "Checked-in vehicle $plate in Slot $slotId. Permit verified: ${permit.permitType}.",
                        "تم تسجيل دخول المركبة $plate للموقف $slotId. تم التحقق من التصريح: ${permit.permitType}."
                    )
                }
            } else {
                postMessage("Failed to register check-in. Slot may be occupied.", "فشل تسجيل الدخول. قد يكون الموقف مشغولاً بالفعل.")
            }
        }
    }

    // 6. Security: Register Check-Out (Vehicle Exit)
    fun securityCheckOut(slotId: String) {
        viewModelScope.launch {
            val success = repository.registerVehicleExit(slotId)
            if (success) {
                postMessage(
                    "Successfully registered vehicle Exit for Slot $slotId.",
                    "تم تسجيل خروج المركبة من الموقف $slotId بنجاح."
                )
            } else {
                postMessage("Slot is already empty or invalid.", "الموقف فارغ بالفعل أو غير صالح.")
            }
        }
    }

    // 7. Security: Issue Parking Violation
    fun issueViolation(plate: String, type: String, amount: Double, location: String, notes: String) {
        viewModelScope.launch {
            if (plate.isBlank()) {
                postMessage("Plate number is required to issue a violation.", "رقم اللوحة مطلوب لإصدار المخالفة.")
                return@launch
            }
            val violation = Violation(
                vehiclePlate = plate,
                violationType = type,
                fineAmount = amount,
                isPaid = false,
                timestamp = System.currentTimeMillis(),
                location = location,
                notes = if (notes.isBlank()) null else notes
            )
            repository.insertViolation(violation)
            postMessage(
                "Violation ($type) of $$amount issued successfully to vehicle $plate.",
                "تم إصدار مخالفة ($type) بمبلغ $$amount بنجاح ضد المركبة $plate."
            )
        }
    }

    // 8. Security: Generate Occupancy Report Snapchat
    fun generateOccupancyReport() {
        viewModelScope.launch {
            val operator = "Officer S505"
            val snapshot = repository.generateCurrentReport(operator)
            postMessage(
                "New Occupancy Report snapshot generated with total occupancy of ${"%.1f".format(snapshot.totalOccupancyPercent)}%!",
                "تم إنشاء تقرير نسبة الإشغال بنسبة ${"%.1f".format(snapshot.totalOccupancyPercent)}% بنجاح!"
            )
        }
    }
}

class ParkingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParkingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParkingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
