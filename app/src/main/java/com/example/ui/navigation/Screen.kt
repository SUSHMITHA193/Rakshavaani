package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object NoiseSettings : Screen("noise_settings")
    object Preview : Screen("preview")
    object Transmitting : Screen("transmitting")
    object Result : Screen("result")
    object History : Screen("history")
    object Settings : Screen("settings")
}
