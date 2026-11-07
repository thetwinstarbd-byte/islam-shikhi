package com.islamshikhi.app.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.islamshikhi.app.IslamShikhiApp
import com.islamshikhi.app.MainActivity
import com.islamshikhi.app.R
import com.islamshikhi.app.feature.prayer.PrayerEngine
import com.islamshikhi.app.util.TimeFormatBn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * হোম স্ক্রিন উইজেট: পরের নামাজের নাম ও সময়। গণনা পুরোটাই ফোনে, ইন্টারনেট লাগে না।
 * সিস্টেম প্রতি ৩০ মিনিটে এবং প্রতিটি নামাজের রিমাইন্ডার বাজলে নতুন করে আঁকা হয়।
 */
class NextPrayerWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        refreshAll(context)
    }

    companion object {
        /** সব উইজেট আবার আঁকে। যেকোনো থ্রেড থেকে ডাকা যায়। */
        fun refreshAll(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = manager.getAppWidgetIds(ComponentName(app, NextPrayerWidget::class.java))
            if (ids.isEmpty()) return
            CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                val container = (app as IslamShikhiApp).container
                val settings = container.prayerSettings.settings.first()
                val next = runCatching { PrayerEngine.next(settings, System.currentTimeMillis()) }.getOrNull()
                val open = PendingIntent.getActivity(
                    app, 0, Intent(app, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                for (id in ids) {
                    val v = RemoteViews(app.packageName, R.layout.widget_next_prayer)
                    if (next != null) {
                        v.setTextViewText(R.id.widget_prayer, next.prayer.labelBn)
                        v.setTextViewText(R.id.widget_time, TimeFormatBn.format(next.atMillis))
                        v.setTextViewText(R.id.widget_place, settings.placeLabelBn)
                    } else {
                        v.setTextViewText(R.id.widget_prayer, "সময় পাওয়া যায়নি")
                    }
                    v.setOnClickPendingIntent(R.id.widget_root, open)
                    manager.updateAppWidget(id, v)
                }
            }
        }
    }
}
