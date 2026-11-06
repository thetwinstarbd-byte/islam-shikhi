package com.islamshikhi.app.ui.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import com.islamshikhi.app.data.content.Chapter
import com.islamshikhi.app.data.content.DuaEntry
import com.islamshikhi.app.data.content.IlmArticle
import com.islamshikhi.app.data.content.Masala
import com.islamshikhi.app.data.content.SearchHit
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.navigation.Routes
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.delay

private const val PER_GROUP = 8

private fun hit(q: String, vararg fields: String?): Boolean = fields.any { it?.contains(q, ignoreCase = true) == true }

private fun cut(s: String, n: Int): String = if (s.length <= n) s else s.take(n).trimEnd() + "…"

/** একটি সার্চ বক্সে দোয়া, মাসআলা, জ্ঞান-প্রবন্ধ, সূরা ও আয়াত একসঙ্গে খোঁজা। সবই ফোনের ভেতরে, ইন্টারনেট লাগে না। */
@Composable
fun SearchScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val container = rememberContainer()
    var query by remember { mutableStateOf("") }
    var duas by remember { mutableStateOf<List<DuaEntry>>(emptyList()) }
    var masail by remember { mutableStateOf<List<Masala>>(emptyList()) }
    var articles by remember { mutableStateOf<List<IlmArticle>>(emptyList()) }
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var ayahHits by remember { mutableStateOf<List<SearchHit>>(emptyList()) }

    LaunchedEffect(Unit) {
        duas = container.content.duas().entries
        masail = container.content.masail().entries
        articles = container.content.ilm().articles
        chapters = container.content.chapters()
    }

    val q = query.trim()
    LaunchedEffect(q) {
        if (q.length >= 2) {
            delay(300)
            ayahHits = container.content.searchAyahs(q, PER_GROUP)
        } else {
            ayahHits = emptyList()
        }
    }

    val duaHits = if (q.length >= 2) duas.filter { hit(q, it.titleBn, it.meaningBn, it.transliterationBn) }.take(PER_GROUP) else emptyList()
    val masalaHits = if (q.length >= 2) masail.filter { hit(q, it.questionBn, it.shortAnswerBn) }.take(PER_GROUP) else emptyList()
    val articleHits = if (q.length >= 2) articles.filter { hit(q, it.titleBn, it.summaryBn) }.take(PER_GROUP) else emptyList()
    val chapterHits = if (q.isNotEmpty()) chapters.filter {
        hit(q, it.bn, it.ar, it.en, it.meaning) || it.id.toString() == q
    }.take(PER_GROUP) else emptyList()
    val any = duaHits.isNotEmpty() || masalaHits.isNotEmpty() || articleHits.isNotEmpty() || chapterHits.isNotEmpty() || ayahHits.isNotEmpty()

    ScreenColumn("সার্চ", onBack) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text("দোয়া, মাসআলা, প্রবন্ধ বা সূরার নাম") }
        )
        if (q.isEmpty()) {
            InfoCard(
                title = "একসঙ্গে খুঁজুন",
                body = "দোয়া, মাসআলা, ইসলামি জ্ঞানের প্রবন্ধ, সূরার নাম ও কুরআনের আয়াত (আরবি বা বাংলা শব্দে) এক জায়গায় খোঁজা যায়। দুই অক্ষরের বেশি লিখুন।"
            )
        } else if (!any && q.length >= 2) {
            Text("কিছু পাওয়া যায়নি। অন্য শব্দে খুঁজে দেখুন।", style = MaterialTheme.typography.bodyMedium)
        }

        if (duaHits.isNotEmpty()) SectionTitle("দোয়া")
        duaHits.forEach { MenuCard(it.titleBn, cut(it.meaningBn, 90)) { onNavigate(Routes.dua(it.id)) } }

        if (masalaHits.isNotEmpty()) SectionTitle("মাসআলা")
        masalaHits.forEach { MenuCard(cut(it.questionBn, 80), cut(it.shortAnswerBn, 90)) { onNavigate(Routes.masala(it.id)) } }

        if (articleHits.isNotEmpty()) SectionTitle("জ্ঞান-প্রবন্ধ")
        articleHits.forEach { MenuCard(it.titleBn, cut(it.summaryBn, 90)) { onNavigate(Routes.ilm(it.id)) } }

        if (chapterHits.isNotEmpty()) SectionTitle("সূরা")
        chapterHits.forEach {
            MenuCard("${DateFormatBn.digits(it.id)}. ${it.bn} (${it.ar})", "${it.meaning} • ${DateFormatBn.digits(it.verses)} আয়াত") {
                onNavigate(Routes.quranRead(it.id))
            }
        }

        if (ayahHits.isNotEmpty()) SectionTitle("কুরআনের আয়াত")
        ayahHits.forEach {
            val name = chapters.firstOrNull { c -> c.id == it.chapter }?.bn ?: "সূরা ${DateFormatBn.digits(it.chapter)}"
            MenuCard("$name ${DateFormatBn.digits(it.chapter)}:${DateFormatBn.digits(it.ayah)}", cut(it.snippet, 100)) {
                onNavigate(Routes.quranRead(it.chapter, it.ayah))
            }
        }
    }
}
