package com.fitlife.calorietracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.calorietracker.data.model.*
import com.fitlife.calorietracker.data.model.SmartDefaults
import com.fitlife.calorietracker.data.repository.CalorieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val selectedDate: String = LocalDate.now().toString(),
    val userProfile: UserProfile = UserProfile(),
    val mealLogs: List<MealLog> = emptyList(),
    val workouts: List<WorkoutLog> = emptyList(),
    val waterLog: WaterLog? = null,
    val recentFoods: List<MealLog> = emptyList(),
    val streakInfo: StreakInfo = StreakInfo(),
    val greeting: String = SmartDefaults.getMealGreeting(),
    val dailyTip: String = "",
    val isLoading: Boolean = false
) {
    val totalCaloriesConsumed: Double = mealLogs.sumOf { it.calories }
    val totalProteinConsumed: Double = mealLogs.sumOf { it.proteinGrams }
    val totalCarbsConsumed: Double = mealLogs.sumOf { it.carbsGrams }
    val totalFatConsumed: Double = mealLogs.sumOf { it.fatGrams }
    val totalCaloriesBurned: Double = workouts.sumOf { it.caloriesBurned }

    val breakfastItems: List<MealLog> = mealLogs.filter { it.mealType == MealType.BREAKFAST.name }
    val lunchItems: List<MealLog> = mealLogs.filter { it.mealType == MealType.LUNCH.name }
    val dinnerItems: List<MealLog> = mealLogs.filter { it.mealType == MealType.DINNER.name }
    val snackItems: List<MealLog> = mealLogs.filter { it.mealType == MealType.SNACK.name }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: CalorieRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val userProfileFlow: Flow<UserProfile> = repository.getUserProfile()
        .filterNotNull()

    private val mealLogsFlow: Flow<List<MealLog>> = _selectedDate.flatMapLatest { date ->
        repository.getMealLogsForDate(date)
    }

    private val workoutsFlow: Flow<List<WorkoutLog>> = _selectedDate.flatMapLatest { date ->
        repository.getWorkoutsForDate(date)
    }

    private val waterFlow: Flow<WaterLog?> = _selectedDate.flatMapLatest { date ->
        repository.getWaterForDate(date)
    }

    private val recentFoodsFlow: Flow<List<MealLog>> = repository.getRecentFoods()
    private val streakFlow: Flow<StreakInfo> = repository.getStreakInfo()

    // Triple for water, recent foods, and streak
    private val extraStateFlow: Flow<Triple<WaterLog?, List<MealLog>, StreakInfo>> = combine(
        waterFlow,
        recentFoodsFlow,
        streakFlow
    ) { water, recent, streak -> Triple(water, recent, streak) }

    val uiState: StateFlow<DashboardUiState> = combine(
        _selectedDate,
        userProfileFlow,
        mealLogsFlow,
        workoutsFlow,
        extraStateFlow
    ) { date, profile, meals, workouts, (water, recent, streak) ->
        val tip = SmartDefaults.getDailyTip(
            caloriesConsumed = meals.sumOf { it.calories },
            calorieGoal = profile.dailyCalorieGoal,
            proteinConsumed = meals.sumOf { it.proteinGrams },
            proteinGoal = profile.proteinGoalGrams
        )
        DashboardUiState(
            selectedDate = date,
            userProfile = profile,
            mealLogs = meals,
            workouts = workouts,
            waterLog = water,
            recentFoods = recent,
            streakInfo = streak,
            dailyTip = tip
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun changeDate(offsetDays: Long) {
        val current = LocalDate.parse(_selectedDate.value)
        _selectedDate.value = current.plusDays(offsetDays).toString()
    }

    fun setDate(date: String) {
        _selectedDate.value = date
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val target = uiState.value.userProfile.dailyWaterGoalMl
            repository.addWater(_selectedDate.value, amountMl, target)
        }
    }

    fun subtractWater(amountMl: Int) {
        viewModelScope.launch {
            val target = uiState.value.userProfile.dailyWaterGoalMl
            repository.subtractWater(_selectedDate.value, amountMl, target)
        }
    }

    fun deleteMealItem(item: MealLog) {
        viewModelScope.launch {
            repository.deleteMealLog(item)
        }
    }

    fun undoDeleteMeal() {
        viewModelScope.launch {
            repository.undoLastDeletedMeal()
        }
    }

    fun quickAddMeal(
        foodName: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        mealType: MealType
    ) {
        viewModelScope.launch {
            val log = MealLog(
                date = _selectedDate.value,
                mealType = mealType.name,
                foodName = foodName,
                calories = calories,
                proteinGrams = protein,
                carbsGrams = carbs,
                fatGrams = fat
            )
            repository.insertMealLog(log)
        }
    }

    fun addWorkout(workout: WorkoutLog) {
        viewModelScope.launch {
            repository.insertWorkout(workout.copy(date = _selectedDate.value))
        }
    }

    fun copyYesterdayMeals(mealType: MealType? = null) {
        viewModelScope.launch {
            val current = LocalDate.parse(_selectedDate.value)
            val yesterday = current.minusDays(1).toString()
            repository.copyMealsFromDate(yesterday, _selectedDate.value, mealType?.name)
        }
    }
}
