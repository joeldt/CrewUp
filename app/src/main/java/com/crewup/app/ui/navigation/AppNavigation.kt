package com.crewup.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crewup.app.ui.screens.auth.*
import com.crewup.app.ui.screens.home.HomeScreen

sealed class Screen(val route: String) {
    object Welcome      : Screen("welcome")
    object Accueil      : Screen("accueil")
    object Login        : Screen("login")
    object Register     : Screen("register")
    object SetupProfile : Screen("setup_profile")
    object Home         : Screen("home")
    object Notifications: Screen("notifications")
    object Explorer     : Screen("explorer")
    object CreateStep1  : Screen("create_step1")
    object CreateStep2  : Screen("create_step2")
    object CreateStep3  : Screen("create_step3")
    object Confirmation : Screen("confirmation")
    object Profile      : Screen("profile")
    object Historique   : Screen("historique")
    object Hub          : Screen("hub/{eventId}") {
        fun createRoute(id: String) = "hub/$id"
    }
    object PostEvent    : Screen("post_event/{eventId}") {
        fun createRoute(id: String) = "post_event/$id"
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController  = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route)      { WelcomeScreen(navController) }
        composable(Screen.Login.route)        { LoginScreen(navController) }
        composable(Screen.Register.route)     { RegisterScreen(navController) }
        composable(Screen.SetupProfile.route) { SetupProfileScreen(navController) }
        composable(Screen.Home.route)         { HomeScreen(navController) }
    }
}