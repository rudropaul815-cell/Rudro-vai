package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameSimulation
import com.example.model.Gear
import com.example.ui.theme.BdEmerald
import com.example.ui.theme.BdGold
import com.example.ui.theme.BdGreen
import com.example.ui.theme.BdRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarningOrange
import kotlin.math.abs

@Composable
fun DashboardHUD(
    sim: GameSimulation,
    onPauseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // --- 1. TOP STATUS BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Earnings & Route
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pause Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("pause_button")
                        .clip(CircleShape)
                        .background(Color(0xCC1E2824))
                        .clickable { onPauseClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Currency Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD0D1E16))
                        .border(1.dp, BdEmerald, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "৳ ${sim.fareCollected}",
                        color = BdGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Passenger count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC1A2621))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Passengers",
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${sim.passengersOnboard}/${sim.maxPassengers}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            // Right: Health & Fuel Bars + Camera Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Damage indicator
                val healthPct = (100f - sim.damagePercent).coerceIn(0f, 100f)
                val healthColor = if (healthPct > 60f) BdEmerald else if (healthPct > 30f) WarningOrange else BdRed
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC1A2621))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Bus Health",
                        tint = healthColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${healthPct.toInt()}%",
                        color = healthColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Fuel Indicator
                val fuelPct = (sim.fuelLiters / sim.maxFuelLiters).coerceIn(0f, 1f)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC1A2621))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = "Fuel",
                        tint = if (fuelPct < 0.2f) BdRed else BdGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(fuelPct * 100).toInt()}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Camera Angle Toggle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("camera_switch_button")
                        .clip(CircleShape)
                        .background(Color(0xDD0E2A1E))
                        .border(1.dp, BdEmerald, CircleShape)
                        .clickable { sim.switchCamera() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Switch Camera View",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // --- 2. FLOATING HUD TOAST BANNER ---
        AnimatedVisibility(
            visible = sim.hudMessageTimer > 0f,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xEE0B1A14))
                    .border(1.dp, BdEmerald, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = sim.hudMessage,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 3. BOTTOM COCKPIT HUD (SPEEDOMETER, GEAR, LIGHTS, HORN, DOOR) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Speedometer & Indicators Widget (Center-Left)
            CockpitGaugesWidget(sim = sim)

            // Center Quick Controls (Horn, Door, Lights, Wipers)
            CockpitActionButtons(sim = sim)
        }
    }
}

@Composable
fun CockpitGaugesWidget(sim: GameSimulation) {
    Box(
        modifier = Modifier
            .size(width = 130.dp, height = 95.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xDD0F1916))
            .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(14.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        val speed = abs(sim.speedKmh).toInt()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Turn indicator icons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left arrow
                val leftActive = (sim.turnIndicatorLeft || sim.hazardLights) && sim.indicatorBlink
                Text(
                    text = "◀",
                    color = if (leftActive) WarningOrange else Color(0x33FFA000),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Gear indicator
                Text(
                    text = sim.currentGear.shortName,
                    color = if (sim.currentGear == Gear.REVERSE) BdRed else BdEmerald,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )

                // Right arrow
                val rightActive = (sim.turnIndicatorRight || sim.hazardLights) && sim.indicatorBlink
                Text(
                    text = "▶",
                    color = if (rightActive) WarningOrange else Color(0x33FFA000),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Large Digital Speed
            Text(
                text = "$speed",
                color = NeonCyan,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "KM / H",
                color = Color(0xFF90A4AE),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // RPM progress arc/bar
            val rpmNorm = ((sim.engineRpm - 700f) / 2200f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { rpmNorm },
                modifier = Modifier
                    .width(100.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (rpmNorm > 0.85f) BdRed else BdEmerald,
                trackColor = Color(0xFF263238)
            )
        }
    }
}

@Composable
fun CockpitActionButtons(sim: GameSimulation) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Melodic Bangladesh Pressure Horn!
        Box(
            modifier = Modifier
                .size(54.dp)
                .testTag("horn_button")
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(BdRed, Color(0xFF880E4F))
                    )
                )
                .border(2.dp, Color(0xFFFF8A80), CircleShape)
                .clickable { sim.honkHorn() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Air Horn",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text("HORN", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Passenger Door Open/Close
        val doorReady = abs(sim.speedKmh) < 4f
        Box(
            modifier = Modifier
                .size(46.dp)
                .testTag("door_button")
                .clip(CircleShape)
                .background(if (sim.doorOpen) BdGold else if (doorReady) BdEmerald else Color(0xFF37474F))
                .border(1.5.dp, Color.White, CircleShape)
                .clickable { sim.toggleDoor() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.DoorFront,
                    contentDescription = "Door Control",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (sim.doorOpen) "OPEN" else "DOOR",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Headlights Toggle
        Box(
            modifier = Modifier
                .size(42.dp)
                .testTag("headlights_button")
                .clip(CircleShape)
                .background(if (sim.headlightsOn) Color(0xFFFBC02D) else Color(0xFF263238))
                .border(1.dp, Color(0xFF78909C), CircleShape)
                .clickable { sim.toggleHeadlights() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LightMode,
                contentDescription = "Headlights",
                tint = if (sim.headlightsOn) Color(0xFF212121) else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Windshield Wipers Toggle
        Box(
            modifier = Modifier
                .size(42.dp)
                .testTag("wipers_button")
                .clip(CircleShape)
                .background(if (sim.wipersOn) Color(0xFF0288D1) else Color(0xFF263238))
                .border(1.dp, Color(0xFF78909C), CircleShape)
                .clickable { sim.toggleWipers() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = "Wipers",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
