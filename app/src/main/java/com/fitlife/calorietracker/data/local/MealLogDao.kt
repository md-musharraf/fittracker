package com.fitlife.calorietracker.data.local

import androidx.room.*
import com.fitlife.calorietracker.data.model.MealLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE date = :date ORDER BY timestamp ASC")
    fun getLogsForDate(date: String): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE date = :date AND mealType = :mealType ORDER BY timestamp ASC")
    fun getLogsForDateAndMeal(date: String, mealType: String): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, timestamp ASC")
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE id IN (SELECT MAX(id) FROM meal_logs GROUP BY foodName) ORDER BY timestamp DESC LIMIT 20")
    fun getRecentFoods(): Flow<List<MealLog>>

    @Query("SELECT DISTINCT date FROM meal_logs ORDER BY date DESC")
    fun getAllLoggedDates(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM meal_logs WHERE date = :date")
    fun getMealCountForDate(date: String): Flow<Int>

    @Query("SELECT * FROM meal_logs ORDER BY date DESC, timestamp DESC")
    fun getAllLogs(): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(log: MealLog): Long

    @Delete
    suspend fun deleteMealLog(log: MealLog)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLogById(id: Long)
}
