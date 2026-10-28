package com.islamshikhi.app

import android.content.Context
import com.islamshikhi.app.data.content.ContentRepository
import com.islamshikhi.app.data.profile.ProfileRepository
import com.islamshikhi.app.data.user.UserDataRepository
import com.islamshikhi.app.feature.prayer.PrayerSettingsStore
import com.islamshikhi.app.feature.reminder.ReminderScheduler

/** সহজ ডিপেন্ডেন্সি কন্টেইনার। নতুন Repository এখানে যোগ হবে। */
class AppContainer(private val context: Context) {
    val profileRepository = ProfileRepository(context.applicationContext)
    val userData = UserDataRepository(context.applicationContext)
    val content = ContentRepository(context.applicationContext)
    val prayerSettings = PrayerSettingsStore(userData)
    val scheduler = ReminderScheduler(context.applicationContext, userData, profileRepository)

    /** "সব ডেটা মুছুন": প্রোফাইল, ব্যক্তিগত ডেটা ও নির্ধারিত অ্যালার্ম — সবই। */
    suspend fun resetEverything() {
        scheduler.cancelAll()
        userData.clearAll()
        profileRepository.clearAll()
    }
}
