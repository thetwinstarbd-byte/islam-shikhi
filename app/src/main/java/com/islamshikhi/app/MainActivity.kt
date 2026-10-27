package com.islamshikhi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamshikhi.app.domain.model.ThemeMode
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.ui.main.MainScreen
import com.islamshikhi.app.ui.onboarding.OnboardingScreen
import com.islamshikhi.app.ui.theme.IslamShikhiTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as IslamShikhiApp).container.profileRepository
        // Flow covariant হওয়ায় UserProfile? হিসেবে নেওয়া যায়; null মানে এখনও লোড হচ্ছে।
        val profileFlow: Flow<UserProfile?> = repository.profile

        setContent {
            val profile by profileFlow.collectAsStateWithLifecycle(initialValue = null)
            val scope = rememberCoroutineScope()

            IslamShikhiTheme(
                themeMode = profile?.themeMode ?: ThemeMode.SYSTEM,
                fontScale = profile?.fontScale ?: 1.0f
            ) {
                val current = profile
                when {
                    current == null -> Unit // সংরক্ষিত তথ্য লোড হচ্ছে
                    !current.onboardingCompleted -> OnboardingScreen()
                    else -> MainScreen(
                        profile = current,
                        onResetData = { scope.launch { repository.clearAll() } }
                    )
                }
            }
        }
    }
}
