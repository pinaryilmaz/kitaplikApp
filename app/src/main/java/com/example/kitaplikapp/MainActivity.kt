package com.example.kitaplikapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.kitaplikapp.ui.navigation.AppNavGraph
import com.example.kitaplikapp.ui.theme.KitaplikAppTheme
import com.example.kitaplikapp.viewmodel.AuthViewModel
import com.example.kitaplikapp.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private val authVm: AuthViewModel by viewModels()
    private val settingsVm: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val darkMode by settingsVm.darkMode.collectAsState()

            KitaplikAppTheme(
                darkTheme = darkMode
            ) {
                AppNavGraph(authVm = authVm)
            }
        }
    }
}


