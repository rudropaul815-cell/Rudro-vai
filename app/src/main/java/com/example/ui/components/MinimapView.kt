package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import com.example.game.GameSimulation
import com.example.ui.theme.BdEmerald
import com.example.ui.theme.BdGold
import com.example.ui.theme.BdRed
import kotlin.math.abs

@Composable
fun MinimapRadar(
    sim: GameSimulation,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 80.dp, height = 110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xDD0C1613))
            .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Road corridor lines
            val roadLeft = w * 0.25f
            val roadRight = w * 0.75f
            val centerLine = w * 0.5f

            // Road gray strip
            drawRect(
                color = Color(0xFF1E2824),
                topLeft = Offset(roadLeft, 0f),
                size = androidx.compose.ui.geometry.Size(roadRight - roadLeft, h)
            )

            // Center dashed yellow
            drawLine(
                color = Color(0x88FFD54F),
                start = Offset(centerLine, 0f),
                end = Offset(centerLine, h),
                strokeWidth = 2f
            )

            val radarRangeM = 160f
            val playerScreenY = h * 0.75f

            // Traffic vehicles on radar
            for (v in sim.trafficManager.vehicles) {
                val relZ = v.z - sim.posZ
                if (relZ in -30f..radarRangeM) {
                    val vy = playerScreenY - (relZ / radarRangeM) * (h * 0.7f)
                    val vx = centerLine + (v.x / 6.0f) * (w * 0.22f)
                    val col = if (v.isOncoming) BdRed else Color(0xFF64B5F6)
                    drawCircle(color = col, radius = 3.5f, center = Offset(vx, vy))
                }
            }

            // Passenger stops on radar
            for (stop in sim.stops) {
                if (!stop.isCompleted) {
                    val relZ = stop.z - sim.posZ
                    if (relZ in 0f..radarRangeM) {
                        val sy = playerScreenY - (relZ / radarRangeM) * (h * 0.7f)
                        drawCircle(color = BdGold, radius = 5f, center = Offset(roadRight - 4f, sy))
                    }
                }
            }

            // Player Bus icon (Green Arrow at fixed radar position)
            val px = centerLine + (sim.posX / 6.0f) * (w * 0.22f)
            val py = playerScreenY

            val arrowPath = Path().apply {
                moveTo(px, py - 7f)
                lineTo(px + 5f, py + 5f)
                lineTo(px, py + 3f)
                lineTo(px - 5f, py + 5f)
                close()
            }
            drawPath(arrowPath, color = BdEmerald, style = Fill)
        }
    }
}
