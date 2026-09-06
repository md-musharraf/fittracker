package com.fitlife.calorietracker.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.calorietracker.data.model.*
import com.fitlife.calorietracker.data.repository.CalorieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GoalsUiState(
    val userProfile: UserProfile = UserProfile(),
    val bmiResult: BmiResult = FitnessCalculations.calculateBmi(75.0, 175.0),
    val bmr: Double = 1718.75,
    val tdee: Double = 2664.0,
    val recommendations: MacroRecommendation = FitnessCalculations.calculateRecommendations(
        75.0, 175.0, 26, "Male", ActivityLevel.MODERATELY_ACTIVE, PersonaType.GYM_GUY, GoalType.LOSE_WEIGHT_MODERATE
    ),
    val isSavedSuccess: Boolean = false
)

class GoalsViewModel(
    private val repository: CalorieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getUserProfile().filterNotNull().collect { profile ->
                recomputeState(profile)
            }
        }
    }

    private fun recomputeState(profile: UserProfile) {
        val bmi = FitnessCalculations.calculateBmi(profile.currentWeightKg, profile.heightCm)
        val isMale = profile.gender.equals("Male", ignoreCase = true)
        val bmr = FitnessCalculations.calculateBmr(profile.currentWeightKg, profile.heightCm, profile.age, isMale)
        val tdee = FitnessCalculations.calculateTdee(bmr, profile.activityLevel)
        val recs = FitnessCalculations.calculateRecommendations(
            weightKg = profile.currentWeightKg,
            heightCm = profile.heightCm,
            age = profile.age,
            gender = profile.gender,
            activityLevel = profile.activityLevel,
            persona = profile.persona,
            goal = profile.goalType
        )

        _uiState.value = _uiState.value.copy(
            userProfile = profile,
            bmiResult = bmi,
            bmr = bmr,
            tdee = tdee,
            recommendations = recs
        )
    }

    fun updateProfile(
        name: String,
        gender: String,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        targetWeightKg: Double,
        persona: PersonaType,
        activityLevel: ActivityLevel,
        goalType: GoalType,
        dailyCalorieGoal: Int,
        proteinGoalGrams: Int,
        carbsGoalGrams: Int,
        fatGoalGrams: Int,
        dailyWaterGoalMl: Int
    ) {
        val updated = _uiState.value.userProfile.copy(
            name = name,
            gender = gender,
            age = age,
            heightCm = heightCm,
            currentWeightKg = weightKg,
            targetWeightKg = targetWeightKg,
            persona = persona,
            activityLevel = activityLevel,
            goalType = goalType,
            dailyCalorieGoal = dailyCalorieGoal,
            proteinGoalGrams = proteinGoalGrams,
            carbsGoalGrams = carbsGoalGrams,
            fatGoalGrams = fatGoalGrams,
            dailyWaterGoalMl = dailyWaterGoalMl
        )

        viewModelScope.launch {
            repository.updateProfile(updated)
            // Log weight entry if weight changed
            val today = java.time.LocalDate.now().toString()
            repository.logWeight(today, weightKg, heightCm, "Updated via Goals profile")
            _uiState.value = _uiState.value.copy(isSavedSuccess = true)
        }
    }

    fun applyRecommendations() {
        val recs = _uiState.value.recommendations
        val profile = _uiState.value.userProfile
        val updated = profile.copy(
            dailyCalorieGoal = recs.dailyCalories,
            proteinGoalGrams = recs.proteinGrams,
            carbsGoalGrams = recs.carbsGrams,
            fatGoalGrams = recs.fatGrams,
            dailyWaterGoalMl = recs.waterMl
        )
        viewModelScope.launch {
            repository.updateProfile(updated)
        }
    }

    fun clearSavedSuccess() {
        _uiState.value = _uiState.value.copy(isSavedSuccess = false)
    }

    suspend fun getExportSummary(): String {
        return repository.exportDataSummary()
    }
}
