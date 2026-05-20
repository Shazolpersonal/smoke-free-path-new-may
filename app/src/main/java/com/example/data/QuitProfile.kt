package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quit_profile")
data class QuitProfile(
    @PrimaryKey val id: Int = 0,
    val quitTimestamp: Long, // Epoch millis of when the user decided to quit
    val userName: String = "ধূমপানমুক্ত যোদ্ধা", // User's name
    val cravingsResisted: Int = 0, // Number of times they successfully resisted a craving
    val breathsCompleted: Int = 0, // Number of deep breathing sessions finished
    val cigarettesPerDay: Int = 10, // Daily cigarette consumption count
    val pricePerCigarette: Double = 15.0 // Average price per cigarette in BDT
)
