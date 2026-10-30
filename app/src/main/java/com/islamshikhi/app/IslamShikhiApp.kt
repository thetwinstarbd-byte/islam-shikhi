package com.islamshikhi.app

import android.app.Application
import com.islamshikhi.app.util.CrashReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class IslamShikhiApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
        container = AppContainer(this)
        // অ্যাপ চালু হলে নামাজ/রিমাইন্ডারের অ্যালার্ম নতুন করে বসানো হয়
        appScope.launch { runCatching { container.scheduler.rescheduleNext() } }
    }
}
