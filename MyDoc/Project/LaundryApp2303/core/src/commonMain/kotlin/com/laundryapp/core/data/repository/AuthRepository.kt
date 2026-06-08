package com.laundryapp.core.data.repository

import com.laundryapp.core.data.model.User
import com.laundryapp.core.data.model.UserRole
import com.laundryapp.core.util.ImageUploader
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AuthRepository(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val imageUploader: ImageUploader
) {
    private val _authState = MutableStateFlow(auth.currentUser)
    val authState = _authState.asStateFlow()

    init {
        MainScope().launch {
            auth.authStateChanged.collect { user ->
                _authState.value = user
            }
        }
    }

    val currentUser get() = auth.currentUser
    val isLoggedIn get() = auth.currentUser != null

    private fun getCollection(role: UserRole): String {
        return when (role) {
            UserRole.SUPER_ADMIN -> "admins"
            UserRole.DELIVERY_PARTNER -> "drivers"
            UserRole.CUSTOMER -> "customers"
            UserRole.VENDOR -> "vendors"
        }
    }

    suspend fun signInAdmin(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password)
        val firebaseUser = result.user ?: throw Exception("Login failed")
        
        val doc = firestore.collection("admins").document(firebaseUser.uid).get()
        if (!doc.exists) {
            throw Exception("Not authorized as Admin")
        }
        doc.data<User>()
    }

    suspend fun signInWithCredential(credential: AuthCredential): Result<User> = runCatching {
        val result = auth.signInWithCredential(credential)
        val firebaseUser = result.user ?: throw Exception("Sign in failed")

        val roles = UserRole.entries
        var user: User? = null
        
        for (role in roles) {
            val doc = firestore.collection(getCollection(role)).document(firebaseUser.uid).get()
            if (doc.exists) {
                user = doc.data<User>()
                break
            }
        }

        if (user == null) {
            val newUser = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                phone = firebaseUser.phoneNumber ?: "",
                profileImageUrl = "",
                role = UserRole.CUSTOMER
            )
            firestore.collection(getCollection(UserRole.CUSTOMER)).document(firebaseUser.uid).set(newUser)
            newUser
        } else {
            user
        }
    }

    suspend fun signOut() = auth.signOut()

    suspend fun getUserProfile(userId: String): Result<User> = runCatching {
        for (role in UserRole.entries) {
            val doc = firestore.collection(getCollection(role)).document(userId).get()
            if (doc.exists) {
                return@runCatching doc.data<User>()
            }
        }
        throw Exception("User not found")
    }

    fun getAllUsers(role: UserRole): Flow<List<User>> {
        return firestore.collection(getCollection(role)).snapshots.map { snapshot: QuerySnapshot ->
            snapshot.documents.map { it.data<User>() }
        }
    }

    suspend fun approveVendor(vendorId: String): Result<Unit> = runCatching {
        firestore.collection("vendors").document(vendorId).update("isApproved" to true, "isRejected" to false)
    }

    suspend fun rejectVendor(vendorId: String): Result<Unit> = runCatching {
        firestore.collection("vendors").document(vendorId).update("isApproved" to false, "isRejected" to true)
    }

    suspend fun uploadProfilePicture(userId: String, imageData: Any, role: UserRole): Result<String> = runCatching {
        val downloadUrl = imageUploader.uploadImage(imageData).getOrThrow()
        firestore.collection(getCollection(role)).document(userId).update("profileImageUrl" to downloadUrl)
        downloadUrl
    }
}
