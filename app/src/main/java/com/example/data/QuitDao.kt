package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuitDao {
    @Query("SELECT * FROM quit_profile WHERE id = 0")
    fun getProfileFlow(): Flow<QuitProfile?>

    @Query("SELECT * FROM quit_profile WHERE id = 0")
    suspend fun getProfile(): QuitProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: QuitProfile)
}
