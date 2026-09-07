package com.fitlife.calorietracker.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.fitlife.calorietracker.data.model.MealLog
import com.fitlife.calorietracker.data.model.MealType
import com.fitlife.calorietracker.data.model.ValidationUtils
import com.fitlife.calorietracker.ui.theme.CarbsColor
import com.fitlife.calorietracker.ui.theme.FatColor
import com.fitlife.calorietracker.ui.theme.PrimaryOrange
import com.fitlife.calorietracker.ui.theme.ProteinColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMealDialog(
    mealLog: MealLog,
    onDismiss: () -> Unit,
    onSave: (MealLog) -> Unit,
    onDelete: () -> Unit
) {
    // Current meal type
    var selectedMealType by remember {
        mutableStateOf(
            try {
                MealType.valueOf(mealLog.mealType)
            } catch (e: Exception) {
                MealType.BREAKFAST
            }
        )
    }

    // Base values per 1.0 serving
    val initialServing = mealLog.servingCount.coerceAtLeast(0.01)
    val baseCalories = mealLog.calories / initialServing
    val baseProtein = mealLog.proteinGrams / initialServing
    val baseCarbs = mealLog.carbsGrams / initialServing
    val baseFat = mealLog.fatGrams / initialServing

    var servingsText by remember { mutableStateOf(initialServing.toString()) }
    var caloriesText by remember { mutableStateOf(mealLog.calories.roundToInt().toString()) }
    var proteinText by remember { mutableStateOf((mealLog.proteinGrams * 10).roundToInt().let { it / 10.0 }.toString()) }
    var carbsText by remember { mutableStateOf((mealLog.carbsGrams * 10).roundToInt().let { it / 10.0 }.toString()) }
    var fatText by remember { mutableStateOf((mealLog.fatGrams * 10).roundToInt().let { it / 10.0 }.toString()) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    fun updateFromMultiplier(newServing: Double) {
        val clampedServing = newServing.coerceIn(0.1, 50.0)
        servingsText = ((clampedServing * 10).roundToInt() / 10.0).toString()
        caloriesText = (baseCalories * clampedServing).roundToInt().toString()
        proteinText = ((baseProtein * clampedServing * 10).roundToInt() / 10.0).toString()
        carbsText = ((baseCarbs * clampedServing * 10).roundToInt() / 10.0).toString()
        fatText = ((baseFat * clampedServing * 10).roundToInt() / 10.0).toString()
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete This Meal?") },
            text = { Text("Are you sure you want to remove '${mealLog.foodName}' from your log?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Edit Logged Food",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = mealLog.foodName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryOrange
                    )
                }
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Meal")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // 1. Move to different meal type (e.g. accidental Breakfast -> Lunch)
                Text(
                    text = "Meal Type (Tap to move)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type },
                            label = { Text(type.displayName, maxLines = 1) },
                            leadingIcon = if (selectedMealType == type) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Servings Adjuster
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Serving Size / Multiplier",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    // Quick stepper buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedIconButton(
                            onClick = {
                                val cur = ValidationUtils.safeParseDouble(servingsText, default = 1.0)
                                updateFromMultiplier(cur - 0.5)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedIconButton(
                            onClick = {
                                val cur = ValidationUtils.safeParseDouble(servingsText, default = 1.0)
                                updateFromMultiplier(cur + 0.5)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Serving Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.5, 1.0, 1.5, 2.0, 3.0).forEach { preset ->
                        AssistChip(
                            onClick = { updateFromMultiplier(preset) },
                            label = { Text("${preset}x") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Servings text input
                OutlinedTextField(
                    value = servingsText,
                    onValueChange = {
                        servingsText = it
                        val parsed = ValidationUtils.safeParseDouble(it, default = 1.0)
                        if (parsed > 0) {
                            caloriesText = (baseCalories * parsed).roundToInt().toString()
                            proteinText = ((baseProtein * parsed * 10).roundToInt() / 10.0).toString()
                            carbsText = ((baseCarbs * parsed * 10).roundToInt() / 10.0).toString()
                            fatText = ((baseFat * parsed * 10).roundToInt() / 10.0).toString()
                        }
                    },
                    label = { Text("Servings (${mealLog.servingUnit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Nutrition Breakdown & Custom Adjustments
                Text(
                    text = "Nutrition Values",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = caloriesText,
                        onValueChange = { caloriesText = it },
                        label = { Text("Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalCalories = ValidationUtils.sanitizeCalories(
                        ValidationUtils.safeParseDouble(caloriesText, default = mealLog.calories)
                    )
                    val finalProtein = ValidationUtils.sanitizeMacro(
                        ValidationUtils.safeParseDouble(proteinText, default = mealLog.proteinGrams)
                    )
                    val finalCarbs = ValidationUtils.sanitizeMacro(
                        ValidationUtils.safeParseDouble(carbsText, default = mealLog.carbsGrams)
                    )
                    val finalFat = ValidationUtils.sanitizeMacro(
                        ValidationUtils.safeParseDouble(fatText, default = mealLog.fatGrams)
                    )
                    val finalServing = ValidationUtils.safeParseDouble(servingsText, default = 1.0)
                        .coerceIn(ValidationUtils.MIN_SERVING, ValidationUtils.MAX_SERVING)

                    val updated = mealLog.copy(
                        mealType = selectedMealType.name,
                        servingCount = finalServing,
                        calories = finalCalories,
                        proteinGrams = finalProtein,
                        carbsGrams = finalCarbs,
                        fatGrams = finalFat
                    )
                    onSave(updated)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
