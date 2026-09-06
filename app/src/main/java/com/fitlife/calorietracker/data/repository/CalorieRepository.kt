package com.fitlife.calorietracker.data.repository

import com.fitlife.calorietracker.data.local.*
import com.fitlife.calorietracker.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class CalorieRepository(private val database: AppDatabase) {

    private val foodDao = database.foodDao()
    private val mealLogDao = database.mealLogDao()
    private val workoutDao = database.workoutDao()
    private val waterDao = database.waterDao()
    private val weightDao = database.weightDao()
    private val userProfileDao = database.userProfileDao()

    // Last deleted meal for quick undo
    private var lastDeletedMeal: MealLog? = null

    // Foods
    fun getAllFoods(): Flow<List<FoodItem>> = foodDao.getAllFoods()
    fun searchFoods(query: String): Flow<List<FoodItem>> = foodDao.searchFoods(query)
    fun getFoodsByCategory(category: String): Flow<List<FoodItem>> = foodDao.getFoodsByCategory(category)
    fun getFavoriteFoods(): Flow<List<FoodItem>> = foodDao.getFavoriteFoods()

    suspend fun insertFood(food: FoodItem): Long = withContext(Dispatchers.IO) {
        val sanitized = food.copy(
            name = ValidationUtils.cleanName(food.name),
            calories = ValidationUtils.sanitizeCalories(food.calories),
            proteinGrams = ValidationUtils.sanitizeMacro(food.proteinGrams),
            carbsGrams = ValidationUtils.sanitizeMacro(food.carbsGrams),
            fatGrams = ValidationUtils.sanitizeMacro(food.fatGrams),
            fiberGrams = ValidationUtils.sanitizeMacro(food.fiberGrams)
        )
        foodDao.insertFood(sanitized)
    }

    suspend fun updateFood(food: FoodItem) = withContext(Dispatchers.IO) {
        foodDao.updateFood(food)
    }

    suspend fun deleteFood(food: FoodItem) = withContext(Dispatchers.IO) {
        foodDao.deleteFood(food)
    }

    // Meal Logs
    fun getMealLogsForDate(date: String): Flow<List<MealLog>> = mealLogDao.getLogsForDate(date)
    fun getMealLogsBetweenDates(startDate: String, endDate: String): Flow<List<MealLog>> =
        mealLogDao.getLogsBetweenDates(startDate, endDate)
    fun getRecentFoods(): Flow<List<MealLog>> = mealLogDao.getRecentFoods()

    // Streaks
    fun getStreakInfo(): Flow<StreakInfo> = mealLogDao.getAllLoggedDates().map { dates ->
        StreakTracker.calculateStreak(dates)
    }

    suspend fun insertMealLog(log: MealLog): Long = withContext(Dispatchers.IO) {
        val sanitized = log.copy(
            foodName = ValidationUtils.cleanName(log.foodName),
            calories = ValidationUtils.sanitizeCalories(log.calories),
            proteinGrams = ValidationUtils.sanitizeMacro(log.proteinGrams),
            carbsGrams = ValidationUtils.sanitizeMacro(log.carbsGrams),
            fatGrams = ValidationUtils.sanitizeMacro(log.fatGrams),
            servingCount = log.servingCount.coerceIn(ValidationUtils.MIN_SERVING, ValidationUtils.MAX_SERVING)
        )
        mealLogDao.insertMealLog(sanitized)
    }

    suspend fun deleteMealLog(log: MealLog) = withContext(Dispatchers.IO) {
        lastDeletedMeal = log
        mealLogDao.deleteMealLog(log)
    }

    suspend fun undoLastDeletedMeal(): MealLog? = withContext(Dispatchers.IO) {
        val item = lastDeletedMeal ?: return@withContext null
        mealLogDao.insertMealLog(item.copy(id = 0))
        lastDeletedMeal = null
        item
    }

    suspend fun deleteMealLogById(id: Long) = withContext(Dispatchers.IO) {
        mealLogDao.deleteMealLogById(id)
    }

    suspend fun copyMealsFromDate(sourceDate: String, targetDate: String, mealType: String? = null): Int = withContext(Dispatchers.IO) {
        val sourceLogs = mealLogDao.getLogsForDate(sourceDate).firstOrNull() ?: emptyList()
        val filtered = if (mealType != null) sourceLogs.filter { it.mealType == mealType } else sourceLogs
        filtered.forEach { item ->
            mealLogDao.insertMealLog(
                item.copy(
                    id = 0,
                    date = targetDate,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        filtered.size
    }

    // Workouts
    fun getWorkoutsForDate(date: String): Flow<List<WorkoutLog>> = workoutDao.getWorkoutsForDate(date)
    fun getWorkoutsBetweenDates(startDate: String, endDate: String): Flow<List<WorkoutLog>> =
        workoutDao.getWorkoutsBetweenDates(startDate, endDate)

    suspend fun insertWorkout(workout: WorkoutLog): Long = withContext(Dispatchers.IO) {
        val sanitized = workout.copy(
            exerciseName = ValidationUtils.cleanName(workout.exerciseName, "Workout"),
            durationMinutes = workout.durationMinutes.coerceIn(1, 720),
            caloriesBurned = ValidationUtils.sanitizeCalories(workout.caloriesBurned)
        )
        workoutDao.insertWorkout(sanitized)
    }

    suspend fun deleteWorkout(workout: WorkoutLog) = withContext(Dispatchers.IO) {
        workoutDao.deleteWorkout(workout)
    }

    // Water
    fun getWaterForDate(date: String): Flow<WaterLog?> = waterDao.getWaterForDate(date)

    suspend fun addWater(date: String, amountMl: Int, targetMl: Int = 3500) = withContext(Dispatchers.IO) {
        val current = waterDao.getWaterForDate(date).firstOrNull()
        val currentAmount = current?.amountMl ?: 0
        val updated = (currentAmount + amountMl).coerceIn(ValidationUtils.MIN_WATER_ML, ValidationUtils.MAX_WATER_ML)
        waterDao.insertOrUpdateWater(WaterLog(date = date, amountMl = updated, targetMl = targetMl))
    }

    suspend fun subtractWater(date: String, amountMl: Int, targetMl: Int = 3500) = withContext(Dispatchers.IO) {
        val current = waterDao.getWaterForDate(date).firstOrNull() ?: return@withContext
        val updated = (current.amountMl - amountMl).coerceAtLeast(0)
        waterDao.insertOrUpdateWater(current.copy(amountMl = updated, targetMl = targetMl))
    }

    suspend fun setWater(date: String, amountMl: Int, targetMl: Int) = withContext(Dispatchers.IO) {
        val clamped = amountMl.coerceIn(ValidationUtils.MIN_WATER_ML, ValidationUtils.MAX_WATER_ML)
        waterDao.insertOrUpdateWater(WaterLog(date = date, amountMl = clamped, targetMl = targetMl))
    }

    // Weight Logs
    fun getAllWeightLogs(): Flow<List<WeightLog>> = weightDao.getAllWeightLogs()
    fun getLatestWeightLog(): Flow<WeightLog?> = weightDao.getLatestWeightLog()

    suspend fun logWeight(date: String, weightKg: Double, heightCm: Double, notes: String = ""): Long = withContext(Dispatchers.IO) {
        val safeWeight = ValidationUtils.sanitizeWeight(weightKg)
        val safeHeight = ValidationUtils.sanitizeHeight(heightCm)
        val bmi = FitnessCalculations.calculateBmi(safeWeight, safeHeight).bmi
        val log = WeightLog(
            date = date,
            weightKg = safeWeight,
            bmi = bmi,
            notes = notes.take(120)
        )
        val id = weightDao.insertWeightLog(log)

        // Also update current weight in user profile
        val profile = userProfileDao.getUserProfileSync()
        if (profile != null) {
            userProfileDao.insertOrUpdateProfile(profile.copy(currentWeightKg = safeWeight))
        }
        id
    }

    suspend fun deleteWeightLog(log: WeightLog) = withContext(Dispatchers.IO) {
        weightDao.deleteWeightLog(log)
    }

    // Profile & Goals
    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile()
    suspend fun getUserProfileSync(): UserProfile? = withContext(Dispatchers.IO) { userProfileDao.getUserProfileSync() }
    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        val sanitized = profile.copy(
            name = ValidationUtils.cleanName(profile.name, "Athlete"),
            currentWeightKg = ValidationUtils.sanitizeWeight(profile.currentWeightKg),
            targetWeightKg = ValidationUtils.sanitizeWeight(profile.targetWeightKg),
            heightCm = ValidationUtils.sanitizeHeight(profile.heightCm),
            age = profile.age.coerceIn(12, 110),
            dailyCalorieGoal = profile.dailyCalorieGoal.coerceIn(500, 10_000),
            proteinGoalGrams = profile.proteinGoalGrams.coerceIn(20, 500),
            carbsGoalGrams = profile.carbsGoalGrams.coerceIn(20, 800),
            fatGoalGrams = profile.fatGoalGrams.coerceIn(10, 300),
            dailyWaterGoalMl = profile.dailyWaterGoalMl.coerceIn(500, 10_000)
        )
        userProfileDao.insertOrUpdateProfile(sanitized)
    }

    /**
     * Export all user data summary as a structured text block for backup / sharing
     */
    suspend fun exportDataSummary(): String = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfileSync() ?: UserProfile()
        val allWeights = weightDao.getAllWeightLogs().firstOrNull() ?: emptyList()
        val recentMeals = mealLogDao.getAllLogs().firstOrNull() ?: emptyList()
        val streak = StreakTracker.calculateStreak(recentMeals.map { it.date })

        buildString {
            appendLine("=== FITTRACK PRO FITNESS DATA EXPORT ===")
            appendLine("Generated: ${LocalDate.now()}")
            appendLine("User: ${profile.name} (${profile.gender}, ${profile.age} yrs)")
            appendLine("Height: ${profile.heightCm} cm | Current Weight: ${profile.currentWeightKg} kg | Target: ${profile.targetWeightKg} kg")
            appendLine("Persona: ${profile.persona.title}")
            appendLine("Daily Goals: ${profile.dailyCalorieGoal} kcal | P: ${profile.proteinGoalGrams}g, C: ${profile.carbsGoalGrams}g, F: ${profile.fatGoalGrams}g | Water: ${profile.dailyWaterGoalMl}ml")
            appendLine()
            appendLine("--- STREAK STATS ---")
            appendLine("Current Streak: ${streak.currentStreak} days | Best Streak: ${streak.bestStreak} days | Total Logged Days: ${streak.totalDaysLogged}")
            appendLine()
            appendLine("--- WEIGHT HISTORY (${allWeights.size} entries) ---")
            allWeights.take(10).forEach { w ->
                appendLine("${w.date}: ${w.weightKg} kg (BMI: ${w.bmi}) - ${w.notes}")
            }
            appendLine()
            appendLine("--- RECENT MEALS (${recentMeals.size} total) ---")
            recentMeals.take(15).forEach { m ->
                appendLine("${m.date} [${m.mealType}]: ${m.foodName} - ${m.calories.toInt()} kcal (P:${m.proteinGrams.toInt()}g, C:${m.carbsGrams.toInt()}g, F:${m.fatGrams.toInt()}g)")
            }
            appendLine("=== END OF FITTRACK PRO EXPORT ===")
        }
    }
}
