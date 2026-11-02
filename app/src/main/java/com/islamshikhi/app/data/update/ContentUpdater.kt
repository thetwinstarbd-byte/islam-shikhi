package com.islamshikhi.app.data.update

import android.content.Context
import com.islamshikhi.app.data.net.Http
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

sealed class UpdateResult {
    data object UpToDate : UpdateResult()
    data class Applied(val version: Int) : UpdateResult()
    data class Failed(val reason: String) : UpdateResult()
}

/**
 * কনটেন্ট (জ্ঞান-প্রবন্ধ, মাসআলা, দোয়া, কুইজ ইত্যাদি) অ্যাপ নতুন করে ইনস্টল না করেই হালনাগাদ করে।
 * শুধু ব্যবহারকারী বাটন চাপলে চলে। প্রতিটি ফাইল SHA-256 ও JSON-বৈধতা মিললে তবেই বসে; একটিও না মিললে কিছুই বদলায় না।
 * নতুন কনটেন্ট কার্যকর হয় অ্যাপ পরের বার চালু হলে। কুরআনের আরবি/অনুবাদ কখনো এভাবে বদলায় না।
 */
object ContentUpdater {
    const val MANIFEST_URL = "https://raw.githubusercontent.com/thetwinstarbd-byte/islam-shikhi/main/updates/manifest.json"
    private const val SCHEMA = 1
    private val ALLOWED = setOf("ilm", "masail", "duas", "quiz", "kids", "emergency", "library")

    private fun dir(context: Context) = File(context.filesDir, "content_updates")

    fun bundledVersion(context: Context): Int = runCatching {
        JSONObject(context.assets.open("content/version.json").bufferedReader().use { it.readText() }).getInt("version")
    }.getOrDefault(1)

    fun appliedVersion(context: Context): Int = runCatching {
        JSONObject(File(dir(context), "version.json").readText()).getInt("version")
    }.getOrDefault(0)

    /** আপডেট ফোল্ডার, যদি তার সংস্করণ অ্যাপে থাকা সংস্করণের চেয়ে নতুন হয়; নইলে পুরোনো ফোল্ডার মুছে null। */
    fun activeDir(context: Context): File? {
        val d = dir(context)
        if (!d.isDirectory) return null
        return if (appliedVersion(context) > bundledVersion(context)) d else { d.deleteRecursively(); null }
    }

    fun currentVersion(context: Context): Int = maxOf(bundledVersion(context), appliedVersion(context))

    suspend fun check(context: Context): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val m = JSONObject(String(Http.get(MANIFEST_URL, maxBytes = 200_000), Charsets.UTF_8))
            if (m.getInt("schema") != SCHEMA) return@withContext UpdateResult.Failed("এই অ্যাপের সংস্করণের সাথে মেলে না; অ্যাপ আপডেট করুন।")
            val remote = m.getInt("version")
            if (remote <= currentVersion(context)) return@withContext UpdateResult.UpToDate

            val base = MANIFEST_URL.substringBeforeLast('/') + "/"
            val files = m.getJSONObject("files")
            val staged = HashMap<String, ByteArray>()
            for (name in files.keys()) {
                if (name !in ALLOWED) continue
                val f = files.getJSONObject(name)
                val bytes = Http.get(base + f.getString("path"), timeoutMs = 25_000)
                val sha = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
                if (!sha.equals(f.getString("sha256"), ignoreCase = true)) return@withContext UpdateResult.Failed("ফাইল যাচাই মেলেনি; কিছু বদলানো হয়নি।")
                JSONObject(String(bytes, Charsets.UTF_8)) // বৈধ JSON কি না
                staged[name] = bytes
            }
            if (staged.isEmpty()) return@withContext UpdateResult.Failed("আপডেটে কোনো ফাইল নেই।")

            val tmp = File(context.filesDir, "content_updates_tmp")
            tmp.deleteRecursively(); tmp.mkdirs()
            staged.forEach { (n, b) -> File(tmp, "$n.json").writeBytes(b) }
            File(tmp, "version.json").writeText("{\"version\":$remote}")
            val d = dir(context)
            d.deleteRecursively()
            if (!tmp.renameTo(d)) return@withContext UpdateResult.Failed("সংরক্ষণ করা যায়নি।")
            UpdateResult.Applied(remote)
        } catch (e: Exception) {
            UpdateResult.Failed("আপডেট আনা যায়নি — ইন্টারনেট সংযোগ দেখে আবার চেষ্টা করুন।")
        }
    }
}
