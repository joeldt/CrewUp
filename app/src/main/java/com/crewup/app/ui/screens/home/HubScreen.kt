package com.crewup.app.ui.screens.home

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubScreen(
    navController: NavHostController,
    viewModel: HubViewModel = viewModel()
) {
    val event        by viewModel.event.collectAsStateWithLifecycle()
    val participants by viewModel.participants.collectAsStateWithLifecycle()
    val canFreeze    by viewModel.canFreeze.collectAsStateWithLifecycle()

    var selectedTab      by remember { mutableIntStateOf(0) }
    var showParticipants by remember { mutableStateOf(false) }

    Scaffold(containerColor = CrewUpGray) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HubHeader(
                eventName          = event.name,
                participants       = participants,
                onBack             = { navController.popBackStack() },
                onParticipantClick = { showParticipants = true }
            )

            HubTabBar(
                selectedTab   = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            when (selectedTab) {
                0 -> VoteTab(viewModel = viewModel, event = event, canFreeze = canFreeze)
                1 -> LogistiqueTab(viewModel = viewModel)
                2 -> ChatTab(viewModel = viewModel)
            }
        }
    }

    if (showParticipants) {
        ModalBottomSheet(
            onDismissRequest = { showParticipants = false },
            containerColor   = Color.White,
            dragHandle       = { BottomSheetDefaults.DragHandle() }
        ) {
            ParticipantsList(participants = participants)
        }
    }
}

//HEADER

