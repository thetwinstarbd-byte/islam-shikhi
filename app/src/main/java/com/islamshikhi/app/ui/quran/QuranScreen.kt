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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import com.islamshikhi.app.ui.theme.AmiriQuran
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
import com.islamshikhi.app.ui.effects.LuxCard
import com.islamshikhi.app.ui.effects.goldBorder
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("কুরআন", style = MaterialTheme.typography.headlineSmall)
            // শুধু নাম (আয়াত নয়); আরবি ফন্টের সৌন্দর্য দেখানোর জন্য
            Text(
                "القرآن",
                fontFamily = AmiriQuran,
                fontSize = 30.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val parts = last?.split(":")
            val lastChapter = parts?.getOrNull(0)?.toIntOrNull()
            val lastAyah = parts?.getOrNull(1)?.toIntOrNull() ?: 1
            val lastName = chapters.firstOrNull { it.id == lastChapter }?.bn
            if (lastChapter != null && lastName != null) {
                item {
                    LuxCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate(Routes.quranRead(lastChapter, lastAyah)) }
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

            // উৎস ও লাইসেন্সের নোটিশ: দরকারি, কিন্তু প্রথম দৃষ্টিতে জরুরি নয়, তাই তালিকার শেষে ছোট করে
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "আরবি: Tanzil Project (উসমানী)। বাংলা অনুবাদ ও টীকা: আবু বকর যাকারিয়া, QuranEnc.com, সংস্করণ ১.১.১। লেখা অপরিবর্তিত।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { onNavigate(Routes.QURAN_NOTICE) }) { Text("উৎস ও লাইসেন্স দেখুন") }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(done: Int, goal: Int, onChange: (Int) -> Unit) {
    LuxCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("আজকের পড়ার লক্ষ্য", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${DateFormatBn.digits(done)} / ${DateFormatBn.digits(goal)} আয়াত",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onChange((goal - 5).coerceAtLeast(5)) }) { Text("− ৫") }
                    OutlinedButton(onClick = { onChange((goal + 5).coerceAtMost(500)) }) { Text("+ ৫") }
                }
            }
            LinearProgressIndicator(
                progress = { if (goal <= 0) 0f else (done / goal.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "পাঠে আয়াতের পাশের ✓ চাপলে সেটি আজকের পড়া হিসেবে গোনা হয়।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChapterRow(c: Chapter, onClick: () -> Unit) {
    LuxCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape).goldBorder(CircleShape, alpha = 0.7f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    DateFormatBn.digits(c.id),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(c.bn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${c.typeBn} • ${DateFormatBn.digits(c.verses)} আয়াত",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(c.ar, fontFamily = AmiriQuran, fontSize = 24.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
