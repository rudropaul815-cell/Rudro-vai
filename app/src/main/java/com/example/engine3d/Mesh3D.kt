package com.example.engine3d

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

data class Polygon3D(
    val vertices: List<Vector3>,
    val color: Color,
    val isEmissive: Boolean = false,
    val isWireframe: Boolean = false,
    val wireframeWidth: Float = 2f
) {
    fun averageZ(camera: Camera3D): Float {
        if (vertices.isEmpty()) return 0f
        var sumZ = 0f
        for (v in vertices) {
            val translated = v - camera.position
            val rotatedYaw = translated.rotateY(-camera.yaw)
            val rotated = rotatedYaw.rotateX(-camera.pitch)
            sumZ += rotated.z
        }
        return sumZ / vertices.size
    }
}

class RenderablePoly(
    val points: List<ProjectedPoint>,
    val color: Color,
    val depth: Float,
    val isWireframe: Boolean = false,
    val wireframeWidth: Float = 2f
) {
    fun render(drawScope: DrawScope) {
        if (points.size < 3) return
        val path = Path().apply {
            moveTo(points[0].screenX, points[0].screenY)
            for (i in 1 until points.size) {
                lineTo(points[i].screenX, points[i].screenY)
            }
            close()
        }

        if (isWireframe) {
            drawScope.drawPath(path, color, style = Stroke(width = wireframeWidth))
        } else {
            drawScope.drawPath(path, color, style = Fill)
        }
    }
}
