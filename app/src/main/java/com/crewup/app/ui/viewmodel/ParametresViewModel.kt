package com.crewup.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotifPrefs(
    val rappelsSortie: Boolean = true,
    val nouveauxVotes: Boolean = true,
    val invitations: Boolean   = false
)

sealed class ParametresUiState {
    object Loading : ParametresUiState()
    data class Success(val notifPrefs: NotifPrefs) : ParametresUiState()
    data class Error(val message: String) : ParametresUiState()
}

sealed class ParametresActionState {
    object Idle              : ParametresActionState()
    object Loading           : ParametresActionState()
    object PasswordResetSent : ParametresActionState()
    object AccountDeleted    : ParametresActionState()
    data class Error(val message: String) : ParametresActionState()
}

class ParametresViewModel : ViewModel() {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState     = MutableStateFlow<ParametresUiState>(ParametresUiState.Loading)
    val uiState: StateFlow<ParametresUiState> = _uiState

    private val _actionState = MutableStateFlow<ParametresActionState>(ParametresActionState.Idle)
    val actionState: StateFlow<ParametresActionState> = _actionState

    init { loadPrefs() }

    private fun loadPrefs() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching {
                val doc = firestore.collection("users").document(uid).get().await()
                NotifPrefs(
                    rappelsSortie = doc.getBoolean("notifRappelsSortie") ?: true,
                    nouveauxVotes = doc.getBoolean("notifNouveauxVotes") ?: true,
                    invitations   = doc.getBoolean("notifInvitations")   ?: false
                )
            }
            .onSuccess { _uiState.value = ParametresUiState.Success(it) }
            .onFailure { _uiState.value = ParametresUiState.Error("Impossible de charger les paramètres") }
        }
    }

    fun toggleRappelsSortie(value: Boolean) = updateNotif("notifRappelsSortie", value) { prefs ->
        prefs.copy(rappelsSortie = value)
    }

    fun toggleNouveauxVotes(value: Boolean) = updateNotif("notifNouveauxVotes", value) { prefs ->
        prefs.copy(nouveauxVotes = value)
    }

    fun toggleInvitations(value: Boolean) = updateNotif("notifInvitations", value) { prefs ->
        prefs.copy(invitations = value)
    }

    private fun updateNotif(key: String, value: Boolean, updatePrefs: (NotifPrefs) -> NotifPrefs) {
        val current = (_uiState.value as? ParametresUiState.Success)?.notifPrefs ?: return
        _uiState.value = ParametresUiState.Success(updatePrefs(current))
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            runCatching { firestore.collection("users").document(uid).update(key, value).await() }
        }
    }

    fun sendPasswordReset() {
        viewModelScope.launch {
            val email = auth.currentUser?.email ?: return@launch
            _actionState.value = ParametresActionState.Loading
            runCatching { auth.sendPasswordResetEmail(email).await() }
                .onSuccess  { _actionState.value = ParametresActionState.PasswordResetSent }
                .onFailure  { _actionState.value = ParametresActionState.Error("Erreur lors de l'envoi de l'email") }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            _actionState.value = ParametresActionState.Loading
            runCatching {
                firestore.collection("users").document(user.uid).delete().await()
                user.delete().await()
            }
            .onSuccess { _actionState.value = ParametresActionState.AccountDeleted }
            .onFailure {
                val msg = if (it is FirebaseAuthRecentLoginRequiredException)
                    "Reconnectez-vous pour supprimer votre compte"
                else "Erreur lors de la suppression du compte"
                _actionState.value = ParametresActionState.Error(msg)
            }
        }
    }

    fun resetActionState() { _actionState.value = ParametresActionState.Idle }
}
