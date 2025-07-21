package com.example.dinahvision.models

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Prevision(var title:String, var description:String, var startDate: Date,
                     var endDate: Date, var predicted: Boolean, var user: User?) {
    @DocumentId
    var id: String = ""
    constructor() : this(
        title = "",
        description = "",
        startDate = java.util.Date(),
        endDate = java.util.Date(),
        predicted = false,
        user = null
    )
}