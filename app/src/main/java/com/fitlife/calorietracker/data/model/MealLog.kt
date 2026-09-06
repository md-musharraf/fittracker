package com.fitlife.calorietracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_logs",
    indices = [
        Index(value = ["date"]),
        Index(value = ["timestamp"]),
        Index(value = ["mealType"])
    ]
)
data class MealLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val mealType: String, // "BREAKFAST", "LUNCH", "DINNER", "SNACK"
    val foodId: Long? = null,
    val foodName: String,
    val servingCount: Double = 1.0,
    val servingUnit: String = "serving",
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val timestamp: Long = System.currentTimeMillis()
)
