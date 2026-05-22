package com.crewup.app.ui.screens.home

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.ProfileEditState
import com.crewup.app.ui.viewmodel.ProfileUiState
import com.crewup.app.ui.viewmodel.ProfileViewModel
import java.io.File

@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context   = LocalContext.current
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()

    val currentData = (uiState as? ProfileUiState.Success)?.data

    var pseudo   by remember(currentData) { mutableStateOf(currentData?.pseudo ?: "") }
    var ville    by remember(currentData) { mutableStateOf(currentData?.ville ?: "") }
    val selected  = remember(currentData) { mutableStateListOf<String>().also { list ->
        list.addAll(currentData?.activites ?: emptyList())
    } }
    var photoUri         by remember { mutableStateOf<Uri?>(null) }
    var showPhotoDialog  by remember { mutableStateOf(false) }
    var cameraImageUri   by remember { mutableStateOf<Uri?>(null) }

    val activites = listOf("⚽", "🍕", "🎬", "🎸", "🏞️", "🎮", "🏊", "🎭")
    val isLoading = editState is ProfileEditState.Loading
    val errorMsg  = (editState as? ProfileEditState.Error)?.message

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photoUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) photoUri = cameraImageUri }

    LaunchedEffect(editState) {
        if (editState is ProfileEditState.Success) {
            viewModel.resetEditState()
            navController.popBackStack()
        }
    }

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Photo de profil", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPhotoDialog = false
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Choisir depuis la galerie", color = CrewUpBlack) }
                    TextButton(
                        onClick = {
                            showPhotoDialog = false
                            val file = File.createTempFile("profile_", ".jpg", context.cacheDir)
                            val uri  = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Prendre une photo", color = CrewUpBlack) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text("Annuler", color = CrewUpGrayMid)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewUpWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }, enabled = !isLoading) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint               = CrewUpBlack
                )
            }
            Text(
                text       = "Modifier le profil",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = CrewUpBlack
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier         = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(CrewUpGray)
                .border(2.dp, CrewUpDivider, CircleShape)
                .clickable(enabled = !isLoading) { showPhotoDialog = true },
            contentAlignment = Alignment.Center
        ) {
            when {
                photoUri != null -> AsyncImage(
                    model              = photoUri,
                    contentDescription = "Photo de profil",
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
                currentData?.photoBase64 != null -> AsyncImage(
                    model              = Base64.decode(currentData.photoBase64, Base64.NO_WRAP),
                    contentDescription = "Photo de profil",
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
                else -> Text(text = "+", fontSize = 28.sp, color = CrewUpGrayMid)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Appuyer pour changer la photo", fontSize = 13.sp, color = CrewUpGrayMid)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value         = pseudo,
            onValueChange = { pseudo = it; if (errorMsg != null) viewModel.resetEditState() },
            label         = { Text("Pseudo") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true,
            enabled       = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value         = ville,
            onValueChange = { ville = it; if (errorMsg != null) viewModel.resetEditState() },
            label         = { Text("Ville") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true,
            enabled       = !isLoading
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text       = "Activités préférées :",
            fontSize   = 15.sp,
            fontWeight = FontWeight.Medium,
            color      = CrewUpBlack,
            modifier   = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                            .clickable(enabled = !isLoading) {
                                if (isSelected) selected.remove(emoji) else selected.add(emoji)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = errorMsg,
                color     = MaterialTheme.colorScheme.error,
                fontSize  = 13.sp,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick  = { viewModel.updateProfile(pseudo, ville, selected.toList(), photoUri) },
            enabled  = !isLoading && pseudo.isNotBlank() && ville.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor         = CrewUpBlack,
                contentColor           = CrewUpWhite,
                disabledContainerColor = CrewUpGrayMid
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(text = "Enregistrer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
