package com.fitlife.calorietracker.data.local

import androidx.room.*
import com.fitlife.calorietracker.data.model.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    fun getWaterForDate(date: String): Flow<WaterLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWater(log: WaterLog)

    @Query("UPDATE water_logs SET amountMl = amountMl + :addedMl WHERE date = :date")
    suspend fun addWater(date: String, addedMl: Int): Int
}
