package com.islamshikhi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamshikhi.app.domain.model.ThemeMode
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.ui.common.CrashScreen
import com.islamshikhi.app.ui.main.MainScreen
import com.islamshikhi.app.ui.onboarding.OnboardingScreen
import com.islamshikhi.app.ui.theme.IslamShikhiTheme
import com.islamshikhi.app.util.CrashReporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val container = (application as IslamShikhiApp).container
        val repository = container.profileRepository
        // Flow covariant হওয়ায় UserProfile? হিসেবে নেওয়া যায়; null মানে এখনও লোড হচ্ছে।
        val profileFlow: Flow<UserProfile?> = repository.profile

        setContent {
            val profile by profileFlow.collectAsStateWithLifecycle(initialValue = null)
            val scope = rememberCoroutineScope()
            // গত বার ক্র্যাশ হয়ে থাকলে আগে সেটার কারণ দেখানো হয়
            var crashReport by remember { mutableStateOf(CrashReporter.read(this@MainActivity)) }

            IslamShikhiTheme(
                themeMode = profile?.themeMode ?: ThemeMode.SYSTEM,
                fontScale = profile?.fontScale ?: 1.0f
            ) {
                val current = profile
                val crash = crashReport
                when {
                    crash != null -> CrashScreen(
                        report = crash,
                        onContinue = {
                            CrashReporter.clear(this@MainActivity)
                            crashReport = null
                        }
                    )
                    current == null -> Unit // সংরক্ষিত তথ্য লোড হচ্ছে
                    !current.onboardingCompleted -> OnboardingScreen()
                    else -> MainScreen(
                        profile = current,
                        onResetData = { scope.launch { container.resetEverything() } }
                    )
                }
            }
        }
    }
}
