package com.example.dinahvision.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date



data class Prevision(
    var title: String = "",
    var description: String = "",
    var startDate: Timestamp? = null,
    var endDate: Timestamp? = null,
    var predicted: Boolean = false,
    var userId: String = "",
    var finished: Boolean = false
) {
    @DocumentId
    var id: String = ""

    fun getStartDateAsDate(): Date = startDate?.toDate() ?: Date()
    fun getEndDateAsDate(): Date = endDate?.toDate() ?: Date()

    override fun toString(): String {
        return "Prevision(id='$id', title='$title', startDate=$startDate, endDate=$endDate)"
    }
}