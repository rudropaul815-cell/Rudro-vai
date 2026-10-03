package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.CameraView
import com.example.model.WeatherType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Renderer3D {

    private val rainParticles = List(120) {
        RainDrop(
            xNorm = Random.nextFloat(),
            yNorm = Random.nextFloat(),
            speed = 0.035f + Random.nextFloat() * 0.04f,
            len = 18f + Random.nextFloat() * 22f
        )
    }

    private data class RainDrop(
        var xNorm: Float,
        var yNorm: Float,
        val speed: Float,
        val len: Float
    )

    fun renderScene(
        drawScope: DrawScope,
        camera: Camera3D,
        cameraView: CameraView,
        polygons: List<Polygon3D>,
        weather: WeatherType,
        headlightsOn: Boolean,
        wiperAngleDeg: Float,
        playerSpeedKmh: Float,
        roadCurve: Float
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        // 1. Draw Sky Gradient & Horizon
        drawSky(drawScope, weather, width, height, roadCurve)

        // 2. Project Polygons
        val renderList = ArrayList<RenderablePoly>(polygons.size)

        for (poly in polygons) {
            val projected = poly.vertices.map { camera.project(it, width, height) }
            val allBehind = projected.all { !it.isVisible && it.depth <= 0.2f }
            if (allBehind) continue

            val anyVisible = projected.any { it.isVisible }
            if (!anyVisible) continue

            // Average depth for painter's algorithm
            val avgDepth = projected.map { it.depth }.average().toFloat()

            // Apply lighting & weather shading
            val shadedColor = applyShading(poly.color, avgDepth, weather, poly.isEmissive, headlightsOn)

            renderList.add(
                RenderablePoly(
                    points = projected,
                    color = shadedColor,
                    depth = avgDepth,
                    isWireframe = poly.isWireframe,
                    wireframeWidth = poly.wireframeWidth
                )
            )
        }

        // 3. Sort back to front (highest depth rendered first)
        renderList.sortByDescending { it.depth }

        // 4. Render 3D polygons
        for (item in renderList) {
            item.render(drawScope)
        }

        // 5. Headlight illumination beam overlay if night/dark
        if (headlightsOn && (weather == WeatherType.NIGHT || weather == WeatherType.FOGGY || weather == WeatherType.MONSOON_RAIN)) {
            drawHeadlightGlow(drawScope, width, height, cameraView)
        }

        // 6. Monsoon Rain streaks & Wiper effect
        if (weather == WeatherType.MONSOON_RAIN) {
            drawRain(drawScope, width, height, wiperAngleDeg, playerSpeedKmh)
        }

        // 7. Interior Windshield & Dashboard frame if CABIN view
        if (cameraView == CameraView.CABIN) {
            drawCabinInteriorOverlay(drawScope, width, height, wiperAngleDeg)
        }
    }

    private fun drawSky(
        drawScope: DrawScope,
        weather: WeatherType,
        width: Float,
        height: Float,
        roadCurve: Float
    ) {
        val horizonY = height * 0.48f

        val (skyTop, skyBottom) = when (weather) {
            WeatherType.SUNNY -> Pair(Color(0xFF1E88E5), Color(0xFFB3E5FC))
            WeatherType.SUNSET -> Pair(Color(0xFFBF360C), Color(0xFFFFB74D))
            WeatherType.MONSOON_RAIN -> Pair(Color(0xFF263238), Color(0xFF546E7A))
            WeatherType.FOGGY -> Pair(Color(0xFF78909C), Color(0xFFCFD8DC))
            WeatherType.NIGHT -> Pair(Color(0xFF050B14), Color(0xFF0F2027))
        }

        // Sky gradient
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(skyTop, skyBottom),
                startY = 0f,
                endY = horizonY
            ),
            topLeft = Offset(0f, 0f),
            size = Size(width, horizonY)
        )

        // Sun / Moon in sky
        val sunX = width * 0.72f - (roadCurve * 40f)
        val sunY = height * 0.22f
        if (weather == WeatherType.SUNNY) {
            drawScope.drawCircle(Color(0xFFFFF9C4), radius = 28f, center = Offset(sunX, sunY))
            drawScope.drawCircle(Color(0x33FFF59D), radius = 48f, center = Offset(sunX, sunY))
        } else if (weather == WeatherType.SUNSET) {
            drawScope.drawCircle(Color(0xFFFF7043), radius = 34f, center = Offset(sunX, sunY + 30f))
            drawScope.drawCircle(Color(0x44FFAB91), radius = 58f, center = Offset(sunX, sunY + 30f))
        } else if (weather == WeatherType.NIGHT) {
            // Crescent moon
            drawScope.drawCircle(Color(0xFFECEFF1), radius = 20f, center = Offset(sunX, sunY))
            drawScope.drawCircle(Color(0xFF050B14), radius = 17f, center = Offset(sunX - 7f, sunY - 4f))
        }

        // Distant Bangladeshi green tree silhouettes / river horizon
        val distantGreen = when (weather) {
            WeatherType.NIGHT -> Color(0xFF0A1410)
            WeatherType.FOGGY -> Color(0xFF607D8B)
            else -> Color(0xFF1B4332)
        }
        drawScope.drawRect(
            color = distantGreen,
            topLeft = Offset(0f, horizonY - 12f),
            size = Size(width, 16f)
        )

        // Ground base plane below horizon (green fields)
        val groundColor = when (weather) {
            WeatherType.NIGHT -> Color(0xFF08120B)
            WeatherType.MONSOON_RAIN -> Color(0xFF1B382B)
            WeatherType.SUNSET -> Color(0xFF233D2D)
            else -> Color(0xFF2D5A3E)
        }
        drawScope.drawRect(
            color = groundColor,
            topLeft = Offset(0f, horizonY),
            size = Size(width, height - horizonY)
        )
    }

    private fun applyShading(
        baseColor: Color,
        depth: Float,
        weather: WeatherType,
        isEmissive: Boolean,
        headlightsOn: Boolean
    ): Color {
        if (isEmissive) return baseColor

        // Distance fog factor
        val maxDist = when (weather) {
            WeatherType.FOGGY -> 75f
            WeatherType.MONSOON_RAIN -> 110f
            WeatherType.NIGHT -> 95f
            else -> 180f
        }
        val fogFactor = (depth / maxDist).coerceIn(0f, 1f)

        // Ambient lighting
        val ambient = weather.skyLight
        var brightness = (ambient * (1f - fogFactor * 0.65f)).coerceIn(0.12f, 1.0f)

        // Headlight cone illuminates close objects in front of bus
        if (headlightsOn && depth < 40f) {
            val boost = ((40f - depth) / 40f) * 0.45f
            brightness = (brightness + boost).coerceAtMost(1.0f)
        }

        val r = (baseColor.red * brightness).coerceIn(0f, 1f)
        val g = (baseColor.green * brightness).coerceIn(0f, 1f)
        val b = (baseColor.blue * brightness).coerceIn(0f, 1f)

        // Fog color blending
        val fogColor = when (weather) {
            WeatherType.FOGGY -> Color(0xFFB0BEC5)
            WeatherType.NIGHT -> Color(0xFF060D12)
            WeatherType.MONSOON_RAIN -> Color(0xFF37474F)
            else -> Color(0xFF81C784)
        }

        val finalR = r * (1f - fogFactor) + fogColor.red * fogFactor
        val finalG = g * (1f - fogFactor) + fogColor.green * fogFactor
        val finalB = b * (1f - fogFactor) + fogColor.blue * fogFactor

        return Color(finalR, finalG, finalB, baseColor.alpha)
    }

    private fun drawHeadlightGlow(drawScope: DrawScope, width: Float, height: Float, cameraView: CameraView) {
        val beamCenter = Offset(width * 0.5f, height * 0.72f)
        drawScope.drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x35FFFDE7), Color(0x15FFF9C4), Color(0x00000000)),
                center = beamCenter,
                radius = width * 0.48f
            ),
            topLeft = Offset(width * 0.1f, height * 0.45f),
            size = Size(width * 0.8f, height * 0.55f)
        )
    }

    private fun drawRain(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        wiperAngleDeg: Float,
        speedKmh: Float
    ) {
        val speedFactor = 1.0f + (speedKmh / 80f)
        for (drop in rainParticles) {
            drop.yNorm += drop.speed * speedFactor
            if (drop.yNorm > 1.0f) {
                drop.yNorm = -0.05f
                drop.xNorm = Random.nextFloat()
            }

            val x = drop.xNorm * width
            val y = drop.yNorm * height

            drawScope.drawLine(
                color = Color(0x80CFD8DC),
                start = Offset(x, y),
                end = Offset(x - 4f, y + drop.len),
                strokeWidth = 2f
            )
        }
    }

    private fun drawCabinInteriorOverlay(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        wiperAngleDeg: Float
    ) {
        val pillarWidth = width * 0.08f
        val roofHeight = height * 0.14f
        val dashHeight = height * 0.28f

        // Roof upper sun-visor bar
        drawScope.drawRect(
            color = Color(0xFF1B2321),
            topLeft = Offset(0f, 0f),
            size = Size(width, roofHeight)
        )

        // Left A-pillar
        drawScope.drawRect(
            color = Color(0xFF161E1C),
            topLeft = Offset(0f, 0f),
            size = Size(pillarWidth, height)
        )

        // Right A-pillar
        drawScope.drawRect(
            color = Color(0xFF161E1C),
            topLeft = Offset(width - pillarWidth, 0f),
            size = Size(pillarWidth, height)
        )

        // Lower Dashboard
        drawScope.drawRect(
            color = Color(0xFF192220),
            topLeft = Offset(0f, height - dashHeight),
            size = Size(width, dashHeight)
        )

        // Center rear-view mirror
        val mirrorW = width * 0.22f
        val mirrorH = height * 0.08f
        val mirrorX = (width - mirrorW) / 2f
        val mirrorY = roofHeight + 10f

        drawScope.drawRoundRect(
            color = Color(0xFF0F1514),
            topLeft = Offset(mirrorX - 4f, mirrorY - 4f),
            size = Size(mirrorW + 8f, mirrorH + 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        drawScope.drawRoundRect(
            color = Color(0xDD37474F),
            topLeft = Offset(mirrorX, mirrorY),
            size = Size(mirrorW, mirrorH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        // Windshield wiper arm in cabin view
        val radW = Math.toRadians(wiperAngleDeg.toDouble()).toFloat()
        val wiperBase = Offset(width * 0.42f, height - dashHeight)
        val wiperLen = height * 0.38f
        val wiperTip = Offset(
            wiperBase.x + sin(radW) * wiperLen,
            wiperBase.y - cos(radW) * wiperLen
        )
        drawScope.drawLine(
            color = Color(0xFF0E1312),
            start = wiperBase,
            end = wiperTip,
            strokeWidth = 6f
        )
    }
}
