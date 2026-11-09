package com.islamshikhi.app.ui.ilm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.luminance
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.islamshikhi.app.domain.model.ThemeMode
import com.islamshikhi.app.ui.i18n.tr
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.islamshikhi.app.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.content.Ayah
import com.islamshikhi.app.data.content.Chapter
import com.islamshikhi.app.data.content.IlmArticle
import com.islamshikhi.app.data.content.IlmData
import com.islamshikhi.app.data.content.IlmHadith
import com.islamshikhi.app.data.content.IlmSection
import com.islamshikhi.app.data.content.QuranRef
import com.islamshikhi.app.data.content.Viewpoint
import com.islamshikhi.app.ui.common.ArabicText
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.StatusBadge
import com.islamshikhi.app.ui.common.WarningCard
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.effects.LuxCard
import com.islamshikhi.app.ui.effects.LuxSectionTitle
import com.islamshikhi.app.ui.effects.goldBorder
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch

/**
 * ইসলামি জ্ঞান: বিভাগ অনুযায়ী বিস্তারিত প্রবন্ধের তালিকা। সাধারণ পাঠক থেকে গবেষণা-আগ্রহী পর্যন্ত সবার জন্য;
 * প্রতিটি প্রবন্ধের স্তর (levelBn) কার্ডেই লেখা থাকে।
 */
