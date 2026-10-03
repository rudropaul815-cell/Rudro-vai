package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.model.BusLivery
import com.example.model.BusModel
import com.example.model.RouteInfo
import com.example.model.WeatherType

object BusRepository {

    val liveries = listOf(
        BusLivery("greenline", "Green Line Paribahan", Color(0xFF007A3D), Color(0xFFFFFFFF), Color(0xFFF3C010)),
        BusLivery("hanif", "Hanif Enterprise", Color(0xFFD61828), Color(0xFFFFFFFF), Color(0xFF0C2050)),
        BusLivery("shohag", "Shohag Elite Scania", Color(0xFF154360), Color(0xFFD4AC0D), Color(0xFFE5E7E9)),
        BusLivery("shyamoli", "Shyamoli NR Travels", Color(0xFF2874A6), Color(0xFFE74C3C), Color(0xFFFFFFFF)),
        BusLivery("ena", "Ena Express", Color(0xFF1E8449), Color(0xFFE67E22), Color(0xFFFFFFFF)),
        BusLivery("desh", "Desh Travels", Color(0xFF6C3483), Color(0xFFF1C40F), Color(0xFFFFFFFF)),
        BusLivery("saintmartin", "Saintmartin Paribahan", Color(0xFF117864), Color(0xFF85C1E9), Color(0xFFFFFFFF))
    )

    val allBuses = listOf(
        BusModel(
            id = "bus_hino_1j",
            name = "Hino 1J Classic",
            bengaliSubtitle = "হিনো ১জে ক্লাসিক এক্সপ্রেস",
            coachType = "Highway Non-AC Standard",
            price = 0, // Free starter bus
            topSpeedKmh = 105f,
            acceleration = 0.65f,
            handling = 0.70f,
            braking = 0.68f,
            passengerSeats = 40,
            fuelCapacityLiters = 180f,
            defaultLiveryIndex = 1, // Hanif style red/white
            liveries = liveries,
            soundProfile = "diesel_hino",
            description = "The undisputed king of Bangladeshi highways. Reliable leaf spring suspension, raw turbo diesel roar, and classic air horn."
        ),
        BusModel(
            id = "bus_ashok_leyland",
            name = "Ashok Leyland 1616",
            bengaliSubtitle = "অশোক লেল্যান্ড ভাইকিং",
            coachType = "Intercity Express",
            price = 12000,
            topSpeedKmh = 110f,
            acceleration = 0.72f,
            handling = 0.68f,
            braking = 0.72f,
            passengerSeats = 42,
            fuelCapacityLiters = 200f,
            defaultLiveryIndex = 4, // Ena style green/orange
            liveries = liveries,
            soundProfile = "diesel_leyland",
            description = "Famous for high torque, aggressive highway overtaking ability, and colorful hand-painted local bodywork."
        ),
        BusModel(
            id = "bus_hyundai_universe",
            name = "Hyundai Universe Noble",
            bengaliSubtitle = "হুন্দাই ইউনিভার্স নোবেল এসি",
            coachType = "Luxury AC Coach",
            price = 28000,
            topSpeedKmh = 128f,
            acceleration = 0.85f,
            handling = 0.82f,
            braking = 0.86f,
            passengerSeats = 36,
            fuelCapacityLiters = 260f,
            defaultLiveryIndex = 0, // Green line
            liveries = liveries,
            soundProfile = "diesel_hyundai",
            description = "Quiet Korean air suspension coach with smooth automatic transmission, chilled climate control, and supreme passenger comfort."
        ),
        BusModel(
            id = "bus_scania_multiaxle",
            name = "Scania K410 Multi-Axle",
            bengaliSubtitle = "স্ক্যানিয়া কে৪১০ মাল্টি-অ্যাক্সেল",
            coachType = "Flagship Multi-Axle",
            price = 45000,
            topSpeedKmh = 135f,
            acceleration = 0.90f,
            handling = 0.92f,
            braking = 0.92f,
            passengerSeats = 44,
            fuelCapacityLiters = 320f,
            defaultLiveryIndex = 2, // Shohag blue
            liveries = liveries,
            soundProfile = "diesel_scania",
            description = "Twin-axle monster with unmatched highway stability, electronic EBS disc brakes, and iconic pneumatic musical horn."
        ),
        BusModel(
            id = "bus_volvo_b11r",
            name = "Volvo B11R Royal Sleeper",
            bengaliSubtitle = "ভলভো বি১১আর রয়্যাল স্লিপার",
            coachType = "Double-Decker Sleeper",
            price = 65000,
            topSpeedKmh = 130f,
            acceleration = 0.88f,
            handling = 0.85f,
            braking = 0.94f,
            passengerSeats = 32,
            fuelCapacityLiters = 350f,
            defaultLiveryIndex = 5, // Desh travels
            liveries = liveries,
            soundProfile = "diesel_volvo",
            description = "Ultimate luxury sleeper coach. Two-tier sleeping cabins for comfortable overnight journeys across Bangladesh."
        )
    )

