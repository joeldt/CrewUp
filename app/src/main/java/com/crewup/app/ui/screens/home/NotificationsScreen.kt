package com.crewup.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.crewup.app.ui.components.BottomNavBar
import com.crewup.app.ui.theme.CrewUpGray

@Composable
fun NotificationsScreen(navController: NavHostController) {
    Scaffold(
        bottomBar      = { BottomNavBar(navController) },
        containerColor = CrewUpGray
    ) { innerPadding ->
        Box(
            modifier         = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Notifications — à venir", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
