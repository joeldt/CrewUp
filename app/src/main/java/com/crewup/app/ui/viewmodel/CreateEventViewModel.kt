package com.crewup.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Date

data class NominatimResult(
    val displayName: String,
    val shortName: String,
    val lat: Double,
    val lon: Double
)

data class EventDraft(
    val activityType: Int = -1,
    val name: String = "",
    val description: String = "",
    val dates: List<Long> = emptyList(),
    val address: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val maxParticipants: String = "",
    val invitedFriends: List<String> = emptyList()
)

sealed class CreateEventUiState {
    object Idle    : CreateEventUiState()
    object Loading : CreateEventUiState()
    object Success : CreateEventUiState()
    data class Error(val message: String) : CreateEventUiState()
}

class CreateEventViewModel : ViewModel() {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _draft = MutableStateFlow(EventDraft())
    val draft: StateFlow<EventDraft> = _draft

    private val _nominatimResults = MutableStateFlow<List<NominatimResult>>(emptyList())
    val nominatimResults: StateFlow<List<NominatimResult>> = _nominatimResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _createState = MutableStateFlow<CreateEventUiState>(CreateEventUiState.Idle)
    val createState: StateFlow<CreateEventUiState> = _createState

    val eventId: String = firestore.collection("events").document().id

    private var searchJob: Job? = null

    // Étape 1
    fun setActivityType(index: Int) { _draft.update { it.copy(activityType = index) } }
    fun setName(name: String)       { _draft.update { it.copy(name = name) } }
    fun setDescription(desc: String){ _draft.update { it.copy(description = desc) } }

    // Étape 2 — dates
    fun addDate(millis: Long) {
        val current = _draft.value.dates
        if (current.size < 3 && millis !in current) {
            _draft.update { it.copy(dates = current + millis) }
        }
    }
    fun removeDate(millis: Long) { _draft.update { it.copy(dates = it.dates - millis) } }

    // Étape 2 — adresse via Nominatim
    fun searchAddress(query: String) {
        searchJob?.cancel()
        if (query.length < 3) { _nominatimResults.value = emptyList(); return }
        searchJob = viewModelScope.launch {
            delay(350)
            _isSearching.value = true
            runCatching { withContext(Dispatchers.IO) { queryNominatim(query) } }
                .onSuccess { _nominatimResults.value = it }
                .onFailure { _nominatimResults.value = emptyList() }
            _isSearching.value = false
        }
    }

    fun clearSuggestions() { _nominatimResults.value = emptyList() }

    fun selectAddress(result: NominatimResult) {
        _draft.update { it.copy(address = result.displayName, lat = result.lat, lon = result.lon) }
        _nominatimResults.value = emptyList()
    }

    fun selectLocationOnMap(lat: Double, lon: Double) {
        _draft.update { it.copy(lat = lat, lon = lon) }
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { reverseGeocode(lat, lon) } }
                .onSuccess { addr -> _draft.update { it.copy(address = addr) } }
        }
    }

    fun setMaxParticipants(value: String) { _draft.update { it.copy(maxParticipants = value) } }

    // Étape 3
    fun toggleFriend(uid: String) {
        val current = _draft.value.invitedFriends
        _draft.update {
            it.copy(invitedFriends = if (uid in current) current - uid else current + uid)
        }
    }

    fun createEvent(onSuccess: () -> Unit) {
        val draft = _draft.value
        val uid   = auth.currentUser?.uid ?: return
        _createState.value = CreateEventUiState.Loading
        viewModelScope.launch {
            runCatching {
                val data = mapOf(
                    "activityType"    to draft.activityType,
                    "name"            to draft.name.trim(),
                    "description"     to draft.description.trim(),
                    "dates"           to draft.dates.map { Timestamp(Date(it)) },
                    "address"         to draft.address,
                    "lat"             to draft.lat,
                    "lon"             to draft.lon,
                    "maxParticipants" to (draft.maxParticipants.toIntOrNull() ?: 0),
                    "organizerId"     to uid,
                    "invitedFriends"  to draft.invitedFriends,
                    "shareCode"       to eventId,
                    "createdAt"       to FieldValue.serverTimestamp()
                )
                firestore.collection("events").document(eventId).set(data).await()
            }
            .onSuccess {
                _createState.value = CreateEventUiState.Success
                onSuccess()
            }
            .onFailure {
                _createState.value = CreateEventUiState.Error("Erreur lors de la création du crew")
            }
        }
    }

    fun resetCreateState() { _createState.value = CreateEventUiState.Idle }

    private fun queryNominatim(query: String): List<NominatimResult> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val conn = URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=5")
            .openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "CrewUp/1.0 (tapajoel16@gmail.com)")
        conn.connectTimeout = 5_000
        conn.readTimeout    = 5_000
        val text  = conn.inputStream.bufferedReader().readText()
        val array = JSONArray(text)
        return (0 until array.length()).map { i ->
            val obj         = array.getJSONObject(i)
            val displayName = obj.getString("display_name")
            NominatimResult(
                displayName = displayName,
                shortName   = displayName.substringBefore(",").trim(),
                lat         = obj.getDouble("lat"),
                lon         = obj.getDouble("lon")
            )
        }
    }

    private fun reverseGeocode(lat: Double, lon: Double): String {
        val conn = URL("https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json")
            .openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "CrewUp/1.0 (tapajoel16@gmail.com)")
        conn.connectTimeout = 5_000
        conn.readTimeout    = 5_000
        return JSONObject(conn.inputStream.bufferedReader().readText())
            .optString("display_name", "$lat, $lon")
    }
}
