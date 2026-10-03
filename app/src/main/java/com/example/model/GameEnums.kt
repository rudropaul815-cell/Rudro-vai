package com.example.model

enum class Division(val displayName: String, val bengaliName: String) {
    DHAKA("Dhaka", "ঢাকা"),
    CHATTOGRAM("Chattogram", "চট্টগ্রাম"),
    SYLHET("Sylhet", "সিলেট"),
    RAJSHAHI("Rajshahi", "রাজশাহী"),
    KHULNA("Khulna", "খুলনা"),
    BARISHAL("Barishal", "বরিশাল"),
    RANGPUR("Rangpur", "রংপুর"),
    MYMENSINGH("Mymensingh", "ময়মনসিংহ")
}

enum class WeatherType(val title: String, val rainIntensity: Float, val fogIntensity: Float, val skyLight: Float) {
    SUNNY("Sunny Day", 0.0f, 0.0f, 1.0f),
    SUNSET("Golden Sunset", 0.0f, 0.1f, 0.75f),
    MONSOON_RAIN("Monsoon Rain", 0.9f, 0.35f, 0.45f),
    FOGGY("Winter Fog", 0.0f, 0.75f, 0.5f),
    NIGHT("Starry Night", 0.0f, 0.15f, 0.2f)
}

enum class CameraView(val label: String) {
    CHASE("Chase Cam"),
    CABIN("Interior Cockpit"),
    HOOD("Bumper Cam"),
    DRONE("Drone Top-Down")
}

enum class Gear(val shortName: String, val ratio: Float) {
    PARK("P", 0.0f),
    REVERSE("R", -0.35f),
    NEUTRAL("N", 0.0f),
    DRIVE("D", 1.0f),
    GEAR_1("1", 0.25f),
    GEAR_2("2", 0.45f),
    GEAR_3("3", 0.70f),
    GEAR_4("4", 1.00f),
    GEAR_5("5", 1.30f),
    GEAR_6("6", 1.65f)
}

enum class ControlMode(val title: String) {
    STEERING_WHEEL("Virtual Steering Wheel"),
    TOUCH_BUTTONS("Touch Arrow Buttons"),
    TILT("Gyro / Tilt Steering")
}

enum class TrafficType(val title: String, val speedFactor: Float, val widthM: Float, val lengthM: Float) {
    CNG_AUTO("CNG Auto-Rickshaw", 0.7f, 1.3f, 2.6f),
    RICKSHAW("Cycle Rickshaw", 0.3f, 1.1f, 2.2f),
    TRUCK("Bangladeshi Cargo Truck", 0.75f, 2.5f, 7.8f),
    LOCAL_BUS("Dhaka Local Murir Tin Bus", 0.85f, 2.6f, 8.5f),
    CAR("Private Sedan Car", 1.05f, 1.8f, 4.5f),
    MOTORCYCLE("Motorcycle", 1.15f, 0.8f, 2.0f),
    PEDESTRIAN("Roadside Pedestrian", 0.08f, 0.6f, 0.6f)
}

enum class GameScreenState {
    MAIN_MENU,
    ROUTE_SELECT,
    GARAGE,
    PLAYING,
    PAUSED,
    MISSION_SUCCESS,
    GAME_OVER,
    SETTINGS
}
