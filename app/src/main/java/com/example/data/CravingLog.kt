package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "craving_logs")
data class CravingLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val trigger: String, // e.g. "কাজের চাপ", "আড্ডা বা বন্ধু", "খাবারের পর", "উদ্বেগ"
    val severity: String, // e.g. "তীব্র", "মাঝারি", "সামান্য"
    val copingMethod: String // e.g. "গভীর শ্বাস-প্রশ্বাস", "১ গ্লাস পানি", "হাঁটাহাঁটি"
)
