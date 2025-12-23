package com.example.kitaplikapp.ui.navigation

sealed class Routes(val route: String) {
    data object Login : Routes("login")
    data object Register : Routes("register")
    data object Main : Routes("main")

    data object Home : Routes("home")
    data object Library : Routes("library")
    data object Settings : Routes("settings")
}
