package com.crewup.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

data class EventDetail(
    val id: String = "",
    val name: String = "",
    val organizerId: String = "",
    val dates: List<Long> = emptyList(),
    val votes: Map<String, Int> = emptyMap(),
    val absent: List<String> = emptyList(),
    val confirmedDateIndex: Int? = null,
    val maxParticipants: Int = 0,
    val invitedFriends: List<String> = emptyList()
)

data class TaskItem(
    val id: String = "",
    val text: String = "",
    val assignedTo: String = "",
    val isDone: Boolean = false
)

data class ChatMessage(
    val id: String = "",
    val uid: String = "",
    val displayName: String = "",
    val text: String = "",
    val createdAt: Long = 0L
)

data class ParticipantProfile(
    val uid: String = "",
    val pseudo: String = "",
    val photoBase64: String? = null
)

class HubViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUid: String = auth.currentUser?.uid ?: ""
    private val currentDisplayName: String
        get() = auth.currentUser?.displayName ?: "Anonyme"

    private val _event        = MutableStateFlow(EventDetail())
    val event: StateFlow<EventDetail> = _event

    private val _tasks        = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks

    private val _messages     = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _participants = MutableStateFlow<List<ParticipantProfile>>(emptyList())
    val participants: StateFlow<List<ParticipantProfile>> = _participants

    val canFreeze: StateFlow<Boolean> = _event.map { ev ->
        if (ev.organizerId != currentUid || ev.confirmedDateIndex != null) return@map false
        if (ev.dates.isEmpty()) return@map false
        val totalParticipants = 1 + ev.invitedFriends.size
        val votedCount        = ev.votes.size + ev.absent.size
        val twoThirdsReached  = votedCount * 3 >= totalParticipants * 2
        val closestDate       = ev.dates.minOrNull() ?: return@map false
        val daysUntil         = TimeUnit.MILLISECONDS.toDays(closestDate - System.currentTimeMillis())
        val nearDate          = daysUntil <= 2
        twoThirdsReached && nearDate
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        listenToEvent()
        listenToTasks()
        listenToMessages()
    }

    private fun listenToEvent() {
        firestore.collection("events").document(eventId)
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                @Suppress("UNCHECKED_CAST")
                val ev = EventDetail(
                    id                 = snap.id,
                    name               = snap.getString("name") ?: "",
                    organizerId        = snap.getString("organizerId") ?: "",
                    dates              = (snap.get("dates") as? List<Timestamp>)
                        ?.map { it.toDate().time } ?: emptyList(),
                    votes              = (snap.get("votes") as? Map<String, Long>)
                        ?.mapValues { it.value.toInt() } ?: emptyMap(),
                    absent             = (snap.get("absent") as? List<String>) ?: emptyList(),
                    confirmedDateIndex = snap.getLong("confirmedDateIndex")?.toInt(),
                    maxParticipants    = (snap.getLong("maxParticipants") ?: 0L).toInt(),
                    invitedFriends     = (snap.get("invitedFriends") as? List<String>) ?: emptyList()
                )
                _event.value = ev
                loadParticipants(listOf(ev.organizerId) + ev.invitedFriends)
            }
    }

    private fun listenToTasks() {
        firestore.collection("events").document(eventId)
            .collection("tasks")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                _tasks.value = snap.documents.map { doc ->
                    TaskItem(
                        id         = doc.id,
                        text       = doc.getString("text") ?: "",
                        assignedTo = doc.getString("assignedTo") ?: "",
                        isDone     = doc.getBoolean("isDone") ?: false
                    )
                }
            }
    }

    private fun listenToMessages() {
        firestore.collection("events").document(eventId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                snap ?: return@addSnapshotListener
                _messages.value = snap.documents.map { doc ->
                    ChatMessage(
                        id          = doc.id,
                        uid         = doc.getString("uid") ?: "",
                        displayName = doc.getString("displayName") ?: "",
                        text        = doc.getString("text") ?: "",
                        createdAt   = (doc.get("createdAt") as? Timestamp)?.toDate()?.time ?: 0L
                    )
                }
            }
    }

    private fun loadParticipants(uids: List<String>) {
        viewModelScope.launch {
            runCatching {
                uids.distinct().mapNotNull { uid ->
                    runCatching {
                        val doc = firestore.collection("users").document(uid).get().await()
                        ParticipantProfile(
                            uid         = uid,
                            pseudo      = doc.getString("pseudo")
                                ?: doc.getString("prenom") ?: "Membre",
                            photoBase64 = doc.getString("photoBase64")
                        )
                    }.getOrNull()
                }
            }.onSuccess { _participants.value = it }
        }
    }

    fun voteForDate(dateIndex: Int) {
        viewModelScope.launch {
            runCatching {
                firestore.collection("events").document(eventId)
                    .update(mapOf<String, Any>(
                        "votes.$currentUid" to dateIndex,
                        "absent"            to FieldValue.arrayRemove(currentUid)
                    )).await()
            }
        }
    }

    fun toggleAbsent() {
        val isAbsent = currentUid in _event.value.absent
        viewModelScope.launch {
            runCatching {
                if (isAbsent) {
                    firestore.collection("events").document(eventId)
                        .update("absent", FieldValue.arrayRemove(currentUid)).await()
                } else {
                    firestore.collection("events").document(eventId)
                        .update(mapOf<String, Any>(
                            "absent"            to FieldValue.arrayUnion(currentUid),
                            "votes.$currentUid" to FieldValue.delete()
                        )).await()
                }
            }
        }
    }

    fun freezeVotes() {
        val ev = _event.value
        val confirmedIndex = ev.votes.values
            .groupBy { it }
            .maxByOrNull { it.value.size }
            ?.key ?: return
        viewModelScope.launch {
            runCatching {
                firestore.collection("events").document(eventId)
                    .update("confirmedDateIndex", confirmedIndex).await()
            }
        }
    }

    fun addTask(text: String, assignedTo: String) {
        viewModelScope.launch {
            runCatching {
                firestore.collection("events").document(eventId)
                    .collection("tasks")
                    .add(mapOf(
                        "text"       to text.trim(),
                        "assignedTo" to assignedTo.trim(),
                        "isDone"     to false,
                        "createdAt"  to FieldValue.serverTimestamp()
                    )).await()
            }
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            runCatching {
                firestore.collection("events").document(eventId)
                    .collection("tasks").document(taskId)
                    .update("isDone", true).await()
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            runCatching {
                firestore.collection("events").document(eventId)
                    .collection("messages")
                    .add(mapOf(
                        "uid"         to currentUid,
                        "displayName" to currentDisplayName,
                        "text"        to text.trim(),
                        "createdAt"   to FieldValue.serverTimestamp()
                    )).await()
            }
        }
    }
}
