package com.fitlife.calorietracker

import android.app.Application
import com.fitlife.calorietracker.data.local.AppDatabase
import com.fitlife.calorietracker.data.local.PresetFoods
import com.fitlife.calorietracker.data.repository.CalorieRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class CalorieTrackerApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: CalorieRepository
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        database = AppDatabase.getInstance(this)
        repository = CalorieRepository(database)
        instance = this

        // Automatically sync any newly added preset foods to the local Room database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingNames = database.foodDao().getAllFoodNames().toSet()
                val missingItems = PresetFoods.items.filter { it.name !in existingNames }
                if (missingItems.isNotEmpty()) {
                    database.foodDao().insertAll(missingItems)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync preset foods")
            }
        }
    }

    companion object {
        lateinit var instance: CalorieTrackerApp
            private set
    }
}
