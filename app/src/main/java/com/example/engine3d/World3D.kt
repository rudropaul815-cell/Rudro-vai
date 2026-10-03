package com.example.engine3d

import androidx.compose.ui.graphics.Color
import com.example.model.BusLivery
import com.example.model.TrafficType
import kotlin.math.cos
import kotlin.math.sin

object World3D {

    // Generates a 3D box mesh (6 quads)
    fun createBox(
        center: Vector3,
        size: Vector3,
        color: Color,
        yawRad: Float = 0f,
        topColor: Color? = null,
        frontColor: Color? = null,
        backColor: Color? = null,
        sideColor: Color? = null,
        isWireframe: Boolean = false,
        wireframeWidth: Float = 2f
    ): List<Polygon3D> {
        val hx = size.x / 2f
        val hy = size.y / 2f
        val hz = size.z / 2f

        // Local 8 corners
        val corners = listOf(
            Vector3(-hx, -hy, -hz), // 0: bottom back left
            Vector3(hx, -hy, -hz),  // 1: bottom back right
            Vector3(hx, hy, -hz),   // 2: top back right
            Vector3(-hx, hy, -hz),  // 3: top back left
            Vector3(-hx, -hy, hz),  // 4: bottom front left
            Vector3(hx, -hy, hz),   // 5: bottom front right
            Vector3(hx, hy, hz),    // 6: top front right
            Vector3(-hx, hy, hz)    // 7: top front left
        ).map { corner ->
            val rotated = corner.rotateY(yawRad)
            center + rotated
        }

        val cTop = topColor ?: color
        val cFront = frontColor ?: color
        val cBack = backColor ?: color
        val cSide = sideColor ?: color

        return listOf(
            // Front (+z)
            Polygon3D(listOf(corners[4], corners[5], corners[6], corners[7]), cFront, isWireframe = isWireframe, wireframeWidth = wireframeWidth),
            // Back (-z)
            Polygon3D(listOf(corners[1], corners[0], corners[3], corners[2]), cBack, isWireframe = isWireframe, wireframeWidth = wireframeWidth),
            // Top (+y)
            Polygon3D(listOf(corners[7], corners[6], corners[2], corners[3]), cTop, isWireframe = isWireframe, wireframeWidth = wireframeWidth),
            // Bottom (-y)
            Polygon3D(listOf(corners[0], corners[1], corners[5], corners[4]), Color(0xFF151515), isWireframe = isWireframe, wireframeWidth = wireframeWidth),
            // Left (-x)
            Polygon3D(listOf(corners[0], corners[4], corners[7], corners[3]), cSide, isWireframe = isWireframe, wireframeWidth = wireframeWidth),
            // Right (+x)
            Polygon3D(listOf(corners[5], corners[1], corners[2], corners[6]), cSide, isWireframe = isWireframe, wireframeWidth = wireframeWidth)
        )
    }

