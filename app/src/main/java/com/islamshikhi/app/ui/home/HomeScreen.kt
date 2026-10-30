package com.islamshikhi.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.luminance
import com.islamshikhi.app.ui.theme.SystemBarIcons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamshikhi.app.data.content.Chapter
import com.islamshikhi.app.data.content.DuaEntry
import com.islamshikhi.app.data.user.UserKeys
import com.islamshikhi.app.domain.model.AgeGroup
import com.islamshikhi.app.domain.model.Gender
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.feature.prayer.DayTimes
import com.islamshikhi.app.feature.prayer.Prayer
import com.islamshikhi.app.feature.prayer.PrayerEngine
import com.islamshikhi.app.feature.prayer.PrayerSettings
import com.islamshikhi.app.feature.tracker.AmalItem
import com.islamshikhi.app.feature.tracker.AmalKeys
import com.islamshikhi.app.ui.common.ArabicText
import com.islamshikhi.app.ui.common.IconChip
import com.islamshikhi.app.ui.common.StatusBadge
import com.islamshikhi.app.ui.common.labelBn
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.navigation.AppTab
import com.islamshikhi.app.ui.navigation.Routes
import com.islamshikhi.app.util.DateFormatBn
import com.islamshikhi.app.util.TimeFormatBn
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * হোম স্ক্রিন। ওপরে "আকাশ": পরবর্তী নামাজ, কাউন্টডাউন ও সূর্য/চাঁদের অবস্থান। তার নিচে পাঁচ ওয়াক্তের সময়,
 * দ্রুত বাটন, আজকের দোয়া, আমল ও কুরআনের শেষ পড়া। সব তথ্য ফোনের ভেতরের ডেটা থেকে আসে।
 */
@Composable
fun HomeScreen(
    profile: UserProfile,
    onOpenTab: (AppTab) -> Unit,
    onNavigate: (String) -> Unit
) {
    val container = rememberContainer()
    val settings by container.prayerSettings.settings.collectAsState(initial = PrayerSettings())
    val zone = remember { ZoneId.systemDefault() }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val date = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    // নামাজের সময় গণনা ভারী কাজ, তাই দিন বা সেটিংস বদলালেই শুধু নতুন করে হয়; প্রতি সেকেন্ডে নয়
    val days = remember(settings, date) {
        Triple(
            PrayerEngine.timesFor(settings, date.minusDays(1)),
            PrayerEngine.timesFor(settings, date),
            PrayerEngine.timesFor(settings, date.plusDays(1))
        )
    }
    val (yesterday, today, tomorrow) = days
    val sky = computeSky(yesterday, today, tomorrow, now)
    val next = nextPrayer(today, tomorrow, now)

    val scroll = rememberScrollState()
    val heroPx = with(LocalDensity.current) { 280.dp.toPx() }
    val pastHero = scroll.value > heroPx
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // হিরো দেখা গেলে স্ট্যাটাস বারে সাদা আইকন (আকাশের ওপর); স্ক্রল করে পেরিয়ে গেলে থিম অনুযায়ী গাঢ়
    SystemBarIcons(darkIcons = pastHero && !isDark)

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
    ) {
        SkyHero(
            sky = sky,
            next = next,
            now = now,
            settings = settings,
            date = date,
            today = today,
            zone = zone,
            onClick = { onNavigate(Routes.PRAYER) }
        )

        PrayerStrip(
            today = today,
            next = next.first,
            now = now,
            zone = zone,
            modifier = Modifier.padding(horizontal = 16.dp).pullUp(30.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp).padding(top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text(
                "${settings.placeLabelBn} • ${settings.method.labelBn}। মসজিদের জামাতের সময়ের সঙ্গে কয়েক মিনিট পার্থক্য হতে পারে। " +
                    "হিজরি তারিখ গণনা করা; চাঁদ দেখার কারণে এক দিনের পার্থক্য হতে পারে।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            QuickGrid(
                listOf(
                    Quick("নামাজ", Icons.Filled.AccessTime) { onNavigate(Routes.PRAYER) },
                    Quick("কুরআন", Icons.AutoMirrored.Filled.MenuBook) { onOpenTab(AppTab.QURAN) },
                    Quick("দোয়া", Icons.Filled.AutoStories) { onNavigate(Routes.DUA) },
                    Quick("তাসবিহ", Icons.Filled.TouchApp) { onNavigate(Routes.TASBIH) },
                    Quick("কিবলা", Icons.Filled.Explore) { onNavigate(Routes.QIBLA) },
                    Quick("মাসআলা", Icons.Filled.QuestionAnswer) { onOpenTab(AppTab.ASSISTANT) },
                    Quick("যাকাত", Icons.Filled.Calculate) { onNavigate(Routes.ZAKAT) },
                    Quick("বুকমার্ক", Icons.Filled.Bookmark) { onNavigate(Routes.BOOKMARKS) }
                )
            )

            DailyDuaCard(onOpen = { onNavigate(Routes.DUA) })

            TodayAmalCard(today = date, onClick = { onNavigate(Routes.TRACKER) })

            LastReadCard(onOpen = { chapter, ayah -> onNavigate(Routes.quranRead(chapter, ayah)) })

            SuggestionsSection(profile, onOpenAssistant = { onOpenTab(AppTab.ASSISTANT) })
        }
    }

    // স্ক্রল করে হিরো পেরিয়ে গেলে স্ট্যাটাস বারের পিছনে পটভূমির রং, যেন ঘড়ি-আইকন লেখার সঙ্গে জড়িয়ে না যায়
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(MaterialTheme.colorScheme.background.copy(alpha = if (pastHero) 0.97f else 0f))
    )
    }
}

