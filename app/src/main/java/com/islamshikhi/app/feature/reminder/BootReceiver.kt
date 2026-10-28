package com.islamshikhi.app.feature.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.islamshikhi.app.IslamShikhiApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** ফোন রিস্টার্ট বা অ্যাপ আপডেটের পর অ্যালার্ম আবার বসায়। */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val container = (context.applicationContext as IslamShikhiApp).container
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try { container.scheduler.rescheduleNext() } finally { pending.finish() }
        }
    }
}
