package com.islamshikhi.app.feature.reminder

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.islamshikhi.app.IslamShikhiApp
import com.islamshikhi.app.MainActivity
import com.islamshikhi.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** অ্যালার্ম বাজলে নোটিফিকেশন দেখায় এবং পরের অ্যালার্ম বসায়। */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: return
        val body = intent.getStringExtra(ReminderScheduler.EXTRA_BODY) ?: ""
        val container = (context.applicationContext as IslamShikhiApp).container

        container.scheduler.ensureChannel()
        // আজান: ফরজ ওয়াক্তের শুরুতে, আজান-সুইচ চালু থাকলে অডিও বাজে (নোটিফিকেশন সেই সার্ভিসই দেখায়)
        val adhanPrayer = intent.getStringExtra(ReminderScheduler.EXTRA_ADHAN)
        val adhanOn = adhanPrayer != null && kotlinx.coroutines.runBlocking {
            container.userData.bool(com.islamshikhi.app.feature.adhan.AdhanPrefs.KEY_ON, true).first()
        }
        val adhanStarted = adhanOn && com.islamshikhi.app.feature.adhan.AdhanService.start(context, adhanPrayer!!, body)
        val allowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (allowed && !adhanStarted) {
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val n = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body.lineSequence().firstOrNull() ?: "")
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(open)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), n)
        }

        com.islamshikhi.app.feature.widget.NextPrayerWidget.refreshAll(context)
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try { container.scheduler.rescheduleNext() } finally { pending.finish() }
        }
    }
}
