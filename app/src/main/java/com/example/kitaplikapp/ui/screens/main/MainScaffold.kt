package com.example.kitaplikapp.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kitaplikapp.ui.navigation.Routes
import com.example.kitaplikapp.ui.screens.home.HomeScreen
import com.example.kitaplikapp.ui.screens.library.LibraryScreen
import com.example.kitaplikapp.ui.screens.settings.SettingsScreen
import com.example.kitaplikapp.viewmodel.AuthViewModel
import com.example.kitaplikapp.viewmodel.HomeViewModel

@Composable
fun MainScaffold(
    authVm: AuthViewModel,
    onLogout: () -> Unit,
    rootNavController: NavController,
    vm: HomeViewModel
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { MainBottomBar(bottomNavController) }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Home.route) {
                HomeScreen(
                    navController = rootNavController,
                    authVm = authVm,
                    vm = vm
                )
            }

            composable(Routes.Library.route) {
                LibraryScreen(
                    navController = rootNavController,
                    authVm = authVm,
                    vm = vm
                )
            }

            composable(Routes.Settings.route) {
                SettingsScreen(
                    authVm = authVm,
                    homeVm = vm,
                    onLogout = onLogout
                )
            }
        }
    }
}