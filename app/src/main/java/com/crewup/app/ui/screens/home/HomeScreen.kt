package com.crewup.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.crewup.app.ui.viewmodel.HomeUiState
import com.crewup.app.ui.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val prenom = remember {
        FirebaseAuth.getInstance().currentUser?.displayName?.ifBlank { null } ?: "toi"
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadEvents() }

    Scaffold(
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
                0 -> MesCrewsTabContent(uiState = uiState, onEventClick = { eventId ->
                    navController.navigate(Screen.Hub.createRoute(eventId))
                })
                1 -> CreesTabContent(uiState = uiState, onEventClick = { eventId ->
                    navController.navigate(Screen.Hub.createRoute(eventId))
                })
            }
        }
    }
}

@Composable
private fun HomeHeader(
    selectedTab: Int,
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
                TabLabel(
                    text       = "Mes Crews",
                    isSelected = selectedTab == 0,
                    onClick    = { onTabSelected(0) }
                )
                Text(
                    text       = "  |  ",
                    fontSize   = 18.sp,
                    color      = CrewUpGrayMid,
                    fontWeight = FontWeight.Light
                )
                TabLabel(
                    text       = "Crées",
                    isSelected = selectedTab == 1,
                    onClick    = { onTabSelected(1) }
                )
            }

            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector        = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint               = CrewUpBlack,
                    modifier           = Modifier.size(26.dp)
                )
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
private fun MesCrewsTabContent(
    uiState: HomeUiState,
    onEventClick: (String) -> Unit
) {
    EventListContent(
        uiState      = uiState,
        getEvents    = { it.allEvents },
        emptyContent = { EmptyCrewsState() },
        onEventClick = onEventClick
    )
}

@Composable
private fun CreesTabContent(
    uiState: HomeUiState,
    onEventClick: (String) -> Unit
) {
    EventListContent(
        uiState      = uiState,
        getEvents    = { it.createdEvents },
        emptyContent = { EmptyCreesState() },
        onEventClick = onEventClick
    )
}

@Composable
private fun EventListContent(
    uiState: HomeUiState,
    getEvents: (HomeUiState.Success) -> List<EventSummary>,
    emptyContent: @Composable () -> Unit,
    onEventClick: (String) -> Unit
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
                        EventCard(event = event, onClick = { onEventClick(event.id) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: EventSummary, onClick: () -> Unit) {
    val isVote     = event.datesCount > 1
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
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = event.name,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = CrewUpBlack
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape    = CircleShape,
                        color    = statusColor
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text     = statusText,
                        fontSize = 12.sp,
                        color    = statusColor
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Filled.Person,
                    contentDescription = null,
                    tint               = CrewUpGrayMid,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text     = event.maxParticipants.toString(),
                    fontSize = 14.sp,
                    color    = CrewUpGrayMid
                )
            }
        }
    }
}

@Composable
private fun EmptyCrewsState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 40.dp)
        ) {
            Text(
                text       = "Aucun crew pour l'instant",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = CrewUpBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = "Rejoins ou crée un événement pour commencer",
                fontSize  = 13.sp,
                color     = CrewUpGrayMid,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyCreesState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(horizontal = 40.dp)
        ) {
            Text(
                text       = "Tu n'as pas encore créé de crew",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = CrewUpBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = "Appuie sur + pour créer ton premier événement",
                fontSize  = 13.sp,
                color     = CrewUpGrayMid,
                textAlign = TextAlign.Center
            )
        }
    }
}
