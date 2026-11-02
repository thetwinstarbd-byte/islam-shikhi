package com.islamshikhi.app.feature.mosque

import com.islamshikhi.app.data.net.Http
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Mosque(val name: String?, val lat: Double, val lon: Double, val distanceM: Int, val address: String?)

/**
 * কাছের মসজিদ খোঁজে OpenStreetMap-এর ডেটা থেকে (Overpass API, বিনা চাবিতে)।
 * সার্ভারে যায় শুধু আনুমানিক স্থানাঙ্ক ও ব্যাসার্ধ; কোনো পরিচয়-তথ্য যায় না।
 * ডেটা স্বেচ্ছাসেবকদের তোলা, তাই কিছু মসজিদ বাদ বা নামহীন থাকতে পারে। ODbL: © OpenStreetMap contributors।
 */
object MosqueFinder {
    private val ENDPOINTS = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter"
    )

    suspend fun nearby(lat: Double, lon: Double, radiusM: Int): List<Mosque> = withContext(Dispatchers.IO) {
        val query = "[out:json][timeout:20];(" +
            "node[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"](around:$radiusM,$lat,$lon);" +
            "way[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"](around:$radiusM,$lat,$lon);" +
            ");out center 80;"
        var last: Exception? = null
        for (url in ENDPOINTS) {
            try {
                val raw = Http.postForm(url, "data", query)
                return@withContext parse(String(raw, Charsets.UTF_8), lat, lon)
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IllegalStateException("অজানা ত্রুটি")
    }

    internal fun parse(json: String, lat: Double, lon: Double): List<Mosque> {
        val els = JSONObject(json).optJSONArray("elements") ?: return emptyList()
        val out = ArrayList<Mosque>()
        for (i in 0 until els.length()) {
            val e = els.getJSONObject(i)
            val tags = e.optJSONObject("tags")
            val (la, lo) = if (e.has("lat") && e.has("lon")) e.getDouble("lat") to e.getDouble("lon")
            else e.optJSONObject("center")?.let { it.getDouble("lat") to it.getDouble("lon") } ?: continue
            val name = tags?.let {
                listOf("name:bn", "name", "name:en").map { k -> it.optString(k, "").trim() }.firstOrNull { n -> n.isNotEmpty() }
            }
            val address = tags?.let {
                listOf(it.optString("addr:street", ""), it.optString("addr:city", "")).filter { s -> s.isNotBlank() }
                    .joinToString(", ").ifBlank { null }
            }
            out.add(Mosque(name, la, lo, distanceMeters(lat, lon, la, lo), address))
        }
        return out.sortedBy { it.distanceM }.take(40)
    }

    internal fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return (2 * r * asin(sqrt(a))).toInt()
    }
}
