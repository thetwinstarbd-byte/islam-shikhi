package com.islamshikhi.app.ui.assistant

import androidx.compose.foundation.clickable
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
import com.islamshikhi.app.data.content.Evidence
import com.islamshikhi.app.data.content.Masala
import com.islamshikhi.app.data.content.MasalaData
import com.islamshikhi.app.data.user.UserKeys
import com.islamshikhi.app.ui.common.ArabicText
import com.islamshikhi.app.ui.common.ContentGate
import com.islamshikhi.app.ui.common.NoteDialog
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.StatusBadge
import com.islamshikhi.app.ui.common.WarningCard
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.common.rememberLoaded
import kotlinx.coroutines.launch

/** একটি মাসআলার পূর্ণ পাতা: সংক্ষিপ্ত উত্তর, ব্যাখ্যা, দলিল, মতভেদ, সংশ্লিষ্ট প্রশ্ন, যাচাইয়ের তথ্য। */
@Composable
fun MasalaDetailScreen(id: String, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val container = rememberContainer()
    val result = rememberLoaded { container.content.masail() }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("মাসআলা", onBack)
        ContentGate(result) { data ->
            val m = data.entries.firstOrNull { it.id == id }
            if (m == null) {
                Text("এই মাসআলাটি পাওয়া যায়নি।", modifier = Modifier.padding(20.dp))
            } else {
                MasalaBody(m, data, onOpen)
            }
        }
    }
}

@Composable
private fun MasalaBody(m: Masala, data: MasalaData, onOpen: (String) -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val key = UserKeys.masalaKey(m.id)
    val bookmarks by container.userData.bookmarks.collectAsState(initial = emptySet())
    val note by container.userData.note(key).collectAsState(initial = "")
    var editNote by remember { mutableStateOf(false) }
    val bookmarked = key in bookmarks

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                m.questionBn,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { editNote = true }) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "নোট",
                    tint = if (note.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { scope.launch { container.userData.toggleBookmark(key) } }) {
                Icon(
                    if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = if (bookmarked) "বুকমার্ক সরান" else "বুকমার্ক করুন"
                )
            }
        }
        StatusBadge(m.verificationStatus)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("সংক্ষিপ্ত উত্তর", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(m.shortAnswerBn, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        SectionTitle("বিস্তারিত")
        Text(m.detailBn, style = MaterialTheme.typography.bodyLarge)

        if (m.askScholar) {
            WarningCard("এই বিষয়ে ব্যক্তিগত অবস্থার ওপর বিধান বদলাতে পারে। সিদ্ধান্ত নেওয়ার আগে যোগ্য আলেমের পরামর্শ নিন।")
        }

        SectionTitle("দলিল")
        m.evidence.forEach { EvidenceCard(it) }

        if (m.viewpoints.isNotEmpty()) {
            SectionTitle("মতভেদ")
            m.viewpoints.forEach { v ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(v.titleBn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(v.textBn, style = MaterialTheme.typography.bodyMedium)
                        v.sourceRef?.let { Text("সূত্র: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }

        val related = m.related.mapNotNull { rid -> data.entries.firstOrNull { it.id == rid } }
        if (related.isNotEmpty()) {
            SectionTitle("সংশ্লিষ্ট প্রশ্ন")
            related.forEach { r ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpen(r.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(r.questionBn, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (note.isNotBlank()) {
            SectionTitle("আপনার নোট")
            Text(note, style = MaterialTheme.typography.bodyMedium)
        }

        m.checkNoteBn?.let {
            SectionTitle("যাচাইয়ের তথ্য")
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            "এটি ফতোয়া নয়। আপনার অনুসৃত মাযহাব বা যোগ্য আলেমের কাছ থেকে নিশ্চিত হয়ে নিন।",
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (editNote) {
        NoteDialog(
            initial = note,
            title = "নোট",
            onSave = { text -> scope.launch { container.userData.setNote(key, text) } },
            onDismiss = { editNote = false }
        )
    }
}

@Composable
private fun EvidenceCard(e: Evidence) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(e.ref, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            e.arabic?.let { ArabicText(it, sizeSp = 22) }
            e.meaningBn?.let { Text("অর্থ: $it", style = MaterialTheme.typography.bodyMedium) }
            e.creditBn?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
