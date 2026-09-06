package com.fitlife.calorietracker.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fitlife.calorietracker.data.model.FoodItem
import com.fitlife.calorietracker.data.model.MealType
import com.fitlife.calorietracker.ui.theme.CarbsColor
import com.fitlife.calorietracker.ui.theme.FatColor
import com.fitlife.calorietracker.ui.theme.ProteinColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodPortionDialog(
    food: FoodItem,
    initialMealType: MealType,
    onDismiss: () -> Unit,
    onConfirm: (servingMultiplier: Double, mealType: MealType) -> Unit
) {
    var servingsText by remember { mutableStateOf("1.0") }
    var isGramsMode by remember { mutableStateOf(false) }
    var gramsText by remember { mutableStateOf(food.servingWeightGrams.roundToInt().toString()) }
    var selectedMealType by remember { mutableStateOf(initialMealType) }

    val baseWeight = if (food.servingWeightGrams > 0) food.servingWeightGrams else 100.0
    val multiplier = if (isGramsMode) {
        val g = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(gramsText, default = baseWeight, min = 1.0, max = 5000.0)
        (g / baseWeight).coerceAtLeast(0.01)
    } else {
        com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(servingsText, default = 1.0, min = 0.01, max = 100.0)
    }
    val totalCalories = (food.calories * multiplier).roundToInt()
    val totalProtein = (food.proteinGrams * multiplier * 10).roundToInt() / 10.0
    val totalCarbs = (food.carbsGrams * multiplier * 10).roundToInt() / 10.0
    val totalFat = (food.fatGrams * multiplier * 10).roundToInt() / 10.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${food.brand} • 1 serving = ${food.servingSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Meal Type Selector Chips
                Text(
                    text = "Select Meal",
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
                            label = { Text(type.displayName, maxLines = 1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Selector: Servings vs Grams
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isGramsMode,
                        onClick = { isGramsMode = false },
                        label = { Text("By Servings") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isGramsMode,
                        onClick = { isGramsMode = true },
                        label = { Text("By Grams (Scale)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isGramsMode) {
                    // Servings Input
                    OutlinedTextField(
                        value = servingsText,
                        onValueChange = { servingsText = it },
                        label = { Text("Number of Servings") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Servings Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("0.5", "1.0", "1.5", "2.0", "3.0").forEach { preset ->
                            SuggestionChip(
                                onClick = { servingsText = preset },
                                label = { Text("${preset}x", maxLines = 1) }
                            )
                        }
                    }
                } else {
                    // Grams Input
                    OutlinedTextField(
                        value = gramsText,
                        onValueChange = { gramsText = it },
                        label = { Text("Weight in Grams (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Grams Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("50", "100", "150", "200", "250", "300").forEach { preset ->
                            SuggestionChip(
                                onClick = { gramsText = preset },
                                label = { Text("${preset}g", maxLines = 1) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculated Nutrients Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Calories",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$totalCalories kcal",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${totalProtein}g Protein",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ProteinColor
                                )
                            )
                            Text(
                                text = "${totalCarbs}g Carbs",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = CarbsColor
                                )
                            )
                            Text(
                                text = "${totalFat}g Fat",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = FatColor
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (multiplier > 0) {
                        onConfirm(multiplier, selectedMealType)
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
