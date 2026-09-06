package com.fitlife.calorietracker.ui.utils

import androidx.compose.ui.graphics.Color
import com.fitlife.calorietracker.ui.theme.AccentGreen
import com.fitlife.calorietracker.ui.theme.PrimaryOrange
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

enum class BudgetStatus(val label: String, val color: Color) {
    UNDER_BUDGET("On Track", AccentGreen),
    NEAR_GOAL("In Target Zone", PrimaryOrange),
    OVER_BUDGET("Over Target", Color(0xFFEF4444))
}

object FormatUtils {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    private val monthDayFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    private val fullDayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)

    fun formatCalories(calories: Double): String {
        return numberFormat.format(calories.roundToInt())
    }

    fun formatGrams(grams: Double): String {
        return "${grams.roundToInt()}g"
    }

    fun formatWeight(weightKg: Double): String {
        return "%.1f kg".format(Locale.US, weightKg)
    }

    fun formatWaterMl(amountMl: Int): String {
        return "${numberFormat.format(amountMl)} ml"
    }

    fun formatDateHeading(dateString: String): String {
        val date = try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            LocalDate.now()
        }
        val today = LocalDate.now()
        return when (date) {
            today -> "Today, ${date.format(monthDayFormatter)}"
            today.minusDays(1) -> "Yesterday, ${date.format(monthDayFormatter)}"
            today.plusDays(1) -> "Tomorrow, ${date.format(monthDayFormatter)}"
            else -> date.format(fullDayFormatter)
        }
    }

    fun getCalorieStatus(caloriesConsumed: Double, calorieGoal: Int): BudgetStatus {
        if (calorieGoal <= 0) return BudgetStatus.UNDER_BUDGET
        val diff = caloriesConsumed - calorieGoal
        return when {
            diff > 50 -> BudgetStatus.OVER_BUDGET
            diff >= -150 -> BudgetStatus.NEAR_GOAL
            else -> BudgetStatus.UNDER_BUDGET
        }
    }
}
