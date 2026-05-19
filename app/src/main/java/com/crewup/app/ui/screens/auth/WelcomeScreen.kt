package com.crewup.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.crewup.app.R
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.CrewUpBlueEnd
import com.crewup.app.ui.theme.CrewUpBlueStart
import com.crewup.app.ui.theme.CrewUpOrangeEnd
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(2500)
        navController.navigate(Screen.Accueil.route) {
            popUpTo(Screen.Welcome.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CrewUp",
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(CrewUpBlueStart, CrewUpBlueEnd, CrewUpOrangeEnd)
                    ),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter            = painterResource(id = R.drawable.logo),
                contentDescription = "CrewUp Logo",
                modifier           = Modifier.size(160.dp)
            )
        }
    }
}
