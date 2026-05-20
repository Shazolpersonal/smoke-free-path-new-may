package com.example.data

import kotlinx.coroutines.flow.Flow

class QuitRepository(private val quitDao: QuitDao) {

    val profileFlow: Flow<QuitProfile?> = quitDao.getProfileFlow()

    val cravingLogsFlow: Flow<List<CravingLog>> = quitDao.getAllCravingLogsFlow()

    suspend fun getProfile(): QuitProfile? {
        return quitDao.getProfile()
    }

    suspend fun saveProfile(profile: QuitProfile) {
        quitDao.insertOrUpdateProfile(profile)
    }

    suspend fun addCravingLog(log: CravingLog) {
        quitDao.insertCravingLog(log)
        // Also automatically increment overall cravings count
        incrementCravingsResisted()
    }

    suspend fun clearCravingLogs() {
        quitDao.clearAllCravingLogs()
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
