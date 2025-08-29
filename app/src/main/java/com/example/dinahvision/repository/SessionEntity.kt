package com.example.dinahvision.repository

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val loginTime: Long,
    val expirationTime: Long
)
