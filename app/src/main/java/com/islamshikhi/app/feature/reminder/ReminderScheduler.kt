package com.islamshikhi.app.feature.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.islamshikhi.app.data.profile.ProfileRepository
import com.islamshikhi.app.data.user.UserDataRepository
import com.islamshikhi.app.data.user.UserKeys
import com.islamshikhi.app.feature.daily.DailyVerse
import com.islamshikhi.app.feature.prayer.Prayer
import com.islamshikhi.app.feature.prayer.PrayerEngine
import com.islamshikhi.app.feature.prayer.PrayerSettingsStore
import com.islamshikhi.app.util.TimeFormatBn
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** একটি নির্ধারিত নোটিফিকেশন। */
/** [adhanPrayerId] থাকলে (ফরজ ওয়াক্তের শুরুর সময়) আজান বাজানো যাবে। */
data class ScheduledItem(val title: String, val body: String, val atMillis: Long, val adhanPrayerId: String? = null)

/**
 * সবচেয়ে কাছের নামাজ/রিমাইন্ডারের জন্য একটি অ্যালার্ম বসায়। অ্যালার্ম বাজলে [AlarmReceiver] নোটিফিকেশন দেখায়
 * এবং পরের অ্যালার্ম বসায়। ফোন রিস্টার্ট হলে [BootReceiver] আবার বসায়।
 *
 * সীমাবদ্ধতা: Android 12+ এ "সঠিক অ্যালার্ম" অনুমতি না থাকলে নোটিফিকেশন কয়েক মিনিট দেরিতে আসতে পারে।
 * কিছু ফোনের ব্যাটারি-সেভার অ্যালার্ম আটকে দেয়; তখন অ্যাপের জন্য ব্যাটারি "Unrestricted" করতে হয়।
 */
class ReminderScheduler(
    private val context: Context,
    private val userData: UserDataRepository,
    private val profile: ProfileRepository,
    private val content: com.islamshikhi.app.data.content.ContentRepository
) {
    private val prayerStore = PrayerSettingsStore(userData)
    private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun ensureChannel() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "নামাজ ও রিমাইন্ডার", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "নামাজের সময় ও আপনার রিমাইন্ডার"
                }
            )
        }
    }

    suspend fun customReminders(): List<CustomReminder> =
        userData.jsonList(KEY_REMINDERS).first().mapNotNull { CustomReminder.fromJson(it) }

    /** এখন থেকে পরবর্তী ৪৮ ঘণ্টার নির্ধারিত আইটেমের তালিকা (সময় অনুযায়ী সাজানো)। */
    suspend fun upcoming(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): List<ScheduledItem> {
        if (!profile.profile.first().notificationsEnabled) return emptyList()
        val out = ArrayList<ScheduledItem>()
        val settings = prayerStore.settings.first()
        // LocalDate.ofInstant পুরোনো Android-এ (১৩-এর আগে) নেই, তাই atZone ব্যবহার
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        for (d in 0..1) {
            val day = today.plusDays(d.toLong())
            val times = PrayerEngine.timesFor(settings, day)
            for ((p, jt) in PrayerEngine.jamaatTimes(settings, day, zone)) {
                if (p.id in settings.notify && jt > nowMillis) {
                    out += ScheduledItem(
                        title = "${p.labelBn}-এর জামাতের সময় হয়েছে",
                        body = "${settings.placeLabelBn} — ${TimeFormatBn.format(jt, zone)}",
                        atMillis = jt
                    )
                }
            }
            for (p in Prayer.obligations) {
                if (p.id in settings.notify && times[p] > nowMillis) {
                    out += ScheduledItem(
                        title = "${p.labelBn}-এর সময় হয়েছে",
                        body = "${settings.placeLabelBn} — ${TimeFormatBn.format(times[p], zone)}",
                        atMillis = times[p],
                        adhanPrayerId = p.id
                    )
                }
            }
        }
        for (r in customReminders().filter { it.enabled }) {
            var t = today.atTime(r.hour, r.minute).atZone(zone).toInstant().toEpochMilli()
            if (t <= nowMillis) t = today.plusDays(1).atTime(r.hour, r.minute).atZone(zone).toInstant().toEpochMilli()
            out += ScheduledItem(title = r.label, body = "আপনার রিমাইন্ডার", atMillis = t)
        }
        if (userData.bool(UserKeys.DAILY_VERSE_ON, false).first()) {
            val minutes = userData.int(UserKeys.DAILY_VERSE_MIN, DailyVerse.DEFAULT_MINUTES).first()
            var day = today
            var t = day.atTime(minutes / 60, minutes % 60).atZone(zone).toInstant().toEpochMilli()
            if (t <= nowMillis) {
                day = today.plusDays(1)
                t = day.atTime(minutes / 60, minutes % 60).atZone(zone).toInstant().toEpochMilli()
            }
            val msg = runCatching { DailyVerse.message(content, day.toEpochDay()) }.getOrNull()
            if (msg != null) out += ScheduledItem(title = msg.first, body = msg.second, atMillis = t)
        }
        return out.sortedBy { it.atMillis }
    }

    /** সবচেয়ে কাছের আইটেমের জন্য অ্যালার্ম বসায়; কিছু না থাকলে বাতিল করে। */
    suspend fun rescheduleNext() {
        ensureChannel()
        val now = System.currentTimeMillis()
        val items = upcoming(now + 1000)
        if (items.isEmpty()) { cancelAll(); return }
        val first = items.first()
        // একই মিনিটের আইটেম একসঙ্গে একটি নোটিফিকেশনে
        val group = items.filter { it.atMillis in first.atMillis..(first.atMillis + 60_000) }
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, group.first().title)
            putExtra(EXTRA_BODY, group.joinToString("\n") { if (it === group.first()) it.body else "${it.title} — ${it.body}" })
            group.first().adhanPrayerId?.let { putExtra(EXTRA_ADHAN, it) }
        }
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // setAlarmClock সবচেয়ে নির্ভরযোগ্য: ডোজ ও ব্যাটারি-সেভারেও ঠিক সময়ে বাজে এবং "সঠিক অ্যালার্ম" অনুমতি লাগে না।
        // (স্ট্যাটাস বারে ছোট অ্যালার্ম-আইকন দেখা যেতে পারে।) ব্যর্থ হলে সাধারণ সঠিক-অ্যালার্মে ফিরে যায়।
        val show = PendingIntent.getActivity(
            context, REQUEST_CODE + 1, Intent(context, com.islamshikhi.app.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(first.atMillis, show), pi)
        } catch (e: SecurityException) {
            val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            if (exact) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, first.atMillis, pi)
            else alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, first.atMillis, pi)
        }
    }

    fun cancelAll() {
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
    }

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    companion object {
        const val CHANNEL_ID = "prayer_and_reminders"
        const val KEY_REMINDERS = "custom_reminders"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_ADHAN = "adhan_prayer"
        private const val REQUEST_CODE = 4101
    }
}
