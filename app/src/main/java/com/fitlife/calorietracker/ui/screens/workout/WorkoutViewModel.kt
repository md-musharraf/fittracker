package com.fitlife.calorietracker.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.calorietracker.data.model.UserProfile
import com.fitlife.calorietracker.data.model.WorkoutLog
import com.fitlife.calorietracker.data.repository.CalorieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WorkoutUiState(
    val selectedDate: String = LocalDate.now().toString(),
    val userProfile: UserProfile = UserProfile(),
    val workouts: List<WorkoutLog> = emptyList(),
    val isLoading: Boolean = false
) {
    val totalCaloriesBurned: Double = workouts.sumOf { it.caloriesBurned }
    val totalMinutes: Int = workouts.sumOf { it.durationMinutes }
}

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(
    private val repository: CalorieRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val userProfileFlow = repository.getUserProfile().filterNotNull()

    private val workoutsFlow = _selectedDate.flatMapLatest { date ->
        repository.getWorkoutsForDate(date)
    }

    val uiState: StateFlow<WorkoutUiState> = combine(
        _selectedDate,
        userProfileFlow,
        workoutsFlow
    ) { date, profile, workouts ->
        WorkoutUiState(
            selectedDate = date,
            userProfile = profile,
            workouts = workouts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutUiState()
    )

    fun logWorkout(workout: WorkoutLog) {
        viewModelScope.launch {
            repository.insertWorkout(workout.copy(date = _selectedDate.value))
        }
    }

    fun deleteWorkout(workout: WorkoutLog) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }
}
