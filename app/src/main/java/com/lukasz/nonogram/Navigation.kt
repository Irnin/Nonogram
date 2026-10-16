package com.lukasz.nonogram

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lukasz.nonogram.screens.Board
import com.lukasz.nonogram.screens.CameraScreen
import com.lukasz.nonogram.screens.MainScreen

@Composable
fun Navigation(vm: ViewModel) {
    val navController = rememberNavController()

    val startupScreen = Screen.MainScreen

    NavHost(navController = navController, startDestination = startupScreen.route) {
        composable(route = Screen.MainScreen.route) {
            MainScreen(navController)
        }

        composable(route = Screen.GameScreen.route) {
            Board(vm)
        }

        composable(route = Screen.CameraScreen.route) {
            CameraScreen(vm)
        }
    }
}