/** [now]-এর পরের প্রথম ফরজ ওয়াক্ত; আজ শেষ হলে আগামীকালের ফজর। */
private fun nextPrayer(today: DayTimes, tomorrow: DayTimes, now: Long): Pair<Prayer, Long> {
    for (p in Prayer.obligations) if (today[p] > now) return p to today[p]
    return Prayer.FAJR to tomorrow[Prayer.FAJR]
}

/** নিচের অংশ [amount] পরিমাণ ওপরে টেনে আগের উপাদানের ওপর বসায়; পরের উপাদানগুলো ঠিক জায়গায়ই থাকে। */
private fun Modifier.pullUp(amount: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val up = amount.roundToPx()
    layout(placeable.width, (placeable.height - up).coerceAtLeast(0)) { placeable.place(0, -up) }
}

private fun clock(millis: Long, zone: ZoneId): String {
    val t = Instant.ofEpochMilli(millis).atZone(zone)
    val h12 = if (t.hour % 12 == 0) 12 else t.hour % 12
    return "${DateFormatBn.digits(h12)}:${DateFormatBn.digits("%02d".format(t.minute))}"
}

private fun periodOf(millis: Long, zone: ZoneId): String =
    TimeFormatBn.period(Instant.ofEpochMilli(millis).atZone(zone).hour)

@Composable
private fun SkyHero(
    sky: SkyState,
    next: Pair<Prayer, Long>,
    now: Long,
    settings: PrayerSettings,
    date: LocalDate,
    today: DayTimes,
    zone: ZoneId,
    onClick: () -> Unit
) {
    val white = Color.White
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .clickable(onClick = onClick)
    ) {
        SkyBackdrop(sky, Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 22.dp, end = 22.dp, top = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "আসসালামু আলাইকুম",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = white.copy(alpha = 0.95f)
                )
                Surface(shape = CircleShape, color = white.copy(alpha = 0.18f)) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = white, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(settings.placeLabelBn, style = MaterialTheme.typography.labelMedium, color = white)
                    }
                }
            }
            Text(
                DateFormatBn.gregorian(date),
                style = MaterialTheme.typography.bodySmall,
                color = white.copy(alpha = 0.85f)
            )
            Text(
                DateFormatBn.hijri(date),
                style = MaterialTheme.typography.bodySmall,
                color = white.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(20.dp))
            Text(
                "${next.first.labelBn}-এর আর",
                style = MaterialTheme.typography.bodyLarge,
                color = white.copy(alpha = 0.88f)
            )
            Countdown(next.second - now)
            Text(
                "${next.first.labelBn} • ${TimeFormatBn.format(next.second, zone)}",
                style = MaterialTheme.typography.titleMedium,
                color = white
            )

            Spacer(Modifier.height(100.dp)) // সূর্য/চাঁদের চাপের জায়গা
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "সূর্যোদয় ${clock(today[Prayer.SUNRISE], zone)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = white.copy(alpha = 0.9f)
                )
                Text(
                    "সূর্যাস্ত ${clock(today[Prayer.MAGHRIB], zone)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = white.copy(alpha = 0.9f)
                )
            }
            Spacer(Modifier.height(34.dp)) // নিচের ওয়াক্ত-সারি যে অংশ ঢেকে দেবে
        }
    }
}

