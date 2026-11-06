package com.islamshikhi.app.data.backup

import org.json.JSONArray
import org.json.JSONObject

/**
 * ব্যাকআপ ফাইলের ফরম্যাট (JSON)। শুধু এই ফোনের ব্যক্তিগত ডেটা (বুকমার্ক, নোট, ট্র্যাকার, তাসবিহ, নামাজ-সেটিং) থাকে।
 * ফাইলটি ব্যবহারকারী নিজে রাখেন; অ্যাপ এটি কোথাও পাঠায় না।
 */
object BackupCodec {
    const val FORMAT = "islam-shikhi-backup"
    const val VERSION = 1
    private const val MAX_ENTRIES = 20_000
    private const val MAX_KEY = 200

    fun encode(values: Map<String, Any>, createdAt: String): String {
        val entries = JSONArray()
        for ((k, v) in values.toSortedMap()) {
            val o = JSONObject().put("k", k)
            when (v) {
                is String -> o.put("t", "s").put("v", v)
                is Int -> o.put("t", "i").put("v", v)
                is Long -> o.put("t", "l").put("v", v)
                is Boolean -> o.put("t", "b").put("v", v)
                is Double -> o.put("t", "d").put("v", v)
                is Float -> o.put("t", "f").put("v", v.toDouble())
                is Set<*> -> o.put("t", "ss").put("v", JSONArray(v.filterIsInstance<String>().sorted()))
                else -> continue
            }
            entries.put(o)
        }
        return JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("created", createdAt)
            .put("entries", entries)
            .toString(1)
    }

    /** ভুল বা অচেনা ফাইল হলে IllegalArgumentException ছোঁড়ে; কিছুই বদলায় না। */
    fun decode(json: String): Map<String, Any> {
        val root = try { JSONObject(json) } catch (e: Exception) { throw IllegalArgumentException("ফাইলটি ব্যাকআপ ফাইল নয়") }
        require(root.optString("format") == FORMAT) { "ফাইলটি ইসলাম শিখি-র ব্যাকআপ নয়" }
        require(root.optInt("version", 0) in 1..VERSION) { "ব্যাকআপ ফাইলের সংস্করণ এই অ্যাপে চলবে না" }
        val arr = root.optJSONArray("entries") ?: throw IllegalArgumentException("ব্যাকআপে কোনো তথ্য নেই")
        require(arr.length() <= MAX_ENTRIES) { "ব্যাকআপ ফাইল অস্বাভাবিক বড়" }
        val out = LinkedHashMap<String, Any>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val k = o.optString("k")
            require(k.isNotBlank() && k.length <= MAX_KEY) { "ব্যাকআপে ভুল তথ্য আছে" }
            val value: Any = when (o.optString("t")) {
                "s" -> o.getString("v")
                "i" -> o.getInt("v")
                "l" -> o.getLong("v")
                "b" -> o.getBoolean("v")
                "d" -> o.getDouble("v")
                "f" -> o.getDouble("v").toFloat()
                "ss" -> o.getJSONArray("v").let { a -> List(a.length()) { a.getString(it) }.toSet() }
                else -> throw IllegalArgumentException("ব্যাকআপে অচেনা তথ্য আছে")
            }
            out[k] = value
        }
        return out
    }
}
