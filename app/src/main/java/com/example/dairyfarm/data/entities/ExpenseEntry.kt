package com.example.dairyfarm.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_entries")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String = "", // Added for user isolation
    val cowId: Long? = null, // Optional if we want to attribute expense to a specific cow
    val date: Long = System.currentTimeMillis(),
    val category: String, // Fodder, Medical, Labour, Electricity, etc.
    val amount: Double,
    val notes: String
)
