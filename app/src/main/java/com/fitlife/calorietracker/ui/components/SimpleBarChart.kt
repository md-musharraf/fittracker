package com.fitlife.calorietracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlife.calorietracker.ui.theme.AccentGreen
import com.fitlife.calorietracker.ui.theme.CaloriesColor
import kotlin.math.roundToInt

data class DailyBarData(
    val dayLabel: String,
    val date: String,
    val caloriesConsumed: Double,
    val calorieGoal: Int,
    val isToday: Boolean = false
)

@Composable
fun SimpleWeeklyBarChart(
    weeklyData: List<DailyBarData>,
    modifier: Modifier = Modifier
) {
    val maxCalories = (weeklyData.maxOfOrNull { it.caloriesConsumed.coerceAtLeast(it.calorieGoal.toDouble()) } ?: 2500.0) * 1.15

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
                Column {
                    Text(
                        text = "Weekly Calorie Intake",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Last 7 days performance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Goal: ${weeklyData.firstOrNull()?.calorieGoal ?: 2000} kcal",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { item ->
                    val fraction = (item.caloriesConsumed / maxCalories).toFloat().coerceIn(0.04f, 1f)
                    val animatedFraction by animateFloatAsState(
                        targetValue = fraction,
                        animationSpec = tween(durationMillis = 600),
                        label = "BarHeight"
                    )

                    val barColor = when {
                        item.caloriesConsumed == 0.0 -> MaterialTheme.colorScheme.surfaceVariant
                        item.caloriesConsumed > item.calorieGoal * 1.1 -> Color(0xFFEF4444)
                        item.caloriesConsumed >= item.calorieGoal * 0.9 -> AccentGreen
                        else -> CaloriesColor
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Calorie text above bar
                        if (item.caloriesConsumed > 0) {
                            Text(
                                text = "${item.caloriesConsumed.roundToInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Bar
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .weight(1f, fill = false)
                                .fillMaxHeight(animatedFraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(barColor)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day label
                        Text(
                            text = item.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (item.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
