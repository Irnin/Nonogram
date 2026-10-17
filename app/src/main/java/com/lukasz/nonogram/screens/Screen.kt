package com.lukasz.nonogram.screens

sealed class Screen(val route: String, val title: String) {
    object MainScreen : Screen("mainScreen", "Main Screen")
    object CameraScreen : Screen("cameraScreen", "Camera")
    object GameScreen : Screen("gameScreen", "Start Game")
    object SettingsScreen : Screen("settingsScreen", "Settings")

    object CameraDebugScreen : Screen("cameraDebugScreen", "Camera Debug")
}