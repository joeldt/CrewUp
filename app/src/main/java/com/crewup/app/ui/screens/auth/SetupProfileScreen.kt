package com.crewup.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*


@Composable
fun SetupProfileScreen(navController: NavHostController) {
    var pseudo   by remember { mutableStateOf("") }
    var ville    by remember { mutableStateOf("") }
    val activites = listOf("⚽", "🍕", "🎬", "🎸", "🏞️", "🎮", "🏊", "🎭")
    val selected  = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewUpWhite)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier          = Modifier.fillMaxWidth(),
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
                text       = "Inscription",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = CrewUpBlack
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text     = "Terminez la configuration du profil",
            fontSize = 15.sp,
            color    = CrewUpGrayMid,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Avatar placeholder
        Box(
            modifier            = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(CrewUpGray)
                .border(2.dp, CrewUpDivider, CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "+", fontSize = 32.sp, color = CrewUpGrayMid)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text     = "Appuyer pour choisir une photo",
            fontSize = 13.sp,
            color    = CrewUpGrayMid
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value         = pseudo,
            onValueChange = { pseudo = it },
            placeholder   = { Text("Pseudo...") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value         = ville,
            onValueChange = { ville = it },
            placeholder   = { Text("Ville...") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text      = "Vos activités préférées :",
            fontSize  = 15.sp,
            fontWeight = FontWeight.Medium,
            color     = CrewUpBlack,
            modifier  = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grille d'activités
        activites.chunked(4).forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { emoji ->
                    val isSelected = emoji in selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CrewUpBlack else CrewUpGray)
                            .clickable {
                                if (isSelected) selected.remove(emoji)
                                else selected.add(emoji)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick  = { navController.navigate(Screen.Home.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CrewUpBlack,
                contentColor   = CrewUpWhite
            )
        ) {
            Text(text = "Terminer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}