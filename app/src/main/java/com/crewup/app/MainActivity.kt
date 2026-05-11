package com.crewup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.crewup.app.ui.navigation.AppNavigation
import com.crewup.app.ui.theme.CrewUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrewUpTheme {
                AppNavigation()
            }
        }
    }
}