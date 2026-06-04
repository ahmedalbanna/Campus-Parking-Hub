package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingDao {
    // Users
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Vehicles
    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE ownerId = :userId")
    fun getVehiclesByOwner(userId: String): Flow<List<Vehicle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    // Zones
    @Query("SELECT * FROM zones")
    fun getAllZones(): Flow<List<Zone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZone(zone: Zone)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZones(zones: List<Zone>)

    // Parking Slots
    @Query("SELECT * FROM parking_slots")
    fun getAllSlots(): Flow<List<ParkingSlot>>

    @Query("SELECT * FROM parking_slots WHERE zoneId = :zoneId")
    fun getSlotsByZone(zoneId: String): Flow<List<ParkingSlot>>

    @Query("SELECT * FROM parking_slots WHERE id = :slotId LIMIT 1")
    suspend fun getSlotById(slotId: String): ParkingSlot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: ParkingSlot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<ParkingSlot>)

    @Update
    suspend fun updateSlot(slot: ParkingSlot)

    // Reservations
    @Query("SELECT * FROM reservations ORDER BY startTime DESC")
    fun getAllReservations(): Flow<List<Reservation>>

    @Query("SELECT * FROM reservations WHERE userId = :userId ORDER BY startTime DESC")
    fun getReservationsByUser(userId: String): Flow<List<Reservation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: Reservation): Long

    @Update
    suspend fun updateReservation(reservation: Reservation)

    // Violations
    @Query("SELECT * FROM violations ORDER BY timestamp DESC")
    fun getAllViolations(): Flow<List<Violation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViolation(violation: Violation)

    @Update
    suspend fun updateViolation(violation: Violation)

    // Permits
    @Query("SELECT * FROM permits ORDER BY expiryDate DESC")
    fun getAllPermits(): Flow<List<Permit>>

    @Query("SELECT * FROM permits WHERE userId = :userId")
    fun getPermitsByUser(userId: String): Flow<List<Permit>>

    @Query("SELECT * FROM permits WHERE licensePlate = :plate LIMIT 1")
    suspend fun getPermitByPlate(plate: String): Permit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermit(permit: Permit)

    // Payments
    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPaymentsByUser(userId: String): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    // Report Snapshots
    @Query("SELECT * FROM report_snapshots ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportSnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportSnapshot)
}
