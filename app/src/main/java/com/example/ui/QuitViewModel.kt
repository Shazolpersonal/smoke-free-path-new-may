package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface BreathingState {
    object Idle : BreathingState
    data class Active(
        val phase: Phase,
        val secondsRemaining: Int,
        val currentCycle: Int,
        val totalCycles: Int
    ) : BreathingState

    enum class Phase {
        INHALE, // শ্বাস নিন
        HOLD,   // ধরে রাখুন
        EXHALE, // প্রশ্বাস ছাড়ুন
        HOLD_POST // বক্স ব্রিদিংয়ের বাড়তি বিরতি
    }
}

enum class BreathingStyle(val titleBengali: String, val shortDesc: String, val totalCycles: Int) {
    BOX_BREATHING(
        "বক্স ব্রিদিং (Box Breathing)",
        "৪s শ্বাস নিন → ৪s ধরে রাখুন → ৪s ছাড়ুন → ৪s খালি ফুসফুসে থাকুন। মন শান্ত ও স্থির করার জন্য আদর্শ।",
        3
    ),
    LUNG_CHEST_RESTORE(
        "ফুসফুস পুনরুদ্ধার (4-7-8 Technique)",
        "৪s শ্বাস নিন → ৭s ধরে রাখুন → ৮s পুরো শ্বাস মুখ দিয়ে ছাড়ুন। তীব্র ধূমপানের ইচ্ছা তাড়াতে শ্রেষ্ঠ।",
        3
    )
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
    val selectedBreathingStyle: BreathingStyle = BreathingStyle.BOX_BREATHING,
    val cravingLogs: List<CravingLog> = emptyList(),
    val showCustomizeDialog: Boolean = false,
    val showLogCravingDialog: Boolean = false,
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

