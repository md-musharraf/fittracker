package com.fitlife.calorietracker.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fitlife.calorietracker.data.model.FitnessCalculations

@Composable
fun AddWeightDialog(
    currentWeightKg: Double,
    heightCm: Double,
    onDismiss: () -> Unit,
    onConfirm: (weightKg: Double, notes: String) -> Unit
) {
    var weightText by remember { mutableStateOf(currentWeightKg.toString()) }
    var notes by remember { mutableStateOf("") }

    val enteredWeight = weightText.toDoubleOrNull() ?: currentWeightKg
    val liveBmi = FitnessCalculations.calculateBmi(enteredWeight, heightCm)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Log Body Weight",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Body Weight (kg) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Estimated BMI: ${liveBmi.bmi} (${liveBmi.category})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. Morning fasted / Post-workout)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weight = com.fitlife.calorietracker.data.model.ValidationUtils.safeParseDouble(weightText, default = 0.0, min = 20.0, max = 350.0)
                    if (weight >= 20.0) {
                        onConfirm(weight, notes.trim().take(120))
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Weight", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
