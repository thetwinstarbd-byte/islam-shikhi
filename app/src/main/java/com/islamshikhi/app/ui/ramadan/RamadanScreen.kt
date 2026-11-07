package com.islamshikhi.app.ui.ramadan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.feature.prayer.Prayer
import com.islamshikhi.app.feature.prayer.PrayerEngine
import com.islamshikhi.app.feature.prayer.PrayerSettings
import com.islamshikhi.app.feature.tracker.RamadanMath
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.effects.LuxCard
import com.islamshikhi.app.util.DateFormatBn
import com.islamshikhi.app.util.TimeFormatBn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * রমযান মোড: সেহরির শেষ ও ইফতারের কাউন্টডাউন (নামাজের সময়ের একই হিসাব থেকে), দৈনিক চেকলিস্ট ও তারাবীহ-ট্র্যাকার।
 * সেহরির শেষ = ফজরের ওয়াক্ত শুরু; ইফতার = মাগরিবের ওয়াক্ত।
 */
@Composable
fun RamadanScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val settings by container.prayerSettings.settings.collectAsState(initial = PrayerSettings())
    val done by container.userData.set(RamadanMath.DONE_SET).collectAsState(initial = emptySet())
    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now() }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }

    val info = remember(today) { RamadanMath.info(today) }
    val t0 = remember(settings, today) { PrayerEngine.calculated(settings, today) }
    val t1 = remember(settings, today) { PrayerEngine.calculated(settings, today.plusDays(1)) }
    val next = RamadanMath.next(now, t0[Prayer.FAJR], t0[Prayer.MAGHRIB], t1[Prayer.FAJR])
    val label = when {
        next.event == RamadanMath.Event.IFTAR -> "ইফতারের সময়"
        now < t0[Prayer.FAJR] -> "সেহরির শেষ সময়"
        else -> "পরের সেহরি"
    }
    val seconds = ((next.atMillis - now) / 1000).coerceAtLeast(0)
    val clock = DateFormatBn.digits(
        String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
    )

    ScreenColumn("রমযান মোড", onBack) {
        InfoCard(
            title = if (info.inRamadan) "আজ রমযানের ${DateFormatBn.digits(info.dayNumber)} তারিখ"
            else if (info.daysUntil > 0) "রমযান আসতে আনুমানিক ${DateFormatBn.digits(info.daysUntil)} দিন বাকি"
            else "রমযান মোড",
            body = "হিজরি তারিখ গণনা-ভিত্তিক; চাঁদ দেখার ওপর নির্ভর করে এক দিন আগে-পরে হতে পারে। স্থান: ${settings.placeLabelBn}।"
        )

        LuxCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                Text(clock, style = MaterialTheme.typography.displaySmall)
                Text(
                    "সেহরি শেষ: ${TimeFormatBn.format(t0[Prayer.FAJR], zone)}   •   ইফতার: ${TimeFormatBn.format(t0[Prayer.MAGHRIB], zone)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "সেহরির শেষ সময় ফজরের ওয়াক্তের শুরু ধরা হয়েছে (${settings.method.labelBn})।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SectionTitle("আজকের চেকলিস্ট")
        RamadanMath.Deed.entries.forEach { deed ->
            val key = RamadanMath.key(today, deed)
            val checked = key in done
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = checked, role = Role.Checkbox, onValueChange = {
                        scope.launch { container.userData.toggleInSet(RamadanMath.DONE_SET, key) }
                    })
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = checked, onCheckedChange = null)
                Text(deed.labelBn, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (info.inRamadan) {
            SectionTitle("তারাবীহ ট্র্যাকার")
            Text(
                "যে রাতের তারাবীহ পড়েছেন, সেই দিনের ঘরে চাপ দিন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            (1..30).chunked(6).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { d ->
                        val date = info.start.plusDays((d - 1).toLong())
                        val key = RamadanMath.key(date, RamadanMath.Deed.TARAWIH)
                        FilterChip(
                            selected = key in done,
                            enabled = !date.isAfter(today),
                            onClick = { scope.launch { container.userData.toggleInSet(RamadanMath.DONE_SET, key) } },
                            label = { Text(DateFormatBn.digits(d)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            SectionTitle("এই রমযানের সারাংশ")
            Text(
                "রোযা: ${DateFormatBn.digits(RamadanMath.count(done, info.start, RamadanMath.Deed.ROZA))} দিন • " +
                    "তারাবীহ: ${DateFormatBn.digits(RamadanMath.count(done, info.start, RamadanMath.Deed.TARAWIH))} রাত • " +
                    "কুরআন তিলাওয়াত: ${DateFormatBn.digits(RamadanMath.count(done, info.start, RamadanMath.Deed.QURAN))} দিন",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
