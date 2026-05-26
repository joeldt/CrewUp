package com.crewup.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AppNotification(
    val id: String,
    val type: String,
    val eventId: String,
    val eventName: String,
    val fromUid: String,
    val fromPseudo: String,
    val createdAt: Long
)

class NotificationsViewModel : ViewModel() {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUid: String get() = auth.currentUser?.uid ?: ""

    private val _crewInvites   = MutableStateFlow<List<AppNotification>>(emptyList())
    val crewInvites: StateFlow<List<AppNotification>> = _crewInvites

    private val _deletedEvents = MutableStateFlow<List<AppNotification>>(emptyList())
    val deletedEvents: StateFlow<List<AppNotification>> = _deletedEvents

    val totalCount: StateFlow<Int> = combine(_crewInvites, _deletedEvents) { invites, deleted ->
        invites.size + deleted.size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init { loadNotifications() }

    fun loadNotifications() {
        val uid = currentUid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                firestore.collection("users").document(uid)
                    .collection("notifications")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get().await()
                    .documents
                    .mapNotNull { doc ->
                        AppNotification(
                            id         = doc.id,
                            type       = doc.getString("type") ?: return@mapNotNull null,
                            eventId    = doc.getString("eventId") ?: "",
                            eventName  = doc.getString("eventName") ?: "",
                            fromUid    = doc.getString("fromUid") ?: "",
                            fromPseudo = doc.getString("fromPseudo") ?: "",
                            createdAt  = doc.getLong("createdAt") ?: 0L
                        )
                    }
            }.onSuccess { notifs ->
                _crewInvites.value   = notifs.filter { it.type == "crew_invite" }
                _deletedEvents.value = notifs.filter { it.type == "event_deleted" }
            }
        }
    }

    fun acceptCrewInvite(notif: AppNotification) {
        val uid = currentUid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                val batch = firestore.batch()
                batch.update(
                    firestore.collection("events").document(notif.eventId),
                    "invitedFriends", FieldValue.arrayUnion(uid)
                )
                batch.delete(
                    firestore.collection("users").document(uid)
                        .collection("notifications").document(notif.id)
                )
                batch.commit().await()
            }.onSuccess { loadNotifications() }
        }
    }

    fun declineCrewInvite(notifId: String) {
        dismissNotification(notifId)
    }

    fun dismissNotification(notifId: String) {
        val uid = currentUid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                firestore.collection("users").document(uid)
                    .collection("notifications").document(notifId)
                    .delete().await()
            }.onSuccess { loadNotifications() }
        }
    }
}
