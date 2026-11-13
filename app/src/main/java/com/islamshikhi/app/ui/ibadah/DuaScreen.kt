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
import com.islamshikhi.app.ui.i18n.Text
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.islamshikhi.app.ui.effects.LuxCard
import com.islamshikhi.app.ui.effects.LuxSectionTitle
import com.islamshikhi.app.util.DateFormatBn
import com.islamshikhi.app.data.content.DuaData
import com.islamshikhi.app.data.content.DuaEntry
import com.islamshikhi.app.ui.common.ArabicText
import com.islamshikhi.app.ui.common.NoteDialog
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.StatusBadge
import com.islamshikhi.app.ui.common.rememberContainer
import kotlinx.coroutines.launch

/**
 * দোয়ার তালিকা: শুধু শিরোনাম, বিভাগ ধরে সাজানো। কোনো শিরোনামে চাপলে পূর্ণ দোয়া আলাদা পাতায় ([DuaDetailScreen]) খোলে।
 * এখানকার সব দোয়ার সূত্র কুরআন বা সহীহ হাদীস; সূত্রহীন দোয়া অ্যাপে দেখানোই হয় না (ContentRepository.duas())।
 */
@Composable
fun DuaScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    val container = rememberContainer()
    var data by remember { mutableStateOf<DuaData?>(null) }
    LaunchedEffect(Unit) { data = container.content.duas() }
    val bookmarks by container.userData.bookmarks.collectAsState(initial = emptySet())
    var onlyBookmarks by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("দোয়া ও যিকির", onBack)
        val d = data
        if (d == null) {
            Text("লোড হচ্ছে…", modifier = Modifier.padding(20.dp))
            return@Column
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "কুরআন ও সহীহ হাদীস থেকে ${DateFormatBn.digits(d.entries.size)}টি দোয়া ও যিকির। শিরোনামে চাপলে পূর্ণ দোয়া, উচ্চারণ, অর্থ ও সূত্র দেখা যাবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !onlyBookmarks, onClick = { onlyBookmarks = false }, label = { Text("সব") })
                        FilterChip(selected = onlyBookmarks, onClick = { onlyBookmarks = true }, label = { Text("বুকমার্ক") })
                    }
                }
            }
            d.categories.forEach { cat ->
                val rows = d.entries.filter { it.category == cat.id && (!onlyBookmarks || "dua:${it.id}" in bookmarks) }
                if (rows.isEmpty()) return@forEach
                item(key = "h_${cat.id}") { LuxSectionTitle(cat.titleBn, Modifier.padding(top = 8.dp)) }
                items(rows, key = { it.id }) { e ->
                    LuxCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpen(e.id) }, shape = MaterialTheme.shapes.medium) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(e.titleBn, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (e.quranRef != null) "কুরআন" else "সহীহ হাদীস",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if ("dua:${e.id}" in bookmarks) {
                                Icon(Icons.Filled.Bookmark, contentDescription = "বুকমার্ক করা", tint = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(6.dp))
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
            if (onlyBookmarks && d.entries.none { "dua:${it.id}" in bookmarks }) {
                item { Text("এখনও কোনো দোয়া বুকমার্ক করা হয়নি।", style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

/** একটি দোয়ার পূর্ণ পাতা: আরবি (বড় অক্ষরে), উচ্চারণ, অর্থ, ব্যবহার, সূত্র; বুকমার্ক ও নোট। */
@Composable
fun DuaDetailScreen(id: String, onBack: () -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<DuaData?>(null) }
    LaunchedEffect(Unit) { data = container.content.duas() }
    val bookmarks by container.userData.bookmarks.collectAsState(initial = emptySet())
    val notes by container.userData.notes.collectAsState(initial = emptyMap())
    var noteFor by remember { mutableStateOf<DuaEntry?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("দোয়া", onBack)
        val e = data?.entries?.firstOrNull { it.id == id }
        if (e == null) {
            Text(if (data == null) "লোড হচ্ছে…" else "দোয়াটি পাওয়া যায়নি।", modifier = Modifier.padding(20.dp))
            return@Column
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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

    noteFor?.let { n ->
        NoteDialog(
            initial = notes["dua:${n.id}"].orEmpty(),
            title = "নোট: ${n.titleBn}",
            onSave = { text -> scope.launch { container.userData.setNote("dua:${n.id}", text) } },
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
    LuxCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            ArabicText(entry.arabic, sizeSp = 30)
            entry.transliterationBn?.let {
                Text("উচ্চারণ: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text("অর্থ: ${entry.meaningBn}", style = MaterialTheme.typography.bodyMedium)
            entry.usageBn?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            entry.suggestedCount?.let { Text("সংখ্যা: $it বার", style = MaterialTheme.typography.bodySmall) }
            Text("সূত্র: ${entry.sourceRef}", style = MaterialTheme.typography.bodySmall)
            entry.hadithGrade?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            entry.meaningCreditBn?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            if (note.isNotBlank()) {
                Text("আপনার নোট: $note", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
