package com.fitlife.calorietracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_logs",
    indices = [
        Index(value = ["date"])
    ]
)
data class WeightLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val weightKg: Double,
    val bmi: Double,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
