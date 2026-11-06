package com.islamshikhi.app.ui.ibadah

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.feature.tracker.KhatmMath
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** কুরআন খতম ট্র্যাকার: দিন ঠিক করা, পড়া আয়াত গুনে বাকি পরিমাণ দেখা। শুধু ফোনে সংরক্ষিত। */
@Composable
fun KhatmScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val days by container.userData.int(KhatmMath.KEY_DAYS, KhatmMath.DEFAULT_DAYS).collectAsState(initial = KhatmMath.DEFAULT_DAYS)
    val startRaw by container.userData.string(KhatmMath.KEY_START).collectAsState(initial = null)
    val read by container.userData.int(KhatmMath.KEY_READ).collectAsState(initial = 0)
    var total by remember { mutableStateOf(6236) }
    var pick by remember { mutableStateOf(KhatmMath.DEFAULT_DAYS) }
    LaunchedEffect(Unit) { total = container.content.chapters().sumOf { it.verses } }

    val today = remember { LocalDate.now() }
    val start = startRaw?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    fun add(n: Int) {
        scope.launch { container.userData.putInt(KhatmMath.KEY_READ, (read + n).coerceIn(0, total)) }
    }

    ScreenColumn("কুরআন খতম ট্র্যাকার", onBack) {
        if (start == null) {
            InfoCard(
                title = "কত দিনে খতম করতে চান?",
                body = "কুরআনে মোট ${DateFormatBn.digits(total)}টি আয়াত। দিন বেছে নিন; অ্যাপ হিসাব করে দেখাবে প্রতিদিন কত আয়াত পড়তে হবে।"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 15, 30, 60, 90).forEach { d ->
                    FilterChip(selected = pick == d, onClick = { pick = d }, label = { Text(DateFormatBn.digits(d)) })
                }
            }
            Text(
                "${DateFormatBn.digits(pick)} দিনে খতম হলে প্রতিদিন প্রায় ${DateFormatBn.digits(KhatmMath.dailyTarget(total, pick))}টি আয়াত।",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = {
                    scope.launch {
                        container.userData.putInt(KhatmMath.KEY_DAYS, pick)
                        container.userData.putInt(KhatmMath.KEY_READ, 0)
                        container.userData.putString(KhatmMath.KEY_START, today.toString())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("খতম শুরু করুন") }
        } else {
            val remaining = (total - read).coerceAtLeast(0)
            val left = KhatmMath.daysLeft(start, days, today)
            val daily = KhatmMath.dailyTarget(total, days)
            val need = KhatmMath.requiredPerDay(remaining, left)
            val percent = if (total > 0) read * 100 / total else 0

            LinearProgressIndicator(
                progress = { if (total > 0) read.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = MaterialTheme.colorScheme.secondary
            )
            InfoCard(
                title = "পড়া হয়েছে ${DateFormatBn.digits(read)} / ${DateFormatBn.digits(total)} আয়াত (${DateFormatBn.digits(percent)}%)",
                body = if (remaining == 0) "খতম সম্পূর্ণ হয়েছে। আলহামদুলিল্লাহ।"
                else "বাকি ${DateFormatBn.digits(remaining)}টি আয়াত। বাকি দিন ${DateFormatBn.digits(left)}। " +
                    "এখন থেকে প্রতিদিন প্রায় ${DateFormatBn.digits(need)}টি আয়াত পড়লে সময়মতো শেষ হবে।"
            )
            InfoCard(
                title = "মূল লক্ষ্য",
                body = "${DateFormatBn.digits(days)} দিনে খতম, অর্থাৎ প্রতিদিন প্রায় ${DateFormatBn.digits(daily)}টি আয়াত। শুরু: ${DateFormatBn.gregorian(start)}।"
            )
            SectionTitle("আজ যা পড়লেন যোগ করুন")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { add(daily) }, modifier = Modifier.weight(1f)) { Text("+${DateFormatBn.digits(daily)}") }
                OutlinedButton(onClick = { add(10) }, modifier = Modifier.weight(1f)) { Text("+১০") }
                OutlinedButton(onClick = { add(100) }, modifier = Modifier.weight(1f)) { Text("+১০০") }
                OutlinedButton(onClick = { add(-10) }, modifier = Modifier.weight(1f)) { Text("−১০") }
            }
            TextButton(onClick = {
                scope.launch {
                    container.userData.putString(KhatmMath.KEY_START, null)
                    container.userData.putInt(KhatmMath.KEY_READ, 0)
                }
            }) { Text("নতুন করে শুরু করুন") }
        }
    }
}
