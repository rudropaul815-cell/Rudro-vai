package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.audio.GameSoundEngine
import com.example.data.BusRepository
import com.example.data.GamePreferences
import com.example.game.GameSimulation
import com.example.model.GameScreenState
import com.example.model.RouteInfo
import com.example.model.WeatherType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = GamePreferences(application)
    val soundEngine = GameSoundEngine(application)

    private val _screenState = MutableStateFlow(GameScreenState.MAIN_MENU)
    val screenState: StateFlow<GameScreenState> = _screenState.asStateFlow()

    private val _currentSimulation = MutableStateFlow<GameSimulation?>(null)
    val currentSimulation: StateFlow<GameSimulation?> = _currentSimulation.asStateFlow()

    private var lastSelectedRoute: RouteInfo = BusRepository.allRoutes.first()
    private var lastSelectedWeather: WeatherType = WeatherType.SUNNY

    init {
        soundEngine.soundEnabled = prefs.soundEffectsEnabled
    }

    fun startTrip(
        route: RouteInfo = lastSelectedRoute,
        weather: WeatherType = route.recommendedWeather
    ) {
        lastSelectedRoute = route
        lastSelectedWeather = weather

        val bus = BusRepository.getBusById(prefs.currentBusId)
        val livery = bus.liveries[prefs.currentLiveryIndex.coerceIn(0, bus.liveries.lastIndex)]
        val upgrades = prefs.getBusUpgrades(bus.id)

        val sim = GameSimulation(
            busModel = bus,
            livery = livery,
            upgrades = upgrades,
            route = route,
            weather = weather,
            soundEngine = soundEngine
        )

        _currentSimulation.value = sim
        _screenState.value = GameScreenState.PLAYING
    }

    fun onMissionSuccess() {
        val sim = _currentSimulation.value ?: return
        soundEngine.stopEngineSound()
        _screenState.value = GameScreenState.MISSION_SUCCESS
    }

    fun onGameOver() {
        val sim = _currentSimulation.value ?: return
        soundEngine.stopEngineSound()
        _screenState.value = GameScreenState.GAME_OVER
    }

    fun collectMissionRewards(totalEarned: Int, xpEarned: Int) {
        val sim = _currentSimulation.value

        prefs.coins += totalEarned
        prefs.xp += xpEarned
        prefs.totalTripsCompleted += 1
        if (sim != null) {
            prefs.totalPassengersTransported += sim.passengersOnboard
            prefs.totalKmDriven += sim.route.distanceKm
        }

        // Level up formula
        val requiredXp = prefs.level * 400
        if (prefs.xp >= requiredXp) {
            prefs.level += 1
        }

        _screenState.value = GameScreenState.MAIN_MENU
        _currentSimulation.value = null
    }

    fun restartCurrentRoute() {
        startTrip(lastSelectedRoute, lastSelectedWeather)
    }

    fun navigateTo(screen: GameScreenState) {
        if (screen == GameScreenState.MAIN_MENU) {
            soundEngine.stopEngineSound()
        }
        _screenState.value = screen
    }

    fun handleBack(): Boolean {
        return when (_screenState.value) {
            GameScreenState.PLAYING -> {
                soundEngine.stopEngineSound()
                _screenState.value = GameScreenState.MAIN_MENU
                _currentSimulation.value = null
                true
            }
            GameScreenState.ROUTE_SELECT,
            GameScreenState.GARAGE,
            GameScreenState.SETTINGS,
            GameScreenState.MISSION_SUCCESS,
            GameScreenState.GAME_OVER -> {
                _screenState.value = GameScreenState.MAIN_MENU
                true
            }
            GameScreenState.MAIN_MENU -> false
            else -> false
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.stopEngineSound()
    }
}
