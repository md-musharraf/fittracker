package com.fitlife.calorietracker.data.local

import androidx.room.*
import com.fitlife.calorietracker.data.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_logs WHERE date = :date ORDER BY timestamp ASC")
    fun getWorkoutsForDate(date: String): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, timestamp ASC")
    fun getWorkoutsBetweenDates(startDate: String, endDate: String): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(log: WorkoutLog): Long

    @Delete
    suspend fun deleteWorkout(log: WorkoutLog)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteWorkoutById(id: Long)
}
