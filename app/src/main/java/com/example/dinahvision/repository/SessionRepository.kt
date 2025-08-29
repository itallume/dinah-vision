package com.example.dinahvision.repository
class SessionRepository(private val sessionDao: SessionDao) {

    suspend fun createSession(session: SessionEntity) {
        sessionDao.createSession(session)
    }

    suspend fun getSession(): SessionEntity? {
        return sessionDao.getSession()
    }

    suspend fun clearSession() {
        sessionDao.clearSession()
    }
}