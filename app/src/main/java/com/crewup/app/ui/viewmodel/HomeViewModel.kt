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
    val organizerId: String,
    val maxParticipants: Int,
    val datesCount: Int,
    val invitedFriends: List<String>
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val allEvents: List<EventSummary>,
        val createdEvents: List<EventSummary>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _currentUserPseudo = MutableStateFlow("")

    init {
        loadEvents()
        loadCurrentUserPseudo()
    }

    private fun loadCurrentUserPseudo() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching {
                firestore.collection("users").document(uid).get().await().getString("pseudo") ?: ""
            }.onSuccess { _currentUserPseudo.value = it }
        }
    }

    fun loadEvents() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = HomeUiState.Success(emptyList(), emptyList())
            return
        }
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            runCatching {
                val organizedDocs = firestore.collection("events")
                    .whereEqualTo("organizerId", uid)
                    .get().await().documents

                val invitedDocs = firestore.collection("events")
                    .whereArrayContains("invitedFriends", uid)
                    .get().await().documents

                val toSummary: (com.google.firebase.firestore.DocumentSnapshot) -> EventSummary = { doc ->
                    @Suppress("UNCHECKED_CAST")
                    EventSummary(
                        id              = doc.id,
                        name            = doc.getString("name") ?: "",
                        organizerId     = doc.getString("organizerId") ?: "",
                        maxParticipants = (doc.getLong("maxParticipants") ?: 0L).toInt(),
                        datesCount      = (doc.get("dates") as? List<*>)?.size ?: 0,
                        invitedFriends  = (doc.get("invitedFriends") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList()
                    )
                }

                val createdEvents = organizedDocs.map(toSummary)
                val allEvents     = (organizedDocs + invitedDocs)
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

    fun deleteEvent(event: EventSummary) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching {
                val pseudo    = _currentUserPseudo.value
                val batch     = firestore.batch()
                val notifData = mapOf(
                    "type"       to "event_deleted",
                    "eventId"    to event.id,
                    "eventName"  to event.name,
                    "fromUid"    to uid,
                    "fromPseudo" to pseudo,
                    "createdAt"  to System.currentTimeMillis()
                )
                event.invitedFriends.forEach { participantUid ->
                    val ref = firestore.collection("users").document(participantUid)
                        .collection("notifications").document()
                    batch.set(ref, notifData)
                }
                batch.delete(firestore.collection("events").document(event.id))
                batch.commit().await()
            }.onSuccess { loadEvents() }
        }
    }

    fun inviteFriendToEvent(event: EventSummary, friendUid: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching {
                val pseudo = _currentUserPseudo.value
                firestore.collection("users").document(friendUid)
                    .collection("notifications")
                    .add(mapOf(
                        "type"       to "crew_invite",
                        "eventId"    to event.id,
                        "eventName"  to event.name,
                        "fromUid"    to uid,
                        "fromPseudo" to pseudo,
                        "createdAt"  to System.currentTimeMillis()
                    )).await()
            }
        }
    }
}
