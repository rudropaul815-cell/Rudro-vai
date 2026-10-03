package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameScreenState
import com.example.ui.screens.DrivingGameScreen
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.MissionResultScreen
import com.example.ui.screens.RouteSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DeepNight
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepNight
                ) {
                    BusSimulatorApp()
                }
            }
        }
    }
}

@Composable
fun BusSimulatorApp(
    viewModel: GameViewModel = viewModel()
) {
    val screenState by viewModel.screenState.collectAsState()
    val simulation by viewModel.currentSimulation.collectAsState()

    // Handle Android system back gesture / back button
    BackHandler(enabled = screenState != GameScreenState.MAIN_MENU) {
        viewModel.handleBack()
    }

    when (screenState) {
        GameScreenState.MAIN_MENU -> {
            MainMenuScreen(
                prefs = viewModel.prefs,
                onStartDrive = { viewModel.startTrip() },
                onSelectRoutes = { viewModel.navigateTo(GameScreenState.ROUTE_SELECT) },
                onOpenGarage = { viewModel.navigateTo(GameScreenState.GARAGE) },
                onOpenSettings = { viewModel.navigateTo(GameScreenState.SETTINGS) }
            )
        }

        GameScreenState.ROUTE_SELECT -> {
            RouteSelectScreen(
                onRouteChosen = { route, weather ->
                    viewModel.startTrip(route, weather)
                },
                onBack = { viewModel.navigateTo(GameScreenState.MAIN_MENU) }
            )
        }

        GameScreenState.GARAGE -> {
            GarageScreen(
                prefs = viewModel.prefs,
                onBack = { viewModel.navigateTo(GameScreenState.MAIN_MENU) }
            )
        }

        GameScreenState.PLAYING -> {
            val sim = simulation
            if (sim != null) {
                DrivingGameScreen(
                    sim = sim,
                    controlMode = viewModel.prefs.controlMode,
                    soundEngine = viewModel.soundEngine,
                    onMissionSuccess = { viewModel.onMissionSuccess() },
                    onGameOver = { viewModel.onGameOver() },
                    onExitToMenu = { viewModel.navigateTo(GameScreenState.MAIN_MENU) }
                )
            } else {
                viewModel.navigateTo(GameScreenState.MAIN_MENU)
            }
        }

        GameScreenState.MISSION_SUCCESS -> {
            val sim = simulation
            if (sim != null) {
                MissionResultScreen(
                    sim = sim,
                    onContinue = { totalEarned, xpEarned ->
                        viewModel.collectMissionRewards(totalEarned, xpEarned)
                    }
                )
            } else {
                viewModel.navigateTo(GameScreenState.MAIN_MENU)
            }
        }

        GameScreenState.GAME_OVER -> {
            val sim = simulation
            GameOverScreen(
                reason = sim?.gameOverReason ?: "Highway Collision Damage Exceeded 100%",
                onRestart = { viewModel.restartCurrentRoute() },
                onReturnHome = { viewModel.navigateTo(GameScreenState.MAIN_MENU) }
            )
        }

        GameScreenState.SETTINGS -> {
            SettingsScreen(
                prefs = viewModel.prefs,
                onBack = { viewModel.navigateTo(GameScreenState.MAIN_MENU) }
            )
        }

        else -> {
            MainMenuScreen(
                prefs = viewModel.prefs,
                onStartDrive = { viewModel.startTrip() },
                onSelectRoutes = { viewModel.navigateTo(GameScreenState.ROUTE_SELECT) },
                onOpenGarage = { viewModel.navigateTo(GameScreenState.GARAGE) },
                onOpenSettings = { viewModel.navigateTo(GameScreenState.SETTINGS) }
            )
        }
    }
}
