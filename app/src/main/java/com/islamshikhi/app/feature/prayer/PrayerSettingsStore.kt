package com.islamshikhi.app.feature.prayer

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.islamshikhi.app.data.user.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** নামাজের সময়-সংক্রান্ত ব্যবহারকারীর সেটিংস (শহর, GPS, গণনা-পদ্ধতি, আসরের মাযহাব, নোটিফিকেশন)। */
class PrayerSettingsStore(private val ud: UserDataRepository) {

    private val cityKey = stringPreferencesKey("prayer_city")
    private val gpsKey = booleanPreferencesKey("prayer_use_gps")
    private val latKey = doublePreferencesKey("prayer_gps_lat")
    private val lonKey = doublePreferencesKey("prayer_gps_lon")
    private val methodKey = stringPreferencesKey("prayer_method")
    private val asrKey = stringPreferencesKey("prayer_asr")
    private val notifyKey = stringPreferencesKey("prayer_notify")
    private val customKey = booleanPreferencesKey("prayer_custom")
    private val adhanKey = stringPreferencesKey("prayer_adhan_min")
    private val jamaatKey = stringPreferencesKey("prayer_jamaat_min")

    val settings: Flow<PrayerSettings> = ud.data.map { p ->
        val notify = p[notifyKey]
        PrayerSettings(
            cityId = p[cityKey] ?: Cities.DEFAULT_ID,
            useGps = p[gpsKey] ?: false,
            gpsLat = p[latKey],
            gpsLon = p[lonKey],
            method = CalcMethodOption.byId(p[methodKey]),
            hanafiAsr = (p[asrKey] ?: "HANAFI") == "HANAFI",
            useCustom = p[customKey] ?: false,
            adhanMin = CustomTimes.decode(p[adhanKey]),
            jamaatMin = CustomTimes.decode(p[jamaatKey]),
            notify = if (notify == null) PrayerSettings().notify
            else notify.split(",").filter { it.isNotBlank() }.toSet()
        )
    }.distinctUntilChanged()

    suspend fun setCity(id: String) {
        ud.putString("prayer_city", id)
        ud.putBool("prayer_use_gps", false)
    }

    suspend fun setGpsLocation(lat: Double, lon: Double) {
        ud.putDouble("prayer_gps_lat", lat)
        ud.putDouble("prayer_gps_lon", lon)
        ud.putBool("prayer_use_gps", true)
    }

    suspend fun setUseGps(value: Boolean) = ud.putBool("prayer_use_gps", value)
    suspend fun setMethod(m: CalcMethodOption) = ud.putString("prayer_method", m.id)
    suspend fun setHanafiAsr(hanafi: Boolean) = ud.putString("prayer_asr", if (hanafi) "HANAFI" else "SHAFI")

    suspend fun setNotify(prayer: Prayer, enabled: Boolean, current: Set<String>) {
        val next = if (enabled) current + prayer.id else current - prayer.id
        ud.putString("prayer_notify", next.joinToString(","))
    }

    suspend fun setCustomMode(on: Boolean) = ud.putBool("prayer_custom", on)

    /** [minutes] null হলে ওই ওয়াক্তের আজানের সময় ডিফল্টে ফেরে। */
    suspend fun setAdhan(prayer: Prayer, minutes: Int?, current: Map<String, Int>) {
        val next = if (minutes == null) current - prayer.id else current + (prayer.id to minutes)
        ud.putString("prayer_adhan_min", CustomTimes.encode(next).ifEmpty { null })
    }

    suspend fun setJamaat(prayer: Prayer, minutes: Int?, current: Map<String, Int>) {
        val next = if (minutes == null) current - prayer.id else current + (prayer.id to minutes)
        ud.putString("prayer_jamaat_min", CustomTimes.encode(next).ifEmpty { null })
    }

    /** সব নিজের সময় মুছে ডিফল্টে ফেরে। */
    suspend fun resetToDefault() {
        ud.putBool("prayer_custom", false)
        ud.putString("prayer_adhan_min", null)
        ud.putString("prayer_jamaat_min", null)
    }
}
