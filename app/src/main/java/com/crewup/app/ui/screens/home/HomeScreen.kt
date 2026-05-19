package com.crewup.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.crewup.app.ui.components.BottomNavBar
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*

@Composable
fun HomeScreen(navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar    = { BottomNavBar(navController) },
        containerColor = CrewUpGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header : onglets + cloche
            HomeHeader(
                selectedTab          = selectedTab,
                onTabSelected        = { selectedTab = it },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
            )

            // Salutation
            Text(
                text       = "Salut Alex !",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                color      = CrewUpBlack,
                modifier   = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            // Contenu selon l'onglet
            when (selectedTab) {
                0 -> EmptyCrewsState()
                1 -> EmptyCreesState()
            }
        }
    }
}

@Composable
private fun HomeHeader(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onNotificationsClick: () -> Unit
) {
    Surface(color = CrewUpGray, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sélecteur d'onglets "Mes Crews | Crées"
            Row(verticalAlignment = Alignment.CenterVertically) {
                TabLabel(
                    text       = "Mes Crews",
                    isSelected = selectedTab == 0,
                    onClick    = { onTabSelected(0) }
                )
                Text(
                    text     = "  |  ",
                    fontSize = 18.sp,
                    color    = CrewUpGrayMid,
                    fontWeight = FontWeight.Light
                )
                TabLabel(
                    text       = "Crées",
                    isSelected = selectedTab == 1,
                    onClick    = { onTabSelected(1) }
                )
            }

            // Cloche notifications
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector        = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint               = CrewUpBlack,
                    modifier           = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun TabLabel(text: String, isSelected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick      = onClick,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(
            text   = text,
            fontSize   = 17.sp,
            fontWeight = FontWeight.Black,
            color      = if (isSelected) CrewUpBlack else CrewUpGrayMid,
            textDecoration = if (isSelected) TextDecoration.Underline else TextDecoration.None
        )
    }
}

@Composable
private fun EmptyCrewsState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 40.dp)
        ) {
            Text(text = "Aucun crew pour l'instant", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrewUpBlack)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = "Rejoins ou crée un événement pour commencer",
                fontSize  = 13.sp,
                color     = CrewUpGrayMid,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyCreesState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 40.dp)
        ) {
            Text(text = "Tu n'as pas encore créé de crew", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrewUpBlack)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = "Appuie sur + pour créer ton premier événement",
                fontSize  = 13.sp,
                color     = CrewUpGrayMid,
                textAlign = TextAlign.Center
            )
        }
    }
}
