package com.fitlife.calorietracker.data.model

import java.time.LocalTime

object SmartDefaults {
    /**
     * Auto-detect the current meal type based on time of day
     * Before 10 AM → BREAKFAST
     * 10 AM - 2 PM → LUNCH  
     * 2 PM - 5 PM → SNACK
     * 5 PM onwards → DINNER
     */
    fun detectMealType(): MealType {
        val hour = LocalTime.now().hour
        return when {
            hour < 10 -> MealType.BREAKFAST
            hour < 14 -> MealType.LUNCH
            hour < 17 -> MealType.SNACK
            else -> MealType.DINNER
        }
    }

    /**
     * Get meal-appropriate greeting text
     */
    fun getMealGreeting(): String {
        val hour = LocalTime.now().hour
        return when {
            hour < 12 -> "Good Morning! 🌅"
            hour < 17 -> "Good Afternoon! ☀️"
            else -> "Good Evening! 🌙"
        }
    }

    /**
     * Get motivational tip based on time and context
     */
    fun getDailyTip(caloriesConsumed: Double, calorieGoal: Int, proteinConsumed: Double, proteinGoal: Int): String {
        val hour = LocalTime.now().hour
        val caloriePercent = if (calorieGoal > 0) (caloriesConsumed / calorieGoal * 100).toInt() else 0
        val proteinPercent = if (proteinGoal > 0) (proteinConsumed / proteinGoal * 100).toInt() else 0
        
        return when {
            hour < 10 && caloriesConsumed < 100 -> "💪 Start your day strong — a protein-rich breakfast boosts metabolism!"
            hour in 10..13 && caloriePercent < 30 -> "🍽️ Don't skip lunch! Your body needs fuel for the afternoon."
            hour in 14..16 && proteinPercent < 50 -> "🥛 Running low on protein — try a shake or some paneer!"
            caloriePercent > 90 && hour < 18 -> "⚠️ Almost at your calorie limit — stick to veggies for the rest of the day."
            caloriePercent > 100 -> "📊 You've exceeded your target by ${caloriePercent - 100}% — a walk can help burn some off!"
            hour >= 20 && caloriePercent < 70 -> "🌙 Light on calories today — consider a balanced dinner."
            proteinPercent >= 100 -> "✅ Great job! You've hit your protein target today!"
            else -> "📈 Keep tracking — consistency is the key to results!"
        }
    }
    
    /**
     * Auto-calculate macros from calorie input using standard ratios
     * Useful for Quick Add when user only enters calories
     */
    fun estimateMacrosFromCalories(calories: Double): Triple<Double, Double, Double> {
        // Default balanced split: 30% protein, 40% carbs, 30% fat
        val protein = (calories * 0.30) / 4.0  // 4 cal per gram protein
        val carbs = (calories * 0.40) / 4.0    // 4 cal per gram carbs
        val fat = (calories * 0.30) / 9.0      // 9 cal per gram fat
        return Triple(
            (protein * 10).toInt() / 10.0,
            (carbs * 10).toInt() / 10.0,
            (fat * 10).toInt() / 10.0
        )
    }
}
