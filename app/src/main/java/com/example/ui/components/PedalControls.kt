package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Gear
import com.example.ui.theme.BdEmerald
import com.example.ui.theme.BdRed

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GasPedal(
    modifier: Modifier = Modifier,
    onGasPressed: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(58.dp)
            .height(105.dp)
            .testTag("gas_pedal")
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isPressed) listOf(BdEmerald, Color(0xFF004D40))
                    else listOf(Color(0xFF2C3E50), Color(0xFF1A252F))
                )
            )
            .border(
                2.dp,
                if (isPressed) BdEmerald else Color(0xFF455A64),
                RoundedCornerShape(8.dp)
            )
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onGasPressed(true)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onGasPressed(false)
                        true
                    }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "GAS",
                color = if (isPressed) Color.White else Color(0xFFB0BEC5),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Tread grooves
            repeat(4) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.dp)
                        .padding(vertical = 1.dp)
                        .background(Color(0x55000000))
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BrakePedal(
    modifier: Modifier = Modifier,
    onBrakePressed: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(72.dp)
            .height(85.dp)
            .testTag("brake_pedal")
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isPressed) listOf(BdRed, Color(0xFF7F0000))
                    else listOf(Color(0xFF37474F), Color(0xFF212121))
                )
            )
            .border(
                2.dp,
                if (isPressed) BdRed else Color(0xFF546E7A),
                RoundedCornerShape(8.dp)
            )
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onBrakePressed(true)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onBrakePressed(false)
                        true
                    }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "BRAKE",
                color = if (isPressed) Color.White else Color(0xFFCFD8DC),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "AIR",
                color = if (isPressed) Color.White else Color(0xFF90A4AE),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun GearShifter(
    currentGear: Gear,
    onGearSelected: (Gear) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xDD121C18))
            .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(12.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(Gear.DRIVE, Gear.NEUTRAL, Gear.REVERSE, Gear.PARK).forEach { gear ->
            val isSelected = currentGear == gear
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 30.dp)
                    .testTag("gear_${gear.name.lowercase()}")
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) {
                            if (gear == Gear.REVERSE) BdRed else BdEmerald
                        } else Color(0xFF1E2824)
                    )
                    .clickable { onGearSelected(gear) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gear.shortName,
                    color = if (isSelected) Color.White else Color(0xFF8B9E96),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TouchArrowButtons(
    modifier: Modifier = Modifier,
    onSteerChanged: (Float) -> Unit
) {
    var leftPressed by remember { mutableStateOf(false) }
    var rightPressed by remember { mutableStateOf(false) }

    fun updateSteer() {
        val steer = when {
            leftPressed && !rightPressed -> -1.0f
            rightPressed && !leftPressed -> 1.0f
            else -> 0.0f
        }
        onSteerChanged(steer)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Button
        Box(
            modifier = Modifier
                .size(62.dp)
                .testTag("arrow_left")
                .clip(RoundedCornerShape(14.dp))
                .background(if (leftPressed) BdEmerald else Color(0xCC1A2621))
                .border(2.dp, Color(0xFF388E3C), RoundedCornerShape(14.dp))
                .pointerInteropFilter {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            leftPressed = true
                            updateSteer()
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            leftPressed = false
                            updateSteer()
                            true
                        }
                        else -> false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Steer Left",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }

        // Right Button
        Box(
            modifier = Modifier
                .size(62.dp)
                .testTag("arrow_right")
                .clip(RoundedCornerShape(14.dp))
                .background(if (rightPressed) BdEmerald else Color(0xCC1A2621))
                .border(2.dp, Color(0xFF388E3C), RoundedCornerShape(14.dp))
                .pointerInteropFilter {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            rightPressed = true
                            updateSteer()
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            rightPressed = false
                            updateSteer()
                            true
                        }
                        else -> false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Steer Right",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
