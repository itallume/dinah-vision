package com.example.dinahvision.repository

class SessionManager(private val sessionDao: SessionDao) {

    suspend fun createSession(userId: String, username: String, durationMinutes: Int) {
        val expirationTime = System.currentTimeMillis() + durationMinutes * 60 * 1000
        val session = SessionEntity(
            userId = userId,
            username = username,
            expiration = expirationTime
        )
        sessionDao.createSession(session)
    }

    suspend fun isSessionActive(): Boolean {
        val session = sessionDao.getSession() ?: return false
        val isValid = System.currentTimeMillis() <= session.expiration
        if (!isValid) {
            clearSession()
        }
        return isValid
    }

    suspend fun getCurrentUser(): SessionEntity? {
        val session = sessionDao.getSession()
        return if (session != null && System.currentTimeMillis() <= session.expiration) {
            session
        } else {
            clearSession()
            null
        }
    }

    suspend fun clearSession() {
        sessionDao.clearSession()
    }
}