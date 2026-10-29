package com.islamshikhi.app.ui.quran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.content.Chapter
import com.islamshikhi.app.data.content.SearchHit
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.delay

/** আরবি (হরকত ছাড়া লিখলেও চলে) বা বাংলা শব্দ দিয়ে আয়াত খোঁজা। */
@Composable
fun QuranSearchScreen(onBack: () -> Unit, onOpen: (chapter: Int, ayah: Int) -> Unit) {
    val container = rememberContainer()
    var query by remember { mutableStateOf("") }
    var hits by remember { mutableStateOf<List<SearchHit>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }

    LaunchedEffect(Unit) { chapters = container.content.chapters() }
    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            hits = emptyList()
            searching = false
            return@LaunchedEffect
        }
        searching = true
        delay(300) // টাইপ করার সময় প্রতি অক্ষরে খোঁজা এড়াতে
        hits = container.content.searchAyahs(query)
        searching = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("কুরআনে খুঁজুন", onBack)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            label = { Text("আরবি বা বাংলা শব্দ") },
            singleLine = true
        )
        Text(
            "বাংলায় খুঁজলে অনুবাদের লেখায় মেলানো হয়। আরবিতে খুঁজলে হরকত ছাড়া লিখলেও চলবে। সর্বোচ্চ ৬০টি ফল দেখানো হয়।",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when {
                query.trim().length < 2 -> item { Text("কমপক্ষে ২টি অক্ষর লিখুন।", style = MaterialTheme.typography.bodyMedium) }
                searching -> item { Text("খুঁজছি…", style = MaterialTheme.typography.bodyMedium) }
                hits.isEmpty() -> item { Text("কিছু পাওয়া যায়নি।", style = MaterialTheme.typography.bodyMedium) }
            }
            items(hits, key = { "${it.chapter}:${it.ayah}" }) { h ->
                val name = chapters.firstOrNull { it.id == h.chapter }?.bn ?: "সূরা ${DateFormatBn.digits(h.chapter)}"
                val isArabic = h.snippet.any { it in '؀'..'ۿ' }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpen(h.chapter, h.ayah) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "$name, আয়াত ${DateFormatBn.digits(h.ayah)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            h.snippet,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDirection = if (isArabic) TextDirection.Rtl else TextDirection.Content
                            )
                        )
                    }
                }
            }
        }
    }
}
