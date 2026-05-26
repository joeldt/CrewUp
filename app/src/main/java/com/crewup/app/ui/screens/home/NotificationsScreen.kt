package com.crewup.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
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
import com.crewup.app.ui.viewmodel.FriendEntry
import com.crewup.app.ui.viewmodel.FriendsViewModel

@Composable
fun NotificationsScreen(
    navController: NavHostController,
    viewModel: FriendsViewModel
) {
    val pendingReceived by viewModel.pendingReceived.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadFriends() }

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

            if (pendingReceived.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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
                            text      = "Aucune notification",
                            fontSize  = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color     = CrewUpBlack
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
                    Text(
                        text       = "Demandes d'amis (${pendingReceived.size})",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = CrewUpBlack,
                        modifier   = Modifier.padding(vertical = 8.dp)
                    )

                    pendingReceived.forEach { entry ->
                        NotificationFriendCard(
                            entry     = entry,
                            onAccept  = { viewModel.acceptFriendRequest(entry.uid) },
                            onDecline = { viewModel.declineFriendRequest(entry.uid) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
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
                Text(
                    text       = entry.pseudo,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = CrewUpBlack
                )
                Text(
                    text     = "veut être ami avec toi",
                    fontSize = 12.sp,
                    color    = CrewUpGrayMid
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick  = onAccept,
                    modifier = Modifier.size(38.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CrewUpGreen
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Check,
                            contentDescription = "Accepter",
                            tint               = Color.White,
                            modifier           = Modifier
                                .padding(6.dp)
                                .size(18.dp)
                        )
                    }
                }
                IconButton(
                    onClick  = onDecline,
                    modifier = Modifier.size(38.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CrewUpDivider
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Close,
                            contentDescription = "Refuser",
                            tint               = CrewUpGrayMid,
                            modifier           = Modifier
                                .padding(6.dp)
                                .size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
