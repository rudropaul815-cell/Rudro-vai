package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusRepository
import com.example.data.GamePreferences
import com.example.engine3d.Camera3D
import com.example.engine3d.Renderer3D
import com.example.engine3d.Vector3
import com.example.engine3d.World3D
import com.example.model.BusModel
import com.example.model.BusUpgrades
import com.example.model.CameraView
import com.example.model.WeatherType
import com.example.ui.theme.BdEmerald
import com.example.ui.theme.BdGold
import com.example.ui.theme.BdGreen
import com.example.ui.theme.BdRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepNight
import kotlinx.coroutines.delay

@Composable
fun GarageScreen(
    prefs: GamePreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBusIndex by remember {
        mutableIntStateOf(
            BusRepository.allBuses.indexOfFirst { it.id == prefs.currentBusId }.coerceAtLeast(0)
        )
    }

    val selectedBus = BusRepository.allBuses[selectedBusIndex]
    var selectedLiveryIndex by remember(selectedBusIndex) {
        mutableIntStateOf(prefs.currentLiveryIndex.coerceIn(0, selectedBus.liveries.lastIndex))
    }

    var busUpgrades by remember(selectedBus.id) {
        mutableStateOf(prefs.getBusUpgrades(selectedBus.id))
    }

    val isUnlocked = prefs.isBusUnlocked(selectedBus.id)
    val isCurrent = prefs.currentBusId == selectedBus.id

    // 3D Turntable rotation angle
    var turntableYaw by remember { mutableFloatStateOf(0.45f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            turntableYaw += 0.008f
            if (turntableYaw > Math.PI * 2) turntableYaw = 0f
        }
    }

    val renderer = remember { Renderer3D() }
    val camera = remember {
        Camera3D(
            position = Vector3(0f, 2.4f, -12.5f),
            target = Vector3(0f, 1.2f, 0f),
            pitch = 0.08f,
            yaw = 0f,
            fov = 55f
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("garage_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "BUS SHOWROOM",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "বাস গ্যারেজ ও আপগ্রেড",
                            color = BdEmerald,
                            fontSize = 11.sp
                        )
                    }
                }

                // Balance Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xDD0D1E16))
                        .border(1.dp, BdGold, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "৳ ${prefs.coins}",
                        color = BdGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // --- 3D INTERACTIVE SHOWROOM TURNTABLE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F1714))
                    .border(1.dp, Color(0xFF1E3A2E), RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val livery = selectedBus.liveries[selectedLiveryIndex.coerceIn(0, selectedBus.liveries.lastIndex)]
                    val busPolys = World3D.buildPlayerBus(
                        pos = Vector3(0f, 0f, 0f),
                        yawRad = turntableYaw,
                        pitchRad = 0f,
                        livery = livery,
                        headlightsOn = true,
                        braking = false,
                        turnIndicatorLeft = false,
                        turnIndicatorRight = false,
                        indicatorBlink = false,
                        wiperAngleDeg = 0f
                    )

                    renderer.renderScene(
                        drawScope = this,
                        camera = camera,
                        cameraView = CameraView.CHASE,
                        polygons = busPolys,
                        weather = WeatherType.SUNSET,
                        headlightsOn = true,
                        wiperAngleDeg = 0f,
                        playerSpeedKmh = 0f,
                        roadCurve = 0f
                    )
                }

                // Bus Name Tag overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = selectedBus.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = selectedBus.bengaliSubtitle,
                        color = BdEmerald,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bus Model Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(BusRepository.allBuses.indices.toList()) { index ->
                    val bus = BusRepository.allBuses[index]
                    val isSel = selectedBusIndex == index
                    val unlocked = prefs.isBusUnlocked(bus.id)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSel) BdEmerald else DarkSurface)
                            .border(1.dp, if (isSel) BdEmerald else Color(0xFF2C3E50), RoundedCornerShape(14.dp))
                            .clickable {
                                selectedBusIndex = index
                                selectedLiveryIndex = 0
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!unlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = if (isSel) DeepNight else Color(0xFFCFD8DC),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = bus.name,
                                color = if (isSel) DeepNight else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Livery Paint Schemes Selector
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "SELECT LIVERY (কোম্পানি রং):",
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedBus.liveries.indices.toList()) { lIdx ->
                        val livery = selectedBus.liveries[lIdx]
                        val isSelected = selectedLiveryIndex == lIdx

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF16241E))
                                .border(
                                    2.dp,
                                    if (isSelected) BdGold else Color(0xFF2C3E50),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedLiveryIndex = lIdx
                                    if (isCurrent) prefs.currentLiveryIndex = lIdx
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(livery.primaryColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = livery.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bus Specs Bar Meters
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23382F))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PERFORMANCE SPECS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    val topSpeedEffective = selectedBus.topSpeedKmh * busUpgrades.engineMultiplier()
                    SpecBar(label = "Top Speed", value = "${topSpeedEffective.toInt()} km/h", progress = topSpeedEffective / 150f, color = BdEmerald)
                    SpecBar(label = "Acceleration", value = "${(selectedBus.acceleration * 100).toInt()}%", progress = selectedBus.acceleration, color = BdGold)
                    SpecBar(label = "Air Brakes", value = "Level ${busUpgrades.brakeLevel}", progress = busUpgrades.brakeLevel / 5f, color = BdRed)
                    SpecBar(label = "Passenger Seats", value = "${selectedBus.passengerSeats} Seats", progress = selectedBus.passengerSeats / 50f, color = Color(0xFF42A5F5))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Performance Upgrades Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23382F))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "BUS UPGRADES (পারফর্মেন্স পার্টস)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    // Engine Upgrade
                    val engineCost = busUpgrades.engineLevel * 2500
                    UpgradeRow(
                        title = "Engine Tuning",
                        level = busUpgrades.engineLevel,
                        cost = engineCost,
                        canAfford = prefs.coins >= engineCost,
                        onUpgrade = {
                            if (prefs.coins >= engineCost && busUpgrades.engineLevel < 5) {
                                prefs.coins -= engineCost
                                busUpgrades = busUpgrades.copy(engineLevel = busUpgrades.engineLevel + 1)
                                prefs.saveBusUpgrades(selectedBus.id, busUpgrades)
                            }
                        }
                    )

                    // Air Brakes Upgrade
                    val brakeCost = busUpgrades.brakeLevel * 2000
                    UpgradeRow(
                        title = "Air Brakes System",
                        level = busUpgrades.brakeLevel,
                        cost = brakeCost,
                        canAfford = prefs.coins >= brakeCost,
                        onUpgrade = {
                            if (prefs.coins >= brakeCost && busUpgrades.brakeLevel < 5) {
                                prefs.coins -= brakeCost
                                busUpgrades = busUpgrades.copy(brakeLevel = busUpgrades.brakeLevel + 1)
                                prefs.saveBusUpgrades(selectedBus.id, busUpgrades)
                            }
                        }
                    )

                    // Fuel Tank Upgrade
                    val fuelCost = busUpgrades.fuelTankLevel * 1800
                    UpgradeRow(
                        title = "Fuel Tank Capacity",
                        level = busUpgrades.fuelTankLevel,
                        cost = fuelCost,
                        canAfford = prefs.coins >= fuelCost,
                        onUpgrade = {
                            if (prefs.coins >= fuelCost && busUpgrades.fuelTankLevel < 5) {
                                prefs.coins -= fuelCost
                                busUpgrades = busUpgrades.copy(fuelTankLevel = busUpgrades.fuelTankLevel + 1)
                                prefs.saveBusUpgrades(selectedBus.id, busUpgrades)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Buy Bus or Select Active Bus
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isUnlocked) {
                    Button(
                        onClick = {
                            prefs.currentBusId = selectedBus.id
                            prefs.currentLiveryIndex = selectedLiveryIndex
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("select_bus_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrent) Color(0xFF263238) else BdEmerald
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (isCurrent) "CURRENTLY ACTIVE (চালিত হচ্ছে)" else "DRIVE THIS BUS (এই বাসটি চালান)",
                            color = if (isCurrent) Color.White else DeepNight,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    val canAfford = prefs.coins >= selectedBus.price
                    Button(
                        onClick = {
                            if (canAfford) {
                                prefs.coins -= selectedBus.price
                                prefs.unlockBus(selectedBus.id)
                                prefs.currentBusId = selectedBus.id
                                prefs.currentLiveryIndex = selectedLiveryIndex
                            }
                        },
                        enabled = canAfford,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("buy_bus_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BdGold),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (canAfford) "PURCHASE FOR ৳ ${selectedBus.price}" else "NEED ৳ ${selectedBus.price} (অপর্যাপ্ত টাকা)",
                            color = DeepNight,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecBar(label: String, value: String, progress: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color(0xFF90A4AE), fontSize = 11.sp)
            Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1E2824)
        )
    }
}

@Composable
private fun UpgradeRow(
    title: String,
    level: Int,
    cost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Tier $level of 5", color = Color(0xFF81C784), fontSize = 11.sp)
        }

        if (level < 5) {
            Button(
                onClick = onUpgrade,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) Color(0xFF006A4E) else Color(0xFF263238)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = "৳ $cost UPGRADE",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(text = "MAX LEVEL", color = BdGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
