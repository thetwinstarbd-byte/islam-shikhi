package com.islamshikhi.app.data.net

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * ছোট HTTPS সহায়ক। শুধু https চলে; আকার সীমিত; কোনো কুকি বা ট্র্যাকিং নেই।
 * কল করতে হবে ব্যাকগ্রাউন্ড থ্রেড থেকে (Dispatchers.IO)।
 */
object Http {
    private const val USER_AGENT = "IslamShikhi/1.0 (https://github.com/thetwinstarbd-byte/islam-shikhi)"

    fun get(url: String, timeoutMs: Int = 15_000, maxBytes: Int = 8_000_000): ByteArray {
        require(url.startsWith("https://")) { "শুধু https অনুমোদিত" }
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout = timeoutMs
            c.readTimeout = timeoutMs
            c.setRequestProperty("User-Agent", USER_AGENT)
            c.setRequestProperty("Accept-Encoding", "identity")
            c.setRequestProperty("Accept", "application/json, */*")
            val code = c.responseCode
            if (code != 200) throw IOException("HTTP $code")
            return c.inputStream.use { readCapped(it, maxBytes) }
        } finally {
            c.disconnect()
        }
    }

    /** application/x-www-form-urlencoded হিসেবে একটি ক্ষেত্র পাঠায় (Overpass-এর জন্য)। */
    fun postForm(url: String, field: String, value: String, timeoutMs: Int = 25_000, maxBytes: Int = 4_000_000): ByteArray {
        require(url.startsWith("https://")) { "শুধু https অনুমোদিত" }
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout = timeoutMs
            c.readTimeout = timeoutMs
            c.requestMethod = "POST"
            c.doOutput = true
            c.setRequestProperty("User-Agent", USER_AGENT)
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            c.setRequestProperty("Accept", "application/json, */*")
            val body = (field + "=" + URLEncoder.encode(value, "UTF-8")).toByteArray(Charsets.UTF_8)
            c.outputStream.use { it.write(body) }
            val code = c.responseCode
            if (code != 200) throw IOException("HTTP $code")
            return c.inputStream.use { readCapped(it, maxBytes) }
        } finally {
            c.disconnect()
        }
    }

    private fun readCapped(input: InputStream, maxBytes: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(16 * 1024)
        var total = 0
        while (true) {
            val n = input.read(buf)
            if (n < 0) break
            total += n
            if (total > maxBytes) throw IOException("উত্তর খুব বড়")
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }
}
