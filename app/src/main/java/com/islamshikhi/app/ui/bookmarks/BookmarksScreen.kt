package com.islamshikhi.app.ui.bookmarks

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.navigation.Routes
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private data class BookmarkRow(val key: String, val title: String, val subtitle: String?, val route: String)

/** দোয়া, কুরআনের আয়াত, মাসআলা, শিশুদের পাঠ ও লাইব্রেরির সব বুকমার্ক এক জায়গায়। */
@Composable
fun BookmarksScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val bookmarks by container.userData.bookmarks.collectAsState(initial = emptySet())

    var duaTitles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var chapterNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var masalaTitles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var kidTitles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var libraryTitles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var ayahPreview by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // কোনো কনটেন্ট ফাইল পড়তে ব্যর্থ হলে বাকি তালিকা যেন ঠিকঠাক দেখায়
    suspend fun <T> safe(block: suspend () -> T, fallback: T): T =
        try { block() } catch (e: CancellationException) { throw e } catch (e: Exception) { fallback }

    LaunchedEffect(Unit) {
        duaTitles = safe({ container.content.duas().entries.associate { it.id to it.titleBn } }, emptyMap())
        chapterNames = safe({ container.content.chapters().associate { it.id to it.bn } }, emptyMap())
        masalaTitles = safe({ container.content.masail().entries.associate { it.id to it.questionBn } }, emptyMap())
        kidTitles = safe({ container.content.kids().items.associate { it.id to it.titleBn } }, emptyMap())
        libraryTitles = safe({ container.content.library().items.associate { it.id to it.titleBn } }, emptyMap())
    }

    // আয়াতের বাংলা অনুবাদের ছোট অংশ দেখানোর জন্য
    LaunchedEffect(bookmarks) {
        val needed = bookmarks.filter { it.startsWith("ayah:") }
            .mapNotNull { it.split(":").getOrNull(1)?.toIntOrNull() }.distinct()
        val out = HashMap<String, String>()
        for (ch in needed) {
            val ayahs = safe({ container.content.ayahs(ch) }, emptyList())
            ayahs.forEach { out["$ch:${it.number}"] = it.bengali }
        }
        ayahPreview = out
    }

    val rows = remember(bookmarks, duaTitles, chapterNames, masalaTitles, kidTitles, libraryTitles, ayahPreview) {
        bookmarks.sorted().mapNotNull { key ->
            val p = key.split(":")
            when (p.firstOrNull()) {
                "dua" -> BookmarkRow(key, duaTitles[p.getOrNull(1)] ?: return@mapNotNull null, "দোয়া ও যিকির", Routes.DUA)
                "ayah" -> {
                    val ch = p.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                    val n = p.getOrNull(2)?.toIntOrNull() ?: return@mapNotNull null
                    BookmarkRow(
                        key,
                        "সূরা ${chapterNames[ch] ?: DateFormatBn.digits(ch)}, আয়াত ${DateFormatBn.digits(n)}",
                        ayahPreview["$ch:$n"]?.take(120),
                        Routes.quranRead(ch, n)
                    )
                }
                "masala" -> BookmarkRow(key, masalaTitles[p.getOrNull(1)] ?: return@mapNotNull null, "মাসআলা", Routes.masala(p[1]))
                "kid" -> BookmarkRow(key, kidTitles[p.getOrNull(1)] ?: return@mapNotNull null, "শিশুদের শিক্ষা", Routes.kid(p[1]))
                "library" -> BookmarkRow(key, libraryTitles[p.getOrNull(1)] ?: return@mapNotNull null, "লাইব্রেরি", Routes.library(p[1]))
                else -> null
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("বুকমার্ক", onBack)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (rows.isEmpty()) {
                item {
                    Text(
                        "এখনও কোনো বুকমার্ক নেই। দোয়া, আয়াত বা মাসআলার পাশের বুকমার্ক চিহ্নে চাপলে এখানে জমা হবে।",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(rows, key = { it.key }) { r ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(r.route) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(r.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            r.subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                        IconButton(onClick = { scope.launch { container.userData.toggleBookmark(r.key) } }) {
                            Icon(Icons.Filled.Bookmark, contentDescription = "বুকমার্ক সরান", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
