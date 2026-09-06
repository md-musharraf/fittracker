package com.fitlife.calorietracker

import com.fitlife.calorietracker.data.model.*
import org.junit.Assert.*
import org.junit.Test

class FitnessCalculationsTest {

    @Test
    fun `test BMI calculation normal weight`() {
        val result = FitnessCalculations.calculateBmi(weightKg = 70.0, heightCm = 175.0)
        // 70 / (1.75 * 1.75) = 22.86 -> 22.9
        assertEquals(22.9, result.bmi, 0.1)
        assertEquals("Normal Weight", result.category)
        assertTrue(result.healthyMinWeightKg in 56.0..57.0)
        assertTrue(result.healthyMaxWeightKg in 76.0..77.0)
    }

    @Test
    fun `test BMI calculation overweight and obese`() {
        val overweight = FitnessCalculations.calculateBmi(weightKg = 85.0, heightCm = 175.0)
        assertEquals("Overweight", overweight.category)

        val obese = FitnessCalculations.calculateBmi(weightKg = 105.0, heightCm = 175.0)
        assertEquals("Obese Class I", obese.category)

        val underweight = FitnessCalculations.calculateBmi(weightKg = 50.0, heightCm = 175.0)
        assertEquals("Underweight", underweight.category)
    }

    @Test
    fun `test BMR and TDEE formulas for male and female`() {
        // Male: 75kg, 175cm, 26yo
        // BMR = 10 * 75 + 6.25 * 175 - 5 * 26 + 5 = 750 + 1093.75 - 130 + 5 = 1718.75
        val bmrMale = FitnessCalculations.calculateBmr(75.0, 175.0, 26, isMale = true)
        assertEquals(1718.75, bmrMale, 0.01)

        // Female: 60kg, 165cm, 26yo
        // BMR = 10 * 60 + 6.25 * 165 - 5 * 26 - 161 = 600 + 1031.25 - 130 - 161 = 1340.25
        val bmrFemale = FitnessCalculations.calculateBmr(60.0, 165.0, 26, isMale = false)
        assertEquals(1340.25, bmrFemale, 0.01)

        // TDEE with Moderately Active (1.55)
        val tdee = FitnessCalculations.calculateTdee(bmrMale, ActivityLevel.MODERATELY_ACTIVE)
        assertEquals(1718.75 * 1.55, tdee, 0.01)
    }

    @Test
    fun `test Gym Guy macro recommendations prioritize high protein`() {
        val rec = FitnessCalculations.calculateRecommendations(
            weightKg = 80.0,
            heightCm = 180.0,
            age = 25,
            gender = "Male",
            activityLevel = ActivityLevel.VERY_ACTIVE,
            persona = PersonaType.GYM_GUY,
            goal = GoalType.LOSE_WEIGHT_MODERATE
        )

        // Gym guy protein target is 2.2g per kg -> 80 * 2.2 = 176g
        assertEquals(176, rec.proteinGrams)
        // Deficit applied: daily calories should be less than TDEE
        val bmr = FitnessCalculations.calculateBmr(80.0, 180.0, 25, isMale = true)
        val tdee = FitnessCalculations.calculateTdee(bmr, ActivityLevel.VERY_ACTIVE)
        assertTrue(rec.dailyCalories < tdee)
        // Water for gym guy should be at least 3500ml
        assertTrue(rec.waterMl >= 3500)
    }

    @Test
    fun `test Sports Athlete macro recommendations prioritize carbohydrates`() {
        val rec = FitnessCalculations.calculateRecommendations(
            weightKg = 70.0,
            heightCm = 175.0,
            age = 24,
            gender = "Male",
            activityLevel = ActivityLevel.VERY_ACTIVE,
            persona = PersonaType.SPORTS_ATHLETE,
            goal = GoalType.MAINTAIN_WEIGHT
        )

        // Athlete carbs should provide over 50% of total calories (carbs * 4 / total >= 0.45)
        val carbCalories = rec.carbsGrams * 4.0
        val carbRatio = carbCalories / rec.dailyCalories
        assertTrue("Carb ratio should be high for endurance athletes", carbRatio >= 0.48)
    }

    @Test
    fun `test Normal Health balanced nutrition distribution`() {
        val rec = FitnessCalculations.calculateRecommendations(
            weightKg = 65.0,
            heightCm = 168.0,
            age = 30,
            gender = "Female",
            activityLevel = ActivityLevel.LIGHTLY_ACTIVE,
            persona = PersonaType.NORMAL_HEALTH,
            goal = GoalType.MAINTAIN_WEIGHT
        )

        // Normal balanced diet is roughly 25% protein, 50% carbs, 25% fat
        val proteinKcal = rec.proteinGrams * 4.0
        val carbsKcal = rec.carbsGrams * 4.0
        val fatKcal = rec.fatGrams * 9.0

        val proteinRatio = proteinKcal / rec.dailyCalories
        val carbsRatio = carbsKcal / rec.dailyCalories

        assertTrue("Protein should be around 25%", proteinRatio in 0.20..0.30)
        assertTrue("Carbs should be around 50%", carbsRatio in 0.45..0.55)
    }

    @Test
    fun `test SmartDefaults auto macro estimation from calories`() {
        // 500 kcal input should return roughly 30% P, 40% C, 30% F
        // P = 500 * 0.30 / 4 = 37.5g
        // C = 500 * 0.40 / 4 = 50.0g
        // F = 500 * 0.30 / 9 = 16.6g
        val (p, c, f) = SmartDefaults.estimateMacrosFromCalories(500.0)
        assertEquals(37.5, p, 0.1)
        assertEquals(50.0, c, 0.1)
        assertEquals(16.6, f, 0.1)
    }

    @Test
    fun `test SmartDefaults meal greeting and daily tips exist`() {
        val greeting = SmartDefaults.getMealGreeting()
        assertTrue(greeting.isNotBlank())

        val tipLowProtein = SmartDefaults.getDailyTip(
            caloriesConsumed = 1200.0,
            calorieGoal = 2000,
            proteinConsumed = 30.0,
            proteinGoal = 150
        )
        assertTrue(tipLowProtein.isNotBlank())
    }
}
