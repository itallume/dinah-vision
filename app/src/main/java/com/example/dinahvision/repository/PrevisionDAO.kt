package com.example.dinahvision.repository

import android.util.Log
import com.example.dinahvision.models.Prevision
import com.example.dinahvision.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

class PrevisionDAO {
    private val db = FirebaseFirestore.getInstance()
    private val collection = "predictions"

//    suspend fun addPrevision(prevision: Prevision) {
//        try {
//            val data = hashMapOf(
//                "title" to prevision.title,
//                "description" to prevision.description,
//                "startDate" to prevision.startDate,
//                "endDate" to prevision.endDate,
//                "predicted" to prevision.predicted,
//                "user" to prevision.user?.let { db.document("users/${it.uid}") }
//            )
//
//            db.collection(collection)
//                .add(data)
//                .await()
//
//            Log.d("PrevisionDAO", "Previsão adicionada com sucesso!")
//        } catch (e: Exception) {
//            Log.e("PrevisionDAO", "Erro ao adicionar previsão", e)
//        }
//    }

    suspend fun markPredictionAsCorrect(previsionId: String): Boolean {
        return try {
            val updates = hashMapOf<String, Any>(
                "finished" to true,
                "predicted" to true
            )

            db.collection(collection)
                .document(previsionId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            Log.e("PrevisionDAO", "Erro ao marcar como acerto", e)
            false
        }
    }

    suspend fun markPredictionAsWrong(previsionId: String): Boolean {
        return try {
            db.collection(collection)
                .document(previsionId)
                .update(
                    "finished", true,
                    "predicted", false
                )
                .await()
            true
        } catch (e: Exception) {
            Log.e("PrevisionDAO", "Erro ao marcar como erro", e)
            false
        }
    }

    suspend fun listPredictions(): List<Prevision> {
        return try {
            val result = db.collection(collection)
                .orderBy("endDate", Query.Direction.ASCENDING)
                .get()
                .await()

            result.documents.mapNotNull { document ->
                try {
                    Prevision().apply {
                        id = document.id
                        title = document.getString("title") ?: ""
                        description = document.getString("description") ?: ""
                        startDate = document.getTimestamp("startDate")
                        endDate = document.getTimestamp("endDate")
                        predicted = document.getBoolean("predicted") ?: false
                        finished = document.getBoolean("finished") ?: false
                    }
                } catch (e: Exception) {
                    Log.e("PrevisionDAO", "Erro ao mapear documento ${document.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PrevisionDAO", "Erro ao listar previsões", e)
            emptyList()
        }
    }

    suspend fun listPredictionsByUser(): List<Prevision> {
        return try {
            val userID = User.currentUser?.uid ?: throw Exception("Usuário não autenticado")

            val result = db.collection(collection)
                .whereEqualTo("userId", userID)
                .orderBy("endDate", Query.Direction.ASCENDING)
                .get()
                .await()

            result.documents.mapNotNull { document ->
                try {
                    Prevision().apply {
                        id = document.id
                        title = document.getString("title") ?: ""
                        description = document.getString("description") ?: ""
                        startDate = document.getTimestamp("startDate")
                        endDate = document.getTimestamp("endDate")
                        predicted = document.getBoolean("predicted") ?: false
                        finished = document.getBoolean("finished") ?: false
                        userId = document.getString("userId") ?: ""
                    }
                } catch (e: Exception) {
                    Log.e("PrevisionDAO", "Erro ao mapear documento ${document.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PrevisionDAO", "Erro ao listar previsões", e)
            emptyList()
        }
    }

    suspend fun removePrevision(previsionId: String) {
        try {
            db.collection(collection)
                .document(previsionId)
                .delete()
                .await()

            Log.d("PrevisionDAO", "Previsão removida com sucesso!")
        } catch (e: Exception) {
            Log.e("PrevisionDAO", "Erro ao remover previsão", e)
        }
    }

    suspend fun savePrevision(prevision: Prevision): String {
        return try {
            val userID = User.currentUser?.uid ?: throw Exception("Usuário não autenticado")

            val previsionData = hashMapOf(
                "title" to prevision.title,
                "description" to prevision.description,
                "startDate" to prevision.startDate,
                "endDate" to prevision.endDate,
                "predicted" to prevision.predicted,
                "finished" to prevision.finished,
                "userId" to userID
            )

            if (prevision.id.isNotEmpty()) {
                db.collection(collection)
                    .document(prevision.id)
                    .set(previsionData)
                    .await()
                prevision.id
            } else {
                val documentRef = db.collection(collection)
                    .add(previsionData)
                    .await()
                documentRef.id
            }
        } catch (e: Exception) {
            Log.e("PrevisionDAO", "Erro ao salvar previsão", e)
            throw e
        }
    }
}