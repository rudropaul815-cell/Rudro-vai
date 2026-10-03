package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.atan2

@Composable
fun VirtualSteeringWheel(
    modifier: Modifier = Modifier,
    onSteerAngleChanged: (Float) -> Unit, // -1.0 (full left) to +1.0 (full right)
    onHornTapped: () -> Unit = {}
) {
    var wheelAngleDeg by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .size(160.dp)
            .testTag("steering_wheel")
            .pointerInput(Unit) {
                val center = Offset(size.width / 2f, size.height / 2f)
                var lastAngle = 0f

                detectDragGestures(
                    onDragStart = { offset ->
                        lastAngle = Math.toDegrees(atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())).toFloat()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val currentAngle = Math.toDegrees(atan2((change.position.y - center.y).toDouble(), (change.position.x - center.x).toDouble())).toFloat()
                        var delta = currentAngle - lastAngle
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f

                        wheelAngleDeg = (wheelAngleDeg + delta).coerceIn(-135f, 135f)
                        lastAngle = currentAngle
                        onSteerAngleChanged(wheelAngleDeg / 135f)
                    },
                    onDragEnd = {
                        wheelAngleDeg = 0f
                        onSteerAngleChanged(0f)
                    },
                    onDragCancel = {
                        wheelAngleDeg = 0f
                        onSteerAngleChanged(0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 10f

            rotate(wheelAngleDeg, pivot = center) {
                // Outer Grip Ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color(0xFF2C3E50), Color(0xFF1A252F), Color(0xFF2C3E50)),
                        center = center
                    ),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 18f, cap = StrokeCap.Round)
                )

                // Top Grip Marker (Yellow BD racing stripe)
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = Offset(center.x, center.y - radius - 9f),
                    end = Offset(center.x, center.y - radius + 9f),
                    strokeWidth = 6f
                )

                // Three Spokes
                val spokeColor = Color(0xFF7F8C8D)
                // Left Spoke
                drawLine(
                    color = spokeColor,
                    start = center,
                    end = Offset(center.x - radius + 6f, center.y + 15f),
                    strokeWidth = 10f
                )
                // Right Spoke
                drawLine(
                    color = spokeColor,
                    start = center,
                    end = Offset(center.x + radius - 6f, center.y + 15f),
                    strokeWidth = 10f
                )
                // Bottom Spoke
                drawLine(
                    color = spokeColor,
                    start = center,
                    end = Offset(center.x, center.y + radius - 6f),
                    strokeWidth = 10f
                )

                // Center Horn Pad
                drawCircle(
                    color = Color(0xFF0E1715),
                    radius = 32f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF006A4E), // BD Green ring
                    radius = 24f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFF42A41), // Center red circle
                    radius = 12f,
                    center = center
                )
            }
        }
    }
}
