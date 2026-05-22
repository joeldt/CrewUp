package com.crewup.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.ParametresActionState
import com.crewup.app.ui.viewmodel.ParametresUiState
import com.crewup.app.ui.viewmodel.ParametresViewModel

@Composable
fun ParametresScreen(
    navController: NavHostController,
    viewModel: ParametresViewModel = viewModel()
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Réactions aux actions
    LaunchedEffect(actionState) {
        when (actionState) {
            is ParametresActionState.PasswordResetSent -> {
                snackbarHostState.showSnackbar("Email de réinitialisation envoyé !")
                viewModel.resetActionState()
            }
            is ParametresActionState.AccountDeleted -> {
                navController.navigate(Screen.Accueil.route) { popUpTo(0) { inclusive = true } }
            }
            is ParametresActionState.Error -> {
                snackbarHostState.showSnackbar((actionState as ParametresActionState.Error).message)
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    // Dialog confirmation suppression compte
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Supprimer mon compte", fontWeight = FontWeight.Bold) },
            text    = {
                Text(
                    "Cette action est irréversible. Ton compte et toutes tes données seront définitivement supprimés.",
                    color = CrewUpGrayMid
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.deleteAccount() }) {
                    Text("Supprimer", color = CrewUpRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler", color = CrewUpGrayMid)
                }
            }
        )
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = CrewUpGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier          = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = { navController.popBackStack() },
                    enabled  = actionState !is ParametresActionState.Loading
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint               = CrewUpBlack
                    )
                }
                Text(
                    text       = "Paramètres",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Black,
                    color      = CrewUpBlack
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val state = uiState) {
                is ParametresUiState.Loading -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(60.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = CrewUpBlueStart) }
                }
                is ParametresUiState.Error -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(text = state.message, color = CrewUpGrayMid) }
                }
                is ParametresUiState.Success -> {
                    val prefs = state.notifPrefs

                    // === Section NOTIFICATIONS ===
                    SectionLabel("NOTIFICATIONS")
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsCard {
                        SettingToggleRow(
                            label   = "Rappels de sortie",
                            checked = prefs.rappelsSortie,
                            onCheckedChange = { viewModel.toggleRappelsSortie(it) }
                        )
                        SettingDivider()
                        SettingToggleRow(
                            label   = "Nouveaux votes",
                            checked = prefs.nouveauxVotes,
                            onCheckedChange = { viewModel.toggleNouveauxVotes(it) }
                        )
                        SettingDivider()
                        SettingToggleRow(
                            label   = "Invitations",
                            checked = prefs.invitations,
                            onCheckedChange = { viewModel.toggleInvitations(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // === Section COMPTE ===
                    SectionLabel("COMPTE")
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsCard {
                        SettingActionRow(
                            label    = "Changer de mot de passe",
                            enabled  = actionState !is ParametresActionState.Loading,
                            onClick  = { viewModel.sendPasswordReset() }
                        )
                        SettingDivider()
                        SettingActionRow(
                            label     = "Supprimer mon compte",
                            textColor = CrewUpRed,
                            enabled   = actionState !is ParametresActionState.Loading,
                            onClick   = { showDeleteDialog = true }
                        )
                    }

                    if (actionState is ParametresActionState.Loading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier         = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = CrewUpBlueStart, modifier = Modifier.size(28.dp)) }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text,
        fontSize      = 12.sp,
        fontWeight    = FontWeight.Medium,
        color         = CrewUpGrayMid,
        letterSpacing = 0.8.sp,
        modifier      = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape           = RoundedCornerShape(14.dp),
        color           = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = label,
            fontSize   = 15.sp,
            color      = CrewUpBlack,
            modifier   = Modifier.weight(1f)
        )
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors          = SwitchDefaults.colors(
                checkedThumbColor  = Color.White,
                checkedTrackColor  = CrewUpBlueStart,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = CrewUpGrayMid
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    label: String,
    textColor: Color = CrewUpBlack,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text       = label,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = if (enabled) textColor else CrewUpGrayMid
        )
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color    = CrewUpDivider
    )
}
