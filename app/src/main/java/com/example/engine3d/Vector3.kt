package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3): Vector3 = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3): Vector3 = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float): Vector3 = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float): Vector3 = Vector3(x / scalar, y / scalar, z / scalar)

    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3): Vector3 = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.0001f) this / len else Vector3(0f, 0f, 0f)
    }

    fun rotateY(radians: Float): Vector3 {
        val cosA = cos(radians)
        val sinA = sin(radians)
        return Vector3(
            x * cosA + z * sinA,
            y,
            -x * sinA + z * cosA
        )
    }

    fun rotateX(radians: Float): Vector3 {
        val cosA = cos(radians)
        val sinA = sin(radians)
        return Vector3(
            x,
            y * cosA - z * sinA,
            y * sinA + z * cosA
        )
    }

    fun rotateZ(radians: Float): Vector3 {
        val cosA = cos(radians)
        val sinA = sin(radians)
        return Vector3(
            x * cosA - y * sinA,
            x * sinA + y * cosA,
            z
        )
    }
}

data class ProjectedPoint(
    val screenX: Float,
    val screenY: Float,
    val depth: Float,
    val isVisible: Boolean
) {
    fun toOffset(): Offset = Offset(screenX, screenY)
}

class Camera3D(
    var position: Vector3 = Vector3(0f, 2.8f, -6.5f),
    var target: Vector3 = Vector3(0f, 1.2f, 10f),
    var pitch: Float = 0.08f,  // Looking down slightly
    var yaw: Float = 0f,        // Looking forward
    var fov: Float = 65f
) {
    fun project(point: Vector3, screenWidth: Float, screenHeight: Float): ProjectedPoint {
        // Transform to camera local space
        val translated = point - position

        // Apply yaw (turn left/right) and pitch (tilt up/down)
        val rotatedYaw = translated.rotateY(-yaw)
        val rotated = rotatedYaw.rotateX(-pitch)

        if (rotated.z <= 0.2f) {
            return ProjectedPoint(0f, 0f, rotated.z, false)
        }

        val aspect = screenWidth / screenHeight
        val fovRad = Math.toRadians(fov.toDouble()).toFloat()
        val f = 1.0f / kotlin.math.tan(fovRad / 2.0f)

        val projX = (rotated.x * f / aspect) / rotated.z
        val projY = (rotated.y * f) / rotated.z

        val screenX = (projX + 1.0f) * 0.5f * screenWidth
        val screenY = (1.0f - projY) * 0.5f * screenHeight

        val inFrustum = screenX in -screenWidth..(screenWidth * 2f) &&
                screenY in -screenHeight..(screenHeight * 2f) &&
                rotated.z < 250f

        return ProjectedPoint(screenX, screenY, rotated.z, inFrustum)
    }
}