    // Builds the 3D player bus with high detail
    fun buildPlayerBus(
        pos: Vector3,
        yawRad: Float,
        pitchRad: Float,
        livery: BusLivery,
        headlightsOn: Boolean,
        braking: Boolean,
        turnIndicatorLeft: Boolean,
        turnIndicatorRight: Boolean,
        indicatorBlink: Boolean,
        wiperAngleDeg: Float
    ): List<Polygon3D> {
        val polys = mutableListOf<Polygon3D>()

        val busLen = 11.0f
        val busWidth = 2.5f
        val busHeight = 3.2f

        // Main chassis
        val bodyColor = livery.primaryColor
        val sideColor = livery.secondaryColor
        val roofColor = Color(0xFFE8ECEB)

        // 1. Lower chassis & bumpers
        polys.addAll(
            createBox(
                center = pos + Vector3(0f, 0.4f, 0f).rotateY(yawRad),
                size = Vector3(busWidth, 0.8f, busLen),
                color = Color(0xFF222828),
                yawRad = yawRad
            )
        )

        // 2. Main passenger cabin body
        polys.addAll(
            createBox(
                center = pos + Vector3(0f, 1.8f, 0f).rotateY(yawRad),
                size = Vector3(busWidth, 2.0f, busLen - 0.2f),
                color = bodyColor,
                yawRad = yawRad,
                topColor = roofColor,
                sideColor = sideColor,
                frontColor = bodyColor,
                backColor = bodyColor
            )
        )

        // 3. Side livery decorative stripe
        val stripeColor = livery.stripeColor
        polys.addAll(
            createBox(
                center = pos + Vector3(0f, 1.4f, 0f).rotateY(yawRad),
                size = Vector3(busWidth + 0.05f, 0.35f, busLen - 0.5f),
                color = stripeColor,
                yawRad = yawRad
            )
        )

        // 4. Large front windshield & side windows (tinted blue-black glass)
        val glassColor = Color(0xDD1A3238)
        polys.addAll(
            createBox(
                center = pos + Vector3(0f, 2.1f, (busLen / 2f) - 0.02f).rotateY(yawRad),
                size = Vector3(busWidth - 0.2f, 1.1f, 0.15f),
                color = glassColor,
                yawRad = yawRad
            )
        )

        // 5. Roof AC Unit Pod (standard on Bangladesh luxury coaches)
        polys.addAll(
            createBox(
                center = pos + Vector3(0f, 3.0f, -0.5f).rotateY(yawRad),
                size = Vector3(1.6f, 0.4f, 3.2f),
                color = Color(0xFFD6DBDF),
                yawRad = yawRad
            )
        )

        // 6. Dual glowing headlights (Front)
        val headColor = if (headlightsOn) Color(0xFFFFFDE7) else Color(0xFFB0BEC5)
        val hlOffset = (busLen / 2f) + 0.05f
        polys.addAll(
            createBox(
                center = pos + Vector3(-0.85f, 0.9f, hlOffset).rotateY(yawRad),
                size = Vector3(0.45f, 0.3f, 0.1f),
                color = headColor,
                yawRad = yawRad
            )
        )
        polys.addAll(
            createBox(
                center = pos + Vector3(0.85f, 0.9f, hlOffset).rotateY(yawRad),
                size = Vector3(0.45f, 0.3f, 0.1f),
                color = headColor,
                yawRad = yawRad
            )
        )

        // 7. Rear Taillights & Brake lights
        val tailColor = if (braking) Color(0xFFFF1744) else (if (headlightsOn) Color(0xFFB71C1C) else Color(0xFF4A1010))
        val blOffset = -(busLen / 2f) - 0.05f
        polys.addAll(
            createBox(
                center = pos + Vector3(-0.9f, 1.2f, blOffset).rotateY(yawRad),
                size = Vector3(0.35f, 0.7f, 0.1f),
                color = tailColor,
                yawRad = yawRad
            )
        )
        polys.addAll(
            createBox(
                center = pos + Vector3(0.9f, 1.2f, blOffset).rotateY(yawRad),
                size = Vector3(0.35f, 0.7f, 0.1f),
                color = tailColor,
                yawRad = yawRad
            )
        )

        // 8. Turn Indicators (Amber)
        val amberBlink = Color(0xFFFF9100)
        val amberOff = Color(0xFF5A3500)
        val leftIndColor = if (turnIndicatorLeft && indicatorBlink) amberBlink else amberOff
        val rightIndColor = if (turnIndicatorRight && indicatorBlink) amberBlink else amberOff

        polys.addAll(
            createBox(
                center = pos + Vector3(-1.18f, 1.0f, hlOffset - 0.2f).rotateY(yawRad),
                size = Vector3(0.2f, 0.2f, 0.15f),
                color = leftIndColor,
                yawRad = yawRad
            )
        )
        polys.addAll(
            createBox(
                center = pos + Vector3(1.18f, 1.0f, hlOffset - 0.2f).rotateY(yawRad),
                size = Vector3(0.2f, 0.2f, 0.15f),
                color = rightIndColor,
                yawRad = yawRad
            )
        )

        // 9. Wheels (Front and Dual Rear Axles)
        val tireColor = Color(0xFF1A1A1A)
        val rimColor = Color(0xFFB0BEC5)
        val wheelOffsetsZ = listOf(3.6f, -3.2f, -4.5f)
        for (wz in wheelOffsetsZ) {
            // Left wheel
            polys.addAll(
                createBox(
                    center = pos + Vector3(-1.25f, 0.45f, wz).rotateY(yawRad),
                    size = Vector3(0.3f, 0.9f, 0.9f),
                    color = tireColor,
                    yawRad = yawRad,
                    sideColor = rimColor
                )
            )
            // Right wheel
            polys.addAll(
                createBox(
                    center = pos + Vector3(1.25f, 0.45f, wz).rotateY(yawRad),
                    size = Vector3(0.3f, 0.9f, 0.9f),
                    color = tireColor,
                    yawRad = yawRad,
                    sideColor = rimColor
                )
            )
        }

        // 10. Windshield Wipers (visible on windshield)
        val radWiper = Math.toRadians(wiperAngleDeg.toDouble()).toFloat()
        val wiperLen = 0.8f
        val wStart = pos + Vector3(-0.4f, 1.6f, (busLen / 2f) + 0.08f).rotateY(yawRad)
        val wEnd = wStart + Vector3(sin(radWiper) * wiperLen, cos(radWiper) * wiperLen, 0f).rotateY(yawRad)
        polys.add(
            Polygon3D(
                vertices = listOf(wStart, wEnd),
                color = Color(0xFF263238),
                isWireframe = true,
                wireframeWidth = 4f
            )
        )

        return polys
    }

