package com.fitlife.calorietracker.data.model

object ValidationUtils {

    /**
     * Safely parse a double string, handling both decimal commas (e.g. "12,5") and dots ("12.5").
     * Returns the clamped value within [min, max], or [default] if parsing fails.
     */
    fun safeParseDouble(
        input: String?,
        default: Double = 0.0,
        min: Double = 0.0,
        max: Double = 100_000.0
    ): Double {
        if (input.isNullOrBlank()) return default
        val sanitized = input.trim().replace(',', '.')
        val parsed = sanitized.toDoubleOrNull() ?: return default
        return parsed.coerceIn(min, max)
    }

    /**
     * Safely parse an integer string.
     * Returns clamped value within [min, max], or [default] if parsing fails.
     */
    fun safeParseInt(
        input: String?,
        default: Int = 0,
        min: Int = 0,
        max: Int = 100_000
    ): Int {
        if (input.isNullOrBlank()) return default
        val sanitized = input.trim().replace(',', '.').substringBefore('.')
        val parsed = sanitized.toIntOrNull() ?: return default
        return parsed.coerceIn(min, max)
    }

    /**
     * Fitness metric validation limits
     */
    const val MIN_CALORIES = 0.0
    const val MAX_CALORIES = 10_000.0

    const val MIN_MACRO_GRAMS = 0.0
    const val MAX_MACRO_GRAMS = 1_500.0

    const val MIN_WEIGHT_KG = 20.0
    const val MAX_WEIGHT_KG = 350.0

    const val MIN_HEIGHT_CM = 50.0
    const val MAX_HEIGHT_CM = 260.0

    const val MIN_WATER_ML = 0
    const val MAX_WATER_ML = 20_000

    const val MIN_SERVING = 0.01
    const val MAX_SERVING = 10_000.0

    /**
     * Validate and clean a food name
     */
    fun cleanName(rawName: String, fallback: String = "Unnamed Item"): String {
        val trimmed = rawName.trim()
        return if (trimmed.isNotBlank()) trimmed.take(80) else fallback
    }

    /**
     * Clean and clamp weight
     */
    fun sanitizeWeight(weightKg: Double): Double {
        return weightKg.coerceIn(MIN_WEIGHT_KG, MAX_WEIGHT_KG)
    }

    /**
     * Clean and clamp height
     */
    fun sanitizeHeight(heightCm: Double): Double {
        return heightCm.coerceIn(MIN_HEIGHT_CM, MAX_HEIGHT_CM)
    }

    /**
     * Clean and clamp calories
     */
    fun sanitizeCalories(calories: Double): Double {
        return calories.coerceIn(MIN_CALORIES, MAX_CALORIES)
    }

    /**
     * Clean and clamp macro grams
     */
    fun sanitizeMacro(grams: Double): Double {
        return grams.coerceIn(MIN_MACRO_GRAMS, MAX_MACRO_GRAMS)
    }
}
