package com.example.dairyfarm.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "income_entries")
data class IncomeEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String = "", // Added for user isolation
    val cowId: Long? = null, // Nullable if entry is not cow-specific
    val date: Long = System.currentTimeMillis(),
    val liters: Double,
    val pricePerLiter: Double = 0.0,
    val fatPercentage: Double,
    val totalAmount: Double
)
