package com.crewup.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.crewup.app.ui.components.BottomNavBar
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.AppNotification
import com.crewup.app.ui.viewmodel.FriendEntry
import com.crewup.app.ui.viewmodel.FriendsViewModel
import com.crewup.app.ui.viewmodel.NotificationsViewModel

@Composable
fun NotificationsScreen(
    navController: NavHostController,
    viewModel: FriendsViewModel,
    notificationsViewModel: NotificationsViewModel
) {
    val pendingReceived by viewModel.pendingReceived.collectAsStateWithLifecycle()
    val crewInvites    by notificationsViewModel.crewInvites.collectAsStateWithLifecycle()
    val deletedEvents  by notificationsViewModel.deletedEvents.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadFriends()
        notificationsViewModel.loadNotifications()
    }

    val hasNotifications = pendingReceived.isNotEmpty() || crewInvites.isNotEmpty() || deletedEvents.isNotEmpty()

    Scaffold(
        bottomBar      = { BottomNavBar(navController) },
        containerColor = CrewUpGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text       = "Notifications",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                color      = CrewUpBlack,
                modifier   = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )

            if (!hasNotifications) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(horizontal = 40.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint               = CrewUpGrayMid,
                            modifier           = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text       = "Aucune notification",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = CrewUpBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text      = "Les demandes d'amis et alertes apparaîtront ici",
                            fontSize  = 13.sp,
                            color     = CrewUpGrayMid,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // Demandes d'amis
                    if (pendingReceived.isNotEmpty()) {
                        SectionTitle("Demandes d'amis (${pendingReceived.size})")
                        pendingReceived.forEach { entry ->
                            NotificationFriendCard(
                                entry     = entry,
                                onAccept  = { viewModel.acceptFriendRequest(entry.uid) },
                                onDecline = { viewModel.declineFriendRequest(entry.uid) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Invitations de crew
                    if (crewInvites.isNotEmpty()) {
                        SectionTitle("Invitations de crew (${crewInvites.size})")
                        crewInvites.forEach { notif ->
                            CrewInviteCard(
                                notif     = notif,
                                onAccept  = { notificationsViewModel.acceptCrewInvite(notif) },
                                onDecline = { notificationsViewModel.declineCrewInvite(notif.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Événements supprimés
                    if (deletedEvents.isNotEmpty()) {
                        SectionTitle("Événements supprimés (${deletedEvents.size})")
                        deletedEvents.forEach { notif ->
                            DeletedEventCard(
                                notif     = notif,
                                onDismiss = { notificationsViewModel.dismissNotification(notif.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text       = text,
        fontSize   = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color      = CrewUpBlack,
        modifier   = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun NotificationFriendCard(
    entry: FriendEntry,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(14.dp),
        color           = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniAvatar(pseudo = entry.pseudo, photoBase64 = entry.photoBase64, size = 44)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entry.pseudo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CrewUpBlack)
                Text(text = "veut être ami avec toi", fontSize = 12.sp, color = CrewUpGrayMid)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onAccept, modifier = Modifier.size(38.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = CrewUpGreen) {
                        Icon(Icons.Filled.Check, contentDescription = "Accepter", tint = Color.White, modifier = Modifier.padding(6.dp).size(18.dp))
                    }
                }
                IconButton(onClick = onDecline, modifier = Modifier.size(38.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = CrewUpDivider) {
                        Icon(Icons.Filled.Close, contentDescription = "Refuser", tint = CrewUpGrayMid, modifier = Modifier.padding(6.dp).size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CrewInviteCard(
    notif: AppNotification,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(14.dp),
        color           = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = "${notif.fromPseudo} t'invite à rejoindre",
                fontSize   = 13.sp,
                color      = CrewUpGrayMid
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text       = notif.eventName,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Black,
                color      = CrewUpBlack
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick  = onAccept,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(containerColor = CrewUpBlack, contentColor = Color.White),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Text("Rejoindre", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick  = onDecline,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Text("Décliner", color = CrewUpGrayMid, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DeletedEventCard(
    notif: AppNotification,
    onDismiss: () -> Unit
) {
    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(14.dp),
        color           = Color(0xFFFFF8F8),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Filled.Warning,
                contentDescription = null,
                tint               = CrewUpRed,
                modifier           = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "\"${notif.eventName}\" a été supprimé",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = CrewUpBlack
                )
                Text(
                    text     = "par ${notif.fromPseudo}",
                    fontSize = 12.sp,
                    color    = CrewUpGrayMid
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector        = Icons.Filled.Close,
                    contentDescription = "Fermer",
                    tint               = CrewUpGrayMid,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}
