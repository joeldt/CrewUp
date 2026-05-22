package com.crewup.app.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class ProfileData(
    val prenom: String = "",
    val nom: String = "",
    val pseudo: String = "",
    val ville: String = "",
    val activites: List<String> = emptyList(),
    val photoBase64: String? = null,
    val score: Double = 0.0
)

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val data: ProfileData) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileEditState {
    object Idle    : ProfileEditState()
    object Loading : ProfileEditState()
    object Success : ProfileEditState()
    data class Error(val message: String) : ProfileEditState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState   = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _editState = MutableStateFlow<ProfileEditState>(ProfileEditState.Idle)
    val editState: StateFlow<ProfileEditState> = _editState

    fun loadProfile() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.value = ProfileUiState.Loading
            runCatching {
                val doc = firestore.collection("users").document(uid).get().await()
                ProfileData(
                    prenom      = doc.getString("prenom") ?: "",
                    nom         = doc.getString("nom") ?: "",
                    pseudo      = doc.getString("pseudo") ?: "",
                    ville       = doc.getString("ville") ?: "",
                    activites   = (doc.get("activites") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    photoBase64 = doc.getString("photoBase64"),
                    score       = doc.getDouble("score") ?: 0.0
                )
            }
            .onSuccess { _uiState.value = ProfileUiState.Success(it) }
            .onFailure { _uiState.value = ProfileUiState.Error("Impossible de charger le profil") }
        }
    }

    fun updateProfile(
        pseudo: String,
        ville: String,
        activites: List<String>,
        photoUri: Uri? = null
    ) {
        when {
            pseudo.isBlank() -> { _editState.value = ProfileEditState.Error("Le pseudo est obligatoire"); return }
            ville.isBlank()  -> { _editState.value = ProfileEditState.Error("La ville est obligatoire"); return }
        }
        viewModelScope.launch {
            _editState.value = ProfileEditState.Loading
            val uid = auth.currentUser?.uid ?: run {
                _editState.value = ProfileEditState.Error("Non connecté")
                return@launch
            }
            val photoBase64 = photoUri?.let { uri ->
                withContext(Dispatchers.IO) { compressToBase64(uri) }
            }
            val updates = mutableMapOf<String, Any>(
                "pseudo"    to pseudo.trim(),
                "ville"     to ville.trim(),
                "activites" to activites
            )
            if (photoBase64 != null) updates["photoBase64"] = photoBase64
            runCatching {
                firestore.collection("users").document(uid).update(updates).await()
            }
            .onSuccess {
                loadProfile()
                _editState.value = ProfileEditState.Success
            }
            .onFailure {
                _editState.value = ProfileEditState.Error("Erreur lors de la mise à jour")
            }
        }
    }

    fun resetEditState() { _editState.value = ProfileEditState.Idle }

    fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        onComplete()
    }

    private fun compressToBase64(uri: Uri): String {
        val context = getApplication<Application>()
        val inputStream = context.contentResolver.openInputStream(uri)
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        val scaled = Bitmap.createScaledBitmap(original, 150, 150, true)
        original.recycle()
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)
        scaled.recycle()
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }
}
