package com.fitlife.calorietracker.ui.screens.workout

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fitlife.calorietracker.data.model.WorkoutLog
import kotlin.math.roundToInt

data class ExercisePreset(val name: String, val baseMet: Double)

val CommonExercises = listOf(
    ExercisePreset("Weightlifting / Gym Strength", 6.0),
    ExercisePreset("HIIT / Circuit Training", 8.5),
    ExercisePreset("Running / Outdoor Jogging", 9.8),
    ExercisePreset("Treadmill / Incline Walk", 5.0),
    ExercisePreset("Cycling / Spin Class", 7.5),
    ExercisePreset("Swimming (Freestyle/Laps)", 8.0),
    ExercisePreset("Calisthenics / Bodyweight", 5.5),
    ExercisePreset("Walking / Daily Steps", 3.3),
    ExercisePreset("Boxing / Martial Arts", 9.0),
    ExercisePreset("Football / Soccer / Sports", 8.0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutDialog(
    userWeightKg: Double,
    onDismiss: () -> Unit,
    onConfirm: (WorkoutLog) -> Unit
) {
    var selectedExercise by remember { mutableStateOf(CommonExercises[0]) }
    var durationMinutesText by remember { mutableStateOf("45") }
    var selectedIntensity by remember { mutableStateOf("Moderate") }
    var notes by remember { mutableStateOf("") }
    var customCaloriesText by remember { mutableStateOf("") }
    var exerciseMenuExpanded by remember { mutableStateOf(false) }

    val intensityMultiplier = when (selectedIntensity) {
        "Light" -> 0.8
        "Moderate" -> 1.0
        "High" -> 1.25
        "Extreme" -> 1.5
        else -> 1.0
    }

    val durationMin = durationMinutesText.toIntOrNull() ?: 0
    val estimatedCalories = ((selectedExercise.baseMet * intensityMultiplier * 3.5 * userWeightKg / 200.0) * durationMin).roundToInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Log Workout / Activity",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Exercise selection dropdown
                ExposedDropdownMenuBox(
                    expanded = exerciseMenuExpanded,
                    onExpandedChange = { exerciseMenuExpanded = !exerciseMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedExercise.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Exercise Activity") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exerciseMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = exerciseMenuExpanded,
                        onDismissRequest = { exerciseMenuExpanded = false }
                    ) {
                        CommonExercises.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset.name) },
                                onClick = {
                                    selectedExercise = preset
                                    exerciseMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Duration in minutes
                OutlinedTextField(
                    value = durationMinutesText,
                    onValueChange = { durationMinutesText = it },
                    label = { Text("Duration (Minutes) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Intensity chips
                Text(
                    text = "Workout Intensity",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Light", "Moderate", "High", "Extreme").forEach { intensity ->
                        FilterChip(
                            selected = selectedIntensity == intensity,
                            onClick = { selectedIntensity = intensity },
                            label = { Text(intensity, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Calories Burned Calculation & Override
                OutlinedTextField(
                    value = customCaloriesText,
                    onValueChange = { customCaloriesText = it },
                    label = { Text("Calories Burned (Estimated: $estimatedCalories kcal)") },
                    placeholder = { Text("$estimatedCalories") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Workout notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Workout Notes (e.g. Chest & Triceps / 5k)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    val safeDuration = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseInt(durationMinutesText, default = 0, min = 1, max = 720)
                    if (safeDuration > 0) {
                        val customCal = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(customCaloriesText, default = 0.0, min = 1.0, max = 10_000.0)
                        val calories = if (customCal > 0) customCal else estimatedCalories.toDouble()
                        val today = java.time.LocalDate.now().toString()
                        onConfirm(
                            WorkoutLog(
                                date = today,
                                exerciseName = selectedExercise.name,
                                durationMinutes = safeDuration,
                                intensity = selectedIntensity,
                                caloriesBurned = calories,
                                notes = notes.trim().take(120)
                            )
                        )
                    }
                }
            ) {
                Text("Log Activity", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
