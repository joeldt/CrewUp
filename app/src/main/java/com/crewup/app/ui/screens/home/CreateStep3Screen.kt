package com.crewup.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.ClipEntry
import android.content.ClipData
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.CreateEventUiState
import com.crewup.app.ui.viewmodel.CreateEventViewModel
import com.crewup.app.ui.viewmodel.FriendsViewModel

@Composable
fun CreateStep3Screen(
    navController: NavHostController,
    viewModel: CreateEventViewModel,
    friendsViewModel: FriendsViewModel
) {
    val draft       by viewModel.draft.collectAsStateWithLifecycle()
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val friends     by friendsViewModel.friends.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { friendsViewModel.loadFriends() }

    val clipboard         = LocalClipboard.current
    val coroutineScope    = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading         = createState is CreateEventUiState.Loading

    val shareLink = remember(draft.name, viewModel.eventId) {
        val slug = draft.name.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .replace(" ", "-")
            .take(20)
            .trimEnd('-')
            .ifBlank { "crew" }
        "crewup.app/$slug-${viewModel.eventId.take(6)}"
    }

    LaunchedEffect(createState) {
        when (createState) {
            is CreateEventUiState.Success -> {
                navController.navigate(Screen.Confirmation.createRoute(viewModel.eventId)) {
                    popUpTo(Screen.CreateStep1.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is CreateEventUiState.Error -> {
                snackbarHostState.showSnackbar((createState as CreateEventUiState.Error).message)
                viewModel.resetCreateState()
            }
            else -> {}
        }
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
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Inviter le crew",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Black,
                    color      = CrewUpBlack
                )
            }

            CreationStepBar(currentStep = 3)

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // Recherche
                OutlinedTextField(
                    value         = "",
                    onValueChange = {},
                    placeholder   = { Text("Rechercher...", color = CrewUpGrayMid) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(24.dp),
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor  = Color.White,
                        focusedContainerColor    = Color.White,
                        unfocusedBorderColor     = CrewUpDivider,
                        focusedBorderColor       = CrewUpBlack
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mes contacts
                Text(
                    text       = "Mes contacts",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = CrewUpBlack
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color    = CrewUpDivider
                )

                if (friends.isEmpty()) {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector        = Icons.Filled.AccountCircle,
                                contentDescription = null,
                                tint               = CrewUpGrayMid,
                                modifier           = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aucun ami pour l'instant", fontSize = 14.sp, color = CrewUpGrayMid)
                            Text("Ajoute des amis depuis ton profil", fontSize = 12.sp, color = CrewUpGrayMid)
                        }
                    }
                } else {
                    friends.forEach { friend ->
                        val isChecked = friend.uid in draft.invitedFriends
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MiniAvatar(
                                pseudo      = friend.pseudo,
                                photoBase64 = friend.photoBase64,
                                size        = 38
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text       = friend.pseudo,
                                fontSize   = 15.sp,
                                color      = CrewUpBlack,
                                modifier   = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked         = isChecked,
                                onCheckedChange = { viewModel.toggleFriend(friend.uid) },
                                colors          = CheckboxDefaults.colors(
                                    checkedColor   = CrewUpBlack,
                                    uncheckedColor = CrewUpGrayMid
                                )
                            )
                        }
                        HorizontalDivider(color = CrewUpDivider)
                    }
                }

                HorizontalDivider(color = CrewUpDivider)

                Spacer(modifier = Modifier.height(20.dp))

                // Partager un lien
                Text(
                    text       = "ou partager un lien",
                    fontSize   = 14.sp,
                    color      = CrewUpGrayMid
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    color     = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = shareLink,
                            fontSize = 13.sp,
                            color    = CrewUpBlack,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(ClipData.newPlainText("lien", shareLink))
                                    )
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text       = "COPIER",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = CrewUpBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bouton créer
                Button(
                    onClick  = {
                        viewModel.createEvent {
                            // naviguer depuis LaunchedEffect sur Success
                        }
                    },
                    enabled  = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = CrewUpGold,
                        contentColor           = CrewUpBlack,
                        disabledContainerColor = CrewUpGrayMid,
                        disabledContentColor   = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color       = CrewUpBlack,
                            modifier    = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Créer le Crew", fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
