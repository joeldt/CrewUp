package com.crewup.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.PopupProperties
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
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
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

    var showDatePicker  by remember { mutableStateOf(false) }
    var addressQuery    by remember { mutableStateOf(draft.address) }
    var showSuggestions by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEE d MMM yyyy", Locale.FRENCH) }

    LaunchedEffect(nominatimResults) { showSuggestions = nominatimResults.isNotEmpty() }

    // DatePickerState
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { viewModel.addDate(it) }
                }) { Text("OK", color = CrewUpBlack, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
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
        // Header
        Row(
            modifier          = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text       = "Date & lieu",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                color      = CrewUpBlack
            )
        }

        CreationStepBar(currentStep = 2)

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            // === Dates ===
            Text(
                text       = "Dates proposées",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = CrewUpBlack
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Sélecteur de date principale
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp),
                color           = CrewUpGray,
                shadowElevation = 0.dp,
                onClick         = { showDatePicker = true }
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text     = if (draft.dates.isEmpty()) "Selectionner une date..."
                                   else dateFormatter.format(Date(draft.dates.first())),
                        fontSize = 14.sp,
                        color    = if (draft.dates.isEmpty()) CrewUpGrayMid else CrewUpBlack,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector        = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint               = CrewUpGrayMid,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // Dates alternatives affichées
            draft.dates.drop(1).forEach { millis ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    color     = CrewUpGray
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = dateFormatter.format(Date(millis)),
                            fontSize = 14.sp,
                            color    = CrewUpBlack,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick  = { viewModel.removeDate(millis) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Close,
                                contentDescription = "Supprimer",
                                tint               = CrewUpGrayMid
                            )
                        }
                    }
                }
            }

            // Bouton ajouter date alternative
            if (draft.dates.size in 1..2) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(vertical = 4.dp),
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

            // === Lieu ===
            Text(
                text       = "Lieu",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = CrewUpBlack
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value         = addressQuery,
                    onValueChange = {
                        addressQuery = it
                        viewModel.searchAddress(it)
                    },
                    placeholder   = { Text("Ex: parc de la Beaujoire", color = CrewUpGrayMid) },
                    leadingIcon   = {
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
                        unfocusedBorderColor = CrewUpDivider,
                        focusedBorderColor   = CrewUpBlack
                    )
                )

                DropdownMenu(
                    expanded         = showSuggestions,
                    onDismissRequest = { showSuggestions = false },
                    properties       = PopupProperties(focusable = false),
                    modifier         = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    nominatimResults.forEach { result ->
                        DropdownMenuItem(
                            text    = { NominatimItem(result) },
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

            Spacer(modifier = Modifier.height(12.dp))

            // Carte osmdroid
            OsmMapView(
                lat       = draft.lat,
                lon       = draft.lon,
                onMapTap  = { lat, lon -> viewModel.selectLocationOnMap(lat, lon) },
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                color    = CrewUpDivider
            )

            // === Max participants ===
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
                    unfocusedBorderColor = CrewUpDivider,
                    focusedBorderColor   = CrewUpBlack
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Boutons navigation
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrewUpBlack),
                    border = BorderStroke(1.5.dp, CrewUpBlack)
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
                    enabled  = draft.dates.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NominatimItem(result: NominatimResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", 0))
            userAgentValue = "CrewUp/1.0"
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(5.5)
            controller.setCenter(GeoPoint(46.2276, 2.2137))
            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    onMapTap(p.latitude, p.longitude)
                    return true
                }
                override fun longPressHelper(p: GeoPoint): Boolean = false
            })
            overlays.add(eventsOverlay)
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

    AndroidView(
        factory  = { mapView },
        modifier = modifier,
        update   = { mv ->
            mv.overlays.removeAll { it is Marker }
            if (lat != null && lon != null) {
                val marker = Marker(mv).apply {
                    position = GeoPoint(lat, lon)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mv.overlays.add(marker)
                mv.controller.setZoom(15.0)
                mv.controller.setCenter(GeoPoint(lat, lon))
            }
            mv.invalidate()
        }
    )
}
