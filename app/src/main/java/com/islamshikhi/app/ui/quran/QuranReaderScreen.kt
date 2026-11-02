package com.islamshikhi.app.ui.quran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.DisposableEffect
import com.islamshikhi.app.feature.audio.RecitationController
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.content.Ayah
import com.islamshikhi.app.data.content.Chapter
import com.islamshikhi.app.data.content.ChapterText
import com.islamshikhi.app.data.user.UserKeys
import com.islamshikhi.app.ui.common.ArabicText
import com.islamshikhi.app.ui.common.NoteDialog
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import com.islamshikhi.app.util.boldMarkup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.time.LocalDate

/** একটি সূরার পাঠ: আরবি, বাংলা অনুবাদ, টীকা, বুকমার্ক, নোট ও আজকের পড়ার চিহ্ন। */
@Composable
fun QuranReaderScreen(chapterId: Int, startAyah: Int, onBack: () -> Unit) {
    val container = rememberContainer()
    var chapter by remember(chapterId) { mutableStateOf<Chapter?>(null) }
    var text by remember(chapterId) { mutableStateOf<ChapterText?>(null) }
    var error by remember(chapterId) { mutableStateOf<String?>(null) }

    LaunchedEffect(chapterId) {
        try {
            chapter = container.content.chapters().first { it.id == chapterId }
            text = container.content.chapterText(chapterId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = "এই সূরা লোড করা যায়নি। (${e.message ?: "অজানা ত্রুটি"})"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(chapter?.let { "সূরা ${it.bn}" } ?: "কুরআন", onBack)
        val c = chapter
        val t = text
        when {
            error != null -> Text(error.orEmpty(), modifier = Modifier.padding(20.dp))
            c == null || t == null -> Text("লোড হচ্ছে…", modifier = Modifier.padding(20.dp))
            else -> ReaderContent(c, t, startAyah)
        }
    }
}

@Composable
private fun ReaderContent(chapter: Chapter, text: ChapterText, startAyah: Int) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }

    val bookmarks by container.userData.bookmarks.collectAsState(initial = emptySet())
    val notes by container.userData.notes.collectAsState(initial = emptyMap())
    val readToday by container.userData.set(UserKeys.quranRead(today)).collectAsState(initial = emptySet())
    var noteFor by remember { mutableStateOf<Ayah?>(null) }

    // তিলাওয়াত (ইন্টারনেট থেকে স্ট্রিম; শুধু প্লে চাপলে)
    val player = remember { RecitationController() }
    DisposableEffect(Unit) { onDispose { player.release() } }
    val reciterId by container.userData.string("quran_reciter").collectAsState(initial = null)
    val reciter = RecitationController.reciters.firstOrNull { it.id == reciterId } ?: RecitationController.reciters[0]
    var reciterMenu by remember { mutableStateOf(false) }

    // তালিকার শুরুতে: ১টি শিরোনাম কার্ড, (থাকলে) ১টি বিসমিল্লাহ, তারপর আয়াতগুলো
    val headerCount = 1 + if (text.bismillah != null) 1 else 0
    val total = text.ayahs.size
    val startIndex = (headerCount + startAyah - 1).coerceIn(0, headerCount + total - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    // চলমান আয়াতে স্ক্রল
    LaunchedEffect(player.current) {
        player.current?.let { listState.animateScrollToItem((headerCount + it - 1).coerceIn(0, headerCount + total - 1)) }
    }

    // স্ক্রল থামলে "শেষ পড়া" অবস্থান সংরক্ষণ
    LaunchedEffect(chapter.id) {
        snapshotFlow { listState.isScrollInProgress to listState.firstVisibleItemIndex }
            .filter { !it.first }
            .collect { (_, index) ->
                val ayah = (index - headerCount + 1).coerceIn(1, total)
                container.userData.putString(UserKeys.QURAN_LAST, "${chapter.id}:$ayah")
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${chapter.bn} (${chapter.ar})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${chapter.typeBn} • ${DateFormatBn.digits(chapter.verses)} আয়াত",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "বাংলা অনুবাদ: আবু বকর যাকারিয়া (QuranEnc.com, সংস্করণ ১.১.১)। আরবি: Tanzil Project। " +
                            "অনুবাদের [১] চিহ্ন টীকার নির্দেশক।",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            if (player.current != null) player.stop()
                            else player.play(chapter.id, 1, total, reciter.folder, true)
                        }) {
                            Icon(if (player.current != null) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null)
                            Text(if (player.current != null) "থামান" else "পুরো সূরা শুনুন")
                        }
                        OutlinedButton(onClick = { reciterMenu = true }) { Text("ক্বারী", maxLines = 1) }
                        DropdownMenu(expanded = reciterMenu, onDismissRequest = { reciterMenu = false }) {
                            RecitationController.reciters.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(if (r.id == reciter.id) "✓ ${r.nameBn}" else r.nameBn) },
                                    onClick = { reciterMenu = false; scope.launch { container.userData.putString("quran_reciter", r.id) } }
                                )
                            }
                        }
                    }
                    Text(
                        "ক্বারী: ${reciter.nameBn} • তিলাওয়াত ইন্টারনেট থেকে শোনানো হয় (EveryAyah.com)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    player.error?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        text.bismillah?.let { b ->
            item { ArabicText(b, sizeSp = 28) }
        }
        itemsIndexed(text.ayahs, key = { _, a -> a.number }) { _, a ->
            val key = UserKeys.ayahKey(chapter.id, a.number)
            AyahCard(
                ayah = a,
                bookmarked = key in bookmarks,
                read = "${chapter.id}:${a.number}" in readToday,
                note = notes[key].orEmpty(),
                playing = player.current == a.number,
                loadingAudio = player.current == a.number && player.loading,
                onPlay = {
                    if (player.current == a.number) player.stop()
                    else player.play(chapter.id, a.number, total, reciter.folder, false)
                },
                onBookmark = { scope.launch { container.userData.toggleBookmark(key) } },
                onRead = {
                    scope.launch {
                        container.userData.toggleInSet(UserKeys.quranRead(today), "${chapter.id}:${a.number}")
                    }
                },
                onNote = { noteFor = a }
            )
        }
    }

    noteFor?.let { a ->
        val key = UserKeys.ayahKey(chapter.id, a.number)
        NoteDialog(
            initial = notes[key].orEmpty(),
            title = "নোট: ${chapter.bn} ${DateFormatBn.digits(a.number)}",
            onSave = { value -> scope.launch { container.userData.setNote(key, value) } },
            onDismiss = { noteFor = null }
        )
    }
}

