package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.audio.GameSoundEngine
import com.example.data.BusRepository
import com.example.engine3d.Camera3D
import com.example.engine3d.Polygon3D
import com.example.engine3d.Vector3
import com.example.engine3d.World3D
import com.example.model.BusLivery
import com.example.model.BusModel
import com.example.model.BusUpgrades
import com.example.model.CameraView
import com.example.model.Gear
import com.example.model.RouteInfo
import com.example.model.WeatherType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class PassengerStop(
    val z: Float,
    val name: String,
    val isTerminal: Boolean = false,
    var isCompleted: Boolean = false
)

data class TollPlaza(
    val z: Float,
    val name: String,
    val feeTaka: Int = 200,
    var isPaid: Boolean = false
)

class GameSimulation(
    val busModel: BusModel,
    val livery: BusLivery,
    val upgrades: BusUpgrades,
    val route: RouteInfo,
    var weather: WeatherType,
    private val soundEngine: GameSoundEngine
) {
    // Bus Transform & Movement
    var posX = 1.8f // Start in slow lane
    var posY = 0f
    var posZ = 20f

    var yaw = 0f // Facing +Z
    var pitch = 0f
    var speedKmh = 0f
    var targetSpeedKmh = 0f
    var steeringInput = 0f // -1.0 (left) to +1.0 (right)
    var throttleInput = 0f // 0.0 to 1.0
    var brakeInput = 0f // 0.0 to 1.0
    var handbrakeEngaged = false

    var currentGear = Gear.DRIVE

    // Mechanical & Electronics
    var engineRpm = 850f
    var fuelLiters = busModel.fuelCapacityLiters * upgrades.fuelMultiplier()
    val maxFuelLiters = fuelLiters
    var damagePercent = 0f // 0% (pristine) to 100% (totaled)

    var headlightsOn = weather == WeatherType.NIGHT || weather == WeatherType.FOGGY
    var highBeam = false
    var wipersOn = weather == WeatherType.MONSOON_RAIN
    var wiperAngleDeg = 0f
    private var wiperDirection = 1f

    var turnIndicatorLeft = false
    var turnIndicatorRight = false
    var hazardLights = false
    private var indicatorTimer = 0f
    var indicatorBlink = false

    var doorOpen = false
    var doorProgress = 0f // 0 = closed, 1 = fully open

    // Camera
    var cameraView = CameraView.CHASE
    val camera = Camera3D()

    // Passengers & Economy
    var passengersOnboard = 12
    var maxPassengers = busModel.passengerSeats
    var passengerSatisfaction = 100f // 0 to 100
    var fareCollected = 0
    var cleanOvertakes = 0
    var speedViolations = 0

    // Notification / Toast for HUD
    var hudMessage: String = "Welcome aboard ${busModel.name}!"
    var hudMessageTimer: Float = 4.0f

    // Route progress
    val totalRouteDistanceMeters = route.distanceKm * 40f // scaled for gameplay
    var isMissionComplete = false
    var isGameOver = false
    var gameOverReason = ""

    // Stops and Tolls
    val stops = mutableListOf<PassengerStop>()
    val tollPlazas = mutableListOf<TollPlaza>()
    var boardingTimer = 0f
    var isBoardingPassengers = false

    val trafficManager = TrafficManager()

    init {
        // Generate stops along the route
        val stopInterval = totalRouteDistanceMeters / (route.stopsCount + 1)
        for (i in 1..route.stopsCount) {
            val stopZ = i * stopInterval
            val stopName = when (i) {
                1 -> "Highway Passenger Counter"
                2 -> if (route.hasPadmaBridge) "Mawa Terminal Stop" else "Bazaar Junction"
                3 -> if (route.hasTeaGardens) "Sreemangal Tea Counter" else "Bypass Highway Station"
                else -> "Central District Stop"
            }
            stops.add(PassengerStop(stopZ, stopName))
        }
        stops.add(PassengerStop(totalRouteDistanceMeters, route.toCity, isTerminal = true))

        // Toll plaza if applicable
        if (route.hasPadmaBridge) {
            tollPlazas.add(TollPlaza(totalRouteDistanceMeters * 0.35f, "Padma Bridge Toll Plaza", 500))
        } else if (route.hasJamunaBridge) {
            tollPlazas.add(TollPlaza(totalRouteDistanceMeters * 0.4f, "Bangabandhu Bridge Toll Plaza", 450))
        }

        trafficManager.reset(posZ)
    }

    fun honkHorn() {
        soundEngine.playHorn(upgrades.hornType)
        trafficManager.update(0f, posZ, playerHonked = true)
        showHudMessage("Melodic Air Horn!")
    }

    fun toggleHeadlights() {
        headlightsOn = !headlightsOn
        showHudMessage(if (headlightsOn) "Headlights: ON" else "Headlights: OFF")
    }

    fun toggleWipers() {
        wipersOn = !wipersOn
        showHudMessage(if (wipersOn) "Wipers: ON" else "Wipers: OFF")
    }

    fun toggleDoor() {
        if (abs(speedKmh) > 5f) {
            showHudMessage("Cannot open door while moving!")
            return
        }
        doorOpen = !doorOpen
        soundEngine.playAirBrakeHiss()
        showHudMessage(if (doorOpen) "Passenger Door: OPEN" else "Passenger Door: CLOSED")
    }

    fun toggleLeftIndicator() {
        turnIndicatorLeft = !turnIndicatorLeft
        if (turnIndicatorLeft) turnIndicatorRight = false
    }

    fun toggleRightIndicator() {
        turnIndicatorRight = !turnIndicatorRight
        if (turnIndicatorRight) turnIndicatorLeft = false
    }

    fun toggleHazard() {
        hazardLights = !hazardLights
    }

    fun switchCamera() {
        cameraView = when (cameraView) {
            CameraView.CHASE -> CameraView.CABIN
            CameraView.CABIN -> CameraView.HOOD
            CameraView.HOOD -> CameraView.DRONE
            CameraView.DRONE -> CameraView.CHASE
        }
        showHudMessage(cameraView.label)
    }

    fun setGear(gear: Gear) {
        if (currentGear != gear) {
            currentGear = gear
            soundEngine.playAirBrakeHiss()
            showHudMessage("Gear: ${gear.shortName}")
        }
    }

    fun showHudMessage(msg: String) {
        hudMessage = msg
        hudMessageTimer = 3.5f
    }

    fun update(deltaSec: Float) {
        if (isGameOver || isMissionComplete) return

        // Update HUD message timer
        if (hudMessageTimer > 0f) {
            hudMessageTimer -= deltaSec
        }

        // Indicator blinking & sounds
        val anyIndicator = turnIndicatorLeft || turnIndicatorRight || hazardLights
        if (anyIndicator) {
            indicatorTimer += deltaSec
            if (indicatorTimer >= 0.38f) {
                indicatorTimer = 0f
                indicatorBlink = !indicatorBlink
                soundEngine.playIndicatorClick(indicatorBlink)
            }
        } else {
            indicatorBlink = false
            indicatorTimer = 0f
        }

        // Wiper animation
        if (wipersOn) {
            wiperAngleDeg += wiperDirection * 150f * deltaSec
            if (wiperAngleDeg > 75f) {
                wiperAngleDeg = 75f
                wiperDirection = -1f
            } else if (wiperAngleDeg < 0f) {
                wiperAngleDeg = 0f
                wiperDirection = 1f
            }
        }

        // Door open/close animation
        val doorTarget = if (doorOpen) 1.0f else 0.0f
        doorProgress += (doorTarget - doorProgress) * 4.0f * deltaSec

        // Passenger Stop & Boarding Logic
        handlePassengerStops(deltaSec)

        // Toll Plaza Logic
        handleTollPlazas()

        // Physics & Engine simulation
        updatePhysics(deltaSec)

        // Traffic AI & Collision
        trafficManager.update(deltaSec, posZ, playerHonked = false)
        checkCollisions()

        // Fuel Consumption
        val fuelBurnRate = (0.015f + (speedKmh / 140f) * 0.045f) * deltaSec
        fuelLiters = (fuelLiters - fuelBurnRate).coerceAtLeast(0f)
        if (fuelLiters <= 0.01f && speedKmh < 2f) {
            isGameOver = true
            gameOverReason = "Out of Fuel on Highway! Refuel at petrol pump."
        }

        // Update Audio
        soundEngine.engineRpm = engineRpm
        soundEngine.engineLoad = throttleInput

        // Update Camera
        updateCamera()

        // Check if finished destination
        if (posZ >= totalRouteDistanceMeters) {
            isMissionComplete = true
        }
    }

    private fun handlePassengerStops(deltaSec: Float) {
        val currentStop = stops.find { !it.isCompleted && abs(it.z - posZ) < 25f }

        if (currentStop != null) {
            val dist = currentStop.z - posZ
            if (abs(dist) < 8f && abs(speedKmh) < 3f) {
                // Bus stopped in zone
                if (doorOpen) {
                    if (!isBoardingPassengers) {
                        isBoardingPassengers = true
                        boardingTimer = 0f
                        soundEngine.playChime()
                        showHudMessage("Passengers Boarding at ${currentStop.name}...")
                    }

                    boardingTimer += deltaSec
                    if (boardingTimer > 2.5f) {
                        // Completed stop!
                        val newPassengers = Random.nextInt(4, 12).coerceAtMost(maxPassengers - passengersOnboard)
                        passengersOnboard += newPassengers
                        val stopFare = (newPassengers * 180) + 600
                        fareCollected += stopFare
                        currentStop.isCompleted = true
                        isBoardingPassengers = false
                        soundEngine.playChime()
                        showHudMessage("+৳$stopFare collected! Close door to depart.")
                    }
                } else {
                    showHudMessage("STOPPED AT ${currentStop.name.uppercase()}. OPEN DOOR TO BOARD PASSENGERS.")
                }
            }
        }
    }

    private fun handleTollPlazas() {
        val toll = tollPlazas.find { !it.isPaid && abs(it.z - posZ) < 15f }
        if (toll != null && abs(speedKmh) < 5f) {
            toll.isPaid = true
            fareCollected = (fareCollected - toll.feeTaka).coerceAtLeast(0)
            soundEngine.playChime()
            showHudMessage("${toll.name}: Paid ৳${toll.feeTaka} Toll. Gate Open!")
        }
    }

    private fun updatePhysics(deltaSec: Float) {
        val topSpeed = busModel.topSpeedKmh * upgrades.engineMultiplier()
        val accelPower = busModel.acceleration * upgrades.engineMultiplier() * 14.0f
        val brakePower = busModel.braking * upgrades.brakeMultiplier() * 22.0f

        // Transmission drive factor
        val driveFactor = when (currentGear) {
            Gear.DRIVE -> 1.0f
            Gear.REVERSE -> -0.35f
            Gear.GEAR_1 -> 0.35f
            Gear.GEAR_2 -> 0.55f
            Gear.GEAR_3 -> 0.75f
            Gear.GEAR_4 -> 0.95f
            Gear.GEAR_5 -> 1.15f
            Gear.GEAR_6 -> 1.35f
            else -> 0.0f // Neutral / Park
        }

        // Throttle & Acceleration
        if (throttleInput > 0.05f && driveFactor != 0f) {
            val forwardForce = throttleInput * accelPower * driveFactor
            targetSpeedKmh = (topSpeed * throttleInput * driveFactor).coerceIn(-35f, topSpeed)
            speedKmh += forwardForce * deltaSec
        } else {
            // Natural rolling resistance
            val rollDrag = 4.5f * deltaSec
            if (speedKmh > rollDrag) speedKmh -= rollDrag
            else if (speedKmh < -rollDrag) speedKmh += rollDrag
            else speedKmh = 0f
        }

        // Braking
        if (brakeInput > 0.05f) {
            val decel = brakeInput * brakePower * deltaSec
            if (speedKmh > 0f) {
                speedKmh = (speedKmh - decel).coerceAtLeast(0f)
            } else if (speedKmh < 0f) {
                speedKmh = (speedKmh + decel).coerceAtMost(0f)
            }
            // Air brake dump sound when coming to stop
            if (abs(speedKmh) < 2f && abs(speedKmh) > 0.1f) {
                soundEngine.playAirBrakeHiss()
            }
        }

        // Handbrake
        if (handbrakeEngaged) {
            speedKmh = 0f
        }

        // Speed limit clamp
        speedKmh = speedKmh.coerceIn(-35f, topSpeed)

        // Engine RPM calculation
        val speedNorm = (abs(speedKmh) / topSpeed).coerceIn(0f, 1f)
        val targetRpm = 800f + (speedNorm * 2000f) + (throttleInput * 400f)
        engineRpm += (targetRpm - engineRpm) * 6f * deltaSec

        // Steering dynamics (Ackermann-style responsiveness)
        val turnRate = (steeringInput * 0.9f) * (1.0f - (abs(speedKmh) / (topSpeed * 1.5f)))
        yaw += turnRate * (speedKmh / 50f) * deltaSec

        // Position displacement
        val speedMps = (speedKmh * 1000f) / 3600f
        val forwardX = -sin(yaw) * speedMps * deltaSec
        val forwardZ = cos(yaw) * speedMps * deltaSec

        posX += forwardX
        posZ += forwardZ

        // Keep bus reasonably on the highway corridor (-7.5m to +7.5m)
        if (abs(posX) > 6.8f) {
            // Off-roading friction & passenger dissatisfaction
            speedKmh *= (1f - 2.5f * deltaSec)
            passengerSatisfaction = (passengerSatisfaction - 8f * deltaSec).coerceAtLeast(10f)
            if (abs(posX) > 7.4f) {
                posX = if (posX > 0) 7.4f else -7.4f
                damagePercent += 0.4f * deltaSec
                soundEngine.playCrash()
            }
        }

        // Speed limit check (80 km/h on BD national highways)
        if (speedKmh > 85f) {
            speedViolations++
            passengerSatisfaction = (passengerSatisfaction - 0.2f * deltaSec).coerceAtLeast(10f)
        }
    }

    private fun checkCollisions() {
        val hitVehicle = trafficManager.checkCollision(
            playerX = posX,
            playerZ = posZ,
            playerWidth = 2.5f,
            playerLength = 11.0f
        )

        if (hitVehicle != null) {
            soundEngine.playCrash()

            // Push back physics
            val impactSpeed = abs(speedKmh - hitVehicle.speedKmh)
            val damageDealt = (impactSpeed * 0.35f).coerceIn(5f, 45f)
            damagePercent += damageDealt

            speedKmh *= -0.3f // Rebound
            passengerSatisfaction = (passengerSatisfaction - 25f).coerceAtLeast(0f)

            showHudMessage("CRASH! ${hitVehicle.type.title} -${damageDealt.toInt()}% Damage")

            // Knock traffic vehicle ahead
            hitVehicle.z += 10f

            if (damagePercent >= 100f) {
                isGameOver = true
                gameOverReason = "Bus Destroyed in Highway Collision! Total damage 100%."
            }
        }
    }

    private fun updateCamera() {
        when (cameraView) {
            CameraView.CHASE -> {
                // Smooth chase camera behind and above bus
                val chaseDist = 8.5f
                val chaseHeight = 3.6f
                camera.position = Vector3(
                    posX + sin(yaw) * chaseDist,
                    posY + chaseHeight,
                    posZ - cos(yaw) * chaseDist
                )
                camera.target = Vector3(posX, posY + 1.6f, posZ)
                camera.yaw = yaw
                camera.pitch = 0.08f
            }
            CameraView.CABIN -> {
                // Driver cockpit position inside bus (Bangladeshi right-hand drive!)
                val driverX = posX + 0.55f
                val driverY = posY + 2.0f
                val driverZ = posZ + 3.8f
                camera.position = Vector3(driverX, driverY, driverZ)
                camera.target = Vector3(
                    driverX - sin(yaw) * 20f,
                    driverY,
                    driverZ + cos(yaw) * 20f
                )
                camera.yaw = yaw
                camera.pitch = 0.02f
            }
            CameraView.HOOD -> {
                // Low bumper camera
                val hoodX = posX
                val hoodY = posY + 0.9f
                val hoodZ = posZ + 5.2f
                camera.position = Vector3(hoodX, hoodY, hoodZ)
                camera.target = Vector3(
                    hoodX - sin(yaw) * 25f,
                    hoodY,
                    hoodZ + cos(yaw) * 25f
                )
                camera.yaw = yaw
                camera.pitch = 0.02f
            }
            CameraView.DRONE -> {
                // High tactical overview camera
                camera.position = Vector3(posX, posY + 18f, posZ - 12f)
                camera.target = Vector3(posX, posY, posZ + 8f)
                camera.yaw = yaw
                camera.pitch = 0.65f
            }
        }
    }

    // Collects all 3D polygons for rendering
    fun getAllScenePolygons(): List<Polygon3D> {
        val polys = mutableListOf<Polygon3D>()

        // 1. Road Segments (Asphalt, markings, curbs)
        val roadSegCount = 20
        val segLen = 18f
        val startZ = ((posZ - 30f) / segLen).toInt() * segLen

        for (i in 0 until roadSegCount) {
            val z = startZ + i * segLen

            val isBridge = (route.hasPadmaBridge && z > totalRouteDistanceMeters * 0.32f && z < totalRouteDistanceMeters * 0.48f) ||
                    (route.hasJamunaBridge && z > totalRouteDistanceMeters * 0.38f && z < totalRouteDistanceMeters * 0.52f)

            if (isBridge) {
                // Render Bridge superstructure & pillars
                polys.addAll(World3D.buildBridgeSegment(z, isPadmaBridge = route.hasPadmaBridge))
            }

            // Road surface
            val roadWidth = 13.5f
            val roadColor = if (weather == WeatherType.MONSOON_RAIN) Color(0xFF161A1D) else Color(0xFF22262A)
            polys.addAll(
                World3D.createBox(
                    center = Vector3(0f, -0.1f, z),
                    size = Vector3(roadWidth, 0.2f, segLen),
                    color = roadColor,
                    topColor = roadColor
                )
            )

            // Yellow Center Divider Line
            polys.addAll(
                World3D.createBox(
                    center = Vector3(0f, 0.02f, z),
                    size = Vector3(0.35f, 0.02f, segLen),
                    color = Color(0xFFFFD54F)
                )
            )

            // White Dashed Lane Lines
            val dashZ = z - (segLen / 4f)
            polys.addAll(
                World3D.createBox(
                    center = Vector3(3.2f, 0.02f, dashZ),
                    size = Vector3(0.2f, 0.02f, segLen * 0.4f),
                    color = Color(0xFFE0E0E0)
                )
            )
            polys.addAll(
                World3D.createBox(
                    center = Vector3(-3.2f, 0.02f, dashZ),
                    size = Vector3(0.2f, 0.02f, segLen * 0.4f),
                    color = Color(0xFFE0E0E0)
                )
            )

            // Roadside Elements (Palm trees, Tea stall, Mosque, Brick kiln)
            if (!isBridge) {
                polys.addAll(World3D.buildRoadsideElement(z, side = 1f, typeIdx = i))
                polys.addAll(World3D.buildRoadsideElement(z + (segLen / 2f), side = -1f, typeIdx = i + 2))
            }
        }

        // 2. Upcoming Stop Markers (Yellow glowing ground zone)
        for (stop in stops) {
            if (!stop.isCompleted && abs(stop.z - posZ) < 180f) {
                polys.addAll(
                    World3D.createBox(
                        center = Vector3(3.8f, 0.08f, stop.z),
                        size = Vector3(3.2f, 0.1f, 14f),
                        color = Color(0xCCFFD600),
                        topColor = Color(0xEEFFD600)
                    )
                )
            }
        }

        // 3. Toll Plaza Booths
        for (toll in tollPlazas) {
            if (abs(toll.z - posZ) < 180f) {
                // Overhead Toll Canopy
                polys.addAll(
                    World3D.createBox(
                        center = Vector3(0f, 4.5f, toll.z),
                        size = Vector3(16f, 0.8f, 4f),
                        color = Color(0xFF006A4E),
                        topColor = Color(0xFFF42A41)
                    )
                )
                // Toll collection booths
                polys.addAll(
                    World3D.createBox(
                        center = Vector3(0f, 1.2f, toll.z),
                        size = Vector3(1.4f, 2.4f, 2.2f),
                        color = Color(0xFF37474F)
                    )
                )
            }
        }

        // 4. Traffic Vehicles
        polys.addAll(trafficManager.generatePolygons(headlightsOn))

        // 5. Player Bus (only visible in exterior cameras)
        if (cameraView != CameraView.CABIN && cameraView != CameraView.HOOD) {
            polys.addAll(
                World3D.buildPlayerBus(
                    pos = Vector3(posX, posY, posZ),
                    yawRad = yaw,
                    pitchRad = pitch,
                    livery = livery,
                    headlightsOn = headlightsOn,
                    braking = brakeInput > 0.1f,
                    turnIndicatorLeft = turnIndicatorLeft || hazardLights,
                    turnIndicatorRight = turnIndicatorRight || hazardLights,
                    indicatorBlink = indicatorBlink,
                    wiperAngleDeg = wiperAngleDeg
                )
            )
        }

        return polys
    }
}
