package com.islamshikhi.app.data.user

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.io.IOException

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "userdata")

/**
 * ব্যবহারকারীর ব্যক্তিগত ডেটা: বুকমার্ক, নোট, তাসবিহ, আমল, কুরআন পড়ার অগ্রগতি, নামাজ ও রিমাইন্ডার সেটিংস ইত্যাদি।
 * সবই শুধু এই ফোনের DataStore-এ থাকে; কোনো সার্ভারে যায় না।
 * (Room-এর বদলে DataStore: এই ডেটা ছোট ও সরল, আর বিল্ড সরল রাখা হয়েছে।)
 */
class UserDataRepository(private val context: Context) {

    private val store get() = context.userDataStore

    val data: Flow<Preferences> = store.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    // ---------- সাধারণ সহায়ক ----------
    fun string(name: String): Flow<String?> = data.map { it[stringPreferencesKey(name)] }.distinctUntilChanged()
    fun int(name: String, default: Int = 0): Flow<Int> =
        data.map { it[intPreferencesKey(name)] ?: default }.distinctUntilChanged()
    fun bool(name: String, default: Boolean = false): Flow<Boolean> =
        data.map { it[booleanPreferencesKey(name)] ?: default }.distinctUntilChanged()
    fun double(name: String): Flow<Double?> = data.map { it[doublePreferencesKey(name)] }.distinctUntilChanged()
    fun set(name: String): Flow<Set<String>> =
        data.map { it[stringSetPreferencesKey(name)] ?: emptySet() }.distinctUntilChanged()

    suspend fun putString(name: String, value: String?) {
        store.edit { if (value == null) it.remove(stringPreferencesKey(name)) else it[stringPreferencesKey(name)] = value }
    }

    suspend fun putInt(name: String, value: Int) { store.edit { it[intPreferencesKey(name)] = value } }
    suspend fun putBool(name: String, value: Boolean) { store.edit { it[booleanPreferencesKey(name)] = value } }
    suspend fun putDouble(name: String, value: Double) { store.edit { it[doublePreferencesKey(name)] = value } }
    suspend fun putSet(name: String, value: Set<String>) { store.edit { it[stringSetPreferencesKey(name)] = value } }

    suspend fun toggleInSet(name: String, item: String) {
        store.edit {
            val key = stringSetPreferencesKey(name)
            val cur = it[key] ?: emptySet()
            it[key] = if (item in cur) cur - item else cur + item
        }
    }

    suspend fun incrementInt(name: String, by: Int = 1) {
        store.edit {
            val key = intPreferencesKey(name)
            it[key] = ((it[key] ?: 0) + by).coerceAtLeast(0)
        }
    }

    // ---------- JSON তালিকা (রিমাইন্ডার, যাকাতের রেকর্ড, লক্ষ্য ইত্যাদি) ----------
    fun jsonList(name: String): Flow<List<String>> = string(name).map { raw ->
        if (raw.isNullOrBlank()) emptyList() else runCatching {
            val a = JSONArray(raw); List(a.length()) { a.getString(it) }
        }.getOrDefault(emptyList())
    }

    suspend fun putJsonList(name: String, items: List<String>) {
        putString(name, JSONArray(items).toString())
    }

    // ---------- বুকমার্ক ও নোট ----------
    val bookmarks: Flow<Set<String>> = set(KEY_BOOKMARKS)
    suspend fun toggleBookmark(key: String) = toggleInSet(KEY_BOOKMARKS, key)

    fun note(key: String): Flow<String> = string("note:$key").map { it ?: "" }
    suspend fun setNote(key: String, text: String) = putString("note:$key", text.trim().ifEmpty { null })

    /** সব নোট: কী -> লেখা। */
    val notes: Flow<Map<String, String>> = data.map { prefs ->
        prefs.asMap().mapNotNull { (k, v) ->
            if (k.name.startsWith("note:") && v is String) k.name.removePrefix("note:") to v else null
        }.toMap()
    }

    // ---------- সব মুছে ফেলা ----------
    suspend fun clearAll() { store.edit { it.clear() } }

    companion object {
        const val KEY_BOOKMARKS = "bookmarks"
    }
}
