package com.crewup.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.crewup.app.R
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*

// Gradient bleu → orange (couleurs thème, bouton principal)
private val gradientBleu = Brush.horizontalGradient(
    colors = listOf(CrewUpBlueStart, CrewUpBlueEnd, CrewUpOrangeEnd)
)

@Composable
fun LoginScreen(navController: NavHostController) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewUpWhite)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Bouton retour
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint               = CrewUpBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Titre
        Text(
            text       = "Se connecter",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Black,
            color      = CrewUpBlueStart,
            modifier   = Modifier.fillMaxWidth(),
            textAlign  = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Logo circulaire — même dossier res/drawable/ que bg_accueil
        Box(
            modifier         = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(CrewUpGray)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter            = painterResource(id = R.drawable.logo),
                contentDescription = "Logo CrewUp",
                modifier           = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text      = "Content de vous revoir !",
            fontSize  = 16.sp,
            color     = CrewUpGrayMid,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Champ email / pseudo
        OutlinedTextField(
            value         = email,
            onValueChange = { email = it },
            placeholder   = { Text("Pseudo ou adresse email...") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine    = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ mot de passe
        OutlinedTextField(
            value                  = password,
            onValueChange          = { password = it },
            placeholder            = { Text("Mot de passe ...") },
            modifier               = Modifier.fillMaxWidth(),
            shape                  = RoundedCornerShape(12.dp),
            visualTransformation   = PasswordVisualTransformation(),
            keyboardOptions        = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine             = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mot de passe oublié
        TextButton(
            onClick  = {},
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Mot de passe oublié ?", color = CrewUpGrayMid, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bouton Se connecter — gradient bleu → orange (couleurs thème)
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(gradientBleu),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick  = { navController.navigate(Screen.Home.route) },
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text       = "Se connecter",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lien inscription
        TextButton(
            onClick  = { navController.navigate(Screen.Register.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text      = "Pas de compte ? Inscription",
                color     = CrewUpGrayMid,
                textAlign = TextAlign.Center
            )
        }

        // Séparateur
        Text(
            text      = "ou continuer avec",
            color     = CrewUpGrayMid,
            fontSize  = 13.sp,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Icônes SSO
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            // Google
            IconButton(
                onClick  = {},
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrewUpGray)
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier           = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Apple
            IconButton(
                onClick  = {},
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrewUpGray)
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.apple),
                    contentDescription = "Apple",
                    modifier           = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Mail
            IconButton(
                onClick  = {},
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrewUpGray)
            ) {
                Icon(
                    imageVector        = Icons.Default.Email,
                    contentDescription = "Email",
                    modifier           = Modifier.size(28.dp),
                    tint               = CrewUpBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
