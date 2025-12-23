package com.example.kitaplikapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kitaplikapp.ui.screens.auth.LoginScreen
import com.example.kitaplikapp.ui.screens.auth.RegisterScreen
import com.example.kitaplikapp.ui.screens.detail.BookDetailScreen
import com.example.kitaplikapp.ui.screens.main.MainScaffold
import com.example.kitaplikapp.ui.screens.splash.SplashScreen // ✅ Yeni import
import com.example.kitaplikapp.viewmodel.AuthViewModel
import com.example.kitaplikapp.viewmodel.HomeViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authVm: AuthViewModel
) {
    val sharedHomeVm: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash" // ✅ BAŞLANGIÇ NOKTASI DEĞİŞTİ
    ) {
        // ✅ YENİ: SPLASH EKRANI ROTASI
        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    // Splash'ten Login'e git ve geri dönülememesini sağla
                    navController.navigate(Routes.Login.route) {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    // Splash'ten Ana ekrana git ve geri dönülememesini sağla
                    navController.navigate(Routes.Main.route) {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                authVm = authVm
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.Register.route) },
                authVm = authVm
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Routes.Login.route) },
                onNavigateToLogin = { navController.popBackStack() },
                authVm = authVm
            )
        }

        composable(Routes.Main.route) {
            MainScaffold(
                authVm = authVm,
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                },
                rootNavController = navController,
                vm = sharedHomeVm
            )
        }

        composable(
            route = "detail/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""

            BookDetailScreen(
                bookId = bookId,
                navController = navController,
                authVm = authVm,
                vm = sharedHomeVm
            )
        }
    }
}