package com.fitlife.calorietracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_logs",
    indices = [
        Index(value = ["date"])
    ]
)
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val exerciseName: String,
    val durationMinutes: Int,
    val intensity: String = "Moderate", // "Light", "Moderate", "High", "Extreme"
    val caloriesBurned: Double,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
