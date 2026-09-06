package com.fitlife.calorietracker.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class StreakInfo(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalDaysLogged: Int = 0,
    val isLoggedToday: Boolean = false
)

object StreakTracker {

    /**
     * Compute streak information from a list of unique dates (formatted "yyyy-MM-dd").
     */
    fun calculateStreak(
        dateStrings: List<String>,
        referenceDate: LocalDate = LocalDate.now()
    ): StreakInfo {
        if (dateStrings.isEmpty()) {
            return StreakInfo(currentStreak = 0, bestStreak = 0, totalDaysLogged = 0, isLoggedToday = false)
        }

        // Parse and sort dates descending (latest first)
        val sortedDates = dateStrings.mapNotNull {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                null
            }
        }.distinct().sortedDescending()

        if (sortedDates.isEmpty()) {
            return StreakInfo(currentStreak = 0, bestStreak = 0, totalDaysLogged = 0, isLoggedToday = false)
        }

        val today = referenceDate
        val yesterday = referenceDate.minusDays(1)

        val isLoggedToday = sortedDates.contains(today)
        val isLoggedYesterday = sortedDates.contains(yesterday)

        // Determine current streak
        var currentStreak = 0
        if (isLoggedToday || isLoggedYesterday) {
            var checkDate = if (isLoggedToday) today else yesterday
            while (sortedDates.contains(checkDate)) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            }
        }

        // Determine best (maximum) streak across all history
        // Sort ascending to calculate runs
        val ascDates = sortedDates.sorted()
        var bestStreak = 0
        var runningStreak = 0
        var previousDate: LocalDate? = null

        for (date in ascDates) {
            if (previousDate == null) {
                runningStreak = 1
            } else {
                val daysDiff = ChronoUnit.DAYS.between(previousDate, date)
                if (daysDiff == 1L) {
                    runningStreak++
                } else if (daysDiff > 1L) {
                    runningStreak = 1
                }
            }
            if (runningStreak > bestStreak) {
                bestStreak = runningStreak
            }
            previousDate = date
        }

        return StreakInfo(
            currentStreak = currentStreak,
            bestStreak = maxOf(bestStreak, currentStreak),
            totalDaysLogged = sortedDates.size,
            isLoggedToday = isLoggedToday
        )
    }
}
