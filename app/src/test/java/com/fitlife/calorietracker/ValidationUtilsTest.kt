package com.fitlife.calorietracker

import com.fitlife.calorietracker.data.model.ValidationUtils
import org.junit.Assert.*
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `safeParseDouble handles comma and dot correctly`() {
        assertEquals(12.5, ValidationUtils.safeParseDouble("12,5"), 0.001)
        assertEquals(12.5, ValidationUtils.safeParseDouble("12.5"), 0.001)
        assertEquals(12.5, ValidationUtils.safeParseDouble("  12,5  "), 0.001)
    }

    @Test
    fun `safeParseDouble returns default on invalid string`() {
        assertEquals(5.0, ValidationUtils.safeParseDouble("abc", default = 5.0), 0.001)
        assertEquals(0.0, ValidationUtils.safeParseDouble("", default = 0.0), 0.001)
        assertEquals(0.0, ValidationUtils.safeParseDouble(null, default = 0.0), 0.001)
    }

    @Test
    fun `safeParseDouble clamps to min and max bounds`() {
        assertEquals(10.0, ValidationUtils.safeParseDouble("5.0", min = 10.0, max = 100.0), 0.001)
        assertEquals(100.0, ValidationUtils.safeParseDouble("250.0", min = 10.0, max = 100.0), 0.001)
    }

    @Test
    fun `safeParseInt handles decimal strings and comma gracefully`() {
        assertEquals(2200, ValidationUtils.safeParseInt("2200"))
        assertEquals(2200, ValidationUtils.safeParseInt("2200.5"))
        assertEquals(2200, ValidationUtils.safeParseInt("2200,5"))
        assertEquals(0, ValidationUtils.safeParseInt("xyz", default = 0))
    }

    @Test
    fun `cleanName trims whitespace and caps max length`() {
        assertEquals("Paneer", ValidationUtils.cleanName("   Paneer   "))
        assertEquals("Unnamed Item", ValidationUtils.cleanName("   ", fallback = "Unnamed Item"))
        val longString = "A".repeat(120)
        assertEquals(80, ValidationUtils.cleanName(longString).length)
    }

    @Test
    fun `sanitization helpers clamp within safe fitness bounds`() {
        assertEquals(ValidationUtils.MIN_WEIGHT_KG, ValidationUtils.sanitizeWeight(5.0), 0.01)
        assertEquals(ValidationUtils.MAX_WEIGHT_KG, ValidationUtils.sanitizeWeight(600.0), 0.01)
        assertEquals(ValidationUtils.MIN_CALORIES, ValidationUtils.sanitizeCalories(-100.0), 0.01)
        assertEquals(ValidationUtils.MAX_CALORIES, ValidationUtils.sanitizeCalories(50_000.0), 0.01)
    }
}
