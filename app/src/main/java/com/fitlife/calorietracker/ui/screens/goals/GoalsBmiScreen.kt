package com.fitlife.calorietracker.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlife.calorietracker.data.model.*
import com.fitlife.calorietracker.ui.components.BmiGauge
import com.fitlife.calorietracker.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsBmiScreen(
    viewModel: GoalsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.userProfile

    // Form states
    var name by remember(profile) { mutableStateOf(profile.name) }
    var gender by remember(profile) { mutableStateOf(profile.gender) }
    var ageText by remember(profile) { mutableStateOf(profile.age.toString()) }
    var heightText by remember(profile) { mutableStateOf(profile.heightCm.toString()) }
    var weightText by remember(profile) { mutableStateOf(profile.currentWeightKg.toString()) }
    var targetWeightText by remember(profile) { mutableStateOf(profile.targetWeightKg.toString()) }
    var selectedPersona by remember(profile) { mutableStateOf(profile.persona) }
    var selectedActivity by remember(profile) { mutableStateOf(profile.activityLevel) }
    var selectedGoal by remember(profile) { mutableStateOf(profile.goalType) }

    var calorieGoalText by remember(profile) { mutableStateOf(profile.dailyCalorieGoal.toString()) }
    var proteinGoalText by remember(profile) { mutableStateOf(profile.proteinGoalGrams.toString()) }
    var carbsGoalText by remember(profile) { mutableStateOf(profile.carbsGoalGrams.toString()) }
    var fatGoalText by remember(profile) { mutableStateOf(profile.fatGoalGrams.toString()) }
    var waterGoalText by remember(profile) { mutableStateOf(profile.dailyWaterGoalMl.toString()) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            snackbarHostState.showSnackbar("Goals and Profile successfully updated!")
            viewModel.clearSavedSuccess()
        }
    }

    // Dynamic BMI calculation based on current input values
    val currentWeight = weightText.toDoubleOrNull() ?: profile.currentWeightKg
    val currentHeight = heightText.toDoubleOrNull() ?: profile.heightCm
    val liveBmi = FitnessCalculations.calculateBmi(currentWeight, currentHeight)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BMI & Goal Tracking",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
        ) {
            // BMI Gauge Card
            item(key = "bmi_gauge", contentType = "bmi_gauge") {
                BmiGauge(bmiResult = liveBmi)
            }

            // Body Metrics Input Card
            item(key = "body_metrics", contentType = "body_metrics") {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Body & Biometric Stats",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Gender Chips & Age
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = gender == "Male",
                                onClick = { gender = "Male" },
                                label = { Text("Male") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = gender == "Female",
                                onClick = { gender = "Female" },
                                label = { Text("Female") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = ageText,
                                onValueChange = { ageText = it },
                                label = { Text("Age") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Height & Weight Inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = heightText,
                                onValueChange = { heightText = it },
                                label = { Text("Height (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = { Text("Weight (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = targetWeightText,
                            onValueChange = { targetWeightText = it },
                            label = { Text("Target Goal Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Energy expenditure metrics info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BMR (Basal Rate)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${uiState.bmr.roundToInt()} kcal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryOrange)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TDEE (Daily Burn)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${uiState.tdee.roundToInt()} kcal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = AccentGreen)
                            }
                        }
                    }
                }
            }

            // Persona Selection (Gym Guy, Sports Person, Normal Person)
            item(key = "persona_selection", contentType = "persona_selection") {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Fitness Persona & Lifestyle",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tailors macro ratios and nutritional balance to your lifestyle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        PersonaType.values().forEach { persona ->
                            val isSelected = selectedPersona == persona
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedPersona = persona
                                        // Update recommendation numbers
                                        val rec = FitnessCalculations.calculateRecommendations(
                                            currentWeight,
                                            currentHeight,
                                            ageText.toIntOrNull() ?: 26,
                                            gender,
                                            selectedActivity,
                                            persona,
                                            selectedGoal
                                        )
                                        calorieGoalText = rec.dailyCalories.toString()
                                        proteinGoalText = rec.proteinGrams.toString()
                                        carbsGoalText = rec.carbsGrams.toString()
                                        fatGoalText = rec.fatGrams.toString()
                                        waterGoalText = rec.waterMl.toString()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedPersona = persona }
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = persona.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = persona.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Goal Type Selection
            item(key = "goal_type_selection", contentType = "goal_type_selection") {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Calorie & Weight Goal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        GoalType.values().forEach { goal ->
                            val isSelected = selectedGoal == goal
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedGoal = goal
                                        val rec = FitnessCalculations.calculateRecommendations(
                                            currentWeight,
                                            currentHeight,
                                            ageText.toIntOrNull() ?: 26,
                                            gender,
                                            selectedActivity,
                                            selectedPersona,
                                            goal
                                        )
                                        calorieGoalText = rec.dailyCalories.toString()
                                        proteinGoalText = rec.proteinGrams.toString()
                                        carbsGoalText = rec.carbsGrams.toString()
                                        fatGoalText = rec.fatGrams.toString()
                                        waterGoalText = rec.waterMl.toString()
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedGoal = goal }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = goal.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Daily Targets & Macros
            item(key = "daily_targets", contentType = "daily_targets") {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Targets & Macro Split",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    val rec = FitnessCalculations.calculateRecommendations(
                                        currentWeight,
                                        currentHeight,
                                        ageText.toIntOrNull() ?: 26,
                                        gender,
                                        selectedActivity,
                                        selectedPersona,
                                        selectedGoal
                                    )
                                    calorieGoalText = rec.dailyCalories.toString()
                                    proteinGoalText = rec.proteinGrams.toString()
                                    carbsGoalText = rec.carbsGrams.toString()
                                    fatGoalText = rec.fatGrams.toString()
                                    waterGoalText = rec.waterMl.toString()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Auto-Tune ⚡",
                                    maxLines = 1,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = calorieGoalText,
                            onValueChange = { calorieGoalText = it },
                            label = { Text("Daily Calorie Target (kcal) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = proteinGoalText,
                                onValueChange = { proteinGoalText = it },
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = carbsGoalText,
                                onValueChange = { carbsGoalText = it },
                                label = { Text("Carbs (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = fatGoalText,
                                onValueChange = { fatGoalText = it },
                                label = { Text("Fat (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = waterGoalText,
                            onValueChange = { waterGoalText = it },
                            label = { Text("Daily Water Hydration Goal (ml)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Save Goals Button
            item(key = "save_goals_button", contentType = "save_button") {
                Button(
                    onClick = {
                        val age = ValidationUtils.safeParseInt(ageText, default = 26, min = 12, max = 110)
                        val height = ValidationUtils.safeParseDouble(heightText, default = 175.0, min = 50.0, max = 260.0)
                        val weight = ValidationUtils.safeParseDouble(weightText, default = 75.0, min = 20.0, max = 350.0)
                        val targetWeight = ValidationUtils.safeParseDouble(targetWeightText, default = 72.0, min = 20.0, max = 350.0)
                        val cal = ValidationUtils.safeParseInt(calorieGoalText, default = 2200, min = 500, max = 10000)
                        val p = ValidationUtils.safeParseInt(proteinGoalText, default = 160, min = 20, max = 500)
                        val c = ValidationUtils.safeParseInt(carbsGoalText, default = 220, min = 20, max = 800)
                        val f = ValidationUtils.safeParseInt(fatGoalText, default = 60, min = 10, max = 300)
                        val water = ValidationUtils.safeParseInt(waterGoalText, default = 3500, min = 500, max = 10000)

                        viewModel.updateProfile(
                            name = name,
                            gender = gender,
                            age = age,
                            heightCm = height,
                            weightKg = weight,
                            targetWeightKg = targetWeight,
                            persona = selectedPersona,
                            activityLevel = selectedActivity,
                            goalType = selectedGoal,
                            dailyCalorieGoal = cal,
                            proteinGoalGrams = p,
                            carbsGoalGrams = c,
                            fatGoalGrams = f,
                            dailyWaterGoalMl = water
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Goals & Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Data Backup & Export Section
            item(key = "data_backup_section", contentType = "data_backup") {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Data Safety & Export",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your fitness data is stored securely on your local device. You can export or backup your data summary anytime.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        var showExportDialog by remember { mutableStateOf(false) }
                        var exportContent by remember { mutableStateOf("") }
                        val coroutineScope = rememberCoroutineScope()
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    exportContent = viewModel.getExportSummary()
                                    showExportDialog = true
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export / View Fitness Summary")
                        }

                        if (showExportDialog) {
                            AlertDialog(
                                onDismissRequest = { showExportDialog = false },
                                title = { Text("Fitness Data Summary") },
                                text = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 280.dp)
                                    ) {
                                        androidx.compose.foundation.lazy.LazyColumn {
                                            item {
                                                Text(
                                                    text = exportContent,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(exportContent))
                                            showExportDialog = false
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Summary copied to clipboard!")
                                            }
                                        }
                                    ) {
                                        Text("Copy to Clipboard")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showExportDialog = false }) {
                                        Text("Close")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
