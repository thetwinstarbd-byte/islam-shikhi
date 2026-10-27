package com.islamshikhi.app.data.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.islamshikhi.app.domain.model.AgeGroup
import com.islamshikhi.app.domain.model.AppLanguage
import com.islamshikhi.app.domain.model.Gender
import com.islamshikhi.app.domain.model.LearningGoal
import com.islamshikhi.app.domain.model.LearningInterest
import com.islamshikhi.app.domain.model.ThemeMode
import com.islamshikhi.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profile")

class ProfileRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val GENDER = stringPreferencesKey("gender")
        val AGE_GROUP = stringPreferencesKey("age_group")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val INTERESTS = stringSetPreferencesKey("interests")
        val GOAL = stringPreferencesKey("learning_goal")
        val THEME = stringPreferencesKey("theme_mode")
        val FONT_SCALE = floatPreferencesKey("font_scale")
    }

    val profile: Flow<UserProfile> = context.profileDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { it.toProfile() }

    suspend fun completeOnboarding(
        gender: Gender,
        ageGroup: AgeGroup,
        language: AppLanguage,
        notificationsEnabled: Boolean,
        interests: Set<LearningInterest>,
        goal: LearningGoal?
    ) {
        context.profileDataStore.edit { p ->
            p[Keys.GENDER] = gender.name
            p[Keys.AGE_GROUP] = ageGroup.name
            p[Keys.LANGUAGE] = language.name
            p[Keys.NOTIFICATIONS] = notificationsEnabled
            p[Keys.INTERESTS] = interests.map { it.name }.toSet()
            if (goal != null) p[Keys.GOAL] = goal.name else p.remove(Keys.GOAL)
            p[Keys.ONBOARDING_DONE] = true
        }
    }

    suspend fun setGender(value: Gender) {
        context.profileDataStore.edit { it[Keys.GENDER] = value.name }
    }

    suspend fun setAgeGroup(value: AgeGroup) {
        context.profileDataStore.edit { it[Keys.AGE_GROUP] = value.name }
    }

    suspend fun setLanguage(value: AppLanguage) {
        context.profileDataStore.edit { it[Keys.LANGUAGE] = value.name }
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.profileDataStore.edit { it[Keys.NOTIFICATIONS] = value }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.profileDataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setFontScale(scale: Float) {
        context.profileDataStore.edit { it[Keys.FONT_SCALE] = scale.coerceIn(0.85f, 1.5f) }
    }

    /** সব স্থানীয় প্রোফাইল ও সেটিংস মুছে ফেলে (গোপনীয়তা: "ডেটা মুছুন")। */
    suspend fun clearAll() {
        context.profileDataStore.edit { it.clear() }
    }

    private fun Preferences.toProfile(): UserProfile = UserProfile(
        onboardingCompleted = this[Keys.ONBOARDING_DONE] ?: false,
        gender = this[Keys.GENDER].toEnumOrNull<Gender>(),
        ageGroup = this[Keys.AGE_GROUP].toEnumOrNull<AgeGroup>(),
        language = this[Keys.LANGUAGE].toEnumOrNull<AppLanguage>() ?: AppLanguage.BANGLA,
        notificationsEnabled = this[Keys.NOTIFICATIONS] ?: true,
        interests = (this[Keys.INTERESTS] ?: emptySet())
            .mapNotNull { it.toEnumOrNull<LearningInterest>() }
            .toSet(),
        learningGoal = this[Keys.GOAL].toEnumOrNull<LearningGoal>(),
        themeMode = this[Keys.THEME].toEnumOrNull<ThemeMode>() ?: ThemeMode.SYSTEM,
        fontScale = this[Keys.FONT_SCALE] ?: 1.0f
    )
}

private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
    this?.let { name -> runCatching { enumValueOf<T>(name) }.getOrNull() }
