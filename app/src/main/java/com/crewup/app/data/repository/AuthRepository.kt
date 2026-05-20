package com.crewup.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut() = auth.signOut()

    suspend fun loginWithEmail(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        prenom: String
    ): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = auth.currentUser ?: error("Utilisateur non connecté")
        user.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(prenom.trim()).build()
        ).await()
    }

    suspend fun loginWithGoogle(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    suspend fun saveUserProfile(
        pseudo: String,
        ville: String,
        activites: List<String>,
        nom: String,
        prenom: String
    ): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("Utilisateur non connecté")
        val data = mapOf(
            "uid"       to user.uid,
            "email"     to (user.email ?: ""),
            "prenom"    to prenom.ifBlank { user.displayName ?: "" },
            "nom"       to nom,
            "pseudo"    to pseudo.trim(),
            "ville"     to ville.trim(),
            "activites" to activites,
            "createdAt" to System.currentTimeMillis()
        )
        // Fire-and-forget : la déconnexion suit immédiatement, l'écriture se synchronise en arrière-plan
        firestore.collection("users").document(user.uid).set(data)
    }
}
