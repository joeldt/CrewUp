package com.crewup.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ConfirmationScreen(
    navController: NavHostController,
    eventId: String
) {
    var invitedCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(eventId) {
        runCatching {
            val doc = FirebaseFirestore.getInstance()
                .collection("events").document(eventId).get().await()
            @Suppress("UNCHECKED_CAST")
            (doc.get("invitedFriends") as? List<*>)?.size ?: 0
        }.onSuccess { invitedCount = it }
    }

    Scaffold(containerColor = CrewUpGray) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Titre
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text       = "Confirmation",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Black,
                    color      = CrewUpBlack
                )
            }

            // Contenu centré
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Cercle checkmark
                    Box(
                        modifier         = Modifier
                            .size(100.dp)
                            .background(CrewUpBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Check,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text       = "crew créé !",
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Black,
                        color      = CrewUpBlack
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text     = "Invitations envoyées à $invitedCount membres",
                        fontSize = 14.sp,
                        color    = CrewUpGrayMid
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Bouton voir le Hub
                    Button(
                        onClick  = {
                            navController.navigate(Screen.Hub.createRoute(eventId)) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrewUpGold,
                            contentColor   = CrewUpBlack
                        )
                    ) {
                        Text(text = "voir le Hub", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bouton retour à l'accueil
                    OutlinedButton(
                        onClick  = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape  = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, CrewUpBlack),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrewUpBlack)
                    ) {
                        Text(text = "Retour à l'accueil", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
