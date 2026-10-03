package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameSimulation
import com.example.ui.theme.BdEmerald
import com.example.ui.theme.BdGold
import com.example.ui.theme.BdGreen
import com.example.ui.theme.BdRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepNight

@Composable
fun MissionResultScreen(
    sim: GameSimulation,
    onContinue: (totalEarned: Int, xpEarned: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val baseFare = sim.route.baseRewardCoins
    val ticketSales = sim.fareCollected
    val safeBonus = if (sim.damagePercent < 15f) 800 else if (sim.damagePercent < 40f) 350 else 0
    val damageDeduction = (sim.damagePercent * 25).toInt()
    val totalNet = (baseFare + ticketSales + safeBonus - damageDeduction).coerceAtLeast(1000)
    val xpEarned = sim.route.xpReward + 150

    val satisfactionRating = sim.passengerSatisfaction.toInt().coerceIn(0, 100)
    val starCount = when {
        satisfactionRating >= 85 -> 5
        satisfactionRating >= 70 -> 4
        satisfactionRating >= 50 -> 3
        else -> 2
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trophy badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF004D40))
                    .border(2.dp, BdEmerald, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Success",
                    tint = BdGold,
                    modifier = Modifier.size(42.dp)
                )
            }

            Text(
                text = "TRIP COMPLETED!",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text(
                text = "${sim.route.fromCity} ➔ ${sim.route.toCity}",
                color = BdEmerald,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            // Star Rating
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = if (index < starCount) BdGold else Color(0xFF37474F),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = "Passenger Satisfaction: $satisfactionRating%",
                color = Color(0xFFCFD8DC),
                fontSize = 13.sp
            )

            // Earnings Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A2E))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FareRow(label = "Base Route Contract", amount = "+৳ $baseFare", color = Color.White)
                    FareRow(label = "Passenger Ticket Fares", amount = "+৳ $ticketSales", color = BdGold)
                    FareRow(label = "Safe Driving Bonus", amount = "+৳ $safeBonus", color = BdEmerald)
                    if (damageDeduction > 0) {
                        FareRow(label = "Highway Damage Deductions", amount = "-৳ $damageDeduction", color = BdRed)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF263238)))
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL NET EARNINGS:",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "৳ $totalNet",
                            color = BdGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }

                    Text(
                        text = "+$xpEarned XP Experience Awarded",
                        color = BdEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onContinue(totalNet, xpEarned) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("collect_earnings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BdEmerald),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "COLLECT EARNINGS (টাকা গ্রহণ করুন)",
                    color = DeepNight,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun FareRow(label: String, amount: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF90A4AE), fontSize = 13.sp)
        Text(text = amount, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
