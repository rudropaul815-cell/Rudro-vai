package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.BusUpgrades
import com.example.model.ControlMode

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bd_bus_simulator_prefs", Context.MODE_PRIVATE)

    var coins: Int
        get() = prefs.getInt("coins", 5000) // Starter cash ৳5,000 for good feeling
        set(value) = prefs.edit().putInt("coins", value.coerceAtLeast(0)).apply()

    var xp: Int
        get() = prefs.getInt("xp", 100)
        set(value) = prefs.edit().putInt("xp", value.coerceAtLeast(0)).apply()

    var level: Int
        get() = prefs.getInt("level", 1)
        set(value) = prefs.edit().putInt("level", value.coerceAtLeast(1)).apply()

    var currentBusId: String
        get() = prefs.getString("current_bus_id", "bus_hino_1j") ?: "bus_hino_1j"
        set(value) = prefs.edit().putString("current_bus_id", value).apply()

    var currentLiveryIndex: Int
        get() = prefs.getInt("current_livery_idx", 1)
        set(value) = prefs.edit().putInt("current_livery_idx", value).apply()

    var unlockedBusIds: Set<String>
        get() = prefs.getStringSet("unlocked_buses", setOf("bus_hino_1j")) ?: setOf("bus_hino_1j")
        set(value) = prefs.edit().putStringSet("unlocked_buses", value).apply()

    fun unlockBus(busId: String) {
        val updated = unlockedBusIds.toMutableSet()
        updated.add(busId)
        unlockedBusIds = updated
    }

    fun isBusUnlocked(busId: String): Boolean = unlockedBusIds.contains(busId)

    fun getBusUpgrades(busId: String): BusUpgrades {
        val engine = prefs.getInt("upgrade_${busId}_engine", 1)
        val brake = prefs.getInt("upgrade_${busId}_brake", 1)
        val fuel = prefs.getInt("upgrade_${busId}_fuel", 1)
        val horn = prefs.getInt("upgrade_${busId}_horn", 1)
        return BusUpgrades(engine, brake, fuel, horn)
    }

    fun saveBusUpgrades(busId: String, upgrades: BusUpgrades) {
        prefs.edit()
            .putInt("upgrade_${busId}_engine", upgrades.engineLevel)
            .putInt("upgrade_${busId}_brake", upgrades.brakeLevel)
            .putInt("upgrade_${busId}_fuel", upgrades.fuelTankLevel)
            .putInt("upgrade_${busId}_horn", upgrades.hornType)
            .apply()
    }

    var controlMode: ControlMode
        get() {
            val name = prefs.getString("control_mode", ControlMode.STEERING_WHEEL.name)
            return try {
                ControlMode.valueOf(name ?: ControlMode.STEERING_WHEEL.name)
            } catch (e: Exception) {
                ControlMode.STEERING_WHEEL
            }
        }
        set(value) = prefs.edit().putString("control_mode", value.name).apply()

    var soundEffectsEnabled: Boolean
        get() = prefs.getBoolean("sfx_enabled", true)
        set(value) = prefs.edit().putBoolean("sfx_enabled", value).apply()

    var totalTripsCompleted: Int
        get() = prefs.getInt("total_trips", 0)
        set(value) = prefs.edit().putInt("total_trips", value).apply()

    var totalPassengersTransported: Int
        get() = prefs.getInt("total_passengers", 0)
        set(value) = prefs.edit().putInt("total_passengers", value).apply()

    var totalKmDriven: Float
        get() = prefs.getFloat("total_km", 0f)
        set(value) = prefs.edit().putFloat("total_km", value).apply()
}
