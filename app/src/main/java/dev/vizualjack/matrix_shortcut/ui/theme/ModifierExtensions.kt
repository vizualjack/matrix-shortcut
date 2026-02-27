package dev.vizualjack.matrix_shortcut.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 1.5.dp,
    cornerRadius: Dp = 10.dp,
    dashWidth: Float = 10f,
    dashGap: Float = 10f
) = this.drawBehind {

    val strokePx = strokeWidth.toPx()
    val radiusPx = cornerRadius.toPx()

    drawRoundRect(
        color = color,
        size = size,
        cornerRadius = CornerRadius(radiusPx, radiusPx),
        style = Stroke(
            width = strokePx,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashWidth, dashGap),
                0f
            )
        )
    )
}