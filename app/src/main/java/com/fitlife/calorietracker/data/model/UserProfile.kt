package com.fitlife.calorietracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PersonaType(val title: String, val subtitle: String) {
    GYM_GUY("Gym Guy / Bodybuilding", "Focus on high protein, hypertrophy & lean bulk/cut"),
    SPORTS_ATHLETE("Sports Person / Athlete", "High energy, carb fueling, endurance & hydration"),
    NORMAL_HEALTH("Everyday Health & Fitness", "Balanced nutrition, steady fat loss & healthy lifestyle")
}

enum class GoalType(val displayName: String, val calorieAdjustment: Int) {
    LOSE_WEIGHT_AGGRESSIVE("Aggressive Cut (-750 kcal)", -750),
    LOSE_WEIGHT_MODERATE("Moderate Fat Loss (-500 kcal)", -500),
    LOSE_WEIGHT_SLOW("Slow & Sustainable Cut (-250 kcal)", -250),
    MAINTAIN_WEIGHT("Maintain Weight (0 kcal)", 0),
    LEAN_BULK("Lean Muscle Gain (+250 kcal)", 250),
    HEAVY_BULK("Muscle & Strength Bulk (+500 kcal)", 500)
}

enum class ActivityLevel(val displayName: String, val multiplier: Double) {
    SEDENTARY("Sedentary (Little or no exercise)", 1.2),
    LIGHTLY_ACTIVE("Lightly Active (1-3 days/wk)", 1.375),
    MODERATELY_ACTIVE("Moderately Active (3-5 days/wk)", 1.55),
    VERY_ACTIVE("Very Active (6-7 days gym/sports)", 1.725),
    EXTREMELY_ACTIVE("Extremely Active (Athletes / 2x/day)", 1.9)
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Athlete",
    val gender: String = "Male", // "Male", "Female"
    val age: Int = 26,
    val heightCm: Double = 175.0,
    val currentWeightKg: Double = 75.0,
    val targetWeightKg: Double = 72.0,
    val persona: PersonaType = PersonaType.GYM_GUY,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
    val goalType: GoalType = GoalType.LOSE_WEIGHT_MODERATE,
    val dailyCalorieGoal: Int = 2200,
    val proteinGoalGrams: Int = 165,
    val carbsGoalGrams: Int = 220,
    val fatGoalGrams: Int = 60,
    val dailyWaterGoalMl: Int = 3500
)