@Composable
private fun HubHeader(
    eventName: String,
    participants: List<ParticipantProfile>,
    onBack: () -> Unit,
    onParticipantClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 4.dp, end = 8.dp, top = 4.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint               = CrewUpBlack
                )
            }
            Text(
                text       = eventName,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Black,
                color      = CrewUpBlack,
                modifier   = Modifier.weight(1f)
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector        = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint               = CrewUpBlack,
                    modifier           = Modifier.size(22.dp)
                )
            }
        }

        if (participants.isNotEmpty()) {
            Row(
                modifier          = Modifier
                    .padding(start = 16.dp, bottom = 8.dp)
                    .clickable { onParticipantClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val shown    = participants.take(3)
                val overflow = participants.size - shown.size

                shown.forEachIndexed { i, p ->
                    Box(modifier = Modifier.offset(x = (-8 * i).dp)) {
                        ParticipantAvatar(profile = p, size = 34)
                    }
                }

                if (overflow > 0) {
                    Spacer(modifier = Modifier.width((8 + 4).dp))
                    Text(
                        text       = "+$overflow",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = CrewUpBlack
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantAvatar(profile: ParticipantProfile, size: Int) {
    val bitmap = remember(profile.photoBase64) {
        profile.photoBase64?.let { b64 ->
            runCatching {
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.getOrNull()
        }
    }
    Box(
        modifier         = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(CrewUpGrayMid),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap             = bitmap,
                contentDescription = profile.pseudo,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
        } else {
            Text(
                text       = profile.pseudo.firstOrNull()?.uppercase() ?: "?",
                fontSize   = (size / 2.6).sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }
    }
}

//tabs bar

@Composable
private fun HubTabBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("votes", "Logistique", "Chat crew")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedTab
            Surface(
                onClick = { onTabSelected(index) },
                shape   = RoundedCornerShape(50),
                color   = if (isSelected) CrewUpBlack else Color.White,
                border  = if (!isSelected) BorderStroke(1.dp, CrewUpDivider) else null
            ) {
                Text(
                    text       = label,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (isSelected) Color.White else CrewUpGrayMid,
                    modifier   = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

// vote tab

@Composable
private fun VoteTab(
    viewModel: HubViewModel,
    event: EventDetail,
    canFreeze: Boolean
) {
    val dateFormatter     = remember { SimpleDateFormat("EEEE d MMMM", Locale.FRENCH) }
    val myVote            = event.votes[viewModel.currentUid]
    val isAbsent          = viewModel.currentUid in event.absent
    val isConfirmed       = event.confirmedDateIndex != null
    val isCreator         = event.organizerId == viewModel.currentUid
    val totalParticipants = maxOf(1, 1 + event.invitedFriends.size)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text     = "voter pour une date",
            fontSize = 14.sp,
            color    = CrewUpGrayMid,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        if (isConfirmed) {
            val millis = event.dates.getOrNull(event.confirmedDateIndex!!)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                color    = Color.White,
                border   = BorderStroke(2.dp, CrewUpBlack)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text       = millis?.let { dateFormatter.format(Date(it)) } ?: "—",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Black,
                        color      = CrewUpBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text       = "OK",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = CrewUpGreen
                    )
                }
            }
        } else {
            event.dates.forEachIndexed { index, dateMillis ->
                val voteCount = event.votes.values.count { it == index }
                DateVoteCard(
                    dateText  = dateFormatter.format(Date(dateMillis)),
                    voteCount = voteCount,
                    total     = totalParticipants,
                    isMyVote  = myVote == index,
                    onClick   = { viewModel.voteForDate(index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = {},
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = BorderStroke(
                        1.5.dp,
                        if (myVote != null) CrewUpBlack else CrewUpDivider
                    ),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (myVote != null) CrewUpBlack else CrewUpGrayMid
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Check,
                        contentDescription = null,
                        modifier           = Modifier.size(15.dp),
                        tint               = if (myVote != null) CrewUpGold else CrewUpGrayMid
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("je vote", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick  = { viewModel.toggleAbsent() },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = BorderStroke(
                        1.5.dp,
                        if (isAbsent) CrewUpRed else CrewUpDivider
                    ),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isAbsent) CrewUpRed else CrewUpGrayMid
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Close,
                        contentDescription = null,
                        modifier           = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Absent", fontSize = 13.sp)
                }
            }

            if (isCreator) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick  = { viewModel.freezeVotes() },
                    enabled  = canFreeze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = CrewUpBlack,
                        contentColor           = Color.White,
                        disabledContainerColor = CrewUpDivider,
                        disabledContentColor   = CrewUpGrayMid
                    )
                ) {
                    Text(
                        text       = "Figer les choix",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DateVoteCard(
    dateText: String,
    voteCount: Int,
    total: Int,
    isMyVote: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape   = RoundedCornerShape(14.dp),
        color   = Color.White,
        border  = BorderStroke(
            width = if (isMyVote) 2.dp else 1.dp,
            color = if (isMyVote) CrewUpGold else CrewUpDivider
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text       = dateText,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Black,
                color      = CrewUpBlack
            )
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress          = { if (total > 0) voteCount.toFloat() / total else 0f },
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color             = CrewUpGold,
                trackColor        = CrewUpDivider,
                drawStopIndicator = {}
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text     = "$voteCount/$total votes",
                fontSize = 12.sp,
                color    = CrewUpGrayMid
            )
        }
    }
}

// tab logistique

@Composable
private fun LogistiqueTab(viewModel: HubViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var showAddTask   by remember { mutableStateOf(false) }
    var confirmTaskId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text       = "Tâches",
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = CrewUpBlack,
            modifier   = Modifier.padding(vertical = 12.dp)
        )

        tasks.forEach { task ->
            TaskRow(
                task    = task,
                onCheck = { if (!task.isDone) confirmTaskId = task.id }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bouton ajouter (style pointillé simulé)
        Surface(
            onClick = { showAddTask = true },
            shape   = RoundedCornerShape(12.dp),
            color   = Color.Transparent,
            border  = BorderStroke(1.dp, CrewUpGrayMid)
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.Add,
                    contentDescription = null,
                    tint               = CrewUpGrayMid,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text     = " Ajouter une tâche",
                    fontSize = 14.sp,
                    color    = CrewUpGrayMid
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showAddTask) {
        AddTaskDialog(
            onConfirm = { text, assignedTo ->
                viewModel.addTask(text, assignedTo)
                showAddTask = false
            },
            onDismiss = { showAddTask = false }
        )
    }

    if (confirmTaskId != null) {
        AlertDialog(
            onDismissRequest = { confirmTaskId = null },
            title            = { Text("Confirmation", fontWeight = FontWeight.Bold) },
            text             = { Text("Marquer la tâche comme effectuée ?") },
            confirmButton    = {
                TextButton(onClick = {
                    viewModel.completeTask(confirmTaskId!!)
                    confirmTaskId = null
                }) { Text("Oui", color = CrewUpBlack, fontWeight = FontWeight.Bold) }
            },
            dismissButton    = {
                TextButton(onClick = { confirmTaskId = null }) {
                    Text("Annuler", color = CrewUpGrayMid)
                }
            }
        )
    }
}

@Composable
private fun TaskRow(task: TaskItem, onCheck: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked         = task.isDone,
            onCheckedChange = { if (!task.isDone) onCheck() },
            colors          = CheckboxDefaults.colors(
                checkedColor         = CrewUpBlack,
                uncheckedColor       = CrewUpGrayMid,
                disabledCheckedColor = CrewUpGrayMid
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        val label = if (task.assignedTo.isBlank()) task.text
                    else "${task.text} - ${task.assignedTo}"
        Text(
            text           = label,
            fontSize       = 14.sp,
            color          = if (task.isDone) CrewUpGrayMid else CrewUpBlack,
            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
            modifier       = Modifier.alpha(if (task.isDone) 0.45f else 1f)
        )
    }
}

@Composable
private fun AddTaskDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var text       by remember { mutableStateOf("") }
    var assignedTo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text("Nouvelle tâche", fontWeight = FontWeight.Bold) },
        text             = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = text,
                    onValueChange = { text = it },
                    placeholder   = { Text("Que faut-il prévoir ?", color = CrewUpGrayMid) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value         = assignedTo,
                    onValueChange = { assignedTo = it },
                    placeholder   = { Text("Qui s'en charge ? (optionnel)", color = CrewUpGrayMid) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton    = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text, assignedTo) },
                enabled = text.isNotBlank()
            ) { Text("Ajouter", color = CrewUpBlack, fontWeight = FontWeight.Bold) }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = CrewUpGrayMid) }
        }
    )
}