    val allRoutes = listOf(
        RouteInfo(
            id = "route_dhaka_ctg",
            name = "Dhaka to Chattogram Expressway",
            fromCity = "Dhaka (Sayedabad)",
            toCity = "Chattogram (Dampara)",
            highwayCode = "N1 Dhaka-Ctg Hwy",
            distanceKm = 245f,
            estimatedTimeMin = 18,
            baseRewardCoins = 3800,
            xpReward = 450,
            recommendedWeather = WeatherType.SUNSET,
            keyLandmark = "Meghna & Gumti Bridges, Feni Bypass",
            description = "The busiest highway in Bangladesh. Overtake trucks, cross massive river bridges, and navigate bustling roadside bazaars.",
            stopsCount = 4
        ),
        RouteInfo(
            id = "route_dhaka_padma_barishal",
            name = "Padma Bridge & Bhanga to Barishal",
            fromCity = "Dhaka (Jatrabari)",
            toCity = "Barishal (Nathullabad)",
            highwayCode = "N8 Bangabandhu Expressway",
            distanceKm = 175f,
            estimatedTimeMin = 14,
            baseRewardCoins = 4200,
            xpReward = 520,
            recommendedWeather = WeatherType.SUNNY,
            keyLandmark = "Padma Multipurpose Bridge (6.15 km)",
            description = "Drive across Bangladesh's crown jewel: the 6.15 km Padma Bridge over the mighty Padma river, then speed down the smooth 4-lane expressway.",
            hasPadmaBridge = true,
            stopsCount = 3
        ),
        RouteInfo(
            id = "route_dhaka_sylhet",
            name = "Dhaka to Sylhet Tea Valley",
            fromCity = "Dhaka (Mohakhali)",
            toCity = "Sylhet (Kadamtoli)",
            highwayCode = "N2 Dhaka-Sylhet Hwy",
            distanceKm = 238f,
            estimatedTimeMin = 16,
            baseRewardCoins = 3900,
            xpReward = 480,
            recommendedWeather = WeatherType.MONSOON_RAIN,
            keyLandmark = "Bhairab River Bridge & Sreemangal Tea Estates",
            description = "Monsoon rain drops sweep across your windshield as you drive through lush green tea gardens, curving village roads, and river valleys.",
            hasTeaGardens = true,
            stopsCount = 3
        ),
        RouteInfo(
            id = "route_dhaka_jamuna_rajshahi",
            name = "Jamuna Bridge to Rajshahi Mango City",
            fromCity = "Dhaka (Gabtoli)",
            toCity = "Rajshahi (Shiroil)",
            highwayCode = "N5 & N6 North Bengal Hwy",
            distanceKm = 256f,
            estimatedTimeMin = 17,
            baseRewardCoins = 4100,
            xpReward = 500,
            recommendedWeather = WeatherType.FOGGY,
            keyLandmark = "Bangabandhu Jamuna Bridge (4.8 km)",
            description = "Cross the 4.8 km Jamuna Bridge over the Brahmaputra basin, driving through morning river mist into the historic silk and mango capital.",
            hasJamunaBridge = true,
            stopsCount = 4
        ),
        RouteInfo(
            id = "route_dhaka_khulna",
            name = "Dhaka to Khulna & Sundarbans Gateway",
            fromCity = "Dhaka (Sayedabad)",
            toCity = "Khulna (Royal Mor)",
            highwayCode = "N805 Padma-Rupsha Hwy",
            distanceKm = 195f,
            estimatedTimeMin = 15,
            baseRewardCoins = 3600,
            xpReward = 430,
            recommendedWeather = WeatherType.SUNNY,
            keyLandmark = "Khan Jahan Ali Rupsha Bridge",
            description = "Scenic southwest route crossing southern rivers, shrimp farms, and village greenery toward the Sundarbans mangrove gateway.",
            hasPadmaBridge = true,
            stopsCount = 3
        ),
        RouteInfo(
            id = "route_dhaka_rangpur",
            name = "Northern Highway to Rangpur",
            fromCity = "Dhaka (Gabtoli)",
            toCity = "Rangpur (Central Terminal)",
            highwayCode = "N5 Rangpur Highway",
            distanceKm = 305f,
            estimatedTimeMin = 20,
            baseRewardCoins = 5000,
            xpReward = 600,
            recommendedWeather = WeatherType.NIGHT,
            keyLandmark = "Bogra Mahasthangarh & Teesta Route",
            description = "Long haul night driving under starry skies with bright headlights cutting through darkness, loaded trucks, and midnight highway tea stalls.",
            hasJamunaBridge = true,
            stopsCount = 5
        ),
        RouteInfo(
            id = "route_dhaka_mymensingh",
            name = "Brahmaputra Green Corridor",
            fromCity = "Dhaka (Mohakhali)",
            toCity = "Mymensingh (Maskanda)",
            highwayCode = "N3 Dhaka-Mymensingh 4-Lane",
            distanceKm = 120f,
            estimatedTimeMin = 10,
            baseRewardCoins = 2500,
            xpReward = 300,
            recommendedWeather = WeatherType.SUNNY,
            keyLandmark = "Gazipur Safari Corridor & Old Brahmaputra Bridge",
            description = "High-speed 4-lane corridor with rich agricultural fields, brick kilns with tall chimneys, and colorful rural bazaars.",
            stopsCount = 2
        ),
        RouteInfo(
            id = "route_ctg_coxsbazar",
            name = "Chattogram to Cox's Bazar Marine Highway",
            fromCity = "Chattogram (Bahaddarhat)",
            toCity = "Cox's Bazar (Kolatoli Beach)",
            highwayCode = "N1 Cox's Bazar Hwy",
            distanceKm = 150f,
            estimatedTimeMin = 13,
            baseRewardCoins = 4500,
            xpReward = 550,
            recommendedWeather = WeatherType.SUNSET,
            keyLandmark = "Chunati Wildlife Sanctuary & Sea Beach Approach",
            description = "Dramatic coastal highway with winding hill slopes, pine trees, and tourist coaches heading to the world's longest unbroken sea beach.",
            hasHillyTerrain = true,
            stopsCount = 3
        )
    )

    fun getBusById(id: String): BusModel {
        return allBuses.find { it.id == id } ?: allBuses.first()
    }

    fun getRouteById(id: String): RouteInfo {
        return allRoutes.find { it.id == id } ?: allRoutes.first()
    }
}
