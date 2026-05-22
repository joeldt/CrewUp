package com.crewup.app.ui.screens.home

import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.crewup.app.ui.components.BottomNavBar
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.ProfileUiState
import com.crewup.app.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    Scaffold(
        bottomBar      = { BottomNavBar(navController) },
        containerColor = CrewUpGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text       = "Profil",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                color      = CrewUpBlack,
                modifier   = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )

            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(60.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = CrewUpBlueStart) }
                }
                is ProfileUiState.Error -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = state.message, color = CrewUpGrayMid, textAlign = TextAlign.Center)
                    }
                }
                is ProfileUiState.Success -> {
                    val data = state.data

                    // Card profil
                    Surface(
                        modifier        = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape           = RoundedCornerShape(20.dp),
                        color           = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier            = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier         = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(CrewUpGray),
                                contentAlignment = Alignment.Center
                            ) {
                                if (data.photoBase64 != null) {
                                    AsyncImage(
                                        model              = Base64.decode(data.photoBase64, Base64.NO_WRAP),
                                        contentDescription = "Photo de profil",
                                        modifier           = Modifier.fillMaxSize(),
                                        contentScale       = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector        = Icons.Filled.AccountCircle,
                                        contentDescription = null,
                                        tint               = CrewUpGrayMid,
                                        modifier           = Modifier.size(90.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text       = "${data.prenom} ${data.nom}".trim(),
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.Black,
                                color      = CrewUpBlack
                            )

                            if (data.pseudo.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "@${data.pseudo}", fontSize = 14.sp, color = CrewUpGrayMid)
                            }

                            if (data.ville.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector        = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint               = CrewUpGrayMid,
                                        modifier           = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(text = data.ville, fontSize = 13.sp, color = CrewUpGrayMid)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            StarRating(score = data.score)

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text     = if (data.score == 0.0) "Pas encore noté"
                                           else "Score de fiabilité ${"%.1f".format(data.score)}",
                                fontSize = 13.sp,
                                color    = CrewUpGrayMid
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color    = CrewUpDivider
                            )

                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                StatItem(value = "0", label = "Crées")
                                VerticalDivider(modifier = Modifier.height(36.dp), color = CrewUpDivider)
                                StatItem(value = "0", label = "Rejoints")
                                VerticalDivider(modifier = Modifier.height(36.dp), color = CrewUpDivider)
                                StatItem(value = "0", label = "Amis")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileActionButton("Modifier le profil") {
                        navController.navigate(Screen.EditProfile.route)
                    }
                    ProfileActionButton("Mes Amis") {}
                    ProfileActionButton("Paramètres") {
                        navController.navigate(Screen.Parametres.route)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ProfileActionButton("Se déconnecter", textColor = CrewUpRed) {
                        viewModel.signOut {
                            navController.navigate(Screen.Accueil.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StarRating(score: Double, maxStars: Int = 5) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(maxStars) { i ->
            Icon(
                imageVector        = Icons.Filled.Star,
                contentDescription = null,
                tint               = if (i + 1 <= score) CrewUpBlack else CrewUpGrayMid,
                modifier           = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = CrewUpBlack)
        Text(text = label, fontSize = 12.sp, color = CrewUpGrayMid)
    }
}

@Composable
private fun ProfileActionButton(
    label: String,
    textColor: Color = CrewUpBlack,
    onClick: () -> Unit
) {
    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape           = RoundedCornerShape(14.dp),
        color           = Color.White,
        shadowElevation = 1.dp,
        onClick         = onClick
    ) {
        Text(
            text       = label,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = textColor,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.padding(vertical = 16.dp)
        )
    }
}
