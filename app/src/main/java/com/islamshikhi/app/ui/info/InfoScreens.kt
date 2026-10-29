package com.islamshikhi.app.ui.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.content.InfoData
import com.islamshikhi.app.data.content.InfoItem
import com.islamshikhi.app.ui.common.ContentGate
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.NoteDialog
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.StatusBadge
import com.islamshikhi.app.ui.common.WarningCard
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.common.rememberLoaded
import kotlinx.coroutines.launch

/**
 * শিশুদের শিক্ষা, জরুরি গাইড ও লাইব্রেরি—তিনটিই একই কাঠামোর কনটেন্ট, তাই একই স্ক্রিন।
 * [warning] থাকলে সবার ওপরে লাল কার্ডে দেখায় (যেমন জরুরি গাইডে "আগে চিকিৎসা")।
 */
@Composable
fun InfoListScreen(
    title: String,
    intro: String,
    warning: String?,
    loader: suspend () -> InfoData,
    onBack: () -> Unit,
    onOpen: (String) -> Unit
) {
    val result = rememberLoaded { loader() }
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title, onBack)
        ContentGate(result) { data ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (warning != null) WarningCard(warning)
                Text(intro, style = MaterialTheme.typography.bodyMedium)
                data.categories.forEach { cat ->
                    val items = data.items.filter { it.category == cat.id }
                    if (items.isNotEmpty()) {
                        SectionTitle(cat.titleBn)
                        items.forEach { item -> MenuCard(item.titleBn, item.summaryBn) { onOpen(item.id) } }
                    }
                }
            }
        }
    }
}

/** একটি কনটেন্টের পূর্ণ পাতা। [bookmarkKey] দিলে বুকমার্ক ও নোটের বাটন দেখায়। */
@Composable
fun InfoDetailScreen(
    id: String,
    headerTitle: String,
    loader: suspend () -> InfoData,
    bookmarkKey: ((String) -> String)?,
    onBack: () -> Unit
) {
    val result = rememberLoaded { loader() }
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(headerTitle, onBack)
        ContentGate(result) { data ->
            val item = data.items.firstOrNull { it.id == id }
            if (item == null) {
                Text("এই পাতাটি পাওয়া যায়নি।", modifier = Modifier.padding(20.dp))
            } else {
                InfoBody(item, bookmarkKey?.invoke(item.id))
            }
        }
    }
}

@Composable
private fun InfoBody(item: InfoItem, key: String?) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val bookmarks by container.userData.bookmarks.collectAsState(initial = emptySet())
    val note by container.userData.note(key ?: "none").collectAsState(initial = "")
    var editNote by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                item.titleBn,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (key != null) {
                IconButton(onClick = { editNote = true }) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "নোট",
                        tint = if (note.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val marked = key in bookmarks
                IconButton(onClick = { scope.launch { container.userData.toggleBookmark(key) } }) {
                    Icon(
                        if (marked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (marked) "বুকমার্ক সরান" else "বুকমার্ক করুন"
                    )
                }
            }
        }
        StatusBadge(item.verificationStatus)

        item.sections.forEach { s ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(s.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(s.bodyBn, style = MaterialTheme.typography.bodyLarge)
                    s.sourceRef?.let { Text("সূত্র: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }

        if (key != null && note.isNotBlank()) {
            SectionTitle("আপনার নোট")
            Text(note, style = MaterialTheme.typography.bodyMedium)
        }
        item.checkNoteBn?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Text(
            "এটি ফতোয়া নয়। নিশ্চিত হতে যোগ্য আলেম বা নির্ভরযোগ্য মূল গ্রন্থ দেখুন।",
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (editNote && key != null) {
        NoteDialog(
            initial = note,
            title = "নোট",
            onSave = { text -> scope.launch { container.userData.setNote(key, text) } },
            onDismiss = { editNote = false }
        )
    }
}
