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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** ডিজিটাল তাসবিহ: বড় বাটনে চাপ দিলে গোনা হয়। গণনা ও দৈনিক মোট শুধু এই ফোনে থাকে। */
@Composable
fun TasbihScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val ud = container.userData
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val today = remember { LocalDate.now().toString() }

    val count by ud.int("tasbih_count").collectAsState(initial = 0)
    val target by ud.int("tasbih_target", 33).collectAsState(initial = 33)
    val goal by ud.int("tasbih_goal", 100).collectAsState(initial = 100)
    val todayTotal by ud.int("tasbih_total:$today").collectAsState(initial = 0)

    ScreenColumn("ডিজিটাল তাসবিহ", onBack) {
        InfoCard(
            title = "আজকের মোট: ${DateFormatBn.digits(todayTotal)}",
            body = "দৈনিক লক্ষ্য: ${DateFormatBn.digits(goal)}"
        )
        LinearProgressIndicator(
            progress = { (todayTotal.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                scope.launch {
                    ud.incrementInt("tasbih_count")
                    ud.incrementInt("tasbih_total:$today")
                }
                // লক্ষ্যে পৌঁছালে একটু জোরে কম্পন
                haptic.performHapticFeedback(
                    if ((count + 1) % target == 0) HapticFeedbackType.LongPress
                    else HapticFeedbackType.TextHandleMove
                )
            },
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) {
            Text(DateFormatBn.digits(count), style = MaterialTheme.typography.displayLarge)
        }
        Text(
            "প্রতি ${DateFormatBn.digits(target)} গণনায় কম্পন হবে। গণনা: ${DateFormatBn.digits(count)}",
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedButton(
            onClick = { scope.launch { ud.putInt("tasbih_count", 0) } },
            modifier = Modifier.fillMaxWidth()
        ) { Text("গণনা শূন্য করুন") }

        SectionTitle("প্রতি রাউন্ডের সংখ্যা")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(33, 99, 100).forEach { n ->
                FilterChip(
                    selected = target == n,
                    onClick = { scope.launch { ud.putInt("tasbih_target", n) } },
                    label = { Text(DateFormatBn.digits(n)) }
                )
            }
        }

        SectionTitle("দৈনিক লক্ষ্য")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(100, 300, 500, 1000).forEach { n ->
                FilterChip(
                    selected = goal == n,
                    onClick = { scope.launch { ud.putInt("tasbih_goal", n) } },
                    label = { Text(DateFormatBn.digits(n)) }
                )
            }
        }
        Text(
            "কোন যিকির কতবার পড়বেন তা 'দোয়া ও যিকির' পাতায় সূত্রসহ দেখুন।",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
