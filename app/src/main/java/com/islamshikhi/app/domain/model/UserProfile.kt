package com.islamshikhi.app.domain.model

enum class Gender { MALE, FEMALE }

enum class AgeGroup { CHILD_TEEN, ADULT, SENIOR }

enum class AppLanguage { BANGLA, ENGLISH }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class LearningInterest { QURAN, DUA_ZIKR, SALAH, MASAIL, ZAKAT, FAMILY, SELF_IMPROVEMENT }

enum class LearningGoal { DAILY_HABIT, LEARN_BASICS, DEEPEN_KNOWLEDGE }

/**
 * ব্যবহারকারীর প্রোফাইল ও সেটিংস। শুধু ডিভাইসের DataStore-এ থাকে, কোনো সার্ভারে যায় না।
 * লিঙ্গ শুধু প্রাসঙ্গিক কনটেন্ট সাজানোর জন্য; এটি পরিচয় যাচাই নয়।
 */
data class UserProfile(
    val onboardingCompleted: Boolean = false,
    val gender: Gender? = null,
    val ageGroup: AgeGroup? = null,
    val language: AppLanguage = AppLanguage.BANGLA,
    val notificationsEnabled: Boolean = true,
    val interests: Set<LearningInterest> = emptySet(),
    val learningGoal: LearningGoal? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1.0f
)
