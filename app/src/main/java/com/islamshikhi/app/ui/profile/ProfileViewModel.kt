package com.islamshikhi.app.ui.profile

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
import com.islamshikhi.app.domain.model.ThemeMode
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    fun setGender(value: Gender) = viewModelScope.launch { repository.setGender(value) }
    fun setAgeGroup(value: AgeGroup) = viewModelScope.launch { repository.setAgeGroup(value) }
    fun setLanguage(value: AppLanguage) = viewModelScope.launch { repository.setLanguage(value) }
    fun setNotifications(value: Boolean) =
        viewModelScope.launch { repository.setNotificationsEnabled(value) }
    fun setThemeMode(value: ThemeMode) = viewModelScope.launch { repository.setThemeMode(value) }
    fun setFontScale(value: Float) = viewModelScope.launch { repository.setFontScale(value) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as IslamShikhiApp
                ProfileViewModel(app.container.profileRepository)
            }
        }
    }
}
