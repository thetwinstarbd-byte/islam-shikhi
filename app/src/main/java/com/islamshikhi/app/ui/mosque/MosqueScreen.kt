package com.islamshikhi.app.ui.mosque

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.islamshikhi.app.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.feature.location.LocationHelper
import com.islamshikhi.app.feature.mosque.Mosque
import com.islamshikhi.app.feature.mosque.MosqueFinder
import com.islamshikhi.app.feature.prayer.PrayerSettings
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch

/** কাছের মসজিদ: OpenStreetMap ডেটা থেকে তালিকা; চাপলে ফোনের ম্যাপ অ্যাপে খোলে। শুধু ব্যবহারকারী বাটন চাপলে ইন্টারনেট ব্যবহার হয়। */
@Composable
fun MosqueScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val settings by container.prayerSettings.settings.collectAsState(initial = PrayerSettings())

    var radiusKm by remember { mutableStateOf(3) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<Mosque>?>(null) }

    fun search(lat: Double, lon: Double) {
        scope.launch {
            busy = true
            message = null
            try {
                val list = MosqueFinder.nearby(lat, lon, radiusKm * 1000)
                results = list
                if (list.isEmpty()) message = "এই ব্যাসার্ধে কোনো মসজিদ পাওয়া যায়নি। দূরত্ব বাড়িয়ে চেষ্টা করুন।"
            } catch (e: Exception) {
                results = null
                message = "মসজিদের তালিকা আনা যায়নি — ইন্টারনেট সংযোগ দেখে আবার চেষ্টা করুন। (${e.message ?: e.javaClass.simpleName})"
            }
            busy = false
        }
    }

    suspend fun searchHere() {
        busy = true
        message = null
        val ll = LocationHelper.currentLatLon(context)
        busy = false
        if (ll == null) message = "অবস্থান পাওয়া যায়নি। ফোনের লোকেশন চালু আছে কি না দেখুন, অথবা নামাজের সময়ে বেছে নেওয়া স্থান থেকে খুঁজুন।"
        else search(ll.first, ll.second)
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scope.launch { searchHere() }
        else message = "অনুমতি দেওয়া হয়নি। নামাজের সময়ে বেছে নেওয়া স্থান থেকে খুঁজতে পারেন।"
    }

    fun open(m: Mosque) {
        val label = Uri.encode(m.name ?: "মসজিদ")
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${m.lat},${m.lon}?q=${m.lat},${m.lon}($label)"))
        try {
            context.startActivity(geo)
        } catch (e: Exception) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/?mlat=${m.lat}&mlon=${m.lon}#map=18/${m.lat}/${m.lon}")))
            } catch (e2: Exception) {
                message = "ম্যাপ খোলার মতো অ্যাপ পাওয়া যায়নি।"
            }
        }
    }

    ScreenColumn("কাছের মসজিদ", onBack) {
        SectionTitle("দূরত্ব")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 3, 5).forEach { km ->
                FilterChip(
                    selected = radiusKm == km,
                    onClick = { radiusKm = km },
                    label = { Text("${DateFormatBn.digits(km)} কিমি") }
                )
            }
        }
        Button(
            onClick = {
                if (LocationHelper.hasPermission(context)) scope.launch { searchHere() }
                else permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            },
            enabled = !busy
        ) { Text(if (busy) "খুঁজছি…" else "আমার বর্তমান অবস্থান থেকে খুঁজুন") }
        OutlinedButton(
            onClick = { search(settings.lat, settings.lon) },
            enabled = !busy
        ) { Text("নামাজের সময়ের স্থান থেকে খুঁজুন (${settings.placeLabelBn})") }
        Text(
            "খোঁজার সময় আপনার আনুমানিক স্থানাঙ্ক OpenStreetMap-ভিত্তিক সার্ভারে পাঠানো হয়; অন্য কোনো তথ্য নয়।",
            style = MaterialTheme.typography.bodySmall
        )
        message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

        results?.let { list ->
            if (list.isNotEmpty()) SectionTitle("পাওয়া গেছে ${DateFormatBn.digits(list.size)}টি")
            list.forEach { m ->
                val dist = if (m.distanceM < 1000) "${DateFormatBn.digits(m.distanceM)} মিটার"
                else DateFormatBn.digits(String.format("%.1f", m.distanceM / 1000.0)) + " কিমি"
                MenuCard(
                    title = m.name ?: "নামহীন মসজিদ",
                    subtitle = dist + (m.address?.let { " • $it" } ?: ""),
                    icon = Icons.Filled.Place
                ) { open(m) }
            }
            Text(
                "মানচিত্রের ডেটা © OpenStreetMap contributors (ODbL)। স্বেচ্ছাসেবকদের তোলা ডেটা, তাই কিছু মসজিদ বাদ বা নামহীন থাকতে পারে। চাপলে ফোনের ম্যাপ অ্যাপে খুলবে।",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
