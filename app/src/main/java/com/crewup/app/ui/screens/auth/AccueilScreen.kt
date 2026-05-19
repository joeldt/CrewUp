package com.crewup.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.crewup.app.ui.theme.CrewUpBlueEnd
import com.crewup.app.ui.theme.CrewUpBlueStart
import com.crewup.app.ui.theme.CrewUpOrangeEnd

// Gradient orange-bleu (bouton Créer un compte)
private val gradientOrange = Brush.horizontalGradient(
    colors = listOf(CrewUpOrangeEnd, CrewUpBlueEnd, CrewUpBlueStart)
)

@Composable
fun AccueilScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter            = painterResource(id = R.drawable.bg_accueil),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        // Dégradé sombre en bas pour lisibilité des boutons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
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
            Spacer(modifier = Modifier.height(56.dp))

            // Titre
            Text(
                text       = "CrewUp",
                fontSize   = 36.sp,
                fontWeight = FontWeight.Black,
                color      = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logo sur fond blanc circulaire
            Box(
                modifier         = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo CrewUp",
                    modifier           = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Boutons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Se connecter — fond blanc, texte bleu
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
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
                            color      = CrewUpBlueStart
                        )
                    }
                }

                // Créer un compte — gradient orange → bleu (couleurs thème)
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(gradientOrange),
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