    // Builds 3D AI Traffic Vehicles
    fun buildTrafficVehicle(
        type: TrafficType,
        pos: Vector3,
        yawRad: Float,
        headlightsOn: Boolean
    ): List<Polygon3D> {
        val polys = mutableListOf<Polygon3D>()

        when (type) {
            TrafficType.CNG_AUTO -> {
                // Iconic Bangladeshi Green CNG Auto-Rickshaw
                val cngGreen = Color(0xFF1B5E20)
                val cngYellow = Color(0xFFFDD835)
                val cngBlack = Color(0xFF212121)

                // Base Body
                polys.addAll(createBox(pos + Vector3(0f, 0.7f, 0f).rotateY(yawRad), Vector3(1.3f, 1.1f, 2.4f), cngGreen, yawRad))
                // Front hood sloped
                polys.addAll(createBox(pos + Vector3(0f, 0.55f, 1.1f).rotateY(yawRad), Vector3(0.9f, 0.8f, 0.6f), cngYellow, yawRad))
                // Top Roof Canopy
                polys.addAll(createBox(pos + Vector3(0f, 1.4f, 0f).rotateY(yawRad), Vector3(1.35f, 0.35f, 2.3f), cngBlack, yawRad))
                // Front single headlight
                val headCol = if (headlightsOn) Color(0xFFFFFDE7) else Color(0xFFCFD8DC)
                polys.addAll(createBox(pos + Vector3(0f, 0.6f, 1.42f).rotateY(yawRad), Vector3(0.25f, 0.25f, 0.08f), headCol, yawRad))
                // Taillight
                polys.addAll(createBox(pos + Vector3(0f, 0.5f, -1.22f).rotateY(yawRad), Vector3(0.6f, 0.2f, 0.08f), Color(0xFFD50000), yawRad))
            }

            TrafficType.RICKSHAW -> {
                // Bangladeshi Cycle Rickshaw
                val rickshawColor = Color(0xFF00ACC1)
                val hoodColor = Color(0xFFD81B60)
                // Passenger seat box & back art
                polys.addAll(createBox(pos + Vector3(0f, 0.65f, -0.3f).rotateY(yawRad), Vector3(1.0f, 0.8f, 0.9f), rickshawColor, yawRad))
                // Curved decorative hood
                polys.addAll(createBox(pos + Vector3(0f, 1.25f, -0.4f).rotateY(yawRad), Vector3(1.05f, 0.6f, 0.8f), hoodColor, yawRad))
                // Puller cycle frame
                polys.addAll(createBox(pos + Vector3(0f, 0.5f, 0.65f).rotateY(yawRad), Vector3(0.2f, 0.8f, 1.0f), Color(0xFF212121), yawRad))
                // Rear artistic painted plate
                polys.addAll(createBox(pos + Vector3(0f, 0.45f, -0.76f).rotateY(yawRad), Vector3(0.85f, 0.35f, 0.05f), Color(0xFFFFEB3B), yawRad))
            }

            TrafficType.TRUCK -> {
                // Bangladeshi Heavy Cargo Truck ("Mayer Doa / Horn Ok Please")
                val truckBlue = Color(0xFF1565C0)
                val cargoWood = Color(0xFF795548)
                val cabCenter = pos + Vector3(0f, 1.5f, 2.2f).rotateY(yawRad)
                val cargoCenter = pos + Vector3(0f, 1.8f, -1.2f).rotateY(yawRad)

                // Front Cabin
                polys.addAll(createBox(cabCenter, Vector3(2.4f, 2.2f, 2.4f), truckBlue, yawRad, topColor = Color(0xFFD32F2F)))
                // Heavy Wooden Cargo Box
                polys.addAll(createBox(cargoCenter, Vector3(2.5f, 2.4f, 5.0f), cargoWood, yawRad, sideColor = Color(0xFF8D6E63)))
                // Dual Headlights
                val headCol = if (headlightsOn) Color(0xFFFFF9C4) else Color(0xFFECEFF1)
                polys.addAll(createBox(pos + Vector3(-0.8f, 0.8f, 3.42f).rotateY(yawRad), Vector3(0.4f, 0.3f, 0.1f), headCol, yawRad))
                polys.addAll(createBox(pos + Vector3(0.8f, 0.8f, 3.42f).rotateY(yawRad), Vector3(0.4f, 0.3f, 0.1f), headCol, yawRad))
                // Tailgate with traditional "Horn Ok Please" sign
                polys.addAll(createBox(pos + Vector3(0f, 1.0f, -3.72f).rotateY(yawRad), Vector3(2.4f, 0.45f, 0.1f), Color(0xFFF57F17), yawRad))
            }

            TrafficType.LOCAL_BUS -> {
                // Dhaka "Murir Tin" Local City Bus
                val busYellow = Color(0xFFFBC02D)
                val busRed = Color(0xFFC62828)
                polys.addAll(createBox(pos + Vector3(0f, 1.6f, 0f).rotateY(yawRad), Vector3(2.5f, 2.3f, 8.0f), busYellow, yawRad, sideColor = busRed))
                // Roof rack with passenger luggage & boxes
                polys.addAll(createBox(pos + Vector3(0f, 2.9f, 0f).rotateY(yawRad), Vector3(2.0f, 0.4f, 6.0f), Color(0xFF4E342E), yawRad))
                // Headlights
                val headCol = if (headlightsOn) Color(0xFFFFFDE7) else Color(0xFFCFD8DC)
                polys.addAll(createBox(pos + Vector3(-0.8f, 0.8f, 4.02f).rotateY(yawRad), Vector3(0.4f, 0.3f, 0.1f), headCol, yawRad))
                polys.addAll(createBox(pos + Vector3(0.8f, 0.8f, 4.02f).rotateY(yawRad), Vector3(0.4f, 0.3f, 0.1f), headCol, yawRad))
                // Rear brake lights
                polys.addAll(createBox(pos + Vector3(0f, 1.0f, -4.02f).rotateY(yawRad), Vector3(1.8f, 0.3f, 0.1f), Color(0xFFD50000), yawRad))
            }

            TrafficType.CAR -> {
                // Private Sedan
                val carWhite = Color(0xFFE0E0E0)
                polys.addAll(createBox(pos + Vector3(0f, 0.6f, 0f).rotateY(yawRad), Vector3(1.8f, 0.8f, 4.4f), carWhite, yawRad))
                polys.addAll(createBox(pos + Vector3(0f, 1.15f, -0.3f).rotateY(yawRad), Vector3(1.5f, 0.7f, 2.2f), Color(0xFF37474F), yawRad))
                val headCol = if (headlightsOn) Color(0xFFFFFDE7) else Color(0xFFCFD8DC)
                polys.addAll(createBox(pos + Vector3(-0.65f, 0.55f, 2.22f).rotateY(yawRad), Vector3(0.35f, 0.25f, 0.08f), headCol, yawRad))
                polys.addAll(createBox(pos + Vector3(0.65f, 0.55f, 2.22f).rotateY(yawRad), Vector3(0.35f, 0.25f, 0.08f), headCol, yawRad))
            }

            TrafficType.MOTORCYCLE -> {
                // Motorcycle with rider
                polys.addAll(createBox(pos + Vector3(0f, 0.5f, 0f).rotateY(yawRad), Vector3(0.4f, 0.7f, 1.8f), Color(0xFFD32F2F), yawRad))
                // Rider torso & helmet
                polys.addAll(createBox(pos + Vector3(0f, 1.25f, -0.1f).rotateY(yawRad), Vector3(0.5f, 0.6f, 0.4f), Color(0xFF1E88E5), yawRad))
                polys.addAll(createBox(pos + Vector3(0f, 1.7f, -0.1f).rotateY(yawRad), Vector3(0.35f, 0.35f, 0.35f), Color(0xFFFFA000), yawRad))
            }

            TrafficType.PEDESTRIAN -> {
                // Roadside Pedestrian (villager / passenger with bag)
                polys.addAll(createBox(pos + Vector3(0f, 0.7f, 0f).rotateY(yawRad), Vector3(0.4f, 0.8f, 0.3f), Color(0xFF43A047), yawRad))
                polys.addAll(createBox(pos + Vector3(0f, 1.35f, 0f).rotateY(yawRad), Vector3(0.3f, 0.3f, 0.3f), Color(0xFF8D6E63), yawRad))
            }
        }

        return polys
    }

    // Builds Padma Bridge / Jamuna Bridge 3D Segment
    fun buildBridgeSegment(centerZ: Float, isPadmaBridge: Boolean): List<Polygon3D> {
        val polys = mutableListOf<Polygon3D>()

        val roadWidth = 14f
        val segLen = 40f
        val steelColor = if (isPadmaBridge) Color(0xFF455A64) else Color(0xFF546E7A)
        val pierColor = Color(0xFFCFD8DC)
        val waterColor = Color(0xFF1B3B4B)

        // River water surface below bridge
        polys.addAll(
            createBox(
                center = Vector3(0f, -8f, centerZ),
                size = Vector3(120f, 0.5f, segLen),
                color = waterColor,
                topColor = Color(0xFF24485C)
            )
        )

        // Massive Concrete Pier Column
        polys.addAll(
            createBox(
                center = Vector3(0f, -4f, centerZ),
                size = Vector3(4f, 8f, 4f),
                color = pierColor
            )
        )

        // Steel Truss Arches / Superstructure on sides
        // Left Steel Truss
        polys.addAll(
            createBox(
                center = Vector3(-roadWidth / 2f - 0.5f, 4f, centerZ),
                size = Vector3(0.6f, 7.5f, segLen),
                color = steelColor,
                isWireframe = true,
                wireframeWidth = 3f
            )
        )
        // Right Steel Truss
        polys.addAll(
            createBox(
                center = Vector3(roadWidth / 2f + 0.5f, 4f, centerZ),
                size = Vector3(0.6f, 7.5f, segLen),
                color = steelColor,
                isWireframe = true,
                wireframeWidth = 3f
            )
        )

        // Overhead Cross Beams
        polys.addAll(
            createBox(
                center = Vector3(0f, 7.8f, centerZ),
                size = Vector3(roadWidth + 1.6f, 0.4f, 0.6f),
                color = steelColor
            )
        )

        // Crash Barriers / Guardrails
        polys.addAll(
            createBox(
                center = Vector3(-roadWidth / 2f - 0.2f, 0.6f, centerZ),
                size = Vector3(0.3f, 0.8f, segLen),
                color = Color(0xFFECEFF1)
            )
        )
        polys.addAll(
            createBox(
                center = Vector3(roadWidth / 2f + 0.2f, 0.6f, centerZ),
                size = Vector3(0.3f, 0.8f, segLen),
                color = Color(0xFFECEFF1)
            )
        )

        return polys
    }

