package com.fitlife.calorietracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitlife.calorietracker.ui.theme.CarbsColor
import com.fitlife.calorietracker.ui.theme.FatColor
import com.fitlife.calorietracker.ui.theme.ProteinColor
import kotlin.math.roundToInt

@Composable
fun MacroItem(
    title: String,
    currentGrams: Double,
    targetGrams: Int,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (targetGrams > 0) (currentGrams / targetGrams).toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "MacroProgress"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        // Label with Color Bullet
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(barColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Large Current Grams + Target
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${currentGrams.roundToInt()}g",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "/ ${targetGrams}g",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(bottom = 1.dp),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(barColor.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun MacroSummaryRow(
    proteinGrams: Double,
    proteinTarget: Int,
    carbsGrams: Double,
    carbsTarget: Int,
    fatGrams: Double,
    fatTarget: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MacroItem(
            title = "Protein",
            currentGrams = proteinGrams,
            targetGrams = proteinTarget,
            barColor = ProteinColor,
            modifier = Modifier.weight(1f)
        )
        MacroItem(
            title = "Carbs",
            currentGrams = carbsGrams,
            targetGrams = carbsTarget,
            barColor = CarbsColor,
            modifier = Modifier.weight(1f)
        )
        MacroItem(
            title = "Fat",
            currentGrams = fatGrams,
            targetGrams = fatTarget,
            barColor = FatColor,
            modifier = Modifier.weight(1f)
        )
    }
}