@Composable
fun IlmListScreen(onBack: () -> Unit, onOpen: (String) -> Unit, onOpenMasail: () -> Unit) {
    val container = rememberContainer()
    var data by remember { mutableStateOf<IlmData?>(null) }
    LaunchedEffect(Unit) { data = container.content.ilm() }
    val prefs by container.userData.data.collectAsState(initial = emptyPreferences())

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("ইসলামি জ্ঞান", onBack)
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
            d.categories.forEach { cat ->
                item(key = "cat_${cat.id}") {
                    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LuxSectionTitle(cat.titleBn)
                        Text(cat.introBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(d.articles.filter { it.category == cat.id }, key = { it.id }) { a ->
                    LuxCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpen(a.id) }, shape = MaterialTheme.shapes.medium) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(a.titleBn, style = MaterialTheme.typography.titleMedium)
                                Text(a.summaryBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val readPct = prefs[intPreferencesKey("ilm_read:${a.id}")] ?: 0
                                Text(
                                    a.levelBn + if (readPct > 0) " • পড়া হয়েছে ${DateFormatBn.digits(readPct)}%" else "",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
            item(key = "masail_link") {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LuxSectionTitle("ফিকহের প্রশ্নোত্তর")
                    LuxCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenMasail, shape = MaterialTheme.shapes.medium) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("মাসআলা (আগের প্রশ্নোত্তর)", style = MaterialTheme.typography.titleMedium)
                                Text("ওযু, নামাজ, রোজা, যাকাত ইত্যাদির সংক্ষিপ্ত প্রশ্নোত্তর", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

/** একটি প্রবন্ধের পূর্ণ পাতা: সূচি, অংশ অনুযায়ী লেখা, আয়াত, হাদীস, মতভেদ, আরও পড়ার তালিকা। */
@Composable
fun IlmArticleScreen(id: String, onBack: () -> Unit, onOpenAyah: (chapter: Int, ayah: Int) -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<IlmData?>(null) }
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    LaunchedEffect(Unit) {
        data = container.content.ilm()
        chapters = container.content.chapters()
    }
    val listState = rememberLazyListState()
    val profile by container.profileRepository.profile.collectAsState(initial = null)
    val stored by container.userData.int("ilm_read:$id").collectAsState(initial = 0)
    val percent by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val count = info.totalItemsCount
            if (count <= 1) 0 else {
                val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (last >= count - 1) 100 else listState.firstVisibleItemIndex * 100 / (count - 1)
            }
        }
    }
    LaunchedEffect(percent) {
        if (percent > stored) container.userData.putInt("ilm_read:$id", percent)
    }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("ইসলামি জ্ঞান", onBack)
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = MaterialTheme.colorScheme.secondary
        )
        val article = data?.articles?.firstOrNull { it.id == id }
        if (article == null) {
            Text(if (data == null) "লোড হচ্ছে…" else "প্রবন্ধটি পাওয়া যায়নি।", modifier = Modifier.padding(20.dp))
            return@Column
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "top") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(article.titleBn, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
                    Text(article.levelBn, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                    StatusBadge(article.verificationStatus)
                    Text(
                        "${tr("পড়ার অগ্রগতি")}: ${DateFormatBn.digits(percent)}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = {
                            scope.launch { container.profileRepository.setFontScale(((profile?.fontScale ?: 1f) - 0.1f).coerceAtLeast(0.8f)) }
                        }) { Text("A−") }
                        OutlinedButton(onClick = {
                            scope.launch { container.profileRepository.setFontScale(((profile?.fontScale ?: 1f) + 0.1f).coerceAtMost(1.6f)) }
                        }) { Text("A+") }
                        OutlinedButton(onClick = {
                            scope.launch { container.profileRepository.setThemeMode(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK) }
                        }) { Text(if (isDark) "দিনের মোড" else tr("রাতের মোড")) }
                    }
                }
            }
            item(key = "toc") {
                LuxCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("এই প্রবন্ধে", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                        article.sections.forEachIndexed { i, s ->
                            Text(
                                "${DateFormatBn.digits(i + 1)}। ${s.headingBn}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { scope.launch { listState.animateScrollToItem(2 + i) } }
                                    .padding(vertical = 6.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            items(article.sections.size, key = { "sec_$it" }) { i ->
                SectionView(i + 1, article.sections[i], chapters, onOpenAyah)
            }
            item(key = "tail") { ArticleTail(article) }
        }
    }
}

@Composable
private fun SectionView(number: Int, s: IlmSection, chapters: List<Chapter>, onOpenAyah: (Int, Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "${DateFormatBn.digits(number)}। ${s.headingBn}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        s.paragraphs.forEach { p ->
            Text(p, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        s.ayahs.forEach { ref -> AyahBlock(ref, chapters, onOpenAyah) }
        s.hadiths.forEach { h -> HadithBlock(h) }
        if (s.viewpoints.isNotEmpty()) {
            Text("আলেমদের মত", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
            s.viewpoints.forEach { v -> ViewpointBlock(v) }
        }
    }
}

/** কুরআনের আয়াত: আরবি ও বাংলা অনুবাদ অ্যাপের কুরআন-ডেটা (Tanzil + QuranEnc) থেকে সরাসরি আসে, হাতে লেখা নয়। */
@Composable
private fun AyahBlock(ref: QuranRef, chapters: List<Chapter>, onOpenAyah: (Int, Int) -> Unit) {
    val container = rememberContainer()
    var ayahs by remember(ref) { mutableStateOf<List<Ayah>?>(null) }
    LaunchedEffect(ref) { ayahs = container.content.ayahRange(ref) }
    val name = chapters.firstOrNull { it.id == ref.chapter }?.bn ?: ""
    val shown = if (ref.from == ref.to) "${DateFormatBn.digits(ref.chapter)}:${DateFormatBn.digits(ref.from)}"
    else "${DateFormatBn.digits(ref.chapter)}:${DateFormatBn.digits(ref.from)}-${DateFormatBn.digits(ref.to)}"

    LuxCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenAyah(ref.chapter, ref.from) }, shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("কুরআন • সূরা $name ($shown)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            val list = ayahs
            if (list == null) {
                Text("লোড হচ্ছে…", style = MaterialTheme.typography.bodySmall)
            } else {
                list.forEach { a ->
                    ArabicText(a.arabic + " ﴿${DateFormatBn.digits(a.number)}﴾", sizeSp = 24)
                    Text(a.bengali, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun HadithBlock(h: IlmHadith) {
    LuxCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("হাদীস • ${h.narratorBn}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            h.arabic?.let { ArabicText(it, sizeSp = 22) }
            Text("অর্থ: ${h.meaningBn}", style = MaterialTheme.typography.bodyMedium)
            Text("সূত্র: ${h.ref}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("মান: ${h.gradeBn}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ViewpointBlock(v: Viewpoint) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().goldBorder(MaterialTheme.shapes.medium, alpha = 0.35f)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(v.titleBn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(v.textBn, style = MaterialTheme.typography.bodyMedium)
            v.sourceRef?.let { Text("সূত্র: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun ArticleTail(a: IlmArticle) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
        if (a.readingBn.isNotEmpty()) {
            LuxSectionTitle("আরও পড়ুন")
            a.readingBn.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
        }
        Text(
            "কুরআনের আরবি: Tanzil Project (উসমানী)। বাংলা অনুবাদ: আবু বকর যাকারিয়া, QuranEnc.com। হাদীসের আরবি: সহীহ বুখারী ও সহীহ মুসলিম।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
