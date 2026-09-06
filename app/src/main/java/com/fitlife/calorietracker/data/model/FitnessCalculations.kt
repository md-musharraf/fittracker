package com.fitlife.calorietracker.data.model

import kotlin.math.roundToInt

data class BmiResult(
    val bmi: Double,
    val category: String,
    val colorHex: Long,
    val healthyMinWeightKg: Double,
    val healthyMaxWeightKg: Double,
    val advice: String
)

data class MacroRecommendation(
    val dailyCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val waterMl: Int
)

object FitnessCalculations {

    fun calculateBmi(weightKg: Double, heightCm: Double): BmiResult {
        val heightM = heightCm / 100.0
        val bmiRaw = if (heightM > 0) weightKg / (heightM * heightM) else 0.0
        val bmi = (bmiRaw * 10.0).roundToInt() / 10.0

        val minHealthy = ((18.5 * heightM * heightM) * 10.0).roundToInt() / 10.0
        val maxHealthy = ((24.9 * heightM * heightM) * 10.0).roundToInt() / 10.0

        val (category, colorHex, advice) = when {
            bmi < 18.5 -> Triple(
                "Underweight",
                0xFF38BDF8, // Cyan / Blue
                "Consider a clean caloric surplus with nutrient-dense foods and resistance training to build muscle mass safely."
            )
            bmi in 18.5..24.9 -> Triple(
                "Normal Weight",
                0xFF22C55E, // Green
                "Excellent! You are in a healthy body weight range. Focus on athletic performance, strength, and body composition."
            )
            bmi in 25.0..29.9 -> Triple(
                "Overweight",
                0xFFF59E0B, // Amber
                "A moderate caloric deficit combined with consistent weight training and cardio will help shed body fat while preserving muscle."
            )
            bmi in 30.0..34.9 -> Triple(
                "Obese Class I",
                0xFFF97316, // Orange
                "Target a steady 500 kcal deficit, prioritize lean protein to stay satiated, and incorporate daily walking and resistance workouts."
            )
            bmi in 35.0..39.9 -> Triple(
                "Obese Class II",
                0xFFEF4444, // Red
                "Structured portion control and gradual physical activity will yield great health and metabolic improvements."
            )
            else -> Triple(
                "Obese Class III",
                0xFFDC2626, // Deep Red
                "Consult with a healthcare or sports professional to tailor a safe, sustainable nutrition and training roadmap."
            )
        }

        return BmiResult(
            bmi = bmi,
            category = category,
            colorHex = colorHex,
            healthyMinWeightKg = minHealthy,
            healthyMaxWeightKg = maxHealthy,
            advice = advice
        )
    }

    /**
     * Basal Metabolic Rate using Mifflin-St Jeor formula
     */
    fun calculateBmr(weightKg: Double, heightCm: Double, age: Int, isMale: Boolean): Double {
        val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age)
        return if (isMale) base + 5 else base - 161
    }

    /**
     * Total Daily Energy Expenditure
     */
    fun calculateTdee(bmr: Double, activityLevel: ActivityLevel): Double {
        return bmr * activityLevel.multiplier
    }

    /**
     * Recommend complete nutrition target based on persona, goal, and profile
     */
    fun calculateRecommendations(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: String,
        activityLevel: ActivityLevel,
        persona: PersonaType,
        goal: GoalType
    ): MacroRecommendation {
        val isMale = gender.equals("Male", ignoreCase = true)
        val bmr = calculateBmr(weightKg, heightCm, age, isMale)
        val tdee = calculateTdee(bmr, activityLevel)

        val rawTargetCalories = (tdee + goal.calorieAdjustment).roundToInt()
        // Ensure healthy floor
        val minFloor = if (isMale) 1500 else 1200
        val dailyCalories = rawTargetCalories.coerceAtLeast(minFloor)

        val (protein, carbs, fat) = when (persona) {
            PersonaType.GYM_GUY -> {
                // Gym guy: 2.2g protein per kg body weight (optimal for muscle synthesis/retention)
                val targetProteinG = (weightKg * 2.2).roundToInt()
                val proteinKcal = targetProteinG * 4
                // Fat: 25% of total calories
                val fatKcal = (dailyCalories * 0.25)
                val targetFatG = (fatKcal / 9.0).roundToInt()
                // Remaining to carbs
                val remainingKcal = (dailyCalories - proteinKcal - (targetFatG * 9)).coerceAtLeast(200)
                val targetCarbsG = (remainingKcal / 4.0).roundToInt()
                Triple(targetProteinG, targetCarbsG, targetFatG)
            }
            PersonaType.SPORTS_ATHLETE -> {
                // Athlete: High carbohydrate fueling (50-55%), 1.8g protein/kg
                val targetProteinG = (weightKg * 1.8).roundToInt()
                val targetCarbsG = ((dailyCalories * 0.52) / 4.0).roundToInt()
                val targetFatG = (((dailyCalories - (targetProteinG * 4) - (targetCarbsG * 4)).coerceAtLeast(300)) / 9.0).roundToInt()
                Triple(targetProteinG, targetCarbsG, targetFatG)
            }
            PersonaType.NORMAL_HEALTH -> {
                // Normal healthy balanced: 25% Protein, 50% Carbs, 25% Fat
                val targetProteinG = ((dailyCalories * 0.25) / 4.0).roundToInt()
                val targetCarbsG = ((dailyCalories * 0.50) / 4.0).roundToInt()
                val targetFatG = ((dailyCalories * 0.25) / 9.0).roundToInt()
                Triple(targetProteinG, targetCarbsG, targetFatG)
            }
        }

        // Water recommendation: 35ml / kg + gym/sports bonus
        val waterBonus = when (persona) {
            PersonaType.GYM_GUY -> 1000
            PersonaType.SPORTS_ATHLETE -> 1200
            PersonaType.NORMAL_HEALTH -> 500
        }
        val targetWaterMl = ((weightKg * 35.0) + waterBonus).roundToInt().coerceIn(2000, 5000)

        return MacroRecommendation(
            dailyCalories = dailyCalories,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            waterMl = targetWaterMl
        )
    }
}
