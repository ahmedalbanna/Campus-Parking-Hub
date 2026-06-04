package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val role: String, // "STUDENT", "STAFF"
    val email: String
)

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val licensePlate: String,
    val model: String,
    val ownerId: String? // User ID
)

@Entity(tableName = "zones")
data class Zone(
    @PrimaryKey val id: String, // "ZONE_A", "ZONE_B", "ZONE_C", etc.
    val name: String,
    val nameAr: String,
    val type: String, // "STUDENT", "STAFF", "ALL"
    val totalSlots: Int
)

@Entity(tableName = "parking_slots")
data class ParkingSlot(
    @PrimaryKey val id: String, // e.g. "A01", "A02", "B01"
    val zoneId: String,
    val number: Int,
    val isOccupied: Boolean,
    val occupiedByVehiclePlate: String?,
    val type: String // "STUDENT", "STAFF", "ALL"
)

@Entity(tableName = "reservations")
data class Reservation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val vehiclePlate: String,
    val slotId: String,
    val zoneId: String,
    val startTime: Long,
    val endTime: Long,
    val isPaid: Boolean,
    val feeAmount: Double,
    val status: String // "ACTIVE", "COMPLETED", "EXPIRED", "CANCELLED"
)

@Entity(tableName = "violations")
data class Violation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehiclePlate: String,
    val violationType: String, // "NO_PERMIT", "OVERTIME", "WRONG_ZONE"
    val fineAmount: Double,
    val isPaid: Boolean,
    val timestamp: Long,
    val location: String, // Zone name or spot id
    val notes: String?
)

@Entity(tableName = "permits")
data class Permit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val licensePlate: String,
    val permitType: String, // "STUDENT_SEMESTER", "STAFF_ANNUAL", "VISITOR_DAILY"
    val issueDate: Long,
    val expiryDate: Long,
    val isValid: Boolean
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val amount: Double,
    val paymentType: String, // "RESERVATION", "VIOLATION", "PERMIT"
    val referenceId: Int, // id of reservation or violation or permit
    val timestamp: Long,
    val paymentMethod: String // "CREDIT_CARD", "APPLE_PAY", "UNIV_ACCOUNT"
)

@Entity(tableName = "report_snapshots")
data class ReportSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val generatedBy: String,
    val totalOccupancyPercent: Double,
    val activeReservationsCount: Int,
    val activeViolationsCount: Int,
    val totalRevenue: Double
)
