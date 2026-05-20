package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.QuitDatabase
import com.example.data.QuitProfile
import com.example.data.QuitRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface BreathingState {
    object Idle : BreathingState
    data class Active(val phase: Phase, val secondsRemaining: Int) : BreathingState

    enum class Phase {
        INHALE, // ৪ সেকেন্ড শ্বাস নিন
        HOLD,   // ৪ সেকেন্ড ধরে রাখুন
        EXHALE  // ৪ সেকেন্ড ক্ষান্ত হয়ে ছাড়ুন
    }
}

data class TimeRemaining(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val isQuitDateInFuture: Boolean = false
)

data class QuitUiState(
    val profile: QuitProfile = QuitProfile(quitTimestamp = System.currentTimeMillis()),
    val timePassed: TimeRemaining = TimeRemaining(),
    val breathingState: BreathingState = BreathingState.Idle,
    val showCustomizeDialog: Boolean = false,
    val showSuccessMessage: String? = null
)

class QuitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuitRepository
    private val _uiState = MutableStateFlow(QuitUiState())
    val uiState: StateFlow<QuitUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var breathJob: Job? = null

    init {
        val database = QuitDatabase.getDatabase(application)
        repository = QuitRepository(database.quitDao())

        // Observe Room DB profile loading
        viewModelScope.launch {
            repository.profileFlow.collect { dbProfile ->
                val profile = dbProfile ?: run {
                    // Create a default initial profile with the current time (May 20, 2026)
                    val defaultProfile = QuitProfile(
                        quitTimestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 2) // 2 hours ago as helper
                    )
                    repository.saveProfile(defaultProfile)
                    defaultProfile
                }
                _uiState.update { it.copy(profile = profile) }
                startRealtimeCountdown(profile.quitTimestamp)
            }
        }
    }

    private fun startRealtimeCountdown(quitTimestamp: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val isFuture = quitTimestamp > now
                val diff = if (isFuture) quitTimestamp - now else now - quitTimestamp

                val d = diff / (1000 * 60 * 60 * 24)
                val h = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                val m = (diff % (1000 * 60 * 60)) / (1000 * 60)
                val s = (diff % (1000 * 60)) / 1000

                _uiState.update {
                    it.copy(
                        timePassed = TimeRemaining(
                            days = d,
                            hours = h,
                            minutes = m,
                            seconds = s,
                            isQuitDateInFuture = isFuture
                        )
                    )
                }
                delay(1000L)
            }
        }
    }

    fun updateQuitTimestamp(newTimestamp: Long) {
        viewModelScope.launch {
            val current = _uiState.value.profile
            val updated = current.copy(quitTimestamp = newTimestamp)
            repository.saveProfile(updated)
            _uiState.update { it.copy(showCustomizeDialog = false, showSuccessMessage = "ধূমপান বর্জনের সময় সফলভাবে আপডেট করা হয়েছে!") }
            startRealtimeCountdown(newTimestamp)
        }
    }

    fun setShowCustomize(show: Boolean) {
        _uiState.update { it.copy(showCustomizeDialog = show) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(showSuccessMessage = null) }
    }

    fun resistCraving() {
        viewModelScope.launch {
            repository.incrementCravingsResisted()
            _uiState.update { it.copy(showSuccessMessage = "চমৎকার! আপনি একটি ধূমপানের তীব্র ইচ্ছে সফলভাবে প্রতিহত করেছেন!") }
        }
    }

    fun startBreathingExercise() {
        breathJob?.cancel()
        breathJob = viewModelScope.launch {
            // 4s Inhale, 4s Hold, 4s Exhale - repeat 3 times
            for (cycle in 1..3) {
                // Inhale
                for (sec in 4 downTo 1) {
                    _uiState.update { it.copy(breathingState = BreathingState.Active(BreathingState.Phase.INHALE, sec)) }
                    delay(1000L)
                }
                // Hold
                for (sec in 4 downTo 1) {
                    _uiState.update { it.copy(breathingState = BreathingState.Active(BreathingState.Phase.HOLD, sec)) }
                    delay(1000L)
                }
                // Exhale
                for (sec in 4 downTo 1) {
                    _uiState.update { it.copy(breathingState = BreathingState.Active(BreathingState.Phase.EXHALE, sec)) }
                    delay(1000L)
                }
            }
            // Finished!
            repository.incrementBreathsCompleted()
            _uiState.update {
                it.copy(
                    breathingState = BreathingState.Idle,
                    showSuccessMessage = "রিল্যাক্সড! সফলভাবে ফুসফুসের রিলাক্সেশন সম্পন্ন হয়েছে!"
                )
            }
        }
    }

    fun cancelBreathingExercise() {
        breathJob?.cancel()
        _uiState.update { it.copy(breathingState = BreathingState.Idle) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        breathJob?.cancel()
    }
}