@Composable
private fun AyahCard(
    ayah: Ayah,
    bookmarked: Boolean,
    read: Boolean,
    note: String,
    playing: Boolean,
    loadingAudio: Boolean,
    onPlay: () -> Unit,
    onBookmark: () -> Unit,
    onRead: () -> Unit,
    onNote: () -> Unit
) {
    var showFootnote by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (playing) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "আয়াত ${DateFormatBn.digits(ayah.number)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onPlay) {
                    Icon(
                        if (playing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (playing) "তিলাওয়াত থামান" else if (loadingAudio) "লোড হচ্ছে" else "তিলাওয়াত শুনুন",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onRead) {
                    Icon(
                        if (read) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (read) "আজকের পড়া থেকে সরান" else "আজকের পড়া হিসেবে চিহ্নিত করুন",
                        tint = if (read) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNote) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "নোট",
                        tint = if (note.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onBookmark) {
                    Icon(
                        if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (bookmarked) "বুকমার্ক সরান" else "বুকমার্ক করুন"
                    )
                }
            }
            ArabicText(ayah.arabic, sizeSp = 28)
            Text(ayah.bengali, style = MaterialTheme.typography.bodyLarge)
            if (ayah.footnote != null) {
                TextButton(onClick = { showFootnote = !showFootnote }) {
                    Text(if (showFootnote) "টীকা লুকান" else "টীকা দেখুন")
                }
                if (showFootnote) {
                    Text(boldMarkup(ayah.footnote), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (note.isNotBlank()) {
                Text("আপনার নোট: $note", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
