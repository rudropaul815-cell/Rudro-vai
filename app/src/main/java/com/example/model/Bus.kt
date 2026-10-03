package com.example.model

import androidx.compose.ui.graphics.Color

data class BusLivery(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val stripeColor: Color
)

data class BusModel(
    val id: String,
    val name: String,
    val bengaliSubtitle: String,
    val coachType: String,
    val price: Int,
    val topSpeedKmh: Float,
    val acceleration: Float, // 0..1
    val handling: Float,     // 0..1
    val braking: Float,      // 0..1
    val passengerSeats: Int,
    val fuelCapacityLiters: Float,
    val defaultLiveryIndex: Int = 0,
    val liveries: List<BusLivery>,
    val soundProfile: String = "diesel_hino",
    val description: String
)

data class BusUpgrades(
    val engineLevel: Int = 1,    // 1 to 5 (+top speed & acceleration)
    val brakeLevel: Int = 1,     // 1 to 5 (+stopping power)
    val fuelTankLevel: Int = 1,  // 1 to 5 (+fuel capacity)
    val hornType: Int = 1        // 1: Classic, 2: Hydraulic, 3: Melody, 4: Royal Fanfare
) {
    fun engineMultiplier(): Float = 1.0f + (engineLevel - 1) * 0.08f
    fun brakeMultiplier(): Float = 1.0f + (brakeLevel - 1) * 0.12f
    fun fuelMultiplier(): Float = 1.0f + (fuelTankLevel - 1) * 0.15f
}
