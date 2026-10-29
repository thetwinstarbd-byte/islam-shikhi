package com.islamshikhi.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.content.Chapter
import com.islamshikhi.app.data.content.DuaEntry
import com.islamshikhi.app.data.user.UserKeys
import com.islamshikhi.app.domain.model.AgeGroup
import com.islamshikhi.app.domain.model.Gender
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.feature.prayer.PrayerEngine
import com.islamshikhi.app.feature.prayer.PrayerSettings
import com.islamshikhi.app.feature.tracker.AmalItem
import com.islamshikhi.app.feature.tracker.AmalKeys
import com.islamshikhi.app.ui.common.ArabicText
import com.islamshikhi.app.ui.common.StatusBadge
import com.islamshikhi.app.ui.common.labelBn
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.navigation.AppTab
import com.islamshikhi.app.ui.navigation.Routes
import com.islamshikhi.app.util.DateFormatBn
import com.islamshikhi.app.util.TimeFormatBn
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * হোম স্ক্রিন: তারিখ (ইংরেজি + হিজরি), পরবর্তী নামাজ ও কাউন্টডাউন, দ্রুত বাটন, আজকের দোয়া,
 * আজকের আমল, কুরআনের শেষ পড়া, বুকমার্ক ও প্রোফাইল অনুযায়ী বিষয়ের পরামর্শ।
 * সব তথ্য ফোনের ভেতরের ডেটা থেকে আসে।
 */
