package com.fitlife.calorietracker.ui.screens.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlife.calorietracker.data.model.FoodItem
import com.fitlife.calorietracker.data.model.MealLog
import com.fitlife.calorietracker.data.model.MealType
import com.fitlife.calorietracker.data.repository.CalorieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FoodLogUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val foods: List<FoodItem> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class FoodLogViewModel(
    private val repository: CalorieRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val foodsFlow: Flow<List<FoodItem>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        when {
            query.isNotBlank() -> repository.searchFoods(query)
            category == "Favorites" -> repository.getFavoriteFoods()
            category != "All" -> repository.getFoodsByCategory(category)
            else -> repository.getAllFoods()
        }
    }

    val uiState: StateFlow<FoodLogUiState> = combine(
        _searchQuery,
        _selectedCategory,
        foodsFlow
    ) { query, category, foods ->
        FoodLogUiState(
            searchQuery = query,
            selectedCategory = category,
            foods = foods
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FoodLogUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(food: FoodItem) {
        viewModelScope.launch {
            repository.updateFood(food.copy(isFavorite = !food.isFavorite))
        }
    }

    fun createCustomFood(food: FoodItem) {
        viewModelScope.launch {
            repository.insertFood(food)
        }
    }

    fun logFoodToMeal(
        food: FoodItem,
        multiplier: Double,
        mealType: MealType,
        date: String = LocalDate.now().toString()
    ) {
        viewModelScope.launch {
            val log = MealLog(
                date = date,
                mealType = mealType.name,
                foodId = food.id,
                foodName = food.name,
                servingCount = multiplier,
                servingUnit = food.servingSize,
                calories = food.calories * multiplier,
                proteinGrams = food.proteinGrams * multiplier,
                carbsGrams = food.carbsGrams * multiplier,
                fatGrams = food.fatGrams * multiplier
            )
            repository.insertMealLog(log)
        }
    }

    fun undoLastAddedMeal(onUndone: (MealLog) -> Unit = {}) {
        viewModelScope.launch {
            val item = repository.undoLastAddedMeal()
            if (item != null) {
                onUndone(item)
            }
        }
    }
}
