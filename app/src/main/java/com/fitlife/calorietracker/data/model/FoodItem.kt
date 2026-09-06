package com.fitlife.calorietracker.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foods",
    indices = [
        Index(value = ["category"]),
        Index(value = ["name"])
    ]
)
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val brand: String = "Generic",
    val servingSize: String = "100g",
    val servingWeightGrams: Double = 100.0,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double = 0.0,
    val category: String = "Staples", // "Proteins", "Carbs", "Fats", "Gym Staples", "Fruits & Veggies", "Snacks"
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
) {
    @Ignore
    constructor(
        name: String,
        brand: String = "Generic",
        servingSize: String = "100g",
        servingWeightGrams: Double = 100.0,
        calories: Double,
        proteinGrams: Double,
        carbsGrams: Double,
        fatGrams: Double,
        fiberGrams: Double = 0.0,
        category: String = "Staples",
        isCustom: Boolean = false,
        isFavorite: Boolean = false
    ) : this(
        id = 0,
        name = name,
        brand = brand,
        servingSize = servingSize,
        servingWeightGrams = servingWeightGrams,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        fiberGrams = fiberGrams,
        category = category,
        isCustom = isCustom,
        isFavorite = isFavorite
    )
}
