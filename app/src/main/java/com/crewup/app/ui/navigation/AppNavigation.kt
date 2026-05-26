package com.crewup.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crewup.app.ui.screens.auth.*
import com.crewup.app.ui.screens.home.*
import com.crewup.app.ui.viewmodel.AuthViewModel
import com.crewup.app.ui.viewmodel.CreateEventViewModel
import com.crewup.app.ui.viewmodel.FriendsViewModel
import com.crewup.app.ui.viewmodel.HomeViewModel
import com.crewup.app.ui.viewmodel.HubViewModel
import com.crewup.app.ui.viewmodel.ProfileViewModel
sealed class Screen(val route: String) {
    object Welcome       : Screen("welcome")
    object Accueil       : Screen("accueil")
    object Login         : Screen("login")
    object Register      : Screen("register")
    object SetupProfile  : Screen("setup_profile")
    object ForgotPassword  : Screen("forgot_password")
    object AccountCreated  : Screen("account_created")
    object Home          : Screen("home")
    object Explorer      : Screen("explorer")
    object Notifications : Screen("notifications")
    object CreateStep1   : Screen("create_step1")
    object CreateStep2   : Screen("create_step2")
    object CreateStep3   : Screen("create_step3")
    object Confirmation  : Screen("confirmation/{eventId}") {
        fun createRoute(id: String) = "confirmation/$id"
    }
    object Profile       : Screen("profile")
    object EditProfile   : Screen("edit_profile")
    object Parametres    : Screen("parametres")
    object Historique    : Screen("historique")
    object Friends       : Screen("friends")
    object Hub           : Screen("hub/{eventId}") {
        fun createRoute(id: String) = "hub/$id"
    }
    object PostEvent     : Screen("post_event/{eventId}") {
        fun createRoute(id: String) = "post_event/$id"
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel               = viewModel()
    val profileViewModel: ProfileViewModel         = viewModel()
    val createEventViewModel: CreateEventViewModel = viewModel()
    val homeViewModel: HomeViewModel               = viewModel()
    val friendsViewModel: FriendsViewModel         = viewModel()
    val startDestination = Screen.Welcome.route

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        // Flux auth
        composable(Screen.Welcome.route)       { WelcomeScreen(navController, authViewModel) }
        composable(Screen.Accueil.route)       { AccueilScreen(navController) }
        composable(Screen.Login.route)         { LoginScreen(navController, authViewModel) }
        composable(Screen.Register.route)      { RegisterScreen(navController, authViewModel) }
        composable(Screen.SetupProfile.route)  { SetupProfileScreen(navController, authViewModel) }
        composable(Screen.ForgotPassword.route)  { ForgotPasswordScreen(navController, authViewModel) }
        composable(Screen.AccountCreated.route)  { AccountCreatedScreen(navController) }

        // Flux principal (avec bottom nav)
        composable(Screen.Home.route)          { HomeScreen(navController, homeViewModel, friendsViewModel) }
        composable(Screen.Explorer.route)      { ExplorerScreen(navController) }
        composable(Screen.Notifications.route) { NotificationsScreen(navController, friendsViewModel) }
        composable(Screen.Historique.route)    { HistoriqueScreen(navController) }
        composable(Screen.Profile.route)       { ProfileScreen(navController, profileViewModel, friendsViewModel) }
        composable(Screen.EditProfile.route)   { EditProfileScreen(navController, profileViewModel) }
        composable(Screen.Parametres.route)    { ParametresScreen(navController) }
        composable(Screen.CreateStep1.route)   { CreateStep1Screen(navController, createEventViewModel) }
        composable(Screen.CreateStep2.route)   { CreateStep2Screen(navController, createEventViewModel) }
        composable(Screen.CreateStep3.route)   { CreateStep3Screen(navController, createEventViewModel, friendsViewModel) }
        composable(Screen.Friends.route)       { SearchFriendsScreen(navController, friendsViewModel) }
        composable(Screen.Confirmation.route)  { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            ConfirmationScreen(navController, eventId)
        }
        composable(Screen.Hub.route) { backStackEntry ->
            val hubViewModel: HubViewModel = viewModel(backStackEntry)
            HubScreen(navController, hubViewModel)
        }
    }
}
