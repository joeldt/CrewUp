package com.crewup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*

private data class NavItem(
    val route: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
    val label: String
)

// Home | Explorer | [+] | Historique | Profil
private val navItems = listOf(
    NavItem(Screen.Home.route,       Icons.Filled.Home,    Icons.Outlined.Home,    "Accueil"),
    NavItem(Screen.Explorer.route,   Icons.Filled.Search,  Icons.Outlined.Search,  "Explorer"),
    NavItem(Screen.Historique.route, Icons.Filled.History, Icons.Outlined.History, "Historique"),
    NavItem(Screen.Profile.route,    Icons.Filled.Person,  Icons.Outlined.Person,  "Profil"),
)

@Composable
fun BottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Surface(shadowElevation = 8.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            NavIcon(navItems[0], currentRoute == navItems[0].route) {
                navController.navigate(Screen.Home.route) { launchSingleTop = true; restoreState = true }
            }
            NavIcon(navItems[1], currentRoute == navItems[1].route) {
                navController.navigate(Screen.Explorer.route) { launchSingleTop = true; restoreState = true }
            }

            // Bouton Créer central — gradient thème
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(CrewUpBlueStart, CrewUpOrangeEnd))),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick  = { navController.navigate(Screen.CreateStep1.route) { launchSingleTop = true } },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Add,
                        contentDescription = "Créer un crew",
                        tint               = Color.White,
                        modifier           = Modifier.size(28.dp)
                    )
                }
            }

            NavIcon(navItems[2], currentRoute == navItems[2].route) {
                navController.navigate(Screen.Historique.route) { launchSingleTop = true; restoreState = true }
            }
            NavIcon(navItems[3], currentRoute == navItems[3].route) {
                navController.navigate(Screen.Profile.route) { launchSingleTop = true; restoreState = true }
            }
        }
    }
}

@Composable
private fun NavIcon(item: NavItem, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector        = if (isSelected) item.iconFilled else item.iconOutlined,
            contentDescription = item.label,
            tint               = if (isSelected) CrewUpBlueStart else CrewUpGrayMid,
            modifier           = Modifier.size(26.dp)
        )
    }
}
