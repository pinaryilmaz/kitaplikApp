package com.example.kitaplikapp.ui.screens.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kitaplikapp.ui.navigation.Routes
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MainBottomBar(navController: NavController) {

    val orientation = LocalConfiguration.current.orientation

    val barHeight = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        48.dp   // 🔄 yatay
    } else {
        52.dp   // 📱 dikey
    }

    NavigationBar(
        modifier = Modifier.height(barHeight),
        windowInsets = WindowInsets(0, 0, 0, 0), // 🔥 ikon kesilmesini engeller
        tonalElevation = 6.dp
    ) {

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home", fontSize = 10.sp) },
            alwaysShowLabel = orientation != Configuration.ORIENTATION_LANDSCAPE
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Library.route) },
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text("Kitaplığım", fontSize = 10.sp) },
            alwaysShowLabel = orientation != Configuration.ORIENTATION_LANDSCAPE
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.Settings.route) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Ayarlar", fontSize = 10.sp) },
            alwaysShowLabel = orientation != Configuration.ORIENTATION_LANDSCAPE
        )
    }
}
