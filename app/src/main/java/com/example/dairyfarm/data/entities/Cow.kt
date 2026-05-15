package com.example.dairyfarm.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cows")
data class Cow(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String = "", // Added for user isolation
    val name: String,
    val tagNumber: String,
    val isActive: Boolean = true
)
