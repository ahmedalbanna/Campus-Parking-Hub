package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Vehicle::class,
        Zone::class,
        ParkingSlot::class,
        Reservation::class,
        Violation::class,
        Permit::class,
        Payment::class,
        ReportSnapshot::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ParkingDatabase : RoomDatabase() {
    abstract fun parkingDao(): ParkingDao

    companion object {
        @Volatile
        private var INSTANCE: ParkingDatabase? = null

        fun getDatabase(context: Context): ParkingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkingDatabase::class.java,
                    "parking_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
