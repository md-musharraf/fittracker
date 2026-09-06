package com.fitlife.calorietracker.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.calorietracker.data.model.*
import com.fitlife.calorietracker.data.repository.CalorieRepository
import com.fitlife.calorietracker.ui.components.DailyBarData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WeeklyMacroSummary(
    val avgProtein: Double = 0.0,
    val avgCarbs: Double = 0.0,
    val avgFat: Double = 0.0,
    val daysGoalMet: Int = 0,
    val activeDays: Int = 0
)

data class ProgressUiState(
    val userProfile: UserProfile = UserProfile(),
    val weightLogs: List<WeightLog> = emptyList(),
    val weeklyChartData: List<DailyBarData> = emptyList(),
    val averageCalories: Double = 0.0,
    val weeklyMacros: WeeklyMacroSummary = WeeklyMacroSummary(),
    val streakInfo: StreakInfo = StreakInfo(),
    val isLoading: Boolean = false
) {
    val latestWeight: Double = weightLogs.firstOrNull()?.weightKg ?: userProfile.currentWeightKg
    val weightDifferenceToGoal: Double = (latestWeight - userProfile.targetWeightKg)
}

class ProgressViewModel(
    private val repository: CalorieRepository
) : ViewModel() {

    private val userProfileFlow = repository.getUserProfile().filterNotNull()
    private val weightLogsFlow = repository.getAllWeightLogs()
    private val streakFlow = repository.getStreakInfo()

    private val today = LocalDate.now()
    private val daysList = (6 downTo 0).map { today.minusDays(it.toLong()) }
    private val startDate = daysList.first().toString()
    private val endDate = daysList.last().toString()

    private val weeklyMealLogsFlow = repository.getMealLogsBetweenDates(startDate, endDate)

    val uiState: StateFlow<ProgressUiState> = combine(
        userProfileFlow,
        weightLogsFlow,
        weeklyMealLogsFlow,
        streakFlow
    ) { profile, weights, mealLogs, streak ->
        val logsByDate = mealLogs.groupBy { it.date }

        val barData = daysList.map { date ->
            val dateStr = date.toString()
            val consumed = logsByDate[dateStr]?.sumOf { it.calories } ?: 0.0
            val label = date.format(DateTimeFormatter.ofPattern("EEE"))
            DailyBarData(
                dayLabel = label,
                date = dateStr,
                caloriesConsumed = consumed,
                calorieGoal = profile.dailyCalorieGoal,
                isToday = date == today
            )
        }

        val activeDatesWithLogs = logsByDate.filter { it.value.isNotEmpty() }
        val activeCount = activeDatesWithLogs.size.coerceAtLeast(1)

        val totalProtein = mealLogs.sumOf { it.proteinGrams }
        val totalCarbs = mealLogs.sumOf { it.carbsGrams }
        val totalFat = mealLogs.sumOf { it.fatGrams }

        val daysGoalMet = barData.count {
            it.caloriesConsumed > 0 && it.caloriesConsumed <= (it.calorieGoal + 50)
        }

        val macroSummary = WeeklyMacroSummary(
            avgProtein = totalProtein / activeCount,
            avgCarbs = totalCarbs / activeCount,
            avgFat = totalFat / activeCount,
            daysGoalMet = daysGoalMet,
            activeDays = activeDatesWithLogs.size
        )

        val avgCalories = if (activeDatesWithLogs.isNotEmpty()) {
            barData.filter { it.caloriesConsumed > 0 }.map { it.caloriesConsumed }.average()
        } else {
            0.0
        }

        ProgressUiState(
            userProfile = profile,
            weightLogs = weights,
            weeklyChartData = barData,
            averageCalories = avgCalories,
            weeklyMacros = macroSummary,
            streakInfo = streak
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState()
    )

    fun logWeight(weightKg: Double, notes: String) {
        viewModelScope.launch {
            val todayDate = LocalDate.now().toString()
            val height = uiState.value.userProfile.heightCm
            repository.logWeight(todayDate, weightKg, height, notes)
        }
    }

    fun deleteWeight(log: WeightLog) {
        viewModelScope.launch {
            repository.deleteWeightLog(log)
        }
    }
}
