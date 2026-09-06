package com.fitlife.calorietracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fitlife.calorietracker.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FoodItem::class,
        MealLog::class,
        WorkoutLog::class,
        WaterLog::class,
        WeightLog::class,
        UserProfile::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun waterDao(): WaterDao
    abstract fun weightDao(): WeightDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_meal_logs_date ON meal_logs(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_meal_logs_timestamp ON meal_logs(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_meal_logs_mealType ON meal_logs(mealType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_logs_date ON workout_logs(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_weight_logs_date ON weight_logs(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_foods_category ON foods(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_foods_name ON foods(name)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calorie_tracker.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    // Prepopulate foods
                    database.foodDao().insertAll(PresetFoods.items)

                    // Prepopulate default user profile
                    val defaultProfile = UserProfile(
                        id = 1,
                        name = "Athlete",
                        gender = "Male",
                        age = 26,
                        heightCm = 175.0,
                        currentWeightKg = 75.0,
                        targetWeightKg = 72.0,
                        persona = PersonaType.GYM_GUY,
                        activityLevel = ActivityLevel.MODERATELY_ACTIVE,
                        goalType = GoalType.LOSE_WEIGHT_MODERATE,
                        dailyCalorieGoal = 2200,
                        proteinGoalGrams = 165,
                        carbsGoalGrams = 220,
                        fatGoalGrams = 60,
                        dailyWaterGoalMl = 3500
                    )
                    database.userProfileDao().insertOrUpdateProfile(defaultProfile)

                    // Prepopulate initial weight log
                    val initialBmi = FitnessCalculations.calculateBmi(75.0, 175.0).bmi
                    val today = java.time.LocalDate.now().toString()
                    database.weightDao().insertWeightLog(
                        WeightLog(
                            date = today,
                            weightKg = 75.0,
                            bmi = initialBmi,
                            notes = "Starting weight check-in"
                        )
                    )
                }
            }
        }
    }
}
