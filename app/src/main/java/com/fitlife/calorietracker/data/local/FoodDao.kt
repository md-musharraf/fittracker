package com.fitlife.calorietracker.data.local

import androidx.room.*
import com.fitlife.calorietracker.data.model.FoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY isFavorite DESC, name ASC")
    fun getAllFoods(): Flow<List<FoodItem>>

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY isFavorite DESC, name ASC")
    fun searchFoods(query: String): Flow<List<FoodItem>>

    @Query("SELECT * FROM foods WHERE category = :category ORDER BY isFavorite DESC, name ASC")
    fun getFoodsByCategory(category: String): Flow<List<FoodItem>>

    @Query("SELECT * FROM foods WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteFoods(): Flow<List<FoodItem>>

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun getFoodCount(): Int

    @Query("SELECT name FROM foods")
    suspend fun getAllFoodNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodItem>)

    @Update
    suspend fun updateFood(food: FoodItem)

    @Delete
    suspend fun deleteFood(food: FoodItem)
}
