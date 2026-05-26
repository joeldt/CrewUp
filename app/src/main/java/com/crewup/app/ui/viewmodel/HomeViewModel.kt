package com.crewup.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EventSummary(
    val id: String,
    val name: String,
    val maxParticipants: Int,
    val datesCount: Int
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val allEvents: List<EventSummary>,     // mes Crews ( créés + intégrés)
        val createdEvents: List<EventSummary>  // Crées ( organisés par l'utilisateur)
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init { loadEvents() }

    fun loadEvents() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = HomeUiState.Success(emptyList(), emptyList())
            return
        }
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            runCatching {
                // Événements créés par l'utilisateur
                val organizedDocs = firestore.collection("events")
                    .whereEqualTo("organizerId", uid)
                    .get().await().documents

                // Événements où l'utilisateur est invité
                val invitedDocs = firestore.collection("events")
                    .whereArrayContains("invitedFriends", uid)
                    .get().await().documents

                val toSummary: (com.google.firebase.firestore.DocumentSnapshot) -> EventSummary = { doc ->
                    @Suppress("UNCHECKED_CAST")
                    EventSummary(
                        id              = doc.id,
                        name            = doc.getString("name") ?: "",
                        maxParticipants = (doc.getLong("maxParticipants") ?: 0L).toInt(),
                        datesCount      = (doc.get("dates") as? List<*>)?.size ?: 0
                    )
                }

                val createdEvents = organizedDocs.map(toSummary)

                // Fusion sans doublons pour Mes Crews
                val allEvents = (organizedDocs + invitedDocs)
                    .distinctBy { it.id }
                    .map(toSummary)

                Pair(allEvents, createdEvents)
            }
            .onSuccess { (all, created) ->
                _uiState.value = HomeUiState.Success(allEvents = all, createdEvents = created)
            }
            .onFailure {
                _uiState.value = HomeUiState.Error("Impossible de charger les événements")
            }
        }
    }
}
