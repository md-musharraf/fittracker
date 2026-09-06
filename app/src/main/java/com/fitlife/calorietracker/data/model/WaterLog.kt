package com.fitlife.calorietracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey
    val date: String, // "yyyy-MM-dd"
    val amountMl: Int,
    val targetMl: Int = 3000
)
