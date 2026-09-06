package com.fitlife.calorietracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlife.calorietracker.ui.theme.AccentGreen
import com.fitlife.calorietracker.ui.theme.CaloriesColor
import kotlin.math.roundToInt

@Composable
fun CalorieProgressRing(
    calorieGoal: Int,
    caloriesConsumed: Double,
    caloriesBurned: Double,
    modifier: Modifier = Modifier
) {
    val remaining = (calorieGoal - caloriesConsumed + caloriesBurned).roundToInt()
    val rawProgress = if (calorieGoal > 0) (caloriesConsumed / calorieGoal).toFloat() else 0f
    val progressClamped = rawProgress.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progressClamped,
        animationSpec = tween(durationMillis = 800),
        label = "CalorieProgress"
    )

    val ringTrackColor = MaterialTheme.colorScheme.surfaceVariant
    val ringFillColor = if (caloriesConsumed > calorieGoal) {
        Color(0xFFEF4444) // Over budget warning
    } else {
        CaloriesColor
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(190.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 14.dp.toPx()

            // Background Track
            drawArc(
                color = ringTrackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Consumed Progress Arc
            drawArc(
                color = ringFillColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = remaining.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (remaining >= 0) "KCAL LEFT" else "KCAL OVER",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFEF4444)
            )
        }
    }
}