/** বাকি সময় বড় সংখ্যায়: "২ ঘণ্টা ১২ মিনিট"; এক ঘণ্টার কম হলে মিনিট ও সেকেন্ড। */
@Composable
private fun Countdown(millis: Long) {
    val total = (millis / 1000).coerceAtLeast(0)
    val h = (total / 3600).toInt()
    val m = ((total % 3600) / 60).toInt()
    val s = (total % 60).toInt()
    val parts = if (h > 0) listOf(h to "ঘণ্টা", m to "মিনিট") else listOf(m to "মিনিট", s to "সেকেন্ড")
    Row(verticalAlignment = Alignment.Bottom) {
        parts.forEachIndexed { i, (n, unit) ->
            if (i > 0) Spacer(Modifier.width(16.dp))
            Text(
                DateFormatBn.digits(n),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 52.sp, lineHeight = 62.sp),
                color = Color.White
            )
            Spacer(Modifier.width(5.dp))
            Text(
                unit,
                modifier = Modifier.padding(bottom = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.88f)
            )
        }
    }
}

/** পাঁচ ওয়াক্তের সময়ের সারি; পরবর্তী ওয়াক্ত হাইলাইট করা। */
@Composable
private fun PrayerStrip(today: DayTimes, next: Prayer, now: Long, zone: ZoneId, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Row(modifier = Modifier.padding(6.dp)) {
            Prayer.obligations.forEach { p ->
                val t = today[p]
                val isNext = p == next
                val passed = t < now && !isNext
                val textColor = when {
                    isNext -> MaterialTheme.colorScheme.onPrimaryContainer
                    passed -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isNext) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(p.labelBn, style = MaterialTheme.typography.labelMedium, color = textColor)
                    Text(
                        clock(t, zone),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                    Text(periodOf(t, zone), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.75f))
                }
            }
        }
    }
}

private data class Quick(val label: String, val icon: ImageVector, val onClick: () -> Unit)

/** আইকন-গ্রিড: প্রতি সারিতে চারটি। কার্ড নয়, শুধু আইকন ও নাম, যেন শান্ত থাকে। */
@Composable
private fun QuickGrid(items: List<Quick>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items.chunked(4).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { q ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = q.onClick)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconChip(q.icon, size = 56)
                        Spacer(Modifier.height(6.dp))
                        Text(q.label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
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
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // সোনালি দাগ: "আজকের" বোঝাতে
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text("আজকের দোয়া", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
            }
            Text(e.titleBn, style = MaterialTheme.typography.titleMedium)
            ArabicText(e.arabic, sizeSp = 26)
            Text(e.meaningBn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("সূত্র: ${e.sourceRef}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StatusBadge(e.verificationStatus)
        }
    }
}

@Composable
private fun TodayAmalCard(today: LocalDate, onClick: () -> Unit) {
    val container = rememberContainer()
    val done by container.userData.set(AmalKeys.SET).collectAsState(initial = emptySet())
    val count = AmalKeys.doneCount(done, today)
    val total = AmalItem.entries.size
    val fraction = if (total == 0) 0f else count / total.toFloat()
    val track = MaterialTheme.colorScheme.outlineVariant
    val arc = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 7.dp.toPx()
                    val inset = stroke / 2
                    val sz = Size(size.width - stroke, size.height - stroke)
                    drawArc(track, 0f, 360f, false, Offset(inset, inset), sz, style = Stroke(stroke))
                    if (fraction > 0f) {
                        drawArc(arc, -90f, 360f * fraction, false, Offset(inset, inset), sz, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                }
                Text(
                    DateFormatBn.digits(count),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("আজকের আমল", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${DateFormatBn.digits(total)}টির মধ্যে ${DateFormatBn.digits(count)}টি সম্পন্ন",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            IconChip(
                Icons.AutoMirrored.Filled.MenuBook,
                size = 48,
                tint = MaterialTheme.colorScheme.secondary,
                container = MaterialTheme.colorScheme.secondaryContainer
            )
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("কুরআন: যেখানে থেমেছেন", style = MaterialTheme.typography.titleMedium)
                Text(
                    "সূরা ${chapter.bn}, আয়াত ${DateFormatBn.digits(ayah)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * প্রোফাইল অনুযায়ী বিষয়ের পরামর্শ। এখানে কোনো ধর্মীয় বিধান বলা হয় না, শুধু কোন বিষয় খুলতে
 * পারেন তা দেখানো হয়। উভয়ের জন্য সাধারণ বিষয় সবসময় ট্যাবগুলোতে থাকে।
 */
@OptIn(ExperimentalLayoutApi::class)
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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("আপনার জন্য বিষয়", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            topics.forEach { topic ->
                AssistChip(onClick = onOpenAssistant, label = { Text(topic) })
            }
        }
    }
}
