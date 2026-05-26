package com.crewup.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.crewup.app.ui.theme.CrewUpBlack
import com.crewup.app.ui.theme.CrewUpGray
import com.crewup.app.ui.theme.CrewUpGrayMid

@Composable
fun HubScreen(
    navController: NavHostController,
    eventId: String
) {
    Scaffold(containerColor = CrewUpGray) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier          = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint               = CrewUpBlack
                    )
                }
                Text(
                    text       = "Hub",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Black,
                    color      = CrewUpBlack
                )
            }

            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = "Hub à venir...",
                    fontSize = 15.sp,
                    color    = CrewUpGrayMid
                )
            }
        }
    }
}
