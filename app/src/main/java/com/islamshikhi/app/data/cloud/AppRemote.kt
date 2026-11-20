package com.islamshikhi.app.data.cloud

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.islamshikhi.app.BuildConfig
import com.islamshikhi.app.util.RemoteTune
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** হোমে দেখানো এক টুকরো ঘোষণা। [id] বদলালে আগে বন্ধ করা ঘোষণাও আবার দেখা যায়। */
data class RemoteNote(val id: String, val text: String)

/**
 * ঘোষণা, আপডেটের খবর ও দূর-নিয়ন্ত্রিত সমন্বয় (Firebase Remote Config + Cloud Messaging)।
 * সবসময় চালু; ব্যবহারকারী অ্যাপের ভেতরে বন্ধ করতে পারেন না (ফোনের সিস্টেম-সেটিংসে নোটিফিকেশন বন্ধ করা যায়)।
 * Firebase সেট না করা থাকলে কিছুই করে না।
 *
 * Remote Config-এর কী (Firebase Console > Remote Config-এ বানাতে হবে):
 *   announcement_id   — যেকোনো ছোট লেখা (যেমন "2026-11-a"); বদলালে ঘোষণা নতুন হিসেবে দেখায়
 *   announcement_bn   — ঘোষণার লেখা; খালি হলে কিছু দেখায় না
 *   latest_version_code — নতুন সংস্করণের versionCode; অ্যাপের নম্বরের চেয়ে বড় হলে আপডেটের খবর দেখায়
 *   update_message_bn — আপডেটের খবরের লেখা
 *   hijri_offset_days — হিজরি তারিখ আগে/পরে সরানো (-2..2); চাঁদ দেখার পার্থক্যের জন্য
 *   prayer_adjust_{fajr|dhuhr|asr|maghrib|isha}_min — ওয়াক্তের সময়ে মিনিট যোগ/বিয়োগ (-15..15)
 */
object AppRemote {
    const val TOPIC = "announcements"

    private val _notes = MutableStateFlow<List<RemoteNote>>(emptyList())
    val notes: StateFlow<List<RemoteNote>> = _notes

    /** ফেসবুক পেজের লিংক। কোডে বসানো ডিফল্ট ফাঁকা; Remote Config-এর `facebook_page_url` থেকে আসে। ফাঁকা হলে অ্যাপে কিছু দেখায় না। */
    const val DEFAULT_PAGE_URL = ""
    private val _pageUrl = MutableStateFlow(DEFAULT_PAGE_URL)
    val pageUrl: StateFlow<String> = _pageUrl

    /** শুধু ফেসবুকের https লিংক গ্রহণযোগ্য; অন্য কিছু এলে উপেক্ষা করা হয়। */
    fun safePageUrl(raw: String): String {
        val u = raw.trim()
        val ok = Regex("^https://((www|m|web)\\.)?(facebook\\.com|fb\\.com|fb\\.me)/\\S+$").matches(u)
        return if (ok) u else ""
    }

    suspend fun refresh(context: Context) {
        if (!CloudSync.isConfigured(context)) return
        runCatching {
            val rc = FirebaseRemoteConfig.getInstance()
            rc.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(6 * 3600).build())
            rc.setDefaultsAsync(
                mapOf(
                    "announcement_id" to "", "announcement_bn" to "", "latest_version_code" to 0L, "update_message_bn" to "",
                    "hijri_offset_days" to 0L, "facebook_page_url" to "",
                    "prayer_adjust_fajr_min" to 0L, "prayer_adjust_dhuhr_min" to 0L, "prayer_adjust_asr_min" to 0L,
                    "prayer_adjust_maghrib_min" to 0L, "prayer_adjust_isha_min" to 0L
                )
            )
            applyTunables(rc) // আগের সংরক্ষিত মান (নেট না থাকলেও) আগে প্রয়োগ
            suspendCancellableCoroutine<Unit> { c ->
                rc.fetchAndActivate().addOnCompleteListener { if (c.isActive) c.resume(Unit) }
            }
            val out = ArrayList<RemoteNote>()
            val ann = rc.getString("announcement_bn").trim()
            if (ann.isNotEmpty()) out.add(RemoteNote("a:" + rc.getString("announcement_id"), ann))
            val latest = rc.getLong("latest_version_code")
            val upd = rc.getString("update_message_bn").trim()
            if (latest > BuildConfig.VERSION_CODE && upd.isNotEmpty()) out.add(RemoteNote("u:$latest", upd))
            _notes.value = out
            applyTunables(rc)
        }
    }

    private fun applyTunables(rc: FirebaseRemoteConfig) {
        _pageUrl.value = safePageUrl(rc.getString("facebook_page_url")).ifEmpty { DEFAULT_PAGE_URL }
        RemoteTune.hijriOffsetDays = RemoteTune.clampHijri(rc.getLong("hijri_offset_days"))
        RemoteTune.prayerAdjustMin = listOf("fajr", "dhuhr", "asr", "maghrib", "isha").associateWith {
            RemoteTune.clampMinutes(rc.getLong("prayer_adjust_${it}_min"))
        }.filterValues { it != 0 }
    }

    fun clear() { _notes.value = emptyList() }

    /** নোটিফিকেশন-নিবন্ধন (topic: announcements)। */
    fun setPushEnabled(context: Context, enabled: Boolean) {
        if (!CloudSync.isConfigured(context)) return
        runCatching {
            val fm = FirebaseMessaging.getInstance()
            fm.isAutoInitEnabled = enabled
            if (enabled) fm.subscribeToTopic(TOPIC) else fm.unsubscribeFromTopic(TOPIC)
        }
    }
}
