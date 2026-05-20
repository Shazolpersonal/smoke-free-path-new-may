package com.example.data

import kotlinx.coroutines.flow.Flow

class QuitRepository(private val quitDao: QuitDao) {

    val profileFlow: Flow<QuitProfile?> = quitDao.getProfileFlow()

    suspend fun getProfile(): QuitProfile? {
        return quitDao.getProfile()
    }

    suspend fun saveProfile(profile: QuitProfile) {
        quitDao.insertOrUpdateProfile(profile)
    }

    suspend fun incrementCravingsResisted() {
        val current = quitDao.getProfile() ?: QuitProfile(quitTimestamp = System.currentTimeMillis())
        quitDao.insertOrUpdateProfile(
            current.copy(cravingsResisted = current.cravingsResisted + 1)
        )
    }

    suspend fun incrementBreathsCompleted() {
        val current = quitDao.getProfile() ?: QuitProfile(quitTimestamp = System.currentTimeMillis())
        quitDao.insertOrUpdateProfile(
            current.copy(breathsCompleted = current.breathsCompleted + 1)
        )
    }
}
