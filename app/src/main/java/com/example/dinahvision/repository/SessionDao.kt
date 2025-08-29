package com.example.dinahvision.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions LIMIT 1")
    suspend fun getSession(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun clearSession()
}