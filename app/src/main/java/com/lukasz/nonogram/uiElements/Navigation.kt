package com.lukasz.nonogram.uiElements

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lukasz.nonogram.screens.MainScreen
import com.lukasz.nonogram.screens.Screen
import com.lukasz.nonogram.screens.camera.CameraController
import com.lukasz.nonogram.screens.camera.view.CameraScreen
import com.lukasz.nonogram.screens.camera.view.debug.CameraDebug
import com.lukasz.nonogram.screens.camera.view.debug.HintDebug
import com.lukasz.nonogram.screens.game.GameController
import com.lukasz.nonogram.screens.game.view.Board

@Composable
fun Navigation(gameController: GameController, cameraController: CameraController) {
    val navController = rememberNavController()

    val startupScreen = Screen.MainScreen

    NavHost(navController = navController, startDestination = startupScreen.route) {
        composable(route = Screen.MainScreen.route) {
            MainScreen(navController)
        }

        composable(route = Screen.GameScreen.route) {
            Board(gameController)
        }

        composable(route = Screen.CameraScreen.route) {
            CameraScreen(cameraController)
        }

        composable(route = Screen.OpenCvDebugScreen.route) {
            CameraDebug(cameraController)
        }

        composable(route = Screen.HintsDebugScreen.route) {
            HintDebug(cameraController)
        }
    }
}

