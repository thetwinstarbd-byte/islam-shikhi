package com.islamshikhi.app.data.cloud

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.islamshikhi.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** হোমে দেখানো এক টুকরো ঘোষণা। [id] বদলালে আগে বন্ধ করা ঘোষণাও আবার দেখা যায়। */
data class RemoteNote(val id: String, val text: String)

/**
 * ঐচ্ছিক "ঘোষণা ও আপডেটের খবর" (Firebase Remote Config + Cloud Messaging)।
 * ডিফল্টে বন্ধ: ব্যবহারকারী সেটিংসে চালু না করলে Firebase-এর সাথে কোনো যোগাযোগ হয় না।
 * Firebase সেট না করা থাকলে কিছুই করে না।
 *
 * Remote Config-এর কী (Firebase Console > Remote Config-এ বানাতে হবে):
 *   announcement_id   — যেকোনো ছোট লেখা (যেমন "2026-11-a"); বদলালে ঘোষণা নতুন হিসেবে দেখায়
 *   announcement_bn   — ঘোষণার লেখা; খালি হলে কিছু দেখায় না
 *   latest_version_code — নতুন সংস্করণের versionCode; অ্যাপের নম্বরের চেয়ে বড় হলে আপডেটের খবর দেখায়
 *   update_message_bn — আপডেটের খবরের লেখা
 */
object AppRemote {
    const val TOPIC = "announcements"

    private val _notes = MutableStateFlow<List<RemoteNote>>(emptyList())
    val notes: StateFlow<List<RemoteNote>> = _notes

    suspend fun refresh(context: Context) {
        if (!CloudSync.isConfigured(context)) return
        runCatching {
            val rc = FirebaseRemoteConfig.getInstance()
            rc.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(6 * 3600).build())
            rc.setDefaultsAsync(
                mapOf("announcement_id" to "", "announcement_bn" to "", "latest_version_code" to 0L, "update_message_bn" to "")
            )
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
        }
    }

    fun clear() { _notes.value = emptyList() }

    /** চালু করলে বার্তা নেওয়া শুরু; বন্ধ করলে Firebase-এর স্বয়ংক্রিয় নিবন্ধনও বন্ধ। */
    fun setPushEnabled(context: Context, enabled: Boolean) {
        if (!CloudSync.isConfigured(context)) return
        runCatching {
            val fm = FirebaseMessaging.getInstance()
            fm.isAutoInitEnabled = enabled
            if (enabled) fm.subscribeToTopic(TOPIC) else fm.unsubscribeFromTopic(TOPIC)
        }
    }
}
