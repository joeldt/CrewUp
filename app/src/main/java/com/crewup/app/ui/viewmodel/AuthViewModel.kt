package com.crewup.app.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crewup.app.data.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

sealed class AuthUiState {
    object Idle               : AuthUiState()
    object Loading            : AuthUiState()
    object Success            : AuthUiState()
    object SuccessNeedsProfile : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // Conservés entre RegisterScreen et SetupProfileScreen
    private var pendingNom   = ""
    private var pendingPrenom = ""

    fun isAlreadyLoggedIn() = repository.getCurrentUser() != null

    suspend fun checkProfileExists(uid: String) = repository.checkProfileExists(uid)

    fun resetState() { _uiState.value = AuthUiState.Idle }
    fun setError(message: String) { _uiState.value = AuthUiState.Error(message) }

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Veuillez remplir tous les champs")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.loginWithEmail(email, password)
                .onSuccess { setSuccessStateAfterLogin() }
                .onFailure { _uiState.value = AuthUiState.Error(firebaseErrorMessage(it)) }
        }
    }

    fun registerWithEmail(
        email: String,
        password: String,
        confirmPassword: String,
        nom: String,
        prenom: String
    ) {
        when {
            prenom.isBlank() || nom.isBlank() || email.isBlank() || password.isBlank() ->
                _uiState.value = AuthUiState.Error("Veuillez remplir tous les champs")
            !email.matches(Regex("^[^@]+@[^@]+\\.[^@]+$")) ->
                _uiState.value = AuthUiState.Error("Adresse email invalide")
            password.length < 6 ->
                _uiState.value = AuthUiState.Error("Le mot de passe doit contenir au moins 6 caractères")
            !password.any { it.isDigit() } ->
                _uiState.value = AuthUiState.Error("Le mot de passe doit contenir au moins un chiffre")
            !password.any { it.isUpperCase() } ->
                _uiState.value = AuthUiState.Error("Le mot de passe doit contenir au moins une majuscule")
            password != confirmPassword ->
                _uiState.value = AuthUiState.Error("Les mots de passe ne correspondent pas")
            else -> viewModelScope.launch {
                _uiState.value = AuthUiState.Loading
                // Stocke nom et prenom pour les utiliser dans saveUserProfile
                pendingNom    = nom.trim()
                pendingPrenom = prenom.trim()
                repository.registerWithEmail(email, password, prenom)
                    .onSuccess { _uiState.value = AuthUiState.Success }
                    .onFailure { _uiState.value = AuthUiState.Error(firebaseErrorMessage(it)) }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.loginWithGoogle(idToken)
                .onSuccess { setSuccessStateAfterLogin() }
                .onFailure { _uiState.value = AuthUiState.Error(firebaseErrorMessage(it)) }
        }
    }

    private suspend fun setSuccessStateAfterLogin() {
        val uid = repository.getCurrentUser()?.uid
        val hasProfile = if (uid != null) repository.checkProfileExists(uid) else false
        _uiState.value = if (hasProfile) AuthUiState.Success else AuthUiState.SuccessNeedsProfile
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Veuillez saisir votre adresse email")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.sendPasswordReset(email)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(firebaseErrorMessage(it)) }
        }
    }

    fun saveUserProfile(pseudo: String, ville: String, activites: List<String>, photoUri: Uri? = null) {
        when {
            pseudo.isBlank() -> _uiState.value = AuthUiState.Error("Le pseudo est obligatoire")
            ville.isBlank()  -> _uiState.value = AuthUiState.Error("La ville est obligatoire")
            else -> viewModelScope.launch {
                _uiState.value = AuthUiState.Loading
                val photoBase64 = photoUri?.let { uri ->
                    withContext(Dispatchers.IO) { compressToBase64(uri) }
                }
                repository.saveUserProfile(pseudo, ville, activites, pendingNom, pendingPrenom, photoBase64)
                    .onSuccess {
                        repository.signOut()
                        _uiState.value = AuthUiState.Success
                    }
                    .onFailure { _uiState.value = AuthUiState.Error(firebaseErrorMessage(it)) }
            }
        }
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

    private fun firebaseErrorMessage(e: Throwable): String = when (e) {
        is FirebaseNetworkException                -> "Pas de connexion internet. Vérifiez votre réseau."
        is FirebaseAuthInvalidCredentialsException -> "Email ou mot de passe incorrect"
        is FirebaseAuthInvalidUserException        -> "Aucun compte associé à cet email"
        is FirebaseAuthUserCollisionException      -> "Un compte existe déjà avec cet email"
        is FirebaseAuthWeakPasswordException       -> "Le mot de passe doit contenir au moins 6 caractères"
        else                                       -> "Une erreur est survenue. Veuillez réessayer."
    }
}
