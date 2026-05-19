package com.crewup.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crewup.app.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun isAlreadyLoggedIn() = repository.getCurrentUser() != null

    fun resetState() { _uiState.value = AuthUiState.Idle }

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Remplis tous les champs")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.loginWithEmail(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(firebaseErrorMessage(it)) }
        }
    }

    fun registerWithEmail(email: String, password: String, confirmPassword: String, nom: String) {
        when {
            email.isBlank() || password.isBlank() || nom.isBlank() ->
                _uiState.value = AuthUiState.Error("Remplis tous les champs")
            password != confirmPassword ->
                _uiState.value = AuthUiState.Error("Les mots de passe ne correspondent pas")
            password.length < 6 ->
                _uiState.value = AuthUiState.Error("Le mot de passe doit contenir au moins 6 caractères")
            else -> viewModelScope.launch {
                _uiState.value = AuthUiState.Loading
                repository.registerWithEmail(email, password, nom)
                    .onSuccess { _uiState.value = AuthUiState.Success }
                    .onFailure { _uiState.value = AuthUiState.Error(firebaseErrorMessage(it)) }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.loginWithGoogle(idToken)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error("Connexion Google échouée") }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Saisis ton adresse email")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.sendPasswordReset(email)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(firebaseErrorMessage(it)) }
        }
    }

    fun saveUserProfile(pseudo: String, ville: String, activites: List<String>) {
        if (pseudo.isBlank()) {
            _uiState.value = AuthUiState.Error("Le pseudo est obligatoire")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.saveUserProfile(pseudo, ville, activites)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error("Erreur lors de la sauvegarde") }
        }
    }

    private fun firebaseErrorMessage(e: Throwable): String = when (e) {
        is FirebaseAuthInvalidCredentialsException -> "Email ou mot de passe incorrect"
        is FirebaseAuthInvalidUserException        -> "Aucun compte trouvé avec cet email"
        is FirebaseAuthUserCollisionException      -> "Un compte existe déjà avec cet email"
        is FirebaseAuthWeakPasswordException       -> "Mot de passe trop faible (6 caractères min.)"
        else                                       -> e.message ?: "Une erreur est survenue"
    }
}
