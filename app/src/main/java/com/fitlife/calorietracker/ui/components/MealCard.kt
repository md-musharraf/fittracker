package com.fitlife.calorietracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlife.calorietracker.data.model.MealLog
import com.fitlife.calorietracker.data.model.MealType
import com.fitlife.calorietracker.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun MealCard(
    mealType: MealType,
    items: List<MealLog>,
    onAddFoodClick: () -> Unit,
    onQuickAddClick: () -> Unit,
    onDeleteItemClick: (MealLog) -> Unit,
    onEditItemClick: ((MealLog) -> Unit)? = null,
    onClearMealTypeClick: (() -> Unit)? = null,
    onCopyYesterdayClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    val totalCalories = items.sumOf { it.calories }
    val totalProtein = items.sumOf { it.proteinGrams }
    val totalCarbs = items.sumOf { it.carbsGrams }
    val totalFat = items.sumOf { it.fatGrams }

    val (icon: ImageVector, iconTint: Color) = when (mealType) {
        MealType.BREAKFAST -> Icons.Default.WbSunny to Color(0xFFFBBF24)
        MealType.LUNCH -> Icons.Default.Restaurant to Color(0xFFF97316)
        MealType.DINNER -> Icons.Default.Nightlife to Color(0xFFA855F7)
        MealType.SNACK -> Icons.Default.Fastfood to Color(0xFF38BDF8)
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear ${mealType.displayName}?") },
            text = { Text("Are you sure you want to remove all ${items.size} logged items from ${mealType.displayName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmation = false
                        onClearMealTypeClick?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = mealType.displayName,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = mealType.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (items.isNotEmpty()) {
                            Text(
                                text = "P: ${totalProtein.roundToInt()}g  •  C: ${totalCarbs.roundToInt()}g  •  F: ${totalFat.roundToInt()}g",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "No food logged yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${totalCalories.roundToInt()} kcal",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (items.isNotEmpty() || onCopyYesterdayClick != null) {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                if (items.isNotEmpty() && onClearMealTypeClick != null) {
                                    DropdownMenuItem(
                                        text = { Text("Clear ${mealType.displayName}") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DeleteSweep,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            showClearConfirmation = true
                                        }
                                    )
                                }
                                if (onCopyYesterdayClick != null) {
                                    DropdownMenuItem(
                                        text = { Text("Copy Yesterday's ${mealType.displayName}") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            onCopyYesterdayClick()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    if (items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        items.forEach { item ->
                            MealItemRow(
                                item = item,
                                onEdit = { onEditItemClick?.invoke(item) },
                                onDelete = { onDeleteItemClick(item) }
                            )
                        }
                    }

                    if (items.isEmpty() && onCopyYesterdayClick != null) {
                        OutlinedButton(
                            onClick = onCopyYesterdayClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryOrange)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Yesterday's ${mealType.displayName}", fontSize = 13.sp)
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAddFoodClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Add Food", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onQuickAddClick,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryOrange.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryOrange)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Quick Add", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryOrange)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealItemRow(
    item: MealLog,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onEdit() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.foodName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.servingCount != 1.0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                        Text("${item.servingCount}x", fontSize = 10.sp)
                    }
                }
            }
            Text(
                text = "${item.calories.roundToInt()} kcal  •  ${item.proteinGrams.roundToInt()}g P  ${item.carbsGrams.roundToInt()}g C  ${item.fatGrams.roundToInt()}g F",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Meal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
