package com.example.dinahvision.repository

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val username: String,
    val expiration: Long
)
