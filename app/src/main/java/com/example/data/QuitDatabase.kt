package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [QuitProfile::class, CravingLog::class], version = 2, exportSchema = false)
abstract class QuitDatabase : RoomDatabase() {
    abstract fun quitDao(): QuitDao

    companion object {
        @Volatile
        private var INSTANCE: QuitDatabase? = null

        fun getDatabase(context: Context): QuitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuitDatabase::class.java,
                    "mukto_jibon_database"
                )
                .fallbackToDestructiveMigration() // Automatic development schema regeneration
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
