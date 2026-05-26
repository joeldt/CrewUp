package com.crewup.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.crewup.app.ui.components.BottomNavBar
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.EventSummary
import com.crewup.app.ui.viewmodel.FriendEntry
import com.crewup.app.ui.viewmodel.FriendsViewModel
import com.crewup.app.ui.viewmodel.HomeUiState
import com.crewup.app.ui.viewmodel.HomeViewModel
import com.crewup.app.ui.viewmodel.NotificationsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel,
    friendsViewModel: FriendsViewModel,
    notificationsViewModel: NotificationsViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val prenom     = remember { FirebaseAuth.getInstance().currentUser?.displayName?.ifBlank { null } ?: "toi" }
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingReceived by friendsViewModel.pendingReceived.collectAsStateWithLifecycle()
    val friends        by friendsViewModel.friends.collectAsStateWithLifecycle()
    val notifCount     by notificationsViewModel.totalCount.collectAsStateWithLifecycle()

    val badgeCount = pendingReceived.size + notifCount

    var deleteTargetEvent by remember { mutableStateOf<EventSummary?>(null) }
    var inviteTargetEvent by remember { mutableStateOf<EventSummary?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
        friendsViewModel.loadFriends()
        notificationsViewModel.loadNotifications()
    }

    // Dialog suppression
    deleteTargetEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { deleteTargetEvent = null },
            icon = {
                Icon(
                    imageVector        = Icons.Filled.Warning,
                    contentDescription = null,
                    tint               = CrewUpRed,
                    modifier           = Modifier.size(38.dp)
                )
            },
            title = {
                Text(
                    text       = "Supprimer le crew ?",
                    fontWeight = FontWeight.Black,
                    fontSize   = 18.sp,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text      = "Tu es sur le point de supprimer définitivement \"${event.name}\".",
                        textAlign = TextAlign.Center,
                        fontSize  = 14.sp,
                        color     = CrewUpBlack
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF0F0)
                    ) {
                        Text(
                            text       = "Cette action est irréversible.\nTous les participants seront notifiés de la suppression.",
                            textAlign  = TextAlign.Center,
                            fontSize   = 13.sp,
                            color      = CrewUpRed,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick  = { viewModel.deleteEvent(event); deleteTargetEvent = null },
                    colors   = ButtonDefaults.buttonColors(containerColor = CrewUpRed, contentColor = Color.White),
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Supprimer définitivement", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick  = { deleteTargetEvent = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Annuler", color = CrewUpBlack)
                }
            },
            containerColor = Color.White,
            shape          = RoundedCornerShape(20.dp)
        )
    }

    // Dialog invitation d'un ami
    inviteTargetEvent?.let { event ->
        InviteFriendDialog(
            event      = event,
            friends    = friends,
            currentUid = currentUid,
            onInvite   = { friend ->
                viewModel.inviteFriendToEvent(event, friend.uid)
                scope.launch {
                    snackbarHostState.showSnackbar("Invitation envoyée à ${friend.pseudo}")
                }
            },
            onDismiss  = { inviteTargetEvent = null }
        )
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        bottomBar      = { BottomNavBar(navController) },
        containerColor = CrewUpGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HomeHeader(
                selectedTab          = selectedTab,
                notificationCount    = badgeCount,
                onTabSelected        = { selectedTab = it },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
            )

            Text(
                text       = "Salut $prenom !",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                color      = CrewUpBlack,
                modifier   = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            when (selectedTab) {
                0 -> EventListContent(
                    uiState         = uiState,
                    getEvents       = { it.allEvents },
                    currentUid      = currentUid,
                    emptyContent    = { EmptyCrewsState() },
                    onEventClick    = { navController.navigate(Screen.Hub.createRoute(it)) },
                    onDeleteRequest = { deleteTargetEvent = it },
                    onInviteRequest = { inviteTargetEvent = it }
                )
                1 -> EventListContent(
                    uiState         = uiState,
                    getEvents       = { it.createdEvents },
                    currentUid      = currentUid,
                    emptyContent    = { EmptyCreesState() },
                    onEventClick    = { navController.navigate(Screen.Hub.createRoute(it)) },
                    onDeleteRequest = { deleteTargetEvent = it },
                    onInviteRequest = { inviteTargetEvent = it }
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    selectedTab: Int,
    notificationCount: Int,
    onTabSelected: (Int) -> Unit,
    onNotificationsClick: () -> Unit
) {
    Surface(color = CrewUpGray, shadowElevation = 0.dp) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TabLabel(text = "Mes Crews", isSelected = selectedTab == 0, onClick = { onTabSelected(0) })
                Text(text = "  |  ", fontSize = 18.sp, color = CrewUpGrayMid, fontWeight = FontWeight.Light)
                TabLabel(text = "Crées", isSelected = selectedTab == 1, onClick = { onTabSelected(1) })
            }

            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(containerColor = Color.Red) {
                                Text(
                                    text  = if (notificationCount > 9) "9+" else notificationCount.toString(),
                                    color = Color.White
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector        = if (notificationCount > 0) Icons.Filled.Notifications
                                             else Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint               = CrewUpBlack,
                        modifier           = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabLabel(text: String, isSelected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick        = onClick,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(
            text           = text,
            fontSize       = 17.sp,
            fontWeight     = FontWeight.Black,
            color          = if (isSelected) CrewUpBlack else CrewUpGrayMid,
            textDecoration = if (isSelected) TextDecoration.Underline else TextDecoration.None
        )
    }
}

@Composable
private fun EventListContent(
    uiState: HomeUiState,
    getEvents: (HomeUiState.Success) -> List<EventSummary>,
    currentUid: String,
    emptyContent: @Composable () -> Unit,
    onEventClick: (String) -> Unit,
    onDeleteRequest: (EventSummary) -> Unit,
    onInviteRequest: (EventSummary) -> Unit
) {
    when (uiState) {
        is HomeUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CrewUpBlack)
            }
        }
        is HomeUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.message, color = CrewUpGrayMid, fontSize = 14.sp)
            }
        }
        is HomeUiState.Success -> {
            val events = getEvents(uiState)
            if (events.isEmpty()) {
                emptyContent()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    events.forEach { event ->
                        EventCard(
                            event           = event,
                            currentUid      = currentUid,
                            onClick         = { onEventClick(event.id) },
                            onDeleteRequest = { onDeleteRequest(event) },
                            onInviteRequest = { onInviteRequest(event) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: EventSummary,
    currentUid: String,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit,
    onInviteRequest: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val isVote      = event.datesCount > 1
    val statusColor = if (isVote) Color(0xFFFF8C00) else CrewUpGreen
    val statusText  = if (isVote) "Vote en cours" else "Confirmé"

    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape           = RoundedCornerShape(14.dp),
        color           = Color.White,
        border          = BorderStroke(1.dp, CrewUpDivider),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier          = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CrewUpBlack)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = statusColor) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = statusText, fontSize = 12.sp, color = statusColor)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = CrewUpGrayMid, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = event.maxParticipants.toString(), fontSize = 14.sp, color = CrewUpGrayMid)
                Spacer(modifier = Modifier.width(2.dp))

                Box {
                    IconButton(
                        onClick  = { menuExpanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.MoreVert,
                            contentDescription = "Options",
                            tint               = CrewUpGrayMid,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded         = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text        = { Text("Ajouter un ami", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = { menuExpanded = false; onInviteRequest() }
                        )
                        if (event.organizerId == currentUid) {
                            HorizontalDivider(color = CrewUpDivider)
                            DropdownMenuItem(
                                text        = { Text("Supprimer l'événement", fontSize = 14.sp, color = CrewUpRed) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Delete, contentDescription = null, tint = CrewUpRed, modifier = Modifier.size(18.dp))
                                },
                                onClick = { menuExpanded = false; onDeleteRequest() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteFriendDialog(
    event: EventSummary,
    friends: List<FriendEntry>,
    currentUid: String,
    onInvite: (FriendEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var invitedUids by remember { mutableStateOf(emptySet<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Ajouter un ami au crew", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(text = event.name, fontSize = 13.sp, color = CrewUpGrayMid)
            }
        },
        text = {
            if (friends.isEmpty()) {
                Text(
                    text     = "Tu n'as aucun ami à inviter. Ajoute des amis depuis ton profil.",
                    color    = CrewUpGrayMid,
                    fontSize = 14.sp
                )
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    friends.forEach { friend ->
                        val alreadyIn      = friend.uid in event.invitedFriends || friend.uid == event.organizerId
                        val alreadyInvited = friend.uid in invitedUids
                        val disabled       = alreadyIn || alreadyInvited

                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .alpha(if (alreadyIn) 0.4f else 1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MiniAvatar(pseudo = friend.pseudo, photoBase64 = friend.photoBase64, size = 36)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = friend.pseudo,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color      = CrewUpBlack
                                )
                                when {
                                    alreadyIn      -> Text("Déjà participant", fontSize = 11.sp, color = CrewUpGrayMid)
                                    alreadyInvited -> Text("Invitation envoyée", fontSize = 11.sp, color = CrewUpGreen)
                                }
                            }
                            OutlinedButton(
                                onClick        = { onInvite(friend); invitedUids = invitedUids + friend.uid },
                                enabled        = !disabled,
                                shape          = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text     = when {
                                        alreadyIn      -> "Présent"
                                        alreadyInvited -> "Invité"
                                        else           -> "Inviter"
                                    },
                                    fontSize = 12.sp,
                                    color    = if (disabled) CrewUpGrayMid else CrewUpBlack
                                )
                            }
                        }
                        HorizontalDivider(color = CrewUpDivider)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = CrewUpBlack, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = Color.White,
        shape          = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun EmptyCrewsState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 40.dp)) {
            Text(text = "Aucun crew pour l'instant", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrewUpBlack)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Rejoins ou crée un événement pour commencer", fontSize = 13.sp, color = CrewUpGrayMid, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun EmptyCreesState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 40.dp)) {
            Text(text = "Tu n'as pas encore créé de crew", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrewUpBlack)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Appuie sur + pour créer ton premier événement", fontSize = 13.sp, color = CrewUpGrayMid, textAlign = TextAlign.Center)
        }
    }
}
