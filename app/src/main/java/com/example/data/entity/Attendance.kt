package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
    indices = [Index(value = ["worker_id", "date"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "worker_id")
    val workerId: Int,
    val date: String, // "YYYY-MM-DD" e.g., "2026-09-21"
    val month: Int,   // 1 - 12
    val year: Int,    // e.g., 2026
    val status: String, // "Present", "Absent", "Half"
    @ColumnInfo(name = "overtime", defaultValue = "0.0")
    val overtime: Double = 0.0 // Overtime in hours
) {
    val hajiraUnits: Double
        get() = when (status) {
            "Present", "1.0" -> 1.0
            "OneAndHalf", "1.5" -> 1.5
            "Double", "2.0" -> 2.0
            "Half", "0.5" -> 0.5
            else -> 0.0
        }
}
