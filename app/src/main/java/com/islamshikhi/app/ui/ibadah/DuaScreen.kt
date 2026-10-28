package com.islamshikhi.app.ui.ibadah

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.content.DuaData
import com.islamshikhi.app.data.content.DuaEntry
import com.islamshikhi.app.ui.common.ArabicText
import com.islamshikhi.app.ui.common.NoteDialog
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.StatusBadge
import com.islamshikhi.app.ui.common.rememberContainer
import kotlinx.coroutines.launch

@Composable
fun DuaScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<DuaData?>(null) }
    LaunchedEffect(Unit) { data = container.content.duas() }

    val bookmarks by container.userData.bookmarks.collectAsState(initial = emptySet())
    val notes by container.userData.notes.collectAsState(initial = emptyMap())
    var selected by remember { mutableStateOf("all") }
    var noteFor by remember { mutableStateOf<DuaEntry?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("দোয়া ও যিকির", onBack)
        val d = data
        if (d == null) {
            Text("লোড হচ্ছে…", modifier = Modifier.padding(20.dp))
            return@Column
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
        ) {
            item { FilterChip(selected = selected == "all", onClick = { selected = "all" }, label = { Text("সব") }) }
            item { FilterChip(selected = selected == "bookmarks", onClick = { selected = "bookmarks" }, label = { Text("বুকমার্ক") }) }
            items(d.categories) { c ->
                FilterChip(selected = selected == c.id, onClick = { selected = c.id }, label = { Text(c.titleBn) })
            }
        }

        val shown = d.entries.filter {
            when (selected) {
                "all" -> true
                "bookmarks" -> "dua:${it.id}" in bookmarks
                else -> it.category == selected
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "আরবি লেখা ও সূত্র কুরআন (Tanzil) ও সহীহাইনের ডেটাসেটের সঙ্গে মেলানো। বাংলা উচ্চারণ ও ব্যাখ্যা " +
                        "আলেম দ্বারা পর্যালোচিত নয়; আমলের আগে নির্ভরযোগ্য আলেম বা মূল গ্রন্থ থেকে মিলিয়ে নিন।",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (shown.isEmpty()) {
                item { Text("এখানে কিছু নেই।", style = MaterialTheme.typography.bodyMedium) }
            }
            items(shown, key = { it.id }) { e ->
                DuaCard(
                    entry = e,
                    bookmarked = "dua:${e.id}" in bookmarks,
                    hasNote = !notes["dua:${e.id}"].isNullOrBlank(),
                    note = notes["dua:${e.id}"].orEmpty(),
                    onToggleBookmark = { scope.launch { container.userData.toggleBookmark("dua:${e.id}") } },
                    onEditNote = { noteFor = e }
                )
            }
        }
    }

    noteFor?.let { e ->
        NoteDialog(
            initial = notes["dua:${e.id}"].orEmpty(),
            title = "নোট: ${e.titleBn}",
            onSave = { text -> scope.launch { container.userData.setNote("dua:${e.id}", text) } },
            onDismiss = { noteFor = null }
        )
    }
}

@Composable
fun DuaCard(
    entry: DuaEntry,
    bookmarked: Boolean,
    hasNote: Boolean,
    note: String,
    onToggleBookmark: () -> Unit,
    onEditNote: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.titleBn,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditNote) {
                    Icon(Icons.Filled.Edit, contentDescription = "নোট", tint = if (hasNote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (bookmarked) "বুকমার্ক সরান" else "বুকমার্ক করুন"
                    )
                }
            }
            StatusBadge(entry.verificationStatus)
            ArabicText(entry.arabic)
            entry.transliterationBn?.let {
                Text("উচ্চারণ: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text("অর্থ: ${entry.meaningBn}", style = MaterialTheme.typography.bodyMedium)
            entry.usageBn?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            entry.suggestedCount?.let { Text("সংখ্যা: $it বার", style = MaterialTheme.typography.bodySmall) }
            Text("সূত্র: ${entry.sourceRef}", style = MaterialTheme.typography.bodySmall)
            entry.hadithGrade?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            entry.checkNoteBn?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            entry.meaningCreditBn?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            if (note.isNotBlank()) {
                Text("আপনার নোট: $note", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
