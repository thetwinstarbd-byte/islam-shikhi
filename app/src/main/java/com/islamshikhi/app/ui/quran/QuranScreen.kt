package com.islamshikhi.app.ui.quran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.content.Chapter
import com.islamshikhi.app.data.user.UserKeys
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.navigation.Routes
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** কুরআন ট্যাব: শেষ পড়া, দৈনিক লক্ষ্য, অনুসন্ধান, বুকমার্ক, মুখস্থ পরিকল্পনা ও ১১৪ সূরার তালিকা। */
@Composable
fun QuranScreen(onNavigate: (String) -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    LaunchedEffect(Unit) { chapters = container.content.chapters() }

    var query by remember { mutableStateOf("") }
    val last by container.userData.string(UserKeys.QURAN_LAST).collectAsState(initial = null)
    val goal by container.userData.int(UserKeys.QURAN_GOAL, UserKeys.QURAN_GOAL_DEFAULT)
        .collectAsState(initial = UserKeys.QURAN_GOAL_DEFAULT)
    val today = remember { LocalDate.now() }
    val readToday by container.userData.set(UserKeys.quranRead(today)).collectAsState(initial = emptySet())

    val filtered = remember(chapters, query) {
        val q = query.trim()
        if (q.isEmpty()) chapters
        else chapters.filter {
            it.id.toString() == q || DateFormatBn.digits(it.id) == q ||
                it.bn.contains(q) || it.ar.contains(q) || it.en.contains(q, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("কুরআন")
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "আরবি: Tanzil Project (উসমানী)। বাংলা অনুবাদ ও টীকা: আবু বকর যাকারিয়া, QuranEnc.com, সংস্করণ ১.১.১। " +
                                "লেখা অপরিবর্তিত।",
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = { onNavigate(Routes.QURAN_NOTICE) }) { Text("উৎস ও লাইসেন্স দেখুন") }
                    }
                }
            }

            val parts = last?.split(":")
            val lastChapter = parts?.getOrNull(0)?.toIntOrNull()
            val lastAyah = parts?.getOrNull(1)?.toIntOrNull() ?: 1
            val lastName = chapters.firstOrNull { it.id == lastChapter }?.bn
            if (lastChapter != null && lastName != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigate(Routes.quranRead(lastChapter, lastAyah)) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("শেষ পড়া", style = MaterialTheme.typography.labelLarge)
                            Text(
                                "সূরা $lastName, আয়াত ${DateFormatBn.digits(lastAyah)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("চাপ দিয়ে সেখান থেকে আবার শুরু করুন।", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                GoalCard(
                    done = readToday.size,
                    goal = goal,
                    onChange = { newGoal -> scope.launch { container.userData.putInt(UserKeys.QURAN_GOAL, newGoal) } }
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { onNavigate(Routes.QURAN_SEARCH) }, modifier = Modifier.weight(1f)) { Text("খুঁজুন") }
                    OutlinedButton(onClick = { onNavigate(Routes.BOOKMARKS) }, modifier = Modifier.weight(1f)) { Text("বুকমার্ক") }
                }
            }
            item {
                OutlinedButton(onClick = { onNavigate(Routes.QURAN_MEMORIZE) }, modifier = Modifier.fillMaxWidth()) {
                    Text("সূরা মুখস্থ করার পরিকল্পনা")
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("সূরার নাম বা নম্বর") },
                    singleLine = true
                )
            }

            if (chapters.isEmpty()) {
                item { Text("লোড হচ্ছে…", style = MaterialTheme.typography.bodyMedium) }
            } else if (filtered.isEmpty()) {
                item { Text("কোনো সূরা পাওয়া যায়নি।", style = MaterialTheme.typography.bodyMedium) }
            }

            items(filtered, key = { it.id }) { c ->
                ChapterRow(c) { onNavigate(Routes.quranRead(c.id)) }
            }
        }
    }
}

@Composable
private fun GoalCard(done: Int, goal: Int, onChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("আজকের পড়ার লক্ষ্য", style = MaterialTheme.typography.labelLarge)
            Text(
                "${DateFormatBn.digits(done)} / ${DateFormatBn.digits(goal)} আয়াত",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { if (goal <= 0) 0f else (done / goal.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "পাঠের পাতায় প্রতিটি আয়াতের পাশের ✓ চিহ্নে চাপলে সেটি আজকের পড়া হিসেবে গোনা হয়।",
                style = MaterialTheme.typography.bodySmall
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onChange((goal - 5).coerceAtLeast(5)) }) { Text("− ৫") }
                Text("লক্ষ্য: ${DateFormatBn.digits(goal)}", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = { onChange((goal + 5).coerceAtMost(500)) }) { Text("+ ৫") }
            }
        }
    }
}

@Composable
private fun ChapterRow(c: Chapter, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                DateFormatBn.digits(c.id),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(c.bn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${c.typeBn} • ${DateFormatBn.digits(c.verses)} আয়াত",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(c.ar, style = MaterialTheme.typography.titleMedium)
        }
    }
}
