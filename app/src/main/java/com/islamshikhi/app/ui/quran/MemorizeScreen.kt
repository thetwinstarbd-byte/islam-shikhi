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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch

/** নিজের পছন্দের সূরা বেছে "মুখস্থ করার পরিকল্পনা" বানানো এবং মুখস্থ হলে চিহ্ন দেওয়া। সব ডেটা ফোনে থাকে। */
@Composable
fun MemorizeScreen(onBack: () -> Unit, onOpenChapter: (Int) -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    LaunchedEffect(Unit) { chapters = container.content.chapters() }

    val plan by container.userData.set(UserKeys.MEMORIZE_PLAN).collectAsState(initial = emptySet())
    val memorized by container.userData.set(UserKeys.MEMORIZED).collectAsState(initial = emptySet())
    var query by remember { mutableStateOf("") }

    val planned = chapters.filter { it.id.toString() in plan }
    val candidates = remember(chapters, plan, query) {
        val q = query.trim()
        chapters.filter { it.id.toString() !in plan }
            .filter { q.isEmpty() || it.bn.contains(q) || it.id.toString() == q || DateFormatBn.digits(it.id) == q }
    }
    val doneCount = planned.count { it.id.toString() in memorized }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("মুখস্থ পরিকল্পনা", onBack)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "${DateFormatBn.digits(doneCount)} / ${DateFormatBn.digits(planned.size)} সূরা মুখস্থ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { if (planned.isEmpty()) 0f else doneCount / planned.size.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "মুখস্থের সঠিকতা যাচাইয়ের জন্য কোনো শিক্ষকের কাছে শুনিয়ে নেওয়া ভালো।",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item { SectionTitle("আপনার পরিকল্পনা") }
            if (planned.isEmpty()) {
                item { Text("এখনও কোনো সূরা যোগ করেননি। নিচ থেকে বেছে নিন।", style = MaterialTheme.typography.bodyMedium) }
            }
            items(planned, key = { "plan-${it.id}" }) { c ->
                val id = c.id.toString()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = id in memorized,
                            onCheckedChange = { scope.launch { container.userData.toggleInSet(UserKeys.MEMORIZED, id) } }
                        )
                        Column(modifier = Modifier.weight(1f).clickable { onOpenChapter(c.id) }) {
                            Text(c.bn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${DateFormatBn.digits(c.verses)} আয়াত • পড়তে চাপুন",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = { scope.launch { container.userData.toggleInSet(UserKeys.MEMORIZE_PLAN, id) } }) {
                            Text("সরান")
                        }
                    }
                }
            }

            item { SectionTitle("সূরা যোগ করুন") }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("সূরার নাম বা নম্বর") },
                    singleLine = true
                )
            }
            items(candidates, key = { "add-${it.id}" }) { c ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${DateFormatBn.digits(c.id)}. ${c.bn} (${DateFormatBn.digits(c.verses)} আয়াত)",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedButton(onClick = {
                        scope.launch { container.userData.toggleInSet(UserKeys.MEMORIZE_PLAN, c.id.toString()) }
                    }) { Text("যোগ") }
                }
            }
        }
    }
}
