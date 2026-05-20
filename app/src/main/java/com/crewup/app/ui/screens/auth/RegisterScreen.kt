package com.crewup.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.AuthUiState
import com.crewup.app.ui.viewmodel.AuthViewModel

private enum class PasswordStrength { NONE, WEAK, MEDIUM, STRONG }

private fun computeStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.NONE
    val hasDigit   = password.any { it.isDigit() }
    val hasUpper   = password.any { it.isUpperCase() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val longEnough = password.length >= 6
    return when {
        !longEnough || !hasDigit || !hasUpper           -> PasswordStrength.WEAK
        hasSpecial || password.length >= 8              -> PasswordStrength.STRONG
        else                                            -> PasswordStrength.MEDIUM
    }
}

@Composable
private fun PasswordStrengthBar(strength: PasswordStrength) {
    if (strength == PasswordStrength.NONE) return
    val (filledCount, color, label) = when (strength) {
        PasswordStrength.WEAK   -> Triple(1, Color(0xFFD32F2F), "Faible")
        PasswordStrength.MEDIUM -> Triple(2, Color(0xFFF57C00), "Moyen")
        PasswordStrength.STRONG -> Triple(3, Color(0xFF2E7D32), "Fort")
        PasswordStrength.NONE   -> Triple(0, Color.Transparent, "")
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < filledCount) color else CrewUpGray)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text      = label,
            fontSize  = 11.sp,
            color     = color,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    var prenom          by remember { mutableStateOf("") }
    var nom             by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirm         by remember { mutableStateOf("") }
    var cguCheck        by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState is AuthUiState.Loading
    val errorMsg  = (uiState as? AuthUiState.Error)?.message

    val strength        = remember(password) { computeStrength(password) }
    val confirmMismatch = confirm.isNotBlank() && confirm != password
    val emailInvalid    = email.isNotBlank() && !email.matches(Regex("^[^@]+@[^@]+\\.[^@]+$"))

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            navController.navigate(Screen.SetupProfile.route)
            viewModel.resetState()
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

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value         = prenom,
            onValueChange = { prenom = it; if (errorMsg != null) viewModel.resetState() },
            placeholder   = { Text("Prénom...") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true,
            enabled       = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value         = nom,
            onValueChange = { nom = it; if (errorMsg != null) viewModel.resetState() },
            placeholder   = { Text("Nom...") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true,
            enabled       = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value           = email,
            onValueChange   = { email = it; if (errorMsg != null) viewModel.resetState() },
            placeholder     = { Text("Adresse email...") },
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine      = true,
            enabled         = !isLoading,
            isError         = emailInvalid,
            colors          = OutlinedTextFieldDefaults.colors(
                errorBorderColor = Color(0xFFD32F2F),
                errorCursorColor = Color(0xFFD32F2F)
            )
        )
        if (emailInvalid) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text     = "Adresse email invalide",
                color    = Color(0xFFD32F2F),
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Mot de passe avec œil et jauge
        OutlinedTextField(
            value         = password,
            onValueChange = { password = it; if (errorMsg != null) viewModel.resetState() },
            placeholder   = { Text("Mot de passe...") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true,
            enabled       = !isLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector        = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                        tint               = CrewUpGrayMid
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))
        PasswordStrengthBar(strength)

        Spacer(modifier = Modifier.height(12.dp))

        // Confirmer mot de passe avec bordure rouge si différent
        OutlinedTextField(
            value                = confirm,
            onValueChange        = { confirm = it; if (errorMsg != null) viewModel.resetState() },
            placeholder          = { Text("Confirmer mot de passe...") },
            modifier             = Modifier.fillMaxWidth(),
            shape                = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine           = true,
            enabled              = !isLoading,
            isError              = confirmMismatch,
            colors               = OutlinedTextFieldDefaults.colors(
                errorBorderColor      = Color(0xFFD32F2F),
                errorCursorColor      = Color(0xFFD32F2F),
                errorLabelColor       = Color(0xFFD32F2F)
            )
        )
        if (confirmMismatch) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text     = "Les mots de passe ne correspondent pas",
                color    = Color(0xFFD32F2F),
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked         = cguCheck,
                onCheckedChange = { cguCheck = it },
                colors          = CheckboxDefaults.colors(checkedColor = CrewUpBlack),
                enabled         = !isLoading
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text     = "J'accepte la CGU et la politique de confidentialité",
                fontSize = 13.sp,
                color    = CrewUpGrayMid
            )
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

        Button(
            onClick  = { viewModel.registerWithEmail(email, password, confirm, nom, prenom) },
            enabled  = cguCheck && !isLoading && !confirmMismatch && !emailInvalid,
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
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(text = "Créer un compte", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick  = { navController.navigate(Screen.Login.route) },
            modifier = Modifier.fillMaxWidth(),
            enabled  = !isLoading
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
