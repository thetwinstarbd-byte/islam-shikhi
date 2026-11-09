package com.islamshikhi.app.ui.ibadah

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import com.islamshikhi.app.ui.i18n.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.profile.ProfileRepository
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.feature.location.LocationHelper
import com.islamshikhi.app.feature.prayer.CalcMethodOption
import com.islamshikhi.app.feature.prayer.Cities
import com.islamshikhi.app.feature.prayer.Prayer
import com.islamshikhi.app.feature.prayer.PrayerEngine
import com.islamshikhi.app.feature.prayer.PrayerSettings
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.WarningCard
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import com.islamshikhi.app.util.TimeFormatBn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun PrayerScreen(onBack: () -> Unit, onAdhanSetup: () -> Unit = {}) {
    val container = rememberContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by container.prayerSettings.settings.collectAsState(initial = PrayerSettings())
    val profile by container.profileRepository.profile.collectAsState(initial = UserProfile())

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }

    // সেটিংস বদলালে অ্যালার্ম নতুন করে বসে
    LaunchedEffect(settings, profile.notificationsEnabled) { container.scheduler.rescheduleNext() }

    val today = remember(now / 60_000, settings) { PrayerEngine.timesFor(settings, LocalDate.now()) }
    val next = remember(now / 1000, settings) { PrayerEngine.next(settings, now) }
    val jamaat = remember(now / 60_000, settings) { PrayerEngine.jamaatTimes(settings, LocalDate.now()) }
    val defaultToday = remember(now / 60_000, settings) { PrayerEngine.calculated(settings, LocalDate.now()) }
    var more by remember { mutableStateOf(false) }

    fun pickTime(initialMinutes: Int, onPicked: (Int) -> Unit) {
        android.app.TimePickerDialog(context, { _, h, m -> onPicked(h * 60 + m) }, initialMinutes / 60, initialMinutes % 60, false).show()
    }
    fun minutesOf(millis: Long): Int {
        val t = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault())
        return t.hour * 60 + t.minute
    }

    var gpsMessage by remember { mutableStateOf<String?>(null) }
    var gpsBusy by remember { mutableStateOf(false) }

    suspend fun fetchGps() {
        gpsBusy = true
        val ll = LocationHelper.currentLatLon(context)
        if (ll != null) {
            container.prayerSettings.setGpsLocation(ll.first, ll.second)
            gpsMessage = "অবস্থান পাওয়া গেছে।"
        } else {
            gpsMessage = "অবস্থান পাওয়া যায়নি। ফোনের লোকেশন চালু আছে কি না দেখুন, অথবা নিচ থেকে শহর বেছে নিন।"
        }
        gpsBusy = false
    }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scope.launch { fetchGps() }
        else gpsMessage = "অনুমতি দেওয়া হয়নি। ম্যানুয়ালি শহর বেছে নিতে পারেন।"
    }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    ScreenColumn("নামাজের সময়", onBack) {
        InfoCard(
            title = "পরবর্তী: ${next.prayer.labelBn}",
            body = "${TimeFormatBn.format(next.atMillis)} — আর ${TimeFormatBn.countdown(next.atMillis - now)}\n" +
                "স্থান: ${settings.placeLabelBn} • ${DateFormatBn.gregorian(LocalDate.now())}"
        )

        SectionTitle("আজকের সময়সূচি")
        Prayer.entries.forEach { p ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    p.labelBn,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (p == next.prayer) FontWeight.Bold else FontWeight.Normal,
                    color = if (p == next.prayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(TimeFormatBn.format(today[p]), style = MaterialTheme.typography.titleMedium)
                    jamaat[p]?.let {
                        Text("জামাত ${TimeFormatBn.format(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                if (p.isObligation) {
                    val on = p.id in settings.notify
                    IconButton(onClick = {
                        scope.launch { container.prayerSettings.setNotify(p, !on, settings.notify) }
                    }) {
                        Icon(
                            if (on) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                            contentDescription = if (on) "${p.labelBn}-এর নোটিফিকেশন বন্ধ করুন" else "${p.labelBn}-এর নোটিফিকেশন চালু করুন"
                        )
                    }
                } else {
                    Row(modifier = Modifier.padding(start = 48.dp)) {}
                }
            }
        }
        Text(
            "সূর্যোদয় ফরজ ওয়াক্ত নয়; ফজরের ওয়াক্ত শেষ হওয়ার সময় বোঝাতে দেখানো হয়েছে।",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedButton(onClick = onAdhanSetup, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text("আজান ও অনুমতি ঠিক আছে কি না দেখুন")
        }
        SectionTitle("সময় কীভাবে চান?")
        listOf(
            false to ("ডিফল্ট সময়" to "ফোন নিজে হিসাব করে সময় দেয়। আপনাকে কিছু করতে হবে না।"),
            true to ("আমার নিজের সময়" to "আমার মসজিদের আজান ও জামাতের সময় আমি নিজে বসাব।")
        ).forEach { (custom, texts) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = settings.useCustom == custom, role = Role.RadioButton,
                        onClick = { scope.launch { container.prayerSettings.setCustomMode(custom) } })
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RadioButton(selected = settings.useCustom == custom, onClick = null)
                Column {
                    Text(texts.first, style = MaterialTheme.typography.titleMedium)
                    Text(texts.second, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (!settings.useCustom) {
            Button(
                onClick = { scope.launch { container.prayerSettings.setCustomMode(true) } },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text("নিজের আজান ও জামাতের সময় বসান") }
        }
        if (settings.useCustom) {
            Text("সময়ের বাটনে চাপুন, ঘড়ি থেকে সময় বেছে নিন। বসাননি যেটা, সেটা ডিফল্ট থাকবে।", style = MaterialTheme.typography.bodyMedium)
            Prayer.obligations.forEach { p ->
                val adhanSet = settings.adhanMin[p.id]
                val jamaatSet = settings.jamaatMin[p.id]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(p.labelBn, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                                onClick = {
                                    pickTime(adhanSet ?: minutesOf(defaultToday[p])) { m ->
                                        scope.launch { container.prayerSettings.setCustomMode(true); container.prayerSettings.setAdhan(p, m, settings.adhanMin) }
                                    }
                                }
                            ) {
                                Text(if (adhanSet != null) "আজান ${TimeFormatBn.format(adhanSet / 60, adhanSet % 60)}"
                                else "আজান: ${TimeFormatBn.format(defaultToday[p])} (ডিফল্ট)")
                            }
                            if (adhanSet != null) TextButton(onClick = {
                                scope.launch { container.prayerSettings.setAdhan(p, null, settings.adhanMin) }
                            }) { Text("ডিফল্ট") }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                                onClick = {
                                    pickTime(jamaatSet ?: minutesOf(defaultToday[p]) + 15) { m ->
                                        scope.launch { container.prayerSettings.setCustomMode(true); container.prayerSettings.setJamaat(p, m, settings.jamaatMin) }
                                    }
                                }
                            ) {
                                Text(if (jamaatSet != null) "জামাত ${TimeFormatBn.format(jamaatSet / 60, jamaatSet % 60)}"
                                else "জামাতের সময় বসান")
                            }
                            if (jamaatSet != null) TextButton(onClick = {
                                scope.launch { container.prayerSettings.setJamaat(p, null, settings.jamaatMin) }
                            }) { Text("মুছুন") }
                        }
                    }
                }
            }
            OutlinedButton(onClick = { scope.launch { container.prayerSettings.resetToDefault() } }) {
                Text("সব ডিফল্টে ফিরুন")
            }
            Text("নোটিফিকেশন চালু থাকলে আজানের সময় ও আপনার বসানো জামাতের সময় জানানো হবে। রমযানের সেহরি-ইফতার সবসময় ডিফল্ট হিসাবেই দেখানো হয়।", style = MaterialTheme.typography.bodySmall)
        }

        WarningCard(
            "এই সময়গুলো গণনা করা, সরকারি বা মসজিদের সময়সূচির বদলে নয়। গণনা-পদ্ধতি ও এলাকাভেদে কয়েক মিনিট পার্থক্য হতে পারে। " +
                "রোজার সেহরি-ইফতার ও জামাতের সময় আপনার এলাকার মসজিদ বা স্থানীয় সময়সূচির সঙ্গে মিলিয়ে নিন।"
        )

        SectionTitle("স্থান")
        Button(
            onClick = {
                if (LocationHelper.hasPermission(context)) scope.launch { fetchGps() }
                else locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            },
            enabled = !gpsBusy
        ) { Text(if (gpsBusy) "অবস্থান খুঁজছি…" else "আমার বর্তমান অবস্থান ব্যবহার করুন") }
        Text(
            "নামাজের সময়ের গণনা এই ফোনেই হয়; অবস্থান কোথাও পাঠানো হয় না। (শুধু 'কাছের মসজিদ' খুঁজলে আনুমানিক স্থানাঙ্ক সেই খোঁজার জন্য পাঠানো হয়।)",
            style = MaterialTheme.typography.bodySmall
        )
        gpsMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

        var menu by remember { mutableStateOf(false) }
        OutlinedButton(onClick = { menu = true }) {
            Text("শহর: ${if (settings.useGps && settings.gpsLat != null) "বর্তমান অবস্থান (বদলাতে চাপুন)" else settings.city.nameBn}")
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            Cities.all.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.nameBn) },
                    onClick = { menu = false; scope.launch { container.prayerSettings.setCity(c.id) } }
                )
            }
        }

        OutlinedButton(onClick = { more = !more }) { Text(if (more) "আরও সেটিংস লুকান" else "আরও সেটিংস (নোটিফিকেশন, গণনা-পদ্ধতি)") }
        if (more) {
        SectionTitle("নোটিফিকেশন")
        if (!profile.notificationsEnabled) {
            Text("প্রোফাইলে নোটিফিকেশন বন্ধ আছে। চালু করলে নামাজের সময় জানানো হবে।", style = MaterialTheme.typography.bodyMedium)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            OutlinedButton(onClick = { notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                Text("নোটিফিকেশনের অনুমতি দিন")
            }
        }
        if (!container.scheduler.canScheduleExact()) {
            Text(
                "সঠিক সময়ে অ্যালার্মের অনুমতি নেই, তাই নোটিফিকেশন কয়েক মিনিট দেরিতে আসতে পারে।",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }) { Text("সঠিক অ্যালার্মের অনুমতি দিন") }
        }
        Text(
            "কিছু ফোনের ব্যাটারি-সেভার অ্যালার্ম আটকে দেয়। নোটিফিকেশন না এলে ফোনের সেটিংসে এই অ্যাপের ব্যাটারি ব্যবহার \"Unrestricted\" করুন।",
            style = MaterialTheme.typography.bodySmall
        )

        SectionTitle("গণনা-পদ্ধতি")
        CalcMethodOption.entries.forEach { m ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = settings.method == m, role = Role.RadioButton,
                        onClick = { scope.launch { container.prayerSettings.setMethod(m) } })
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RadioButton(selected = settings.method == m, onClick = null)
                androidx.compose.foundation.layout.Column {
                    Text(m.labelBn, style = MaterialTheme.typography.bodyLarge)
                    Text(m.noteBn, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        SectionTitle("আসরের সময় (মাযহাব)")
        listOf(true to "হানাফী: ছায়া বস্তুর দ্বিগুণ হলে আসর শুরু", false to "শাফিয়ী/মালিকী/হাম্বলী: ছায়া বস্তুর সমান হলে আসর শুরু").forEach { (hanafi, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = settings.hanafiAsr == hanafi, role = Role.RadioButton,
                        onClick = { scope.launch { container.prayerSettings.setHanafiAsr(hanafi) } })
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RadioButton(selected = settings.hanafiAsr == hanafi, onClick = null)
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Text(
            "সময়গুলো ফোনের বর্তমান সময় এলাকা (time zone) অনুযায়ী দেখানো হয়।",
            style = MaterialTheme.typography.bodySmall
        )
        }
    }
}
