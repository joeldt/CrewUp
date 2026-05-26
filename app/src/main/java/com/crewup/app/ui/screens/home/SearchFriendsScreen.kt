package com.crewup.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.FriendEntry
import com.crewup.app.ui.viewmodel.FriendStatus
import com.crewup.app.ui.viewmodel.FriendsViewModel
import com.crewup.app.ui.viewmodel.UserSearchResult

@Composable
fun SearchFriendsScreen(
    navController: NavHostController,
    viewModel: FriendsViewModel
) {
    val searchResults   by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching     by viewModel.isSearching.collectAsStateWithLifecycle()
    val friends         by viewModel.friends.collectAsStateWithLifecycle()
    val pendingReceived by viewModel.pendingReceived.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadFriends() }

    Scaffold(containerColor = CrewUpGray) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier          = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint               = CrewUpBlack
                    )
                }
                Text(
                    text       = "Trouver des amis",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Black,
                    color      = CrewUpBlack
                )
            }

            // Barre de recherche
            OutlinedTextField(
                value         = query,
                onValueChange = {
                    query = it
                    viewModel.searchUsers(it)
                },
                placeholder  = { Text("Rechercher par pseudo...", color = CrewUpGrayMid) },
                leadingIcon  = {
                    Icon(
                        imageVector        = Icons.Filled.Search,
                        contentDescription = null,
                        tint               = CrewUpGrayMid
                    )
                },
                trailingIcon = if (isSearching) {
                    { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) }
                } else if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { query = ""; viewModel.clearSearch() }) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = CrewUpGrayMid)
                        }
                    }
                } else null,
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape      = RoundedCornerShape(24.dp),
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor   = Color.White,
                    unfocusedBorderColor    = CrewUpDivider,
                    focusedBorderColor      = CrewUpBlack
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                if (query.length >= 2) {
                    // Résultats de recherche
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text       = "Résultats",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = CrewUpGrayMid,
                        modifier   = Modifier.padding(vertical = 8.dp)
                    )
                    if (searchResults.isEmpty() && !isSearching) {
                        Text(
                            text     = "Aucun utilisateur trouvé",
                            fontSize = 14.sp,
                            color    = CrewUpGrayMid,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    searchResults.forEach { result ->
                        SearchResultRow(
                            result          = result,
                            onAdd           = { viewModel.sendFriendRequest(result.uid) },
                            onAccept        = { viewModel.acceptFriendRequest(result.uid) },
                            onDecline       = { viewModel.declineFriendRequest(result.uid) }
                        )
                        HorizontalDivider(color = CrewUpDivider)
                    }
                } else {
                    // Demandes reçues
                    if (pendingReceived.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text       = "Demandes reçues (${pendingReceived.size})",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = CrewUpBlack,
                            modifier   = Modifier.padding(bottom = 8.dp)
                        )
                        pendingReceived.forEach { entry ->
                            PendingRequestRow(
                                entry     = entry,
                                onAccept  = { viewModel.acceptFriendRequest(entry.uid) },
                                onDecline = { viewModel.declineFriendRequest(entry.uid) }
                            )
                            HorizontalDivider(color = CrewUpDivider)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Liste des amis
                    Text(
                        text       = "Mes amis (${friends.size})",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = CrewUpBlack,
                        modifier   = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    )
                    if (friends.isEmpty()) {
                        Text(
                            text     = "Tu n'as pas encore d'amis. Recherche par pseudo pour en ajouter !",
                            fontSize = 13.sp,
                            color    = CrewUpGrayMid,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    friends.forEach { entry ->
                        FriendRow(entry = entry)
                        HorizontalDivider(color = CrewUpDivider)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    result: UserSearchResult,
    onAdd: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniAvatar(pseudo = result.pseudo, photoBase64 = result.photoBase64)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text       = result.pseudo,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Medium,
            color      = CrewUpBlack,
            modifier   = Modifier.weight(1f)
        )
        when (result.friendStatus) {
            FriendStatus.None -> {
                OutlinedButton(
                    onClick      = onAdd,
                    shape        = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Ajouter", fontSize = 12.sp, color = CrewUpBlack)
                }
            }
            FriendStatus.PendingSent -> {
                Text("En attente", fontSize = 12.sp, color = CrewUpGrayMid)
            }
            FriendStatus.PendingReceived -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick  = onAccept,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Accepter",
                            tint     = CrewUpGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick  = onDecline,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Refuser",
                            tint     = CrewUpRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            FriendStatus.Friend -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint     = CrewUpGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ami", fontSize = 12.sp, color = CrewUpGreen)
                }
            }
        }
    }
}

@Composable
private fun PendingRequestRow(
    entry: FriendEntry,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniAvatar(pseudo = entry.pseudo, photoBase64 = entry.photoBase64)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = entry.pseudo,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium,
                color      = CrewUpBlack
            )
            Text("veut être ami avec toi", fontSize = 12.sp, color = CrewUpGrayMid)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick        = onAccept,
                shape          = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors         = ButtonDefaults.buttonColors(
                    containerColor = CrewUpBlack,
                    contentColor   = Color.White
                )
            ) {
                Text("Accepter", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick        = onDecline,
                shape          = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Refuser", fontSize = 12.sp, color = CrewUpGrayMid)
            }
        }
    }
}

@Composable
private fun FriendRow(entry: FriendEntry) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniAvatar(pseudo = entry.pseudo, photoBase64 = entry.photoBase64)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text       = entry.pseudo,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Medium,
            color      = CrewUpBlack
        )
    }
}

@Composable
internal fun MiniAvatar(pseudo: String, photoBase64: String?, size: Int = 40) {
    Box(
        modifier         = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(CrewUpGrayMid),
        contentAlignment = Alignment.Center
    ) {
        // avatar : initiale si pas de photo (base64 → même logique que HubScreen)
        Text(
            text       = pseudo.firstOrNull()?.uppercase() ?: "?",
            fontSize   = (size / 2.5).sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White
        )
    }
}
