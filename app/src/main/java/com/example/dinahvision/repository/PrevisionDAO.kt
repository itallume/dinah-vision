package com.example.dinahvision.repository

import com.example.dinahvision.models.Prevision
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PrevisionDAO {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addPrevision(prevision: Prevision) {
        db.collection("predictions")
            .add(prevision)
            .addOnSuccessListener {
                println("Previsão feita com sucesso!")
            }
            .addOnFailureListener { e ->
                println("Erro ao adicionar previsão: $e")
            }.await()
    }

    suspend fun listPredictions(): List<Prevision> {
        var predictions: List<Prevision> = ArrayList<Prevision>()
        db.collection("predictions")
            .get()
            .addOnSuccessListener { result ->
                predictions = result.toObjects(Prevision::class.java)
            }
            .addOnFailureListener { e ->
                println("Erro ao listar produtos: $e")
            }.await()
        return predictions
    }

    suspend fun remove(prevision: Prevision) {
        db.collection("produtospredictions")
            .document(prevision.id)
            .delete()
            .addOnSuccessListener { result ->
                println("Previsão removida com sucesso!")
            }
            .addOnFailureListener { e ->
                println("Erro ao remover a previsão ${prevision.toString()}: $e")
            }.await()
    }

}