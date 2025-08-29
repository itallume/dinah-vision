package com.example.dinahvision.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import androidx.room.*

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createSession(session: SessionEntity)

    @Query("SELECT * FROM session LIMIT 1")
    suspend fun getSession(): SessionEntity?

    @Query("DELETE FROM session")
    suspend fun clearSession()
}