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
                    password = document.getString("password") ?: "",
                    points = (document.get("points") as? Number)?.toInt() ?: 0,
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

    suspend fun createUser(user: User) {
        try {
            val userData = hashMapOf(
                "username" to user.username,
                "password" to user.password,
                "points" to user.points
            )

            db.collection(collection)
                .add(userData)
                .await()
        } catch (e: Exception) {
            Log.e("UserDAO", "clarinhoi q nao, erro ao criar usuário", e)
            throw e
        }
    }

    suspend fun updateUser(user: User) {
        try {
            if (user.uid.isBlank()) {
                throw IllegalArgumentException("ID do usuário não pode ser vazio")
            }

            val userData = hashMapOf<String, Any>(
                "username" to user.username,
                "password" to user.password,
                "points" to user.points
            )

            db.collection(collection)
                .document(user.uid)
                .update(userData)
                .await()

        } catch (e: Exception) {
            Log.e("UserDAO", "Erro ao atualizar usuário ${user.uid}", e)
            throw e
        }
    }

    suspend fun getUser(userId: String): User? {
        return try {
            val document = db.collection(collection)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                User(
                    uid = document.id,
                    username = document.getString("username") ?: "",
                    password = document.getString("password") ?: "",
                    points = (document.get("points") as? Number)?.toInt() ?: 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UserDAO", "Erro ao buscar usuário", e)
            null
        }
    }

}

