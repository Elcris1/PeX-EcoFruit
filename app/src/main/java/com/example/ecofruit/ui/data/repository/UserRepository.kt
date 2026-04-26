package com.example.ecofruit.ui.data.repository

import android.net.Uri
import android.util.Log
import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class UserRepository private constructor() {
    private val TAG = "UserRepository"
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val usersCollection = db.collection("users")
    private val storage = Firebase.storage
    private val profilesRef = storage.reference.child("profiles")

    private val _users = MutableStateFlow<List<User>>(emptyList())

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        _users.value = MockData.users
    }

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("Login failed")
        val user = getUserFromFirestore(firebaseUser.uid).getOrThrow() ?: throw Exception("User not found")
        _user.value = user
        user
    }

    suspend fun registerUser(name: String, email: String, password: String): Result<User> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("Registration failed")
        val newUser = User(
            id = firebaseUser.uid,
            name = name,
            email = email,
            createdAt = System.currentTimeMillis()/1000,
            profileImageUrl = "",
            bio = "",
            location = null,
            isProducer = false,
            following = emptyList(),
            followers = 0,
            reviewCount = 0,
            rating = 0.0
        )
        createUserInFirestore(newUser).getOrThrow()
        _user.value = newUser
        newUser
    }

    suspend fun createUserInFirestore(user: User): Result<Unit> = runCatching {
        usersCollection.document(user.id).set(user).await()
        _user.value = user
    }

    suspend fun getUserFromFirestore(userId: String): Result<User?> = runCatching {
        val snapshot = usersCollection.document(userId).get().await()
        Log.d(TAG, snapshot.toString())
        val user = snapshot.toObject(User::class.java)
        if (user != null) _user.value = user
        Log.d(TAG, user.toString())
        user
    }

    suspend fun reloadUser(): Result<User?> = runCatching {
        val currentUser = auth.currentUser ?: return@runCatching null
        getUserFromFirestore(currentUser.uid).getOrThrow()
    }

    suspend fun changeProducerState(): Result<Unit> = runCatching {
        val currentUser = _user.value ?: throw Exception("No user logged in")
        usersCollection.document(currentUser.id).update("isProducer", !currentUser.isProducer).await()
        _user.value = currentUser.copy(isProducer = !currentUser.isProducer)
    }

    suspend fun updateUser(updatedUser: User, imageUri: Uri?): Result<Unit> = runCatching {
        var userToSave = updatedUser

        // 1. Si hay una nueva imagen, subirla a Firebase Storage
        if (imageUri != null) {
            val fileRef = profilesRef.child("${updatedUser.id}.jpg")
            fileRef.putFile(imageUri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            userToSave = updatedUser.copy(profileImageUrl = downloadUrl)
        }

        val batch = db.batch()
        
        // 2. Actualizar el documento principal del usuario
        val userRef = usersCollection.document(userToSave.id)
        batch.set(userRef, userToSave)
        
        // 3. Propagar cambios a sus productos (nombre y avatar)
        val productsQuery = db.collection("products")
            .whereEqualTo("userId", userToSave.id)
            .get()
            .await()
            
        productsQuery.documents.forEach { doc ->
            batch.update(doc.reference, mapOf(
                "userName" to userToSave.name,
                "userAvatar" to userToSave.profileImageUrl
            ))
        }

        // 4. Propagar cambios a las reseñas que ha escrito (nombre y avatar del autor)
        val reviewsQuery = db.collection("reviews")
            .whereEqualTo("userId", userToSave.id)
            .get()
            .await()
            
        reviewsQuery.documents.forEach { doc ->
            batch.update(doc.reference, mapOf(
                "authorName" to userToSave.name,
                "authorAvatar" to userToSave.profileImageUrl
            ))
        }
        
        batch.commit().await()
        _user.value = userToSave
    }

    fun getUserById(userId: String): User? {
        return _users.value.find { it.id == userId }
    }


    fun logOut() {
        auth.signOut()
        _user.value = null
    }

    fun followUser(userId: String) {
        if (_user.value?.following == null) {
            _user.value?.following = listOf(userId)
        } else {
            _user.value?.following += userId
        }

        getUserById(userId)?.let {
            it.followers += 1
        }
    }

    fun unfollowUser(userId: String){
        _user.value?.following -= userId

        getUserById(userId)?.let {
            it.followers -= 1
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository().also { INSTANCE = it }
            }
        }
    }
}
