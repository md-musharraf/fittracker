package com.fitlife.calorietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fitlife.calorietracker.ui.navigation.MainAppScaffold
import com.fitlife.calorietracker.ui.theme.CalorieTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as CalorieTrackerApp).repository

        setContent {
            CalorieTrackerTheme {
                MainAppScaffold(repository = repository)
            }
        }
    }
}
