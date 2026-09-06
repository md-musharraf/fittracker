package com.fitlife.calorietracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlife.calorietracker.ui.theme.WaterColor
import kotlin.math.roundToInt

@Composable
fun WaterTrackerWidget(
    currentMl: Int,
    targetMl: Int,
    onAddWater: (Int) -> Unit,
    onSubtractWater: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progress = if (targetMl > 0) (currentMl.toFloat() / targetMl.toFloat()) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "WaterProgress"
    )

    var showCustomWaterDialog by remember { mutableStateOf(false) }

    if (showCustomWaterDialog) {
        var customAmountText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomWaterDialog = false },
            icon = { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WaterColor) },
            title = { Text("Log Custom Water") },
            text = {
                Column {
                    Text("Enter amount drank in milliliters (ml):", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { customAmountText = it },
                        placeholder = { Text("e.g. 350") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(150, 350, 500, 1000).forEach { preset ->
                            AssistChip(
                                onClick = { customAmountText = preset.toString() },
                                label = { Text("${preset}ml", fontSize = 10.sp, maxLines = 1, softWrap = false) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = customAmountText.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            onAddWater(parsed.coerceIn(10, 5000))
                        }
                        showCustomWaterDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomWaterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WaterColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Water",
                            tint = WaterColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Hydration",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$currentMl / $targetMl ml",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = WaterColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .drawBehind {
                        drawRect(color = surfaceVariant)
                        if (animatedProgress > 0f) {
                            drawRect(
                                color = WaterColor,
                                size = androidx.compose.ui.geometry.Size(
                                    width = size.width * animatedProgress,
                                    height = size.height
                                )
                            )
                        }
                    }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Add & Adjust Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onSubtractWater != null) {
                    OutlinedButton(
                        onClick = { onSubtractWater(250) },
                        enabled = currentMl > 0,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "250ml", fontSize = 11.sp)
                    }
                }

                OutlinedButton(
                    onClick = { onAddWater(250) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "+250ml", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { onAddWater(500) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "+500ml", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { showCustomWaterDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "Custom", fontSize = 11.sp)
                }
            }
        }
    }
}
