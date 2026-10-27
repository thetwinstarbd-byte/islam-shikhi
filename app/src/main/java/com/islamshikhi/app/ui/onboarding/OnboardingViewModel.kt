package com.islamshikhi.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.islamshikhi.app.IslamShikhiApp
import com.islamshikhi.app.data.profile.ProfileRepository
import com.islamshikhi.app.domain.model.AgeGroup
import com.islamshikhi.app.domain.model.AppLanguage
import com.islamshikhi.app.domain.model.Gender
import com.islamshikhi.app.domain.model.LearningGoal
import com.islamshikhi.app.domain.model.LearningInterest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ধাপ: 0 স্বাগত, 1 লিঙ্গ, 2 বয়স, 3 ভাষা, 4 পছন্দ */
data class OnboardingState(
    val step: Int = 0,
    val gender: Gender? = null,
    val ageGroup: AgeGroup? = null,
    val language: AppLanguage = AppLanguage.BANGLA,
    val notificationsEnabled: Boolean = true,
    val interests: Set<LearningInterest> = emptySet(),
    val goal: LearningGoal? = null,
    val saving: Boolean = false
) {
    val canGoNext: Boolean
        get() = when (step) {
            1 -> gender != null      // লিঙ্গ নির্বাচন আবশ্যক
            2 -> ageGroup != null    // বয়সের শ্রেণি আবশ্যক
            else -> true
        }
}

class OnboardingViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun selectGender(value: Gender) = _state.update { it.copy(gender = value) }
    fun selectAgeGroup(value: AgeGroup) = _state.update { it.copy(ageGroup = value) }
    fun selectLanguage(value: AppLanguage) = _state.update { it.copy(language = value) }
    fun setNotifications(value: Boolean) = _state.update { it.copy(notificationsEnabled = value) }
    fun selectGoal(value: LearningGoal) = _state.update { it.copy(goal = value) }

    fun toggleInterest(value: LearningInterest) = _state.update {
        val next = if (value in it.interests) it.interests - value else it.interests + value
        it.copy(interests = next)
    }

    fun back() = _state.update { if (it.step > 0) it.copy(step = it.step - 1) else it }

    /** পরের ধাপে যায়; শেষ ধাপে প্রোফাইল সংরক্ষণ করে। */
    fun next() {
        val s = _state.value
        if (!s.canGoNext || s.saving) return
        if (s.step < LAST_STEP) {
            _state.update { it.copy(step = it.step + 1) }
        } else {
            val gender = s.gender ?: return
            val ageGroup = s.ageGroup ?: return
            _state.update { it.copy(saving = true) }
            viewModelScope.launch {
                repository.completeOnboarding(
                    gender = gender,
                    ageGroup = ageGroup,
                    language = s.language,
                    notificationsEnabled = s.notificationsEnabled,
                    interests = s.interests,
                    goal = s.goal
                )
                // সংরক্ষণ শেষ হলে MainActivity প্রোফাইল পরিবর্তন দেখে নিজেই মূল স্ক্রিনে যাবে।
                // "ডেটা মুছুন"-এর পর আবার সেটআপ দেখালে যেন শুরু থেকে হয়, তাই অবস্থা রিসেট।
                _state.value = OnboardingState()
            }
        }
    }

    companion object {
        const val LAST_STEP = 4

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as IslamShikhiApp
                OnboardingViewModel(app.container.profileRepository)
            }
        }
    }
}
