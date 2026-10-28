package com.islamshikhi.app.ui.ibadah

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.WarningCard
import com.islamshikhi.app.util.DateFormatBn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

private val hijriMonthNames = listOf(
    "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি",
    "রজব", "শা'বান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
)
private val weekHeaders = listOf("শনি", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র")

private data class MonthEvent(val month: Int, val day: Int, val titleBn: String)

private val events = listOf(
    MonthEvent(1, 1, "ইসলামী নববর্ষ (১ মুহাররম)"),
    MonthEvent(1, 10, "আশুরা (১০ মুহাররম)"),
    MonthEvent(9, 1, "রমজানের শুরু"),
    MonthEvent(10, 1, "ঈদুল ফিতর (১ শাওয়াল)"),
    MonthEvent(12, 9, "আরাফার দিন (৯ জিলহজ)"),
    MonthEvent(12, 10, "ঈদুল আযহা (১০ জিলহজ)")
)

/** হিজরি ক্যালেন্ডার: উম্মুল কুরা পঞ্জিকা (java.time) অনুযায়ী গণনা। */
@Composable
fun CalendarScreen(onBack: () -> Unit) {
    var offset by rememberSaveable { mutableIntStateOf(0) }
    val today = remember { LocalDate.now() }
    val base = remember { HijrahDate.from(today).with(ChronoField.DAY_OF_MONTH, 1) }
    val monthStart = base.plus(offset.toLong(), ChronoUnit.MONTHS)
    val year = monthStart.get(ChronoField.YEAR)
    val month = monthStart.get(ChronoField.MONTH_OF_YEAR)
    val length = monthStart.lengthOfMonth()
    val firstGregorian = LocalDate.from(monthStart)
    val todayHijri = remember { HijrahDate.from(today) }

    // শনিবার = ০ ... শুক্রবার = ৬
    val lead = (firstGregorian.dayOfWeek.value + 1) % 7

    ScreenColumn("হিজরি ক্যালেন্ডার", onBack) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { offset-- }) { Text("আগের মাস") }
            Text(
                "${hijriMonthNames[month - 1]} ${DateFormatBn.digits(year)}",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
            )
            OutlinedButton(onClick = { offset++ }) { Text("পরের মাস") }
        }
        if (offset != 0) OutlinedButton(onClick = { offset = 0 }) { Text("আজকের মাসে ফিরুন") }

        Text(
            "ইংরেজি: ${DateFormatBn.gregorian(firstGregorian)} থেকে ${DateFormatBn.gregorian(firstGregorian.plusDays((length - 1).toLong()))}",
            style = MaterialTheme.typography.bodySmall
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            weekHeaders.forEach {
                Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
            }
        }
        val cells = List(lead) { 0 } + (1..length).toList()
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (i in 0 until 7) {
                    val d = week.getOrNull(i) ?: 0
                    val isToday = d != 0 && todayHijri.get(ChronoField.YEAR) == year &&
                        todayHijri.get(ChronoField.MONTH_OF_YEAR) == month && todayHijri.get(ChronoField.DAY_OF_MONTH) == d
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                            .let { if (isToday) it.background(MaterialTheme.colorScheme.primaryContainer, CircleShape) else it },
                        contentAlignment = Alignment.Center
                    ) {
                        if (d != 0) {
                            Text(
                                DateFormatBn.digits(d),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        SectionTitle("এই মাসের গুরুত্বপূর্ণ দিন")
        val monthEvents = events.filter { it.month == month }
        if (monthEvents.isEmpty() && month == 12) {
            Text("—", style = MaterialTheme.typography.bodyMedium)
        }
        monthEvents.forEach { e ->
            val g = firstGregorian.plusDays((e.day - 1).toLong())
            InfoCard(e.titleBn, "আনুমানিক ইংরেজি তারিখ: ${DateFormatBn.gregorian(g)}")
        }
        if (month != 12) {
            val g13 = firstGregorian.plusDays(12)
            InfoCard(
                "আইয়ামে বীযের তারিখ (১৩, ১৪, ১৫)",
                "প্রতি চান্দ্র মাসের ১৩, ১৪ ও ১৫ তারিখে নফল রোজা রাখা মুস্তাহাব। এ মাসে আনুমানিক ${DateFormatBn.gregorian(g13)} থেকে ${DateFormatBn.gregorian(g13.plusDays(2))}।"
            )
        } else {
            Text("জিলহজের ১১, ১২ ও ১৩ তারিখ আইয়ামে তাশরীক; এই দিনগুলোতে নফল রোজা রাখা হয় না।", style = MaterialTheme.typography.bodySmall)
        }

        WarningCard(
            "তারিখগুলো উম্মুল কুরা পঞ্জিকা অনুযায়ী গণনা করা। বাস্তবে চাঁদ দেখার ওপর নির্ভর করে, এবং বাংলাদেশসহ বিভিন্ন দেশে চাঁদ দেখার কারণে " +
                "এক-দুই দিন পার্থক্য হওয়া স্বাভাবিক। রমজান, ঈদ ও হজ্জের নিশ্চিত তারিখের জন্য আপনার এলাকার চাঁদ দেখা কমিটি বা সরকারি ঘোষণা দেখুন।"
        )
    }
}