    // Builds Roadside Bangladeshi Scenery (Village Tea Stall, Mosque, Palm Trees, Brick Kiln)
    fun buildRoadsideElement(elementZ: Float, side: Float, typeIdx: Int): List<Polygon3D> {
        val polys = mutableListOf<Polygon3D>()
        val sideDist = if (side > 0) 10f else -10f
        val pos = Vector3(sideDist, 0f, elementZ)

        when (typeIdx % 5) {
            0 -> {
                // Coconut / Palm Tree
                val trunkColor = Color(0xFF5D4037)
                val leafGreen = Color(0xFF2E7D32)
                // Tall curved trunk
                polys.addAll(createBox(pos + Vector3(0f, 3.5f, 0f), Vector3(0.45f, 7f, 0.45f), trunkColor))
                // Palm foliage crown
                polys.addAll(createBox(pos + Vector3(0f, 7.2f, 0f), Vector3(3.5f, 1.2f, 3.5f), leafGreen))
                polys.addAll(createBox(pos + Vector3(0f, 7.8f, 0f), Vector3(2.2f, 1.0f, 2.2f), Color(0xFF388E3C)))
            }
            1 -> {
                // Roadside Tea Stall ("Tong er Dokan") with bench
                val tinRoof = Color(0xFF78909C)
                val stallWood = Color(0xFF6D4C41)
                // Shack body
                polys.addAll(createBox(pos + Vector3(0f, 1.2f, 0f), Vector3(3.2f, 2.2f, 2.8f), stallWood))
                // Sloped tin corrugated roof
                polys.addAll(createBox(pos + Vector3(0f, 2.4f, 0f), Vector3(3.8f, 0.3f, 3.4f), tinRoof))
                // Colorful shade canopy / umbrella
                polys.addAll(createBox(pos + Vector3(side * -1.5f, 1.8f, 0f), Vector3(1.8f, 0.15f, 2.4f), Color(0xFFE53935)))
                // Wooden bench outside
                polys.addAll(createBox(pos + Vector3(side * -1.6f, 0.35f, 0f), Vector3(0.6f, 0.4f, 1.8f), Color(0xFF8D6E63)))
            }
            2 -> {
                // Bangladeshi Brick Kiln (Eet-Bhata) with tall smoking chimney
                val brickRed = Color(0xFFB71C1C)
                val smokeGrey = Color(0xFFE0E0E0)
                // Base kiln mound
                polys.addAll(createBox(pos + Vector3(side * 5f, 1.5f, 0f), Vector3(10f, 3f, 14f), brickRed))
                // Tall circular tapering chimney
                polys.addAll(createBox(pos + Vector3(side * 5f, 8f, 0f), Vector3(1.6f, 14f, 1.6f), Color(0xFF8D6E63)))
                // Smoke puff near top
                polys.addAll(createBox(pos + Vector3(side * 5f, 16f, 1f), Vector3(2.5f, 2.5f, 2.5f), smokeGrey))
            }
            3 -> {
                // Roadside Mosque with Green Dome & Minaret
                val whiteWall = Color(0xFFECEFF1)
                val domeGreen = Color(0xFF1B5E20)
                // Main prayer hall
                polys.addAll(createBox(pos + Vector3(side * 4f, 2.5f, 0f), Vector3(6f, 5f, 7f), whiteWall))
                // Green hemispherical dome
                polys.addAll(createBox(pos + Vector3(side * 4f, 5.6f, 0f), Vector3(3.5f, 2.2f, 3.5f), domeGreen))
                // Tall Minaret
                polys.addAll(createBox(pos + Vector3(side * 1.5f, 6.5f, 3f), Vector3(1.2f, 13f, 1.2f), whiteWall))
                polys.addAll(createBox(pos + Vector3(side * 1.5f, 13.5f, 3f), Vector3(1.4f, 1.2f, 1.4f), domeGreen))
            }
            4 -> {
                // Roadside Bus Passenger Terminal / Stop Shelter
                val shelterBlue = Color(0xFF0277BD)
                // Concrete passenger platform
                polys.addAll(createBox(pos + Vector3(side * -1.5f, 0.3f, 0f), Vector3(2.5f, 0.4f, 6.0f), Color(0xFFCFD8DC)))
                // Canopy Roof
                polys.addAll(createBox(pos + Vector3(side * -1.5f, 2.2f, 0f), Vector3(2.6f, 0.2f, 5.8f), shelterBlue))
                // Signboard
                polys.addAll(createBox(pos + Vector3(side * -1.5f, 2.6f, 0f), Vector3(0.1f, 0.6f, 3.0f), Color(0xFFFDD835)))
            }
        }

        return polys
    }
}
