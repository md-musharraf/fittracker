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
import com.fitlife.calorietracker.ui.screens.food.EditMealDialog
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
    var mealToEdit by remember { mutableStateOf<MealLog?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { (message, actionLabel) ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (actionLabel != null) {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    if (actionLabel == "Undo") {
                        if (message.startsWith("Added")) {
                            viewModel.undoAddMeal()
                        } else if (message.startsWith("Removed")) {
                            viewModel.undoDeleteMeal()
                        }
                    }
                }
            } else {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    val currentDate = try {
        LocalDate.parse(uiState.selectedDate)
    } catch (e: Exception) {
        LocalDate.now()
    }

    val dateFormatted = FormatUtils.formatDateHeading(uiState.selectedDate)
    val recentFoodsList = remember(uiState.recentFoods) { uiState.recentFoods.take(10) }

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
            item(key = "date_switcher", contentType = "date_switcher") {
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
            item(key = "streak_banner", contentType = "streak") {
                StreakBanner(streakInfo = uiState.streakInfo)
            }

            // Smart Daily Tip
            if (uiState.dailyTip.isNotEmpty()) {
                item(key = "daily_tip", contentType = "tip") {
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
            item(key = "calorie_summary", contentType = "calorie_summary") {
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

                        // Calorie Split Breakdown by Meal
                        if (uiState.totalCaloriesConsumed > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Meal Calorie Split",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Daily Intake %",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val dist = uiState.mealDistribution
                                val splitItems = listOf(
                                    Triple("Bfast", dist[MealType.BREAKFAST] ?: 0, Color(0xFFFBBF24)),
                                    Triple("Lunch", dist[MealType.LUNCH] ?: 0, Color(0xFFF97316)),
                                    Triple("Dinner", dist[MealType.DINNER] ?: 0, Color(0xFFA855F7)),
                                    Triple("Snack", dist[MealType.SNACK] ?: 0, Color(0xFF38BDF8))
                                )
                                splitItems.forEach { (name, pct, color) ->
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(name, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
                                            Text(
                                                "$pct%",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Workouts Activity Quick Card
            item(key = "workouts_card", contentType = "workouts_card") {
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
            item(key = "hydration_card", contentType = "hydration_card") {
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
            if (recentFoodsList.isNotEmpty()) {
                item(key = "recent_foods_header", contentType = "section_header") {
                    Text(
                        text = "Quick Re-Log ⚡",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item(key = "recent_foods_row", contentType = "recent_foods_row") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(recentFoodsList, key = { it.id }) { recentItem ->
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
            item(key = "meals_header", contentType = "section_header") {
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
            }

            // Breakfast Card
            item(key = "meal_breakfast", contentType = "meal_card") {
                MealCard(
                    mealType = MealType.BREAKFAST,
                    items = uiState.breakfastItems,
                    onAddFoodClick = { onNavigateToFoodSearch(MealType.BREAKFAST.name) },
                    onQuickAddClick = { quickAddMealType = MealType.BREAKFAST },
                    onDeleteItemClick = onMealDeleted,
                    onEditItemClick = { mealToEdit = it },
                    onClearMealTypeClick = { viewModel.clearMealType(MealType.BREAKFAST) },
                    onCopyYesterdayClick = { viewModel.copyYesterdayMeals(MealType.BREAKFAST) }
                )
            }

            // Lunch Card
            item(key = "meal_lunch", contentType = "meal_card") {
                MealCard(
                    mealType = MealType.LUNCH,
                    items = uiState.lunchItems,
                    onAddFoodClick = { onNavigateToFoodSearch(MealType.LUNCH.name) },
                    onQuickAddClick = { quickAddMealType = MealType.LUNCH },
                    onDeleteItemClick = onMealDeleted,
                    onEditItemClick = { mealToEdit = it },
                    onClearMealTypeClick = { viewModel.clearMealType(MealType.LUNCH) },
                    onCopyYesterdayClick = { viewModel.copyYesterdayMeals(MealType.LUNCH) }
                )
            }

            // Dinner Card
            item(key = "meal_dinner", contentType = "meal_card") {
                MealCard(
                    mealType = MealType.DINNER,
                    items = uiState.dinnerItems,
                    onAddFoodClick = { onNavigateToFoodSearch(MealType.DINNER.name) },
                    onQuickAddClick = { quickAddMealType = MealType.DINNER },
                    onDeleteItemClick = onMealDeleted,
                    onEditItemClick = { mealToEdit = it },
                    onClearMealTypeClick = { viewModel.clearMealType(MealType.DINNER) },
                    onCopyYesterdayClick = { viewModel.copyYesterdayMeals(MealType.DINNER) }
                )
            }

            // Snacks Card
            item(key = "meal_snack", contentType = "meal_card") {
                MealCard(
                    mealType = MealType.SNACK,
                    items = uiState.snackItems,
                    onAddFoodClick = { onNavigateToFoodSearch(MealType.SNACK.name) },
                    onQuickAddClick = { quickAddMealType = MealType.SNACK },
                    onDeleteItemClick = onMealDeleted,
                    onEditItemClick = { mealToEdit = it },
                    onClearMealTypeClick = { viewModel.clearMealType(MealType.SNACK) },
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

        // Edit Meal Dialog
        mealToEdit?.let { log ->
            EditMealDialog(
                mealLog = log,
                onDismiss = { mealToEdit = null },
                onSave = { updated ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.updateMeal(updated)
                    mealToEdit = null
                },
                onDelete = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.deleteMealItem(log)
                    mealToEdit = null
                }
            )
        }

        // Duplicate Warning Alert Dialog
        viewModel.duplicateWarningCandidate?.let { candidate ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissDuplicateWarning() },
                icon = { Icon(Icons.Default.WarningAmber, contentDescription = null, tint = PrimaryOrange) },
                title = { Text("Duplicate Meal Logged") },
                text = {
                    Text("You already logged '${candidate.foodName}' under ${candidate.mealType} a moment ago.\n\nDid you mean to add another serving?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.confirmAddDuplicate()
                        }
                    ) {
                        Text("Yes, Log Again")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDuplicateWarning() }) {
                        Text("Cancel")
                    }
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
