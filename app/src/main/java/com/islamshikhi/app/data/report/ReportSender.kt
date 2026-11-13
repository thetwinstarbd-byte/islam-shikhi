package com.islamshikhi.app.data.report

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Base64
import com.islamshikhi.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** "ভুল জানান": লেখা ও (ঐচ্ছিক) একটি ফাইল ডেভেলপারের ইমেইলে পাঠায়। সেটআপ: docs/REPORT_SETUP.md */
object ReportSender {
    const val MAX_FILE_BYTES = 4 * 1024 * 1024
    const val MAX_TEXT = 4000

    sealed class Result {
        object Sent : Result()
        object Queued : Result()      // নেট নেই: ফোনে রাখা হলো, পরে নিজে পাঠাবে
        object NotConfigured : Result()
        data class Invalid(val reason: String) : Result()
        data class Rejected(val reason: String) : Result()   // সার্ভার পেয়েছে কিন্তু নেয়নি
    }

    fun isConfigured(): Boolean = BuildConfig.REPORT_URL.startsWith("https://")

    data class Attachment(val name: String, val mime: String, val bytes: ByteArray)

    fun readAttachment(context: Context, uri: Uri): Attachment? {
        val cr = context.contentResolver
        var name = "file"
        cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0)?.let { name = it }
        }
        val mime = cr.getType(uri) ?: "application/octet-stream"
        val bytes = cr.openInputStream(uri)?.use { ins ->
            val buf = java.io.ByteArrayOutputStream()
            val tmp = ByteArray(16 * 1024)
            while (true) {
                val n = ins.read(tmp)
                if (n < 0) break
                buf.write(tmp, 0, n)
                if (buf.size() > MAX_FILE_BYTES) return null
            }
            buf.toByteArray()
        } ?: return null
        return Attachment(name.take(80), mime, bytes)
    }

    fun buildPayload(category: String, message: String, contact: String, att: Attachment?): String {
        val o = JSONObject()
        o.put("token", BuildConfig.REPORT_TOKEN)
        o.put("category", category)
        o.put("message", message.take(MAX_TEXT))
        o.put("contact", contact.take(120))
        o.put("app", "ইসলাম শিখি ${BuildConfig.VERSION_NAME}")
        o.put("device", "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        if (att != null) {
            o.put("filename", att.name)
            o.put("mime", att.mime)
            o.put("data", Base64.encodeToString(att.bytes, Base64.NO_WRAP))
        }
        return o.toString()
    }

    /** সার্ভারের উত্তর ("ok", "denied", HTML…) ফেরত দেয়; নেটওয়ার্ক ব্যর্থ হলে IOException ছোঁড়ে। */
    private fun post(payload: String): String {
        var url = BuildConfig.REPORT_URL
        var method = "POST"
        // Apps Script প্রথমে 302 দিয়ে অন্য ঠিকানায় পাঠায় — সেটা নিজে হাতে অনুসরণ করা হয় (GET দিয়ে)
        for (hop in 0 until 6) {
            val conn = URL(url).openConnection() as HttpURLConnection
            try {
                conn.instanceFollowRedirects = false
                conn.requestMethod = method
                conn.connectTimeout = 15000
                conn.readTimeout = 45000
                if (method == "POST") {
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                    conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                }
                val code = conn.responseCode
                if (code in 301..303 || code == 307 || code == 308) {
                    val loc = conn.getHeaderField("Location") ?: return "redirect-no-location"
                    url = java.net.URL(java.net.URL(url), loc).toString()
                    method = "GET"
                    continue
                }
                val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty().trim()
                return if (code in 200..299) body else "http$code"
            } finally { conn.disconnect() }
        }
        return "too-many-redirects"
    }

    private fun classify(body: String): String? = when {
        body.startsWith("ok") -> null
        body.startsWith("<") -> "access"      // সাধারণত লগইন-পাতা: ডিপ্লয়ে "Who has access = Anyone" নয়
        else -> body.take(30)
    }

    private fun pendingDir(context: Context) = File(context.filesDir, "reports_pending").apply { mkdirs() }

    suspend fun send(context: Context, category: String, message: String, contact: String, uri: Uri?): Result = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext Result.NotConfigured
        if (message.isBlank() && uri == null) return@withContext Result.Invalid("empty")
        var att: Attachment? = null
        if (uri != null) {
            att = try { readAttachment(context, uri) } catch (e: Exception) { null }
            if (att == null) return@withContext Result.Invalid("file")
        }
        val payload = buildPayload(category, message, contact, att)
        val reply: String? = try { post(payload) } catch (e: Exception) { null }
        if (reply != null) {
            val bad = classify(reply)
            return@withContext if (bad == null) Result.Sent else Result.Rejected(bad)
        }
        run {
            // ইন্টারনেট না থাকলে ফোনে রেখে দেওয়া হয়; পরের বার অ্যাপ খুললে নিজে থেকেই আবার পাঠানো হয়
            val pending = pendingDir(context)
            if ((pending.listFiles()?.size ?: 0) < 5) File(pending, "${System.currentTimeMillis()}.json").writeText(payload, Charsets.UTF_8)
            Result.Queued
        }
    }

    /** অ্যাপ খোলার সময় ডাকা হয়: আগের না-পাঠানো রিপোর্টগুলো আবার চেষ্টা করে। */
    suspend fun retryPending(context: Context) = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext
        val files = pendingDir(context).listFiles()?.sortedBy { it.name } ?: return@withContext
        for (f in files) {
            val sent = try { classify(post(f.readText(Charsets.UTF_8))) == null } catch (e: Exception) { false }
            if (sent) f.delete() else break
        }
    }
}
