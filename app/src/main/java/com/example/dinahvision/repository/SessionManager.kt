package com.example.dinahvision.repository

class SessionManager(private val sessionDao: SessionDao) {

    suspend fun createSession(userId: String, username: String, durationMinutes: Long) {
        val now = System.currentTimeMillis()
        val expiration = now + durationMinutes * 60_000
        val session = SessionEntity(userId, username, now, expiration)
        sessionDao.saveSession(session)
    }

    suspend fun getActiveSession(): SessionEntity? {
        val session = sessionDao.getSession()
        val now = System.currentTimeMillis()
        return if (session != null && now < session.expirationTime) {
            session
        } else {
            sessionDao.clearSession()
            null
        }
    }

    suspend fun clearSession() {
        sessionDao.clearSession()
    }
}