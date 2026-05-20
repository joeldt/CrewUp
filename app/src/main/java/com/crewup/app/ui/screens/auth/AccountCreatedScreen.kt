package com.crewup.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.CrewUpBlack
import com.crewup.app.ui.theme.CrewUpGrayMid
import com.crewup.app.ui.theme.CrewUpWhite
import kotlinx.coroutines.delay

@Composable
fun AccountCreatedScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(CrewUpWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 40.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.CheckCircle,
                contentDescription = null,
                tint               = Color(0xFF2E7D32),
                modifier           = Modifier.size(88.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text       = "Compte créé avec succès !",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                color      = CrewUpBlack,
                textAlign  = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text      = "Redirection vers la connexion dans quelques secondes...",
                fontSize  = 14.sp,
                color     = CrewUpGrayMid,
                textAlign = TextAlign.Center
            )
        }
    }
}
