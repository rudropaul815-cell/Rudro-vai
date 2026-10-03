package com.example.model

data class RoutePoint(
    val distanceKm: Float,
    val name: String,
    val type: PointType // TERMINAL, STOP, BRIDGE, TOLL_PLAZA, BAZAAR, VILLAGE
)

enum class PointType {
    TERMINAL,
    PASSENGER_STOP,
    BRIDGE,
    TOLL_PLAZA,
    VILLAGE_BAZAAR,
    TEA_GARDEN
}

data class RouteInfo(
    val id: String,
    val name: String,
    val fromCity: String,
    val toCity: String,
    val highwayCode: String,
    val distanceKm: Float,
    val estimatedTimeMin: Int,
    val baseRewardCoins: Int,
    val xpReward: Int,
    val recommendedWeather: WeatherType,
    val keyLandmark: String,
    val description: String,
    val hasPadmaBridge: Boolean = false,
    val hasJamunaBridge: Boolean = false,
    val hasTeaGardens: Boolean = false,
    val hasHillyTerrain: Boolean = false,
    val stopsCount: Int = 3
)
