package com.crewup.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.crewup.app.ui.navigation.Screen
import com.crewup.app.ui.theme.*
import com.crewup.app.ui.viewmodel.CreateEventViewModel
import com.crewup.app.ui.viewmodel.NominatimResult
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStep2Screen(
    navController: NavHostController,
    viewModel: CreateEventViewModel
) {
    val draft            by viewModel.draft.collectAsStateWithLifecycle()
    val nominatimResults by viewModel.nominatimResults.collectAsStateWithLifecycle()
    val isSearching      by viewModel.isSearching.collectAsStateWithLifecycle()

    var dateSlotCount    by remember { mutableStateOf(minOf(3, maxOf(1, draft.dates.size))) }
    var editingSlotIndex by remember { mutableStateOf<Int?>(null) }
    val datePickerState  = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    var addressQuery    by remember { mutableStateOf(draft.address) }
    var showSuggestions by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEE d MMM yyyy", Locale.FRENCH) }

    val canProceed = draft.dates.isNotEmpty()
            && draft.address.isNotBlank()
            && draft.maxParticipants.isNotBlank()

    //champ adresse si tap sur carte déclenche géocodage inversé
    LaunchedEffect(draft.address) {
        if (draft.address.isNotBlank() && addressQuery != draft.address) {
            addressQuery = draft.address
        }
    }

    LaunchedEffect(nominatimResults) { showSuggestions = nominatimResults.isNotEmpty() }

    if (editingSlotIndex != null) {
        DatePickerDialog(
            onDismissRequest = { editingSlotIndex = null },
            confirmButton = {
                TextButton(onClick = {
                    val idx    = editingSlotIndex ?: return@TextButton
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    viewModel.replaceDate(idx, millis)
                    editingSlotIndex = null
                }) { Text("OK", color = CrewUpBlack, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { editingSlotIndex = null }) {
                    Text("Annuler", color = CrewUpGrayMid)
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier          = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = "Date & lieu",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                color      = CrewUpBlack
            )
        }

        CreationStepBar(currentStep = 2)

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            // Dates
            Text(
                text       = "Dates proposées",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = CrewUpBlack
            )
            Spacer(modifier = Modifier.height(10.dp))

            for (slotIndex in 0 until dateSlotCount) {
                val savedMillis = draft.dates.getOrNull(slotIndex)
                if (slotIndex > 0) Spacer(modifier = Modifier.height(8.dp))
                DateSlotRow(
                    dateText = savedMillis?.let { dateFormatter.format(Date(it)) },
                    onTap    = { editingSlotIndex = slotIndex },
                    onRemove = if (savedMillis != null) {
                        {
                            viewModel.clearDate(slotIndex)
                            if (slotIndex == dateSlotCount - 1 && dateSlotCount > 1) {
                                dateSlotCount--
                            }
                        }
                    } else null
                )
            }

            if (dateSlotCount < 3) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clickable { dateSlotCount++ }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Add,
                        contentDescription = null,
                        tint               = CrewUpBlack,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text       = "Ajouter une date alternative",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = CrewUpBlack
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                color    = CrewUpDivider
            )

            // Lieu
            Text(
                text       = "Lieu",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = CrewUpBlack
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Champ adresse
            OutlinedTextField(
                value         = addressQuery,
                onValueChange = {
                    addressQuery = it
                    if (it.length >= 3) viewModel.searchAddress(it)
                    else viewModel.clearSuggestions()
                },
                placeholder  = { Text("Ex: parc de la Beaujoire", color = CrewUpGrayMid) },
                leadingIcon  = {
                    Icon(
                        imageVector        = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint               = CrewUpRed
                    )
                },
                trailingIcon = if (isSearching) {
                    { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) }
                } else null,
                modifier   = Modifier.fillMaxWidth(),
                shape      = RoundedCornerShape(12.dp),
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor    = CrewUpDivider,
                    focusedBorderColor      = CrewUpBlack,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor   = Color.White
                )
            )

            // Suggestions d'adresse en-dessous du champ
            if (showSuggestions && nominatimResults.isNotEmpty()) {
                Surface(
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                    color           = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Column {
                        nominatimResults.forEach { result ->
                            NominatimRow(
                                result  = result,
                                onClick = {
                                    addressQuery    = result.displayName
                                    showSuggestions = false
                                    viewModel.selectAddress(result)
                                }
                            )
                            HorizontalDivider(color = CrewUpDivider)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Carte OSM , taille fixe
            OsmMapView(
                lat      = draft.lat,
                lon      = draft.lon,
                onMapTap = { lat, lon -> viewModel.selectLocationOnMap(lat, lon) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                color    = CrewUpDivider
            )

            // Max participants
            Text(
                text       = "Max participants",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = CrewUpBlack
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value         = draft.maxParticipants,
                onValueChange = { if (it.length <= 4) viewModel.setMaxParticipants(it) },
                placeholder   = { Text("Ex: 5 personnes", color = CrewUpGrayMid) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor    = CrewUpDivider,
                    focusedBorderColor      = CrewUpBlack,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor   = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = { navController.popBackStack() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = CrewUpBlack),
                    border   = BorderStroke(1.5.dp, CrewUpBlack)
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Retour", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick  = { navController.navigate(Screen.CreateStep3.route) },
                    enabled  = canProceed,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = CrewUpGold,
                        contentColor           = CrewUpBlack,
                        disabledContainerColor = CrewUpGrayMid,
                        disabledContentColor   = Color.White
                    )
                ) {
                    Text(text = "Suivant", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DateSlotRow(
    dateText: String?,
    onTap: () -> Unit,
    onRemove: (() -> Unit)?
) {
    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(12.dp),
        color           = Color.White,
        shadowElevation = 1.dp,
        onClick         = onTap
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = dateText ?: "Sélectionner une date...",
                fontSize = 14.sp,
                color    = if (dateText != null) CrewUpBlack else CrewUpGrayMid,
                modifier = Modifier.weight(1f)
            )
            if (onRemove != null) {
                IconButton(
                    onClick  = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Close,
                        contentDescription = "Supprimer",
                        tint               = CrewUpGrayMid,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector        = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint               = CrewUpGrayMid,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun NominatimRow(result: NominatimResult, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text       = result.shortName,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium,
            color      = CrewUpBlack
        )
        Text(
            text     = result.displayName,
            fontSize = 12.sp,
            color    = CrewUpGrayMid,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OsmMapView(
    lat: Double?,
    lon: Double?,
    onMapTap: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", 0))
            userAgentValue = "CrewUp/1.0"
        }
        MapView(context).apply {
            // MATCH_PARENT pour que la vue respecte les contraintes Compose
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(false)
            controller.setZoom(5.5)
            controller.setCenter(GeoPoint(46.2276, 2.2137))
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    //  conteneur de taille fixe, clip du rendu Android View
    Box(modifier = modifier) {
        AndroidView(
            factory  = { mapView },
            modifier = Modifier.fillMaxSize(),
            update   = { mv ->
                mv.overlays.removeAll { it is Marker }
                if (lat != null && lon != null) {
                    mv.overlays.add(
                        Marker(mv).apply {
                            position = GeoPoint(lat, lon)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                    )
                    mv.controller.setZoom(15.0)
                    mv.controller.setCenter(GeoPoint(lat, lon))
                }
                mv.invalidate()
            }
        )

        // Overlay Compose transparent : les taps sont interceptés et convertis

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val geoPoint = mapView.projection.fromPixels(
                            offset.x.toInt(),
                            offset.y.toInt()
                        )
                        onMapTap(geoPoint.latitude, geoPoint.longitude)
                    }
                }
        )
    }
}
