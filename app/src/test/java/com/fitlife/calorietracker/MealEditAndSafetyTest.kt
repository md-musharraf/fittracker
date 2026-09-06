package com.fitlife.calorietracker

import com.fitlife.calorietracker.data.model.MealLog
import com.fitlife.calorietracker.data.model.MealType
import com.fitlife.calorietracker.data.model.ValidationUtils
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.roundToInt

class MealEditAndSafetyTest {

    @Test
    fun testMealPortionScaling() {
        val original = MealLog(
            id = 1,
            date = "2026-09-06",
            mealType = MealType.BREAKFAST.name,
            foodName = "Oats & Peanut Butter",
            servingCount = 1.0,
            servingUnit = "1 bowl (60g)",
            calories = 300.0,
            proteinGrams = 12.0,
            carbsGrams = 45.0,
            fatGrams = 8.0
        )

        // Scale by 2.0x (double portion)
        val newMultiplier = 2.0
        val scaled = original.copy(
            servingCount = newMultiplier,
            calories = (original.calories / original.servingCount * newMultiplier),
            proteinGrams = (original.proteinGrams / original.servingCount * newMultiplier),
            carbsGrams = (original.carbsGrams / original.servingCount * newMultiplier),
            fatGrams = (original.fatGrams / original.servingCount * newMultiplier)
        )

        assertEquals(600.0, scaled.calories, 0.01)
        assertEquals(24.0, scaled.proteinGrams, 0.01)
        assertEquals(90.0, scaled.carbsGrams, 0.01)
        assertEquals(16.0, scaled.fatGrams, 0.01)
        assertEquals(2.0, scaled.servingCount, 0.01)
    }

    @Test
    fun testMealPortionHalfScaling() {
        val original = MealLog(
            id = 2,
            date = "2026-09-06",
            mealType = MealType.LUNCH.name,
            foodName = "Chicken Curry",
            servingCount = 1.0,
            servingUnit = "1 cup",
            calories = 260.0,
            proteinGrams = 28.0,
            carbsGrams = 8.0,
            fatGrams = 12.0
        )

        // Scale down to 0.5x
        val newMultiplier = 0.5
        val scaled = original.copy(
            servingCount = newMultiplier,
            calories = (original.calories / original.servingCount * newMultiplier),
            proteinGrams = (original.proteinGrams / original.servingCount * newMultiplier),
            carbsGrams = (original.carbsGrams / original.servingCount * newMultiplier),
            fatGrams = (original.fatGrams / original.servingCount * newMultiplier)
        )

        assertEquals(130.0, scaled.calories, 0.01)
        assertEquals(14.0, scaled.proteinGrams, 0.01)
        assertEquals(4.0, scaled.carbsGrams, 0.01)
        assertEquals(6.0, scaled.fatGrams, 0.01)
    }

    @Test
    fun testSwitchingMealType() {
        val loggedByMistake = MealLog(
            id = 5,
            date = "2026-09-06",
            mealType = MealType.BREAKFAST.name, // Logged as breakfast by accident
            foodName = "Grilled Chicken Breast",
            servingCount = 1.0,
            servingUnit = "150g",
            calories = 240.0,
            proteinGrams = 46.0,
            carbsGrams = 0.0,
            fatGrams = 5.0
        )

        // Move to Lunch
        val fixed = loggedByMistake.copy(mealType = MealType.LUNCH.name)

        assertEquals(MealType.LUNCH.name, fixed.mealType)
        assertEquals(240.0, fixed.calories, 0.01)
        assertEquals(46.0, fixed.proteinGrams, 0.01)
    }

    @Test
    fun testDuplicateDetectionLogic() {
        val now = System.currentTimeMillis()
        val recentLogTime = now - 15_000 // 15 seconds ago
        val oldLogTime = now - 90_000 // 90 seconds ago

        val thresholdMillis = 60_000 // 60s

        val isRecent = (now - recentLogTime) < thresholdMillis
        val isOld = (now - oldLogTime) < thresholdMillis

        assertTrue("Should detect 15s ago as duplicate candidate", isRecent)
        assertFalse("Should not flag 90s ago as duplicate", isOld)
    }

    @Test
    fun testMealDistributionCalculation() {
        val meals = listOf(
            MealLog(id = 1, date = "2026-09-06", mealType = MealType.BREAKFAST.name, foodName = "Eggs", calories = 300.0, proteinGrams = 20.0, carbsGrams = 2.0, fatGrams = 18.0),
            MealLog(id = 2, date = "2026-09-06", mealType = MealType.LUNCH.name, foodName = "Rice & Chicken", calories = 600.0, proteinGrams = 45.0, carbsGrams = 70.0, fatGrams = 12.0),
            MealLog(id = 3, date = "2026-09-06", mealType = MealType.DINNER.name, foodName = "Paneer Tikka", calories = 300.0, proteinGrams = 25.0, carbsGrams = 10.0, fatGrams = 15.0)
        )

        val total = meals.sumOf { it.calories } // 1200 kcal
        val bfPct = ((meals.filter { it.mealType == MealType.BREAKFAST.name }.sumOf { it.calories } / total) * 100).roundToInt()
        val lunchPct = ((meals.filter { it.mealType == MealType.LUNCH.name }.sumOf { it.calories } / total) * 100).roundToInt()
        val dinnerPct = ((meals.filter { it.mealType == MealType.DINNER.name }.sumOf { it.calories } / total) * 100).roundToInt()
        val snackPct = ((meals.filter { it.mealType == MealType.SNACK.name }.sumOf { it.calories } / total) * 100).roundToInt()

        assertEquals(25, bfPct)
        assertEquals(50, lunchPct)
        assertEquals(25, dinnerPct)
        assertEquals(0, snackPct)
    }
}
