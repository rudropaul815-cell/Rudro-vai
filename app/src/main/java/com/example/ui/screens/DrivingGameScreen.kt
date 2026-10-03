package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameSoundEngine
import com.example.engine3d.Renderer3D
import com.example.game.GameSimulation
import com.example.model.ControlMode
import com.example.ui.components.BrakePedal
import com.example.ui.components.DashboardHUD
import com.example.ui.components.GasPedal
import com.example.ui.components.GearShifter
import com.example.ui.components.MinimapRadar
import com.example.ui.components.TouchArrowButtons
import com.example.ui.components.VirtualSteeringWheel
import com.example.ui.theme.BdEmerald
import com.example.ui.theme.BdRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepNight

@Composable
fun DrivingGameScreen(
    sim: GameSimulation,
    controlMode: ControlMode,
    soundEngine: GameSoundEngine,
    onMissionSuccess: () -> Unit,
    onGameOver: () -> Unit,
    onExitToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val renderer = remember { Renderer3D() }
    var isPaused by remember { mutableStateOf(false) }

    // Start audio engine
    LaunchedEffect(Unit) {
        soundEngine.startEngineSound(this)
    }

    // High performance 60 FPS simulation loop
    LaunchedEffect(isPaused) {
        if (!isPaused) {
            var lastTimeNanos = System.nanoTime()
            while (true) {
                withFrameNanos { nowNanos ->
                    val deltaSec = ((nowNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                    lastTimeNanos = nowNanos

                    sim.update(deltaSec)

                    if (sim.isGameOver) {
                        onGameOver()
                    } else if (sim.isMissionComplete) {
                        onMissionSuccess()
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNight)
    ) {
        // --- 1. 3D WORLD CANVAS RENDERING ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scenePolys = sim.getAllScenePolygons()
            renderer.renderScene(
                drawScope = this,
                camera = sim.camera,
                cameraView = sim.cameraView,
                polygons = scenePolys,
                weather = sim.weather,
                headlightsOn = sim.headlightsOn,
                wiperAngleDeg = sim.wiperAngleDeg,
                playerSpeedKmh = sim.speedKmh,
                roadCurve = sim.steeringInput
            )
        }

        // --- 2. GPS MINIMAP RADAR (TOP RIGHT) ---
        MinimapRadar(
            sim = sim,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 46.dp, end = 12.dp)
        )

        // --- 3. DASHBOARD HUD (TOP BARS & BOTTOM GAUGES) ---
        DashboardHUD(
            sim = sim,
            onPauseClicked = { isPaused = true },
            modifier = Modifier.fillMaxSize()
        )

        // --- 4. TOUCH STEERING CONTROLS (BOTTOM-LEFT) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 80.dp)
        ) {
            if (controlMode == ControlMode.STEERING_WHEEL) {
                VirtualSteeringWheel(
                    onSteerAngleChanged = { steer -> sim.steeringInput = steer },
                    onHornTapped = { sim.honkHorn() }
                )
            } else {
                TouchArrowButtons(
                    onSteerChanged = { steer -> sim.steeringInput = steer }
                )
            }
        }

        // --- 5. GAS, BRAKE PEDALS & GEAR SHIFTER (BOTTOM-RIGHT) ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 80.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Gear Shifter (P - R - N - D)
            GearShifter(
                currentGear = sim.currentGear,
                onGearSelected = { gear -> sim.setGear(gear) }
            )

            // Brake Pedal
            BrakePedal(
                onBrakePressed = { pressed ->
                    sim.brakeInput = if (pressed) 1.0f else 0.0f
                }
            )

            // Gas Pedal
            GasPedal(
                onGasPressed = { pressed ->
                    sim.throttleInput = if (pressed) 1.0f else 0.0f
                }
            )
        }

        // --- 6. PAUSE DIALOG ---
        if (isPaused) {
            AlertDialog(
                onDismissRequest = { isPaused = false },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = "GAME PAUSED",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Trip: ${sim.route.fromCity} ➔ ${sim.route.toCity}\nBus: ${sim.busModel.name}\nDamage: ${sim.damagePercent.toInt()}%",
                        color = Color(0xFFCFD8DC),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BdEmerald),
                        modifier = Modifier.testTag("resume_button")
                    ) {
                        Text("RESUME TRIP", color = DeepNight, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            isPaused = false
                            onExitToMenu()
                        },
                        modifier = Modifier.testTag("exit_trip_button")
                    ) {
                        Text("EXIT TO MENU", color = Color(0xFFCFD8DC))
                    }
                }
            )
        }
    }
}
