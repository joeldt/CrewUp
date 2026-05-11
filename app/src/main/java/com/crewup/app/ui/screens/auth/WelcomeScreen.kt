package com.crewup.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.crewup.app.ui.navigation.Screen
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.crewup.app.R

@Composable
fun WelcomeScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(2500)
        navController.navigate(Screen.Accueil.route) {
            popUpTo(Screen.Welcome.route) { inclusive = true }
        }
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter            = painterResource(id = R.drawable.logo),
            contentDescription = "CrewUp Logo",
            modifier           = Modifier.size(180.dp)
        )
    }
}