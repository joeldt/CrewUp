package com.crewup.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.crewup.app.ui.components.BottomNavBar
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.CreateEventViewModel

private data class ActivityItem(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val key: String
)

private val activities = listOf(
    ActivityItem(Icons.Filled.FitnessCenter,  "Sport",      Color(0xFFC62828), "sport"),
    ActivityItem(Icons.Filled.MusicNote,      "Musique",    Color(0xFF6A1B9A), "musique"),
    ActivityItem(Icons.Filled.Movie,          "Cinéma",     Color(0xFF8D3B2B), "cinéma"),
    ActivityItem(Icons.Filled.Hiking,         "Randonnée",  Color(0xFF4A5D23), "randonnée"),
    ActivityItem(Icons.Filled.Celebration,    "Fête",       Color(0xFFAD1457), "fête"),
    ActivityItem(Icons.Filled.Restaurant,     "Restaurant", Color(0xFF00838F), "restaurant"),
    ActivityItem(Icons.Filled.OutdoorGrill,   "BBQ",        Color(0xFF607D8B), "bbq"),
    ActivityItem(Icons.Filled.SportsEsports,  "Jeux",       Color(0xFF37474F), "jeux")
)

@Composable
fun CreateStep1Screen(
    navController: NavHostController,
    viewModel: CreateEventViewModel
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar      = { BottomNavBar(navController) },
        containerColor = CrewUpGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier          = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
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
                    text       = "Nouveau Crew",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Black,
                    color      = CrewUpBlack,
                    modifier   = Modifier.weight(1f)
                )
            }

            CreationStepBar(currentStep = 1)

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                Text(
                    text       = "Type d'activités",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = CrewUpBlack
                )

                Spacer(modifier = Modifier.height(12.dp))

                activities.chunked(4).forEachIndexed { rowIndex, row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEachIndexed { colIndex, activity ->
                            val index      = rowIndex * 4 + colIndex
                            val isSelected = draft.activityType == index
                            Box(
                                modifier         = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(activity.color)
                                    .then(
                                        if (isSelected)
                                            Modifier.border(3.dp, Color.White, RoundedCornerShape(12.dp))
                                        else Modifier
                                    )
                                    .clickable { viewModel.setActivityType(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier            = Modifier.padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector        = activity.icon,
                                        contentDescription = activity.label,
                                        tint               = Color.White,
                                        modifier           = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text       = activity.label,
                                        color      = Color.White,
                                        fontSize   = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign  = TextAlign.Center,
                                        maxLines   = 1
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text       = "Nom de l'évènement",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = CrewUpBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value         = draft.name,
                    onValueChange = { viewModel.setName(it) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor    = CrewUpDivider,
                        focusedBorderColor      = CrewUpBlack,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor   = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text       = "Description (optionnel)",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = CrewUpBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value         = draft.description,
                    onValueChange = { viewModel.setDescription(it) },
                    placeholder   = { Text("Dites en plus sur l'évènement...", color = CrewUpGrayMid) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape         = RoundedCornerShape(12.dp),
                    maxLines      = 5,
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor    = CrewUpDivider,
                        focusedBorderColor      = CrewUpBlack,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor   = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick  = { navController.navigate(Screen.CreateStep2.route) },
                    enabled  = draft.activityType >= 0 && draft.name.isNotBlank(),
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
                    Text(text = "Suivant", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
internal fun CreationStepBar(currentStep: Int) {
    val steps = listOf("concept", "Infos", "Equipe")
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        steps.forEachIndexed { index, label ->
            val step     = index + 1
            val isActive = step == currentStep
            val isDone   = step < currentStep
            Column(
                modifier            = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isActive) 3.dp else 2.dp)
                        .background(
                            color = when {
                                isActive -> CrewUpBlack
                                isDone   -> CrewUpBlack
                                else     -> CrewUpDivider
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = label,
                    fontSize   = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color      = if (isActive || isDone) CrewUpBlack else CrewUpGrayMid
                )
            }
        }
    }
}
