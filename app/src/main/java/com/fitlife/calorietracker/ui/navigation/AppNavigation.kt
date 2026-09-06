package com.fitlife.calorietracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.fitlife.calorietracker.data.model.MealType
import com.fitlife.calorietracker.data.model.SmartDefaults
import com.fitlife.calorietracker.data.repository.CalorieRepository
import com.fitlife.calorietracker.ui.screens.dashboard.DashboardScreen
import com.fitlife.calorietracker.ui.screens.dashboard.DashboardViewModel
import com.fitlife.calorietracker.ui.screens.food.FoodLogScreen
import com.fitlife.calorietracker.ui.screens.food.FoodLogViewModel
import com.fitlife.calorietracker.ui.screens.goals.GoalsBmiScreen
import com.fitlife.calorietracker.ui.screens.goals.GoalsViewModel
import com.fitlife.calorietracker.ui.screens.progress.ProgressScreen
import com.fitlife.calorietracker.ui.screens.progress.ProgressViewModel
import com.fitlife.calorietracker.ui.screens.workout.WorkoutScreen
import com.fitlife.calorietracker.ui.screens.workout.WorkoutViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Today", Icons.Default.Dashboard)
    object FoodLog : Screen("food_log?mealType={mealType}", "Food", Icons.Default.Restaurant) {
        fun createRoute(mealType: String = MealType.BREAKFAST.name) = "food_log?mealType=$mealType"
    }
    object Workouts : Screen("workouts", "Workouts", Icons.Default.FitnessCenter)
    object Goals : Screen("goals", "Goals & BMI", Icons.Default.Calculate)
    object Progress : Screen("progress", "Progress", Icons.Default.ShowChart)
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.FoodLog,
    Screen.Workouts,
    Screen.Goals,
    Screen.Progress
)

@Composable
fun MainAppScaffold(
    repository: CalorieRepository,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                BottomNavItems.forEach { screen ->
                    val isSelected = currentRoute?.startsWith(screen.route.substringBefore("?")) == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                val destination = if (screen == Screen.FoodLog) {
                                    Screen.FoodLog.createRoute(SmartDefaults.detectMealType().name)
                                } else {
                                    screen.route
                                }
                                navController.navigate(destination) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Dashboard.route) {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    DashboardViewModel(repository)
                }
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToFoodSearch = { mealType ->
                        navController.navigate(Screen.FoodLog.createRoute(mealType))
                    },
                    onNavigateToGoals = {
                        navController.navigate(Screen.Goals.route)
                    },
                    onNavigateToWorkouts = {
                        navController.navigate(Screen.Workouts.route)
                    }
                )
            }

            composable(
                route = Screen.FoodLog.route,
                arguments = listOf(
                    navArgument("mealType") {
                        type = NavType.StringType
                        defaultValue = SmartDefaults.detectMealType().name
                    }
                )
            ) { backStackEntry ->
                val mealType = backStackEntry.arguments?.getString("mealType") ?: MealType.BREAKFAST.name
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    FoodLogViewModel(repository)
                }
                FoodLogScreen(
                    viewModel = viewModel,
                    defaultMealType = mealType,
                    onFoodLogged = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Workouts.route) {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    WorkoutViewModel(repository)
                }
                WorkoutScreen(viewModel = viewModel)
            }

            composable(Screen.Goals.route) {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    GoalsViewModel(repository)
                }
                GoalsBmiScreen(viewModel = viewModel)
            }

            composable(Screen.Progress.route) {
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                    ProgressViewModel(repository)
                }
                ProgressScreen(viewModel = viewModel)
            }
        }
    }
}
