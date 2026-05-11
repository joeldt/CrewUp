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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*

@Composable
fun RegisterScreen(navController: NavHostController) {
    var nom      by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm  by remember { mutableStateOf("") }
    var cguCheck by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewUpWhite)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint               = CrewUpBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(width = 111.dp, height = 87.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CrewUpGray)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text       = "Rejoignez la troupe",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = CrewUpBlack,
            modifier   = Modifier.fillMaxWidth(),
            textAlign  = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value         = nom,
            onValueChange = { nom = it },
            placeholder   = { Text("Nom & prénom..") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value           = email,
            onValueChange   = { email = it },
            placeholder     = { Text("Adresse email...") },
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine      = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value                = password,
            onValueChange        = { password = it },
            placeholder          = { Text("Mot de passe ...") },
            modifier             = Modifier.fillMaxWidth(),
            shape                = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            singleLine           = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value                = confirm,
            onValueChange        = { confirm = it },
            placeholder          = { Text("Confirmer mot de passe...") },
            modifier             = Modifier.fillMaxWidth(),
            shape                = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            singleLine           = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CGU checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked         = cguCheck,
                onCheckedChange = { cguCheck = it },
                colors          = CheckboxDefaults.colors(checkedColor = CrewUpBlack)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text     = "J'accepte la CGU et la politique de confidentialité",
                fontSize = 13.sp,
                color    = CrewUpGrayMid
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick  = { navController.navigate(Screen.SetupProfile.route) },
            enabled  = cguCheck,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor         = CrewUpBlack,
                contentColor           = CrewUpWhite,
                disabledContainerColor = CrewUpGrayMid
            )
        ) {
            Text(text = "Créer un compte", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick  = { navController.navigate(Screen.Login.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text      = "Déjà membre ? Se connecter",
                color     = CrewUpGrayMid,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}