package com.islamshikhi.app

import android.content.Context
import com.islamshikhi.app.data.profile.ProfileRepository

/** সহজ ডিপেন্ডেন্সি কন্টেইনার। নতুন Repository এখানে যোগ হবে। */
class AppContainer(context: Context) {
    val profileRepository = ProfileRepository(context.applicationContext)
}
