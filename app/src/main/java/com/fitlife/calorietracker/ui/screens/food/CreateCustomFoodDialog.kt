package com.fitlife.calorietracker.ui.screens.food

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
import com.fitlife.calorietracker.data.model.FoodItem

import com.fitlife.calorietracker.data.model.SmartDefaults

@Composable
fun CreateCustomFoodDialog(
    initialName: String = "",
    onDismiss: () -> Unit,
    onSave: (FoodItem) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var brand by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("100g") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var fiberText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Gym Staples") }
    var hasManuallyEditedMacros by remember { mutableStateOf(false) }

    LaunchedEffect(caloriesText) {
        if (!hasManuallyEditedMacros) {
            val cal = caloriesText.toDoubleOrNull()
            if (cal != null && cal > 0) {
                val (p, c, f) = SmartDefaults.estimateMacrosFromCalories(cal)
                proteinText = p.toString()
                carbsText = c.toString()
                fatText = f.toString()
            }
        }
    }

    val categories = listOf("Proteins", "Carbs", "Fats", "Gym Staples", "Fruits & Veggies", "Indian Food", "Snacks", "Beverages")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "New Custom Food",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand / Maker (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = servingSize,
                    onValueChange = { servingSize = it },
                    label = { Text("Serving Size (e.g. 100g, 1 scoop)") },
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
                        onValueChange = { 
                            proteinText = it
                            hasManuallyEditedMacros = true 
                        },
                        label = { Text("Protein (g) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { 
                            carbsText = it
                            hasManuallyEditedMacros = true 
                        },
                        label = { Text("Carbs (g) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { 
                            fatText = it
                            hasManuallyEditedMacros = true 
                        },
                        label = { Text("Fat (g) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = fiberText,
                    onValueChange = { fiberText = it },
                    label = { Text("Fiber (g) (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val calories = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(caloriesText, default = 0.0, min = 1.0, max = 10_000.0)
                    val protein = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(proteinText, default = 0.0, min = 0.0, max = 1_000.0)
                    val carbs = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(carbsText, default = 0.0, min = 0.0, max = 1_000.0)
                    val fat = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(fatText, default = 0.0, min = 0.0, max = 1_000.0)
                    val fiber = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(fiberText, default = 0.0, min = 0.0, max = 500.0)

                    if (name.isNotBlank() && calories > 0) {
                        onSave(
                            FoodItem(
                                name = com.fitlife.calorietracker.data.model.ValidationUtils.cleanName(name),
                                brand = brand.ifBlank { "Custom" }.trim(),
                                servingSize = servingSize.ifBlank { "1 serving" }.trim(),
                                calories = calories,
                                proteinGrams = protein,
                                carbsGrams = carbs,
                                fatGrams = fat,
                                fiberGrams = fiber,
                                category = category,
                                isCustom = true
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save to Food Library", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
