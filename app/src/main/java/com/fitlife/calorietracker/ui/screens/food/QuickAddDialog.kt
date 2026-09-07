package com.fitlife.calorietracker.ui.screens.food

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    var selectedMealType by remember { mutableStateOf(initialMealType) }
    var foodName by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var hasManuallyEditedMacros by remember { mutableStateOf(false) }

    LaunchedEffect(caloriesText) {
        val calories = caloriesText.toDoubleOrNull()
        if (calories != null && calories > 0 && !hasManuallyEditedMacros) {
            val autoP = (calories * 0.25 / 4.0).toInt()
            val autoC = (calories * 0.50 / 4.0).toInt()
            val autoF = (calories * 0.25 / 9.0).toInt()
            proteinText = autoP.toString()
            carbsText = autoC.toString()
            fatText = autoF.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type },
                            label = { Text(type.displayName, maxLines = 1, style = MaterialTheme.typography.labelSmall) }
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
                }
                
                OutlinedTextField(
                    value = fatText,
                    onValueChange = { fatText = it; hasManuallyEditedMacros = true },
                    label = { Text("Fat (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Log to ${selectedMealType.displayName}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
