package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BusRepository
import com.example.data.GamePreferences
import com.example.ui.theme.BdEmerald
import com.example.ui.theme.BdGold
import com.example.ui.theme.BdGreen
import com.example.ui.theme.BdRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepNight

@Composable
fun MainMenuScreen(
    prefs: GamePreferences,
    onStartDrive: () -> Unit,
    onSelectRoutes: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentBus = BusRepository.getBusById(prefs.currentBusId)

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
            // --- HERO BANNER WITH PADMA BRIDGE ART ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_menu_banner),
                    contentDescription = "Bangladesh Highway Bus",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x33000000),
                                    Color(0x99080D0B),
                                    DeepNight
                                )
                            )
                        )
                )

                // Title & Bengali Subtitle
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BdGreen)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BANGLADESH 3D",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "BD BUS SIMULATOR",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "মহাসড়কে বাস্তবসম্মত বাস ড্রাইভিং ও যাত্রী পরিবহন",
                        color = BdEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Top Profile Chip (Coins ৳ & Level)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xDD1A2621))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "LVL ${prefs.level}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // --- CURRENT BUS PROFILE CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF004D40)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = "Active Bus",
                                tint = BdEmerald,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column {
                            Text(
                                text = currentBus.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = currentBus.coachType,
                                color = Color(0xFF90A4AE),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = onOpenGarage,
                        modifier = Modifier.testTag("garage_quick_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3B30)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("GARAGE", color = BdEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- PRIMARY MENU BUTTONS ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // START PLAYING (BIG GREEN BUTTON)
                Button(
                    onClick = onStartDrive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("start_drive_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BdEmerald),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Drive",
                            tint = DeepNight,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START DRIVING (মহাসড়ক যাত্রা)",
                            color = DeepNight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // ROUTE & MAP SELECTION
                MenuNavCard(
                    title = "All Bangladesh Routes & Map",
                    subtitle = "8 Divisions: Padma Bridge, Sylhet, Ctg, Jamuna",
                    icon = Icons.Default.Map,
                    tag = "routes_button",
                    onClick = onSelectRoutes
                )

                // GARAGE & UPGRADES
                MenuNavCard(
                    title = "Bus Showroom & Upgrades",
                    subtitle = "Hino, Scania, Hyundai, Volvo, Liveries & Horns",
                    icon = Icons.Default.Garage,
                    tag = "garage_button",
                    onClick = onOpenGarage
                )

                // SETTINGS
                MenuNavCard(
                    title = "Game Settings",
                    subtitle = "Controls (Steering Wheel / Buttons), Audio & Weather",
                    icon = Icons.Default.Settings,
                    tag = "settings_button",
                    onClick = onOpenSettings
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STATS FOOTER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatPill(title = "Trips", value = "${prefs.totalTripsCompleted}")
                StatPill(title = "Passengers", value = "${prefs.totalPassengersTransported}")
                StatPill(title = "Distance", value = "${prefs.totalKmDriven.toInt()} km")
            }
        }
    }
}

@Composable
private fun MenuNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22362E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF10281F)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = BdEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF88A89A),
                    fontSize = 12.sp
                )
            }

            Text(
                text = "▶",
                color = BdEmerald,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun StatPill(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = title, color = Color(0xFF78909C), fontSize = 11.sp)
    }
}
