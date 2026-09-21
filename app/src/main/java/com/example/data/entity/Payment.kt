package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "worker_id")
    val workerId: Int,
    val date: String, // "YYYY-MM-DD"
    @ColumnInfo(name = "amount_taken")
    val amountTaken: Double,
    val note: String = ""
)
