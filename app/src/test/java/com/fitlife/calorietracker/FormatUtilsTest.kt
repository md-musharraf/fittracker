package com.fitlife.calorietracker

import com.fitlife.calorietracker.ui.utils.BudgetStatus
import com.fitlife.calorietracker.ui.utils.FormatUtils
import org.junit.Assert.*
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun `formatCalories formats properly`() {
        assertEquals("1,200", FormatUtils.formatCalories(1200.0))
        assertEquals("850", FormatUtils.formatCalories(849.6))
        assertEquals("0", FormatUtils.formatCalories(0.0))
    }

    @Test
    fun `formatGrams formats integer grams`() {
        assertEquals("42g", FormatUtils.formatGrams(42.3))
        assertEquals("165g", FormatUtils.formatGrams(164.8))
    }

    @Test
    fun `formatWeight formats with 1 decimal`() {
        assertEquals("75.0 kg", FormatUtils.formatWeight(75.0))
        assertEquals("72.5 kg", FormatUtils.formatWeight(72.54))
    }

    @Test
    fun `getCalorieStatus determines status correctly`() {
        // Goal: 2000
        // Consumed 1800 -> Under
        assertEquals(BudgetStatus.UNDER_BUDGET, FormatUtils.getCalorieStatus(1800.0, 2000))
        // Consumed 1950 -> Target Zone
        assertEquals(BudgetStatus.NEAR_GOAL, FormatUtils.getCalorieStatus(1950.0, 2000))
        // Consumed 2040 -> Target Zone (within 50)
        assertEquals(BudgetStatus.NEAR_GOAL, FormatUtils.getCalorieStatus(2040.0, 2000))
        // Consumed 2150 -> Over
        assertEquals(BudgetStatus.OVER_BUDGET, FormatUtils.getCalorieStatus(2150.0, 2000))
    }
}
