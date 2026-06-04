package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class ParkingRepository(private val parkingDao: ParkingDao) {

    val allUsers: Flow<List<User>> = parkingDao.getAllUsers()
    val allVehicles: Flow<List<Vehicle>> = parkingDao.getAllVehicles()
    val allZones: Flow<List<Zone>> = parkingDao.getAllZones()
    val allSlots: Flow<List<ParkingSlot>> = parkingDao.getAllSlots()
    val allReservations: Flow<List<Reservation>> = parkingDao.getAllReservations()
    val allViolations: Flow<List<Violation>> = parkingDao.getAllViolations()
    val allPermits: Flow<List<Permit>> = parkingDao.getAllPermits()
    val allPayments: Flow<List<Payment>> = parkingDao.getAllPayments()
    val allReports: Flow<List<ReportSnapshot>> = parkingDao.getAllReports()

    fun getSlotsByZone(zoneId: String): Flow<List<ParkingSlot>> = parkingDao.getSlotsByZone(zoneId)
    fun getReservationsByUser(userId: String): Flow<List<Reservation>> = parkingDao.getReservationsByUser(userId)
    fun getPermitsByUser(userId: String): Flow<List<Permit>> = parkingDao.getPermitsByUser(userId)
    fun getVehiclesByOwner(userId: String): Flow<List<Vehicle>> = parkingDao.getVehiclesByOwner(userId)
    fun getPaymentsByUser(userId: String): Flow<List<Payment>> = parkingDao.getPaymentsByUser(userId)

    suspend fun insertUser(user: User) = parkingDao.insertUser(user)
    suspend fun insertVehicle(vehicle: Vehicle) = parkingDao.insertVehicle(vehicle)
    suspend fun insertViolation(violation: Violation) = parkingDao.insertViolation(violation)
    suspend fun insertPermit(permit: Permit) = parkingDao.insertPermit(permit)
    suspend fun updateSlot(slot: ParkingSlot) = parkingDao.updateSlot(slot)
    suspend fun updateReservation(reservation: Reservation) = parkingDao.updateReservation(reservation)
    suspend fun insertReport(report: ReportSnapshot) = parkingDao.insertReport(report)

    // Helper: Verify if user has a valid permit for a vehicle plate
    suspend fun verifyPermit(plate: String): Permit? {
        val permit = parkingDao.getPermitByPlate(plate)
        if (permit != null && permit.isValid && permit.expiryDate > System.currentTimeMillis()) {
            return permit
        }
        return null
    }

    // Helper: View available zones and slot counts
    // Seeding function to populate dummy data
    suspend fun seedDatabaseIfEmpty() {
        // Check if seeded already
        val zones = allZones.first()
        if (zones.isNotEmpty()) return

        // Seed Users
        val user1 = User("U101", "أحمد البنا (Ahmed ElBanna)", "STUDENT", "ahmed.albanna@univ.edu")
        val user2 = User("U202", "د. سارة الصباح (Dr. Sarah Sabah)", "STAFF", "sarah.sabah@univ.edu")
        val securityStaff = User("S505", "الرقيب خالد (Officer Khalid)", "STAFF", "khalid.security@univ.edu")
        
        parkingDao.insertUser(user1)
        parkingDao.insertUser(user2)
        parkingDao.insertUser(securityStaff)

        // Seed Vehicles
        val v1 = Vehicle(0, "ب ب ب ١٢٣٤ (BBB 1234)", "Tesla Model Y", "U101")
        val v2 = Vehicle(0, "س س س ٩٩٩٩ (SSS 9999)", "Lexus RX 350", "U202")
        val v3 = Vehicle(0, "ق ق ق ٤٣٢١ (QQQ 4321)", "Toyota Camry", null)
        
        parkingDao.insertVehicle(v1)
        parkingDao.insertVehicle(v2)
        parkingDao.insertVehicle(v3)

        // Seed Zones
        val z1 = Zone("ZONE_A", "Zone A (Engineering College)", "منطقة أ (كلية الهندسة)", "STUDENT", 12)
        val z2 = Zone("ZONE_B", "Zone B (Administration Staff)", "منطقة ب (المبنى الإداري)", "STAFF", 8)
        val z3 = Zone("ZONE_C", "Zone C (Central Plaza)", "منطقة ج (الساحة المركزية)", "ALL", 10)
        
        parkingDao.insertZones(listOf(z1, z2, z3))

        // Seed Parking Slots
        val slots = mutableListOf<ParkingSlot>()
        // Zone A (Engineering - Student) - 12 slots, initial occupied slots: A01, A04
        for (i in 1..12) {
            val numStr = i.toString().padStart(2, '0')
            val slotId = "A$numStr"
            val isOccupied = i == 1 || i == 4
            val vehiclePlate = when (i) {
                1 -> v1.licensePlate
                4 -> "أ د د ٧٧٧٧"
                else -> null
            }
            slots.add(ParkingSlot(slotId, "ZONE_A", i, isOccupied, vehiclePlate, "STUDENT"))
        }

        // Zone B (Administration Staff) - 8 slots, initial occupied slots: B02
        for (i in 1..8) {
            val numStr = i.toString().padStart(2, '0')
            val slotId = "B$numStr"
            val isOccupied = i == 2
            val vehiclePlate = if (i == 2) v2.licensePlate else null
            slots.add(ParkingSlot(slotId, "ZONE_B", i, isOccupied, vehiclePlate, "STAFF"))
        }

        // Zone C (Central Plaza - All) - 10 slots, initial occupied slots: C05
        for (i in 1..10) {
            val numStr = i.toString().padStart(2, '0')
            val slotId = "C$numStr"
            val isOccupied = i == 5
            val vehiclePlate = if (i == 5) v3.licensePlate else null
            slots.add(ParkingSlot(slotId, "ZONE_C", i, isOccupied, vehiclePlate, "ALL"))
        }
        
        parkingDao.insertSlots(slots)

        val now = System.currentTimeMillis()
        val oneHour = 3600_000L
        val oneDay = 24 * oneHour

        // Seed Permits
        val p1 = Permit(0, "U101", v1.licensePlate, "STUDENT_SEMESTER", now - oneDay * 10, now + oneDay * 100, true)
        val p2 = Permit(0, "U202", v2.licensePlate, "STAFF_ANNUAL", now - oneDay * 30, now + oneDay * 300, true)
        parkingDao.insertPermit(p1)
        parkingDao.insertPermit(p2)

        // Seed Reservations
        val r1 = Reservation(
            0,
            "U101",
            v1.licensePlate,
            "A01",
            "ZONE_A",
            now - oneHour,
            now + oneHour * 2,
            true,
            15.0,
            "ACTIVE"
        )
        val r2 = Reservation(
            0,
            "U202",
            v2.licensePlate,
            "B02",
            "ZONE_B",
            now - 2 * oneHour,
            now - 10 * 60_000, // finished 10 mins ago
            true,
            20.0,
            "COMPLETED"
        )
        parkingDao.insertReservation(r1)
        parkingDao.insertReservation(r2)

        // Seed Violations
        val vio1 = Violation(
            0,
            "أ د د ٧٧٧٧ (ADD 7777)",
            "NO_PERMIT",
            100.0,
            false,
            now - 3 * oneHour,
            "ZONE_A - A04",
            "مركبة متوقفة في منطقة الطلاب بدون تصريح جامعي"
        )
        val vio2 = Violation(
            0,
            "ر س ط ٨٨٨٨ (RST 8888)",
            "WRONG_ZONE",
            100.0,
            true,
            now - 12 * oneHour,
            "ZONE_B - B05",
            "تم دفع المخالفة فورأ عبر البوابة الإلكترونية"
        )
        parkingDao.insertViolation(vio1)
        parkingDao.insertViolation(vio2)

        // Seed Payments
        val pay1 = Payment(0, "U101", 15.0, "RESERVATION", 1, now - oneHour, "APPLE_PAY")
        val pay2 = Payment(0, "U202", 20.0, "RESERVATION", 2, now - 2 * oneHour, "CREDIT_CARD")
        val pay3 = Payment(0, "U555", 100.0, "VIOLATION", 2, now - 11 * oneHour, "CREDIT_CARD")
        parkingDao.insertPayment(pay1)
        parkingDao.insertPayment(pay2)
        parkingDao.insertPayment(pay3)

        // Seed Initial Report Snapshot
        val initReport = ReportSnapshot(
            0,
            now,
            "نظام مراقبة الأمن (Auto)",
            13.3, // (1+1+1+1) / 30 slots
            1,
            1,
            135.0
        )
        parkingDao.insertReport(initReport)
    }

    // Reservation business logic
    suspend fun makeReservation(
        userId: String,
        vehiclePlate: String,
        slotId: String,
        zoneId: String,
        durationHours: Int,
        feePerHour: Double
    ): Boolean {
        val slot = parkingDao.getSlotById(slotId) ?: return false
        if (slot.isOccupied) return false

        val now = System.currentTimeMillis()
        val endTime = now + (durationHours * 3600_000L)
        val fee = durationHours * feePerHour

        // Create reservation
        val reservation = Reservation(
            userId = userId,
            vehiclePlate = vehiclePlate,
            slotId = slotId,
            zoneId = zoneId,
            startTime = now,
            endTime = endTime,
            isPaid = false,
            feeAmount = fee,
            status = "ACTIVE"
        )

        val resId = parkingDao.insertReservation(reservation).toInt()

        // Update Slot status
        val updatedSlot = slot.copy(isOccupied = true, occupiedByVehiclePlate = vehiclePlate)
        parkingDao.updateSlot(updatedSlot)

        return true
    }

    // Extend parking reservation
    suspend fun extendReservation(reservationId: Int, additionalHours: Int, feePerHour: Double): Boolean {
        val reservations = allReservations.first()
        val reservation = reservations.find { it.id == reservationId } ?: return false
        if (reservation.status != "ACTIVE") return false

        val newEndTime = reservation.endTime + (additionalHours * 3600_000L)
        val additionalFee = additionalHours * feePerHour
        val updatedRes = reservation.copy(
            endTime = newEndTime,
            feeAmount = reservation.feeAmount + additionalFee,
            isPaid = false // will need to pay the difference
        )
        parkingDao.updateReservation(updatedRes)
        return true
    }

    // Register vehicle exit (Check-out)
    suspend fun registerVehicleExit(slotId: String): Boolean {
        val slot = parkingDao.getSlotById(slotId) ?: return false
        if (!slot.isOccupied) return false

        // Mark corresponding ACTIVE reservations as COMPLETED
        val reservations = allReservations.first()
        val activeRes = reservations.find { it.slotId == slotId && it.status == "ACTIVE" }
        if (activeRes != null) {
            val updatedRes = activeRes.copy(status = "COMPLETED", endTime = System.currentTimeMillis())
            parkingDao.updateReservation(updatedRes)
        }

        // Clean slot
        val updatedSlot = slot.copy(isOccupied = false, occupiedByVehiclePlate = null)
        parkingDao.updateSlot(updatedSlot)
        return true
    }

    // Register vehicle entry (Check-in)
    suspend fun registerVehicleEntry(slotId: String, vehiclePlate: String): Boolean {
        val slot = parkingDao.getSlotById(slotId) ?: return false
        if (slot.isOccupied) return false

        val updatedSlot = slot.copy(isOccupied = true, occupiedByVehiclePlate = vehiclePlate)
        parkingDao.updateSlot(updatedSlot)
        return true
    }

    // Capture dynamic system report snapshot
    suspend fun generateCurrentReport(operatorName: String): ReportSnapshot {
        val slots = allSlots.first()
        val activeRes = allReservations.first().filter { it.status == "ACTIVE" }
        val activeViolations = allViolations.first().filter { !it.isPaid }
        val paymentsList = allPayments.first()

        val occupiedCount = slots.count { it.isOccupied }
        val occupancyPct = if (slots.isNotEmpty()) {
            (occupiedCount.toDouble() / slots.size.toDouble()) * 100.0
        } else {
            0.0
        }

        val totalRev = paymentsList.sumOf { it.amount }

        val snapshot = ReportSnapshot(
            timestamp = System.currentTimeMillis(),
            generatedBy = operatorName,
            totalOccupancyPercent = occupancyPct,
            activeReservationsCount = activeRes.size,
            activeViolationsCount = activeViolations.size,
            totalRevenue = totalRev
        )

        parkingDao.insertReport(snapshot)
        return snapshot
    }

    // Pay reservation / violation
    suspend fun processItemPayment(
        userId: String,
        amount: Double,
        type: String,
        referenceId: Int,
        method: String
    ): Boolean {
        // Create Payment
        val payment = Payment(
            userId = userId,
            amount = amount,
            paymentType = type,
            referenceId = referenceId,
            timestamp = System.currentTimeMillis(),
            paymentMethod = method
        )
        parkingDao.insertPayment(payment)

        // Update targets
        if (type == "RESERVATION") {
            val reservations = allReservations.first()
            val res = reservations.find { it.id == referenceId }
            if (res != null) {
                parkingDao.updateReservation(res.copy(isPaid = true))
            }
        } else if (type == "VIOLATION") {
            val violations = allViolations.first()
            val vio = violations.find { it.id == referenceId }
            if (vio != null) {
                parkingDao.updateViolation(vio.copy(isPaid = true))
            }
        }
        return true
    }
}
