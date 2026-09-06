package com.fitlife.calorietracker.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.fitlife.calorietracker.data.model.SmartDefaults
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fitlife.calorietracker.data.model.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddDialog(
    initialMealType: MealType,
    onDismiss: () -> Unit,
    onConfirm: (
        foodName: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        mealType: MealType
    ) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf(initialMealType) }

    // Auto-estimate macros when user enters calories (if they haven't manually entered macros)
    var hasManuallyEditedMacros by remember { mutableStateOf(false) }
    
    LaunchedEffect(caloriesText) {
        if (!hasManuallyEditedMacros) {
            val cal = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(caloriesText, default = 0.0)
            if (cal > 0) {
                val (p, c, f) = SmartDefaults.estimateMacrosFromCalories(cal)
                proteinText = p.toString()
                carbsText = c.toString()
                fatText = f.toString()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Quick Calorie Add",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Meal Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MealType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type },
                            label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Item Name (Optional)") },
                    placeholder = { Text("e.g. Quick Lunch / Protein Bar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it },
                    label = { Text("Calories (kcal) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it; hasManuallyEditedMacros = true },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it; hasManuallyEditedMacros = true },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it; hasManuallyEditedMacros = true },
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
                    val calories = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(
                        caloriesText, default = 0.0, min = 1.0, max = 10_000.0
                    )
                    if (calories > 0) {
                        val name = com.fitlife.calorietracker.data.model.ValidationUtils.cleanName(foodName, "Quick Food")
                        val p = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(proteinText, default = 0.0, min = 0.0, max = 1_000.0)
                        val c = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(carbsText, default = 0.0, min = 0.0, max = 1_000.0)
                        val f = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(fatText, default = 0.0, min = 0.0, max = 1_000.0)
                        onConfirm(name, calories, p, c, f, selectedMealType)
                    }
                }
            ) {
                Text("Log to ${selectedMealType.displayName}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
