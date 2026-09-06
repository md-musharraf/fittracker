package com.fitlife.calorietracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlife.calorietracker.data.model.BmiResult
import com.fitlife.calorietracker.ui.theme.*

@Composable
fun BmiGauge(
    bmiResult: BmiResult,
    modifier: Modifier = Modifier
) {
    val categoryColor = Color(bmiResult.colorHex)

    // BMI range: 15.0 to 40.0 for bar percentage mapping
    val fraction = ((bmiResult.bmi - 15.0) / (40.0 - 15.0)).coerceIn(0.0, 1.0).toFloat()
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 800),
        label = "BmiNeedle"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Body Mass Index (BMI)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Weight to height proportion indicator",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Category pill badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(categoryColor.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = bmiResult.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = categoryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // BMI Value Display
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = String.format("%.1f", bmiResult.bmi),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = categoryColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "kg/m²",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual segmented gauge bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Underweight (15 to 18.5 => 3.5/25 = 14%)
                    Box(modifier = Modifier.weight(0.14f).fillMaxHeight().background(BmiUnderweight))
                    // Normal (18.5 to 25 => 6.5/25 = 26%)
                    Box(modifier = Modifier.weight(0.26f).fillMaxHeight().background(BmiNormal))
                    // Overweight (25 to 30 => 5/25 = 20%)
                    Box(modifier = Modifier.weight(0.20f).fillMaxHeight().background(BmiOverweight))
                    // Obese I (30 to 35 => 5/25 = 20%)
                    Box(modifier = Modifier.weight(0.20f).fillMaxHeight().background(BmiObese1))
                    // Obese II & III (35 to 40 => 5/25 = 20%)
                    Box(modifier = Modifier.weight(0.20f).fillMaxHeight().background(BmiObese2))
                }
            }

            // Needle Canvas indicator
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .padding(top = 2.dp)
            ) {
                val needleX = size.width * animatedFraction
                val triangleWidth = 14.dp.toPx()
                val triangleHeight = 10.dp.toPx()

                val path = Path().apply {
                    moveTo(needleX, 0f)
                    lineTo(needleX - triangleWidth / 2, triangleHeight)
                    lineTo(needleX + triangleWidth / 2, triangleHeight)
                    close()
                }
                drawPath(path, color = Color.White)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gauge labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("18.5", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("25.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("30.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("35.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Healthy Range Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Healthy Weight Range",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${bmiResult.healthyMinWeightKg} - ${bmiResult.healthyMaxWeightKg} kg",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bmiResult.advice,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