@Composable
fun HomeScreen(
    profile: UserProfile,
    onOpenTab: (AppTab) -> Unit,
    onNavigate: (String) -> Unit
) {
    val container = rememberContainer()
    val today = remember { LocalDate.now() }
    val gregorianText = remember(today) { DateFormatBn.gregorian(today) }
    val hijriText = remember(today) { DateFormatBn.hijri(today) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DateCard(gregorianText, hijriText)

        NextPrayerCard(onClick = { onNavigate(Routes.PRAYER) })

        Text("দ্রুত খুলুন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickButton("নামাজ", Modifier.weight(1f)) { onNavigate(Routes.PRAYER) }
            QuickButton("কুরআন", Modifier.weight(1f)) { onOpenTab(AppTab.QURAN) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickButton("দোয়া", Modifier.weight(1f)) { onNavigate(Routes.DUA) }
            QuickButton("মাসআলা", Modifier.weight(1f)) { onOpenTab(AppTab.ASSISTANT) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { onNavigate(Routes.QURAN_SEARCH) }, modifier = Modifier.weight(1f)) {
                Text("কুরআনে খুঁজুন")
            }
            OutlinedButton(onClick = { onNavigate(Routes.BOOKMARKS) }, modifier = Modifier.weight(1f)) {
                Text("বুকমার্ক")
            }
        }

        DailyDuaCard(onOpen = { onNavigate(Routes.DUA) })

        TodayAmalCard(today = today, onClick = { onNavigate(Routes.TRACKER) })

        LastReadCard(onOpen = { chapter, ayah -> onNavigate(Routes.quranRead(chapter, ayah)) })

        SuggestionsSection(profile, onOpenAssistant = { onOpenTab(AppTab.ASSISTANT) })
    }
}

@Composable
private fun DateCard(gregorian: String, hijri: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "আসসালামু আলাইকুম",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(gregorian, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(hijri, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                "হিজরি তারিখ গণনা করা। স্থানীয় চাঁদ দেখার ওপর নির্ভর করে এক দিন পর্যন্ত পার্থক্য হতে পারে।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun NextPrayerCard(onClick: () -> Unit) {
    val container = rememberContainer()
    val settings by container.prayerSettings.settings.collectAsState(initial = PrayerSettings())
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val upcoming = remember(settings, now / 1000) { PrayerEngine.next(settings, now) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("পরবর্তী নামাজ", style = MaterialTheme.typography.labelLarge)
            Text(
                "${upcoming.prayer.labelBn} • ${TimeFormatBn.format(upcoming.atMillis)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("বাকি: ${TimeFormatBn.countdown(upcoming.atMillis - now)}", style = MaterialTheme.typography.titleMedium)
            Text(
                "${settings.placeLabelBn} • ${settings.method.labelBn}। মসজিদের জামাতের সময়ের সঙ্গে কয়েক মিনিট পার্থক্য হতে পারে।",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun QuickButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier) { Text(label) }
}

/** প্রতিদিন একটি করে দোয়া/যিকির (বছরের দিন-সংখ্যা অনুযায়ী ঘুরে ঘুরে)। */
@Composable
private fun DailyDuaCard(onOpen: () -> Unit) {
    val container = rememberContainer()
    var entry by remember { mutableStateOf<DuaEntry?>(null) }
    LaunchedEffect(Unit) {
        val all = container.content.duas().entries.filter { it.quranRef == null && it.arabic.length < 220 }
        if (all.isNotEmpty()) entry = all[LocalDate.now().dayOfYear % all.size]
    }
    val e = entry ?: return

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("আজকের দোয়া ও যিকির", style = MaterialTheme.typography.labelLarge)
            Text(e.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            StatusBadge(e.verificationStatus)
            ArabicText(e.arabic, sizeSp = 24)
            Text("অর্থ: ${e.meaningBn}", style = MaterialTheme.typography.bodyMedium)
            Text("সূত্র: ${e.sourceRef}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TodayAmalCard(today: LocalDate, onClick: () -> Unit) {
    val container = rememberContainer()
    val done by container.userData.set(AmalKeys.SET).collectAsState(initial = emptySet())
    val count = AmalKeys.doneCount(done, today)
    val total = AmalItem.entries.size
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("আজকের আমল", style = MaterialTheme.typography.labelLarge)
            Text(
                "${DateFormatBn.digits(count)} / ${DateFormatBn.digits(total)} সম্পন্ন",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.material3.LinearProgressIndicator(
                progress = { if (total == 0) 0f else count / total.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Text("চাপ দিয়ে আমল ট্র্যাকার খুলুন।", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** কুরআনে শেষবার যেখানে পড়া হয়েছিল ("chapter:ayah" আকারে সংরক্ষিত), সেখান থেকে চালু করার কার্ড। */
@Composable
private fun LastReadCard(onOpen: (Int, Int) -> Unit) {
    val container = rememberContainer()
    val last by container.userData.string(UserKeys.QURAN_LAST).collectAsState(initial = null)
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    LaunchedEffect(Unit) { chapters = container.content.chapters() }

    val parts = last?.split(":")
    val chapterId = parts?.getOrNull(0)?.toIntOrNull() ?: return
    val ayah = parts.getOrNull(1)?.toIntOrNull() ?: 1
    val chapter = chapters.firstOrNull { it.id == chapterId } ?: return

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(chapterId, ayah) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("কুরআন: শেষ পড়া", style = MaterialTheme.typography.labelLarge)
            Text(
                "সূরা ${chapter.bn}, আয়াত ${DateFormatBn.digits(ayah)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("চাপ দিয়ে সেখান থেকে আবার শুরু করুন।", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * প্রোফাইল অনুযায়ী বিষয়ের পরামর্শ। এখানে কোনো ধর্মীয় বিধান বলা হয় না, শুধু কোন বিষয় খুলতে
 * পারেন তা দেখানো হয়। উভয়ের জন্য সাধারণ বিষয় সবসময় ট্যাবগুলোতে থাকে।
 */
@Composable
private fun SuggestionsSection(profile: UserProfile, onOpenAssistant: () -> Unit) {
    val topics = buildList {
        profile.interests.forEach { add(it.labelBn()) }
        when (profile.gender) {
            Gender.FEMALE -> add("নারীদের বিষয়")
            Gender.MALE -> add("পুরুষদের বিষয়")
            null -> Unit
        }
        if (profile.ageGroup == AgeGroup.CHILD_TEEN) add("শিশু/কিশোরদের শিক্ষা")
    }.distinct()

    if (topics.isEmpty()) return

    Text("আপনার জন্য বিষয়", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        topics.forEach { topic ->
            AssistChip(onClick = onOpenAssistant, label = { Text(topic) })
        }
    }
}
