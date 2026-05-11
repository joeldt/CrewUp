package com.crewup.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.crewup.app.R
import com.crewup.app.ui.navigation.Screen
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign

// Dégradé bouton principal (bleu → orange)
val buttonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFFFF8C00))
)

// Dégradé bouton secondaire (orange → bleu)
val buttonGradient2 = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF8C00), Color(0xFF42A5F5), Color(0xFF1565C0))
)

@Composable
fun AccueilScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Image de fond
        Image(
            painter            = painterResource(id = R.drawable.bg_welcome),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        // Overlay sombre en bas pour lisibilité
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000))
                    )
                )
        )

        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo + titre
            Image(
                painter            = painterResource(id = R.drawable.logo),
                contentDescription = "Logo CrewUp",
                modifier           = Modifier.size(140.dp)
            )

            Text(
                text       = "CrewUp",
                fontSize   = 36.sp,
                fontWeight = FontWeight.Black,
                color      = Color.White
            )

            Spacer(modifier = Modifier.weight(1f))

            // Boutons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bouton Se connecter
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(buttonGradient),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick  = { navController.navigate(Screen.Login.route) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text       = "Se connecter",
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }

                // Bouton Créer un compte
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(buttonGradient2),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick  = { navController.navigate(Screen.Register.route) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text       = "Créer un compte",
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }
        }
    }
}