// chat tab

@Composable
private fun ChatTab(viewModel: HubViewModel) {
    val messages  by viewModel.messages.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state          = listState,
            modifier       = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    isMe    = msg.uid == viewModel.currentUid
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        HorizontalDivider(color = CrewUpDivider)

        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value         = inputText,
                onValueChange = { inputText = it },
                placeholder   = { Text("écrire un message...", color = CrewUpGrayMid, fontSize = 14.sp) },
                modifier      = Modifier.weight(1f),
                shape         = RoundedCornerShape(24.dp),
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor   = Color.White,
                    unfocusedBorderColor    = CrewUpDivider,
                    focusedBorderColor      = CrewUpBlack
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick  = {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                },
                enabled  = inputText.isNotBlank()
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Envoyer",
                    tint               = if (inputText.isNotBlank()) CrewUpGold else CrewUpGrayMid,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage, isMe: Boolean) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Text(
                text       = message.displayName,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = CrewUpGrayMid,
                modifier   = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        Surface(
            shape = RoundedCornerShape(
                topStart    = if (isMe) 16.dp else 4.dp,
                topEnd      = if (isMe) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd   = 16.dp
            ),
            color           = if (isMe) CrewUpGold else Color.White,
            shadowElevation = 1.dp
        ) {
            Text(
                text     = message.text,
                fontSize = 14.sp,
                color    = CrewUpBlack,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

//liste des participants

@Composable
private fun ParticipantsList(participants: List<ParticipantProfile>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text       = "Participants (${participants.size})",
            fontSize   = 17.sp,
            fontWeight = FontWeight.Black,
            color      = CrewUpBlack,
            modifier   = Modifier.padding(bottom = 16.dp)
        )
        participants.forEach { p ->
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ParticipantAvatar(profile = p, size = 42)
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text       = p.pseudo,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = CrewUpBlack
                )
            }
            HorizontalDivider(color = CrewUpDivider)
        }
    }
}