        // Observe Room DB profile
        viewModelScope.launch {
            repository.profileFlow.collect { dbProfile ->
                val profile = dbProfile ?: run {
                    // Default starting point (Approx 2 hours ago for instant progress values)
                    val defaultProfile = QuitProfile(
                        quitTimestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 2)
                    )
                    repository.saveProfile(defaultProfile)
                    defaultProfile
                }
                _uiState.update { it.copy(profile = profile) }
                startRealtimeCountdown(profile.quitTimestamp)
            }
        }

        // Observe Room DB craving logs
        viewModelScope.launch {
            repository.cravingLogsFlow.collect { logs ->
                _uiState.update { it.copy(cravingLogs = logs) }
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
            _uiState.update {
                it.copy(
                    showCustomizeDialog = false,
                    showSuccessMessage = "ধূমপান বর্জনের সময় সফলভাবে আপডেট করা হয়েছে!"
                )
            }
            startRealtimeCountdown(newTimestamp)
        }
    }

    fun updateProfileSettings(userName: String, quitTimestamp: Long, cigarettesPerDay: Int, pricePerCigarette: Double) {
        viewModelScope.launch {
            val current = _uiState.value.profile
            val updated = current.copy(
                userName = userName,
                quitTimestamp = quitTimestamp,
                cigarettesPerDay = cigarettesPerDay,
                pricePerCigarette = pricePerCigarette
            )
            repository.saveProfile(updated)
            _uiState.update {
                it.copy(
                    showCustomizeDialog = false,
                    showSuccessMessage = "প্রোফাইল এবং সাশ্রয় লক্ষ্যমাত্রা সফলভাবে আপডেট করা হয়েছে!"
                )
            }
            startRealtimeCountdown(quitTimestamp)
        }
    }

    fun setBreathingStyle(style: BreathingStyle) {
        _uiState.update { it.copy(selectedBreathingStyle = style) }
    }

    fun setShowCustomize(show: Boolean) {
        _uiState.update { it.copy(showCustomizeDialog = show) }
    }

    fun setShowLogCraving(show: Boolean) {
        _uiState.update { it.copy(showLogCravingDialog = show) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(showSuccessMessage = null) }
    }

    // Traditional direct resist function
    fun resistCraving() {
        viewModelScope.launch {
            repository.incrementCravingsResisted()
            _uiState.update { it.copy(showSuccessMessage = "ধন্যবাদ! ধূমপানের তীব্র ইচ্ছে সফলভাবে প্রতিহত করা হয়েছে!") }
        }
    }

    // High interactive detailed craving log helper
    fun saveDetailedCravingLog(trigger: String, severity: String, copingMethod: String) {
        viewModelScope.launch {
            val log = CravingLog(
                timestamp = System.currentTimeMillis(),
                trigger = trigger,
                severity = severity,
                copingMethod = copingMethod
            )
            repository.addCravingLog(log)
            _uiState.update {
                it.copy(
                    showLogCravingDialog = false,
                    showSuccessMessage = "ডায়েরি আপডেট হয়েছে! আপনি ধূমপানের তীব্র ইচ্ছে সফলভাবে সামলে নিয়েছেন।"
                )
            }
        }
    }

    fun clearAllCravingLogs() {
        viewModelScope.launch {
            repository.clearCravingLogs()
            _uiState.update { it.copy(showSuccessMessage = "ডায়েরির পূর্ববর্তী সকল ইতিহাস মুছে ফেলা হয়েছে।") }
        }
    }

    fun startBreathingExercise() {
        breathJob?.cancel()
        val style = _uiState.value.selectedBreathingStyle
        breathJob = viewModelScope.launch {
            val cycleLimit = style.totalCycles
            for (cycle in 1..cycleLimit) {
                if (style == BreathingStyle.BOX_BREATHING) {
                    // Box Breathing Cycles: 4s Inhale, 4s Hold, 4s Exhale, 4s Hold Idle
                    for (sec in 4 downTo 1) {
                        _uiState.update { it.copy(breathingState = BreathingState.Active(BreathingState.Phase.INHALE, sec, cycle, cycleLimit)) }
                        delay(1000L)
                    }
                    for (sec in 4 downTo 1) {
                        _uiState.update { it.copy(breathingState = BreathingState.Active(BreathingState.Phase.HOLD, sec, cycle, cycleLimit)) }
                        delay(1000L)
                    }
                    for (sec in 4 downTo 1) {
                        _uiState.update { it.copy(breathingState = BreathingState.Active(BreathingState.Phase.EXHALE, sec, cycle, cycleLimit)) }
                        delay(1000L)
                    }
                    for (sec in 4 downTo 1) {
                        _uiState.update { it.copy(breathingState = BreathingState.Active(BreathingState.Phase.HOLD_POST, sec, cycle, cycleLimit)) }
                        delay(1000L)
                    }
                } else {
                    // 4-7-8 Technique: 4s Inhale, 7s Hold, 8s Exhale
                    for (sec in 4 downTo 1) {
                        _uiState.update { it.copy(breathingState = BreathingState.Active(BreathingState.Phase.INHALE, sec, cycle, cycleLimit)) }
                        delay(1000L)
                    }
                    for (sec in 7 downTo 1) {
                        _uiState.update { it.copy(breathingState = BreathingState.Active(BreathingState.Phase.HOLD, sec, cycle, cycleLimit)) }
                        delay(1000L)
                    }
                    for (sec in 8 downTo 1) {
                        _uiState.update { it.copy(breathingState = BreathingState.Active(BreathingState.Phase.EXHALE, sec, cycle, cycleLimit)) }
                        delay(1000L)
                    }
                }
            }
            // Finished!
            repository.incrementBreathsCompleted()
            _uiState.update {
                it.copy(
                    breathingState = BreathingState.Idle,
                    showSuccessMessage = "প্রশান্তি সম্পন্ন! ফুসফুসের ক্ষমতা বৃদ্ধি এবং মন শান্ত করার ব্যায়াম সম্পন্ন হয়েছে।"
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
