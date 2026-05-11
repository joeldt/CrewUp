package com.crewup.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*
import androidx.compose.foundation.Image // Import pour le logo Google
import androidx.compose.ui.res.painterResource // Import pour painterResource
import com.crewup.app.R // Remplacez par le package correct de votre projet si différent
import androidx.compose.material.icons.filled.Email // Pour l'icône Email



@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewUpWhite)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header avec bouton retour
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = CrewUpBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(width = 111.dp, height = 87.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CrewUpGray)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Content de vous revoir !",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = CrewUpBlack,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Champ email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Pseudo ou adresse email...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ mot de passe
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Mot de passe ...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bouton Se connecter
        Button(
            onClick = { navController.navigate(Screen.Home.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CrewUpBlack,
                contentColor = CrewUpWhite
            )
        ) {
            Text(text = "Se connecter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lien inscription
        TextButton(
            onClick = { navController.navigate(Screen.Register.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Pas de compte ? Inscription",
                color = CrewUpGrayMid,
                textAlign = TextAlign.Center
            )
        }

        // Ou continuer avec
        Text(
            text = "ou continuer avec",
            color = CrewUpGrayMid,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Icônes SSO (Google / Apple / Mail)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Google
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrewUpGray)
            ) {
                Image(
                   painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Apple
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrewUpGray)
            ) {
                Image(
                    // Remplacez Icon(imageVector = Icons.Default.Apple) par Image + painterResource
                    painter = painterResource(id = R.drawable.apple),
                    contentDescription = "Apple",
                    modifier = Modifier.size(28.dp)
                )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Mail
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrewUpGray)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    modifier = Modifier.size(28.dp),
                    tint = CrewUpBlack
                )
            }
        }
    }
}