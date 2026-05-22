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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.crewup.app.R
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.AuthUiState
import com.crewup.app.ui.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private val gradientBleu = Brush.horizontalGradient(
    colors = listOf(CrewUpBlueStart, CrewUpBlueEnd, CrewUpOrangeEnd)
)

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val isLoading = uiState is AuthUiState.Loading
    val errorMsg  = (uiState as? AuthUiState.Error)?.message

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
                viewModel.resetState()
            }
            is AuthUiState.SuccessNeedsProfile -> {
                navController.navigate(Screen.SetupProfile.route) { popUpTo(0) { inclusive = true } }
                viewModel.resetState()
            }
            else -> {}
        }
    }

    fun signInWithGoogle() {
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val signInWithGoogleOption = GetSignInWithGoogleOption
                    .Builder(context.getString(R.string.default_web_client_id))
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    viewModel.loginWithGoogle(idToken)
                } else {
                    viewModel.setError("La connexion avec Google a échoué. Veuillez réessayer.")
                }
            } catch (_: GetCredentialCancellationException) {
                // L'utilisateur a annulé — ne rien afficher
            } catch (_: NoCredentialException) {
                viewModel.setError("Aucun compte Google trouvé sur cet appareil")
            } catch (_: GetCredentialException) {
                viewModel.setError("La connexion avec Google a échoué. Veuillez réessayer.")
            }
        }
    }

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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text       = "Se connecter",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Black,
            color      = CrewUpBlueStart,
            modifier   = Modifier.fillMaxWidth(),
            textAlign  = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        OutlinedTextField(
            value           = email,
            onValueChange   = { email = it; if (errorMsg != null) viewModel.resetState() },
            placeholder     = { Text("Adresse email...") },
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine      = true,
            enabled         = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value                = password,
            onValueChange        = { password = it; if (errorMsg != null) viewModel.resetState() },
            placeholder          = { Text("Mot de passe...") },
            modifier             = Modifier.fillMaxWidth(),
            shape                = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine           = true,
            enabled              = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick  = { navController.navigate(Screen.ForgotPassword.route) },
            modifier = Modifier.align(Alignment.End),
            enabled  = !isLoading
        ) {
            Text(text = "Mot de passe oublié ?", color = CrewUpGrayMid, fontSize = 13.sp)
        }

        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = errorMsg,
                color     = MaterialTheme.colorScheme.error,
                fontSize  = 13.sp,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

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
                    onClick  = { viewModel.loginWithEmail(email, password) },
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick  = { navController.navigate(Screen.Register.route) },
            modifier = Modifier.fillMaxWidth(),
            enabled  = !isLoading
        ) {
            Text(
                text      = "Pas de compte ? Inscription",
                color     = CrewUpGrayMid,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text      = "ou continuer avec",
            color     = CrewUpGrayMid,
            fontSize  = 13.sp,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(
                onClick  = { if (!isLoading) signInWithGoogle() },
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
