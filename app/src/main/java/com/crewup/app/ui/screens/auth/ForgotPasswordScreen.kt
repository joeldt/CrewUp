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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.AuthUiState
import com.crewup.app.ui.viewmodel.AuthViewModel

private val gradientBleu = Brush.horizontalGradient(
    colors = listOf(CrewUpBlueStart, CrewUpBlueEnd, CrewUpOrangeEnd)
)

@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }

    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading  = uiState is AuthUiState.Loading
    val isSuccess  = uiState is AuthUiState.Success
    val errorMsg   = (uiState as? AuthUiState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewUpWhite)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack(); viewModel.resetState() }) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint               = CrewUpBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text       = "Mot de passe oublié",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Black,
            color      = CrewUpBlueStart,
            modifier   = Modifier.fillMaxWidth(),
            textAlign  = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text      = "Saisis ton adresse email pour recevoir un lien de réinitialisation.",
            fontSize  = 15.sp,
            color     = CrewUpGrayMid,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value           = email,
            onValueChange   = { email = it; if (errorMsg != null || isSuccess) viewModel.resetState() },
            placeholder     = { Text("Adresse email...") },
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine      = true,
            enabled         = !isLoading && !isSuccess
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isSuccess -> {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text      = "Email de réinitialisation envoyé !",
                        color     = Color(0xFF2E7D32),
                        fontSize  = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick  = { navController.popBackStack(); viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Retour à la connexion", color = CrewUpBlueStart)
                }
            }
            errorMsg != null -> {
                Text(
                    text      = errorMsg,
                    color     = MaterialTheme.colorScheme.error,
                    fontSize  = 13.sp,
                    modifier  = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isSuccess) {
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isLoading) SolidColor(CrewUpGrayMid) else gradientBleu),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    TextButton(
                        onClick  = { viewModel.sendPasswordReset(email) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text       = "Envoyer le lien",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
