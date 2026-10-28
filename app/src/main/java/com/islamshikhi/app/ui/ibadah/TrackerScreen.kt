package com.islamshikhi.app.ui.ibadah

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.islamshikhi.app.feature.tracker.AmalItem
import com.islamshikhi.app.feature.tracker.AmalKeys
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** আমল ও নামাজের ব্যক্তিগত ট্র্যাকার, সঙ্গে সাপ্তাহিক সারাংশ। শুধু ফোনে সংরক্ষিত। */
@Composable
fun TrackerScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val done by container.userData.set(AmalKeys.SET).collectAsState(initial = emptySet())
    val realToday = remember { LocalDate.now() }
    var date by remember { mutableStateOf(realToday) }

    ScreenColumn("আমল ট্র্যাকার", onBack) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { date = date.minusDays(1) }) { Text("আগের দিন") }
            Text(
                if (date == realToday) "আজ" else DateFormatBn.gregorian(date),
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedButton(onClick = { date = date.plusDays(1) }, enabled = date < realToday) { Text("পরের দিন") }
        }

        InfoCard(
            title = "${DateFormatBn.digits(AmalKeys.doneCount(done, date))} / ${DateFormatBn.digits(AmalItem.entries.size)} সম্পন্ন",
            body = "যা করেছেন তাতে টিক দিন। কোনো হিসাব কারও সঙ্গে শেয়ার হয় না।"
        )

        SectionTitle("নামাজ")
        AmalItem.entries.filter { it.isPrayer }.forEach { item ->
            AmalRow(item, AmalKeys.key(date, item) in done) {
                scope.launch { container.userData.toggleInSet(AmalKeys.SET, AmalKeys.key(date, item)) }
            }
        }
        SectionTitle("অন্যান্য আমল")
        AmalItem.entries.filter { !it.isPrayer }.forEach { item ->
            AmalRow(item, AmalKeys.key(date, item) in done) {
                scope.launch { container.userData.toggleInSet(AmalKeys.SET, AmalKeys.key(date, item)) }
            }
        }

        SectionTitle("গত ৭ দিন")
        val total = AmalItem.entries.size
        var weekSum = 0
        (6 downTo 0).forEach { back ->
            val d = realToday.minusDays(back.toLong())
            val c = AmalKeys.doneCount(done, d)
            weekSum += c
            Text(
                "${if (back == 0) "আজ" else DateFormatBn.gregorian(d)}: ${DateFormatBn.digits(c)}/${DateFormatBn.digits(total)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            "সাপ্তাহিক মোট: ${DateFormatBn.digits(weekSum)}/${DateFormatBn.digits(total * 7)}",
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun AmalRow(item: AmalItem, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = { onToggle() }, role = Role.Checkbox)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(item.labelBn, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
    }
}
