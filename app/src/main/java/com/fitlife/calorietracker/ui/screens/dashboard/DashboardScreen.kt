package com.fitlife.calorietracker.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlife.calorietracker.data.model.MealLog
import com.fitlife.calorietracker.data.model.MealType
import com.fitlife.calorietracker.data.model.SmartDefaults
import com.fitlife.calorietracker.ui.components.*
import com.fitlife.calorietracker.ui.screens.food.QuickAddDialog
import com.fitlife.calorietracker.ui.theme.*
import com.fitlife.calorietracker.ui.utils.FormatUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToFoodSearch: (mealType: String) -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToWorkouts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var quickAddMealType by remember { mutableStateOf<MealType?>(null) }

    val currentDate = try {
        LocalDate.parse(uiState.selectedDate)
    } catch (e: Exception) {
        LocalDate.now()
    }

    val dateFormatted = FormatUtils.formatDateHeading(uiState.selectedDate)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FitTrack",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = " PRO",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryOrange
                                )
                            )
                        }
                        Text(
                            text = uiState.greeting,
                            style = MaterialTheme.typography.labelSmall.copy(color = AccentGreen)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToGoals) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Goals & Macros",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
        ) {
            // Date Switcher Row
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.changeDate(-1)
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                        }

                        AnimatedContent(
                            targetState = dateFormatted,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "DateTextAnim"
                        ) { formattedText ->
                            Text(
                                text = formattedText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.changeDate(1)
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                        }
                    }
                }
            }

            // Consistency & Daily Streak Banner
            item {
                StreakBanner(streakInfo = uiState.streakInfo)
            }

            // Smart Daily Tip
            item {
                if (uiState.dailyTip.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryOrange.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.dailyTip,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Calorie Ring & Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Target Zone Status Badge
                        val budgetStatus = FormatUtils.getCalorieStatus(
                            uiState.totalCaloriesConsumed,
                            uiState.userProfile.dailyCalorieGoal
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = budgetStatus.color.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(budgetStatus.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = budgetStatus.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = budgetStatus.color
                                )
                            }
                        }

                        CalorieProgressRing(
                            calorieGoal = uiState.userProfile.dailyCalorieGoal,
                            caloriesConsumed = uiState.totalCaloriesConsumed,
                            caloriesBurned = uiState.totalCaloriesBurned
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Stats Row: Goal | Consumed | Burned
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CalorieStatItem(
                                label = "Base Goal",
                                value = "${uiState.userProfile.dailyCalorieGoal}",
                                subtext = "kcal",
                                valueColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            CalorieStatItem(
                                label = "Food Eaten",
                                value = "${uiState.totalCaloriesConsumed.roundToInt()}",
                                subtext = "kcal",
                                valueColor = PrimaryOrange,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            CalorieStatItem(
                                label = "Exercise Burn",
                                value = "${uiState.totalCaloriesBurned.roundToInt()}",
                                subtext = "kcal",
                                valueColor = AccentGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Macro targets
                        MacroSummaryRow(
                            proteinGrams = uiState.totalProteinConsumed,
                            proteinTarget = uiState.userProfile.proteinGoalGrams,
                            carbsGrams = uiState.totalCarbsConsumed,
                            carbsTarget = uiState.userProfile.carbsGoalGrams,
                            fatGrams = uiState.totalFatConsumed,
                            fatTarget = uiState.userProfile.fatGoalGrams
                        )
                    }
                }
            }

            // Workouts Activity Quick Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToWorkouts() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(WorkoutColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = "Workouts",
                                    tint = WorkoutColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Workouts & Energy",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (uiState.workouts.isNotEmpty()) {
                                        "${uiState.workouts.size} activities logged (${uiState.totalCaloriesBurned.roundToInt()} kcal burned)"
                                    } else {
                                        "No workouts logged today"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Workouts",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Hydration Card
            item {
                WaterTrackerWidget(
                    currentMl = uiState.waterLog?.amountMl ?: 0,
                    targetMl = uiState.userProfile.dailyWaterGoalMl,
                    onAddWater = { amount ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.addWater(amount)
                    },
                    onSubtractWater = { amount ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.subtractWater(amount)
                    }
                )
            }

            // Quick Re-Log Recent Foods
            if (uiState.recentFoods.isNotEmpty()) {
                item {
                    Text(
                        text = "Quick Re-Log ⚡",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.recentFoods.take(10), key = { it.id }) { recentItem ->
                            val autoMealType = SmartDefaults.detectMealType()
                            AssistChip(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.quickAddMeal(
                                        foodName = recentItem.foodName,
                                        calories = recentItem.calories,
                                        protein = recentItem.proteinGrams,
                                        carbs = recentItem.carbsGrams,
                                        fat = recentItem.fatGrams,
                                        mealType = autoMealType
                                    )
                                },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(
                                            text = recentItem.foodName,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${recentItem.calories.roundToInt()} kcal",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Replay,
                                        contentDescription = "Re-log",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Meals Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Meals & Food Intake",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (uiState.mealLogs.isEmpty()) {
                        TextButton(
                            onClick = { viewModel.copyYesterdayMeals(null) }
                        ) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Yesterday", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            val onMealDeleted: (MealLog) -> Unit = { item ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.deleteMealItem(item)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Removed ${item.foodName}",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDeleteMeal()
                    }
                }
            }

            // Breakfast Card
            item {
                MealCard(
                    mealType = MealType.BREAKFAST,
                    items = uiState.breakfastItems,
                    onAddFoodClick = { onNavigateToFoodSearch(MealType.BREAKFAST.name) },
                    onQuickAddClick = { quickAddMealType = MealType.BREAKFAST },
                    onDeleteItemClick = onMealDeleted,
                    onCopyYesterdayClick = { viewModel.copyYesterdayMeals(MealType.BREAKFAST) }
                )
            }

            // Lunch Card
            item {
                MealCard(
                    mealType = MealType.LUNCH,
                    items = uiState.lunchItems,
                    onAddFoodClick = { onNavigateToFoodSearch(MealType.LUNCH.name) },
                    onQuickAddClick = { quickAddMealType = MealType.LUNCH },
                    onDeleteItemClick = onMealDeleted,
                    onCopyYesterdayClick = { viewModel.copyYesterdayMeals(MealType.LUNCH) }
                )
            }

            // Dinner Card
            item {
                MealCard(
                    mealType = MealType.DINNER,
                    items = uiState.dinnerItems,
                    onAddFoodClick = { onNavigateToFoodSearch(MealType.DINNER.name) },
                    onQuickAddClick = { quickAddMealType = MealType.DINNER },
                    onDeleteItemClick = onMealDeleted,
                    onCopyYesterdayClick = { viewModel.copyYesterdayMeals(MealType.DINNER) }
                )
            }

            // Snacks Card
            item {
                MealCard(
                    mealType = MealType.SNACK,
                    items = uiState.snackItems,
                    onAddFoodClick = { onNavigateToFoodSearch(MealType.SNACK.name) },
                    onQuickAddClick = { quickAddMealType = MealType.SNACK },
                    onDeleteItemClick = onMealDeleted,
                    onCopyYesterdayClick = { viewModel.copyYesterdayMeals(MealType.SNACK) }
                )
            }
        }

        // Quick Add Dialog
        quickAddMealType?.let { mealType ->
            QuickAddDialog(
                initialMealType = mealType,
                onDismiss = { quickAddMealType = null },
                onConfirm = { name, calories, p, c, f, type ->
                    viewModel.quickAddMeal(name, calories, p, c, f, type)
                    quickAddMealType = null
                }
            )
        }
    }
}

@Composable
fun CalorieStatItem(
    label: String,
    value: String,
    subtext: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                ),
                color = valueColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp),
                maxLines = 1
            )
        }
    }
}
