package com.crewup.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class FriendStatus { None, PendingSent, PendingReceived, Friend }

data class UserSearchResult(
    val uid: String,
    val pseudo: String,
    val photoBase64: String?,
    val friendStatus: FriendStatus
)

data class FriendEntry(
    val uid: String,
    val pseudo: String,
    val photoBase64: String?
)

class FriendsViewModel : ViewModel() {

    private val auth        = FirebaseAuth.getInstance()
    val currentUid: String  get() = auth.currentUser?.uid ?: ""
    private val firestore   = FirebaseFirestore.getInstance()

    private val _searchResults   = MutableStateFlow<List<UserSearchResult>>(emptyList())
    val searchResults: StateFlow<List<UserSearchResult>> = _searchResults

    private val _isSearching     = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _friends         = MutableStateFlow<List<FriendEntry>>(emptyList())
    val friends: StateFlow<List<FriendEntry>> = _friends

    private val _pendingReceived = MutableStateFlow<List<FriendEntry>>(emptyList())
    val pendingReceived: StateFlow<List<FriendEntry>> = _pendingReceived

    private val _pendingSentUids = MutableStateFlow<Set<String>>(emptySet())
    val pendingSentUids: StateFlow<Set<String>> = _pendingSentUids

    private var searchJob: Job? = null

    init { loadFriends() }

    fun loadFriends() {
        val uid = currentUid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                val snap = firestore.collection("users").document(uid)
                    .collection("friends").get().await()

                val accepted = snap.documents.filter { it.getString("status") == "accepted" }
                val received = snap.documents.filter {
                    it.getString("status") == "pending" && it.getString("direction") == "received"
                }
                val sentUids = snap.documents
                    .filter { it.getString("status") == "pending" && it.getString("direction") == "sent" }
                    .map { it.id }.toSet()

                suspend fun loadProfile(uid: String): FriendEntry? = runCatching {
                    val doc = firestore.collection("users").document(uid).get().await()
                    FriendEntry(
                        uid         = uid,
                        pseudo      = doc.getString("pseudo") ?: "Membre",
                        photoBase64 = doc.getString("photoBase64")
                    )
                }.getOrNull()

                Triple(
                    accepted.mapNotNull { loadProfile(it.id) },
                    received.mapNotNull { loadProfile(it.id) },
                    sentUids
                )
            }
            .onSuccess { (friends, pending, sent) ->
                _friends.value         = friends
                _pendingReceived.value = pending
                _pendingSentUids.value = sent
            }
        }
    }

    fun searchUsers(query: String) {
        searchJob?.cancel()
        if (query.length < 2) { _searchResults.value = emptyList(); return }
        searchJob = viewModelScope.launch {
            delay(300)
            _isSearching.value = true
            runCatching {
                val q = query.lowercase()
                firestore.collection("users")
                    .orderBy("pseudoLower")
                    .startAt(q)
                    .endAt(q + "")
                    .limit(15)
                    .get().await()
                    .documents
                    .filter { it.id != currentUid }
                    .map { doc ->
                        val uid = doc.id
                        val status = when {
                            _friends.value.any { it.uid == uid }        -> FriendStatus.Friend
                            _pendingSentUids.value.contains(uid)         -> FriendStatus.PendingSent
                            _pendingReceived.value.any { it.uid == uid } -> FriendStatus.PendingReceived
                            else                                          -> FriendStatus.None
                        }
                        UserSearchResult(
                            uid          = uid,
                            pseudo       = doc.getString("pseudo") ?: "",
                            photoBase64  = doc.getString("photoBase64"),
                            friendStatus = status
                        )
                    }
            }
            .onSuccess { _searchResults.value = it }
            .onFailure  { _searchResults.value = emptyList() }
            _isSearching.value = false
        }
    }

    fun clearSearch() { _searchResults.value = emptyList() }

    fun sendFriendRequest(targetUid: String) {
        viewModelScope.launch {
            runCatching {
                val now   = FieldValue.serverTimestamp()
                val batch = firestore.batch()
                batch.set(
                    firestore.collection("users").document(currentUid)
                        .collection("friends").document(targetUid),
                    mapOf("status" to "pending", "direction" to "sent", "since" to now)
                )
                batch.set(
                    firestore.collection("users").document(targetUid)
                        .collection("friends").document(currentUid),
                    mapOf("status" to "pending", "direction" to "received", "since" to now)
                )
                batch.commit().await()
            }
            .onSuccess {
                _pendingSentUids.update { it + targetUid }
                _searchResults.update { list ->
                    list.map {
                        if (it.uid == targetUid) it.copy(friendStatus = FriendStatus.PendingSent) else it
                    }
                }
            }
        }
    }

    fun acceptFriendRequest(fromUid: String) {
        viewModelScope.launch {
            runCatching {
                val now   = FieldValue.serverTimestamp()
                val batch = firestore.batch()
                batch.update(
                    firestore.collection("users").document(currentUid)
                        .collection("friends").document(fromUid),
                    mapOf("status" to "accepted", "direction" to FieldValue.delete(), "since" to now)
                )
                batch.update(
                    firestore.collection("users").document(fromUid)
                        .collection("friends").document(currentUid),
                    mapOf("status" to "accepted", "direction" to FieldValue.delete(), "since" to now)
                )
                batch.commit().await()
            }
            .onSuccess { loadFriends() }
        }
    }

    fun declineFriendRequest(fromUid: String) {
        viewModelScope.launch {
            runCatching {
                val batch = firestore.batch()
                batch.delete(
                    firestore.collection("users").document(currentUid)
                        .collection("friends").document(fromUid)
                )
                batch.delete(
                    firestore.collection("users").document(fromUid)
                        .collection("friends").document(currentUid)
                )
                batch.commit().await()
            }
            .onSuccess { loadFriends() }
        }
    }
}
