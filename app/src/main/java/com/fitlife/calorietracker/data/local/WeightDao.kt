package com.fitlife.calorietracker.data.local

import androidx.room.*
import com.fitlife.calorietracker.data.model.WeightLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC, timestamp DESC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_logs ORDER BY date DESC, timestamp DESC LIMIT 1")
    fun getLatestWeightLog(): Flow<WeightLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLog): Long

    @Delete
    suspend fun deleteWeightLog(log: WeightLog)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLogById(id: Long)
}
