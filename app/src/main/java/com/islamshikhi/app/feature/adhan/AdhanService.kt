package com.islamshikhi.app.feature.adhan

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.islamshikhi.app.MainActivity
import com.islamshikhi.app.R
import com.islamshikhi.app.feature.prayer.Prayer

/**
 * আজান বাজায়। অ্যালার্ম বাজলে [com.islamshikhi.app.feature.reminder.AlarmReceiver] এটি চালু করে।
 * ফোরগ্রাউন্ড সার্ভিস হওয়ায় ব্যাটারি-সেভার বা স্ক্রিন বন্ধ থাকলেও আজান শেষ পর্যন্ত বাজে।
 * নোটিফিকেশনের "বন্ধ করুন" বাটনে যেকোনো সময় থামানো যায়।
 */
class AdhanService : Service() {
    private var player: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var focusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopAll(); return START_NOT_STICKY }
        val prayerId = intent?.getStringExtra(EXTRA_PRAYER) ?: run { stopSelf(); return START_NOT_STICKY }
        val label = Prayer.byId(prayerId)?.labelBn ?: ""
        val preview = intent.getBooleanExtra(EXTRA_PREVIEW, false)
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        ensureChannel(this)

        val notification = buildNotification(label, body, preview, playing = true)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) { stopSelf(); return START_NOT_STICKY }

        play(prayerId)
        return START_NOT_STICKY
    }

    private fun play(prayerId: String) {
        releasePlayer()
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(attrs).build()
            focusRequest = req
            am.requestAudioFocus(req)
        }
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "islamshikhi:adhan").apply { acquire(MAX_MILLIS) }
        try {
            val afd = resources.openRawResourceFd(AdhanPrefs.rawFor(prayerId))
            val mp = MediaPlayer()
            mp.setAudioAttributes(attrs)
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.setOnCompletionListener { finishPlaying() }
            mp.setOnErrorListener { _, _, _ -> finishPlaying(); true }
            mp.prepare()
            mp.start()
            player = mp
        } catch (e: Exception) {
            stopAll()
        }
    }

    /** আজান শেষ: নোটিফিকেশন সাধারণ হয়ে যায় (সরানো যায়), সার্ভিস বন্ধ হয়। */
    private fun finishPlaying() {
        releasePlayer()
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun stopAll() {
        releasePlayer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releasePlayer() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        runCatching { wakeLock?.takeIf { it.isHeld }?.release() }
        wakeLock = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { (getSystemService(Context.AUDIO_SERVICE) as AudioManager).abandonAudioFocusRequest(it) }
        }
        focusRequest = null
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    private fun buildNotification(label: String, body: String, preview: Boolean, playing: Boolean): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, AdhanService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(if (preview) "আজান শুনে দেখা হচ্ছে" else "$label-এর আজান")
            .setContentText(if (preview) label else body.lineSequence().firstOrNull().orEmpty())
            .setContentIntent(open)
            .setOngoing(playing)
            .setAutoCancel(!playing)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "বন্ধ করুন", stop)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "adhan_playing"
        const val NOTIFICATION_ID = 7001
        const val ACTION_STOP = "com.islamshikhi.app.ADHAN_STOP"
        const val EXTRA_PRAYER = "prayer"
        const val EXTRA_BODY = "body"
        const val EXTRA_PREVIEW = "preview"
        /** নিরাপত্তা-সীমা: যেকোনো কারণে ১০ মিনিটের বেশি চলবে না। */
        private const val MAX_MILLIS = 10 * 60 * 1000L

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "আজান", NotificationManager.IMPORTANCE_LOW).apply {
                        description = "আজান বাজার সময়ের নোটিফিকেশন (শব্দ অ্যাপ নিজে বাজায়)"
                        setSound(null, null)
                    }
                )
            }
        }

        /** আজান শুরু করে। ব্যর্থ হলে false (যেমন ব্যাকগ্রাউন্ড থেকে চালুর অনুমতি না থাকলে)। */
        fun start(context: Context, prayerId: String, body: String = "", preview: Boolean = false): Boolean = try {
            val i = Intent(context, AdhanService::class.java)
                .putExtra(EXTRA_PRAYER, prayerId).putExtra(EXTRA_BODY, body).putExtra(EXTRA_PREVIEW, preview)
            ContextCompat.startForegroundService(context, i)
            true
        } catch (e: Exception) { false }

        fun stop(context: Context) {
            runCatching { context.startService(Intent(context, AdhanService::class.java).setAction(ACTION_STOP)) }
        }
    }
}
