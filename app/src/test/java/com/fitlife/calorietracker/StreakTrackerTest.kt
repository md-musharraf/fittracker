package com.fitlife.calorietracker

import com.fitlife.calorietracker.data.model.StreakTracker
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class StreakTrackerTest {

    private val baseDate = LocalDate.of(2026, 9, 6)

    @Test
    fun `empty list returns zero streak`() {
        val streak = StreakTracker.calculateStreak(emptyList(), referenceDate = baseDate)
        assertEquals(0, streak.currentStreak)
        assertEquals(0, streak.bestStreak)
        assertEquals(0, streak.totalDaysLogged)
        assertFalse(streak.isLoggedToday)
    }

    @Test
    fun `logged today only gives 1 day streak`() {
        val dates = listOf("2026-09-06")
        val streak = StreakTracker.calculateStreak(dates, referenceDate = baseDate)
        assertEquals(1, streak.currentStreak)
        assertEquals(1, streak.bestStreak)
        assertEquals(1, streak.totalDaysLogged)
        assertTrue(streak.isLoggedToday)
    }

    @Test
    fun `logged yesterday but not today maintains active streak`() {
        val dates = listOf("2026-09-05", "2026-09-04")
        val streak = StreakTracker.calculateStreak(dates, referenceDate = baseDate)
        // Streak is alive at 2 days until today ends
        assertEquals(2, streak.currentStreak)
        assertEquals(2, streak.bestStreak)
        assertFalse(streak.isLoggedToday)
    }

    @Test
    fun `consecutive days streak accumulates correctly`() {
        val dates = listOf(
            "2026-09-06",
            "2026-09-05",
            "2026-09-04",
            "2026-09-03",
            "2026-09-02"
        )
        val streak = StreakTracker.calculateStreak(dates, referenceDate = baseDate)
        assertEquals(5, streak.currentStreak)
        assertEquals(5, streak.bestStreak)
        assertEquals(5, streak.totalDaysLogged)
    }

    @Test
    fun `gap breaks current streak but preserves best streak`() {
        val dates = listOf(
            "2026-09-06",
            "2026-09-05",
            // 2026-09-04 missed!
            "2026-09-03",
            "2026-09-02",
            "2026-09-01",
            "2026-08-31" // 4 days run
        )
        val streak = StreakTracker.calculateStreak(dates, referenceDate = baseDate)
        assertEquals(2, streak.currentStreak)
        assertEquals(4, streak.bestStreak)
        assertEquals(6, streak.totalDaysLogged)
    }

    @Test
    fun `no recent logs gives 0 current streak`() {
        val dates = listOf("2026-08-01", "2026-08-02", "2026-08-03")
        val streak = StreakTracker.calculateStreak(dates, referenceDate = baseDate)
        assertEquals(0, streak.currentStreak)
        assertEquals(3, streak.bestStreak)
        assertEquals(3, streak.totalDaysLogged)
    }
}
