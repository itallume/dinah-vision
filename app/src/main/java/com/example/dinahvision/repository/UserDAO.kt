package com.example.dinahvision.repository

import android.util.Log
import com.example.dinahvision.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserDAO {
    private val db = FirebaseFirestore.getInstance()
    private val collection = "users"

    suspend fun getUserByUsername(username: String): User? {
        return try {
            val query = db.collection(collection)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) {
                null
            } else {
                val document = query.documents[0]
                User(
                    uid = document.id,
                    username = document.getString("username") ?: "",
                    password = document.getString("password") ?: ""
                ).also {
                    Log.d("UserDAO", "Usuário encontrado: ${it.username}")
                }
            }
        } catch (e: Exception) {
            Log.e("UserDAO", "Erro ao buscar usuário", e)
            null
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        val user = getUserByUsername(username)
        return if (user != null && user.password == password) {
            User.login(user)
            true
        } else {
            false
        }
    }
}
