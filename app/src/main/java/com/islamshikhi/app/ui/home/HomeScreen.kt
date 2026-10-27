package com.islamshikhi.app.ui.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.domain.model.AgeGroup
import com.islamshikhi.app.domain.model.Gender
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.ui.common.labelBn
import com.islamshikhi.app.ui.navigation.AppTab
import com.islamshikhi.app.util.DateFormatBn
import java.time.LocalDate

/**
 * পর্যায় ৩-এর হোম স্ক্রিন।
 * আজকের তারিখ ও হিজরি তারিখ আসল। নামাজের সময়, দৈনিক দোয়া ও আমলের অংশ এখনও তৈরি হয়নি,
 * তাই ভুল বা বানানো তথ্য না দেখিয়ে স্পষ্ট করে লেখা আছে কবে আসবে।
 */
@Composable
fun HomeScreen(profile: UserProfile, onOpenTab: (AppTab) -> Unit) {
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

        InfoCard(
            title = "পরবর্তী নামাজ",
            body = "নামাজের সময় ও কাউন্টডাউন পর্যায় ৫-এ যুক্ত হবে। ভুল সময় দেখানোর চেয়ে " +
                "কোনো সময় না দেখানোই নিরাপদ, তাই এখন কিছু দেখানো হচ্ছে না।"
        )

        Text("দ্রুত খুলুন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickButton("ইবাদত", Modifier.weight(1f)) { onOpenTab(AppTab.IBADAH) }
            QuickButton("কুরআন", Modifier.weight(1f)) { onOpenTab(AppTab.QURAN) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickButton("দোয়া", Modifier.weight(1f)) { onOpenTab(AppTab.IBADAH) }
            QuickButton("মাসআলা", Modifier.weight(1f)) { onOpenTab(AppTab.ASSISTANT) }
        }

        InfoCard(
            title = "আজকের দোয়া ও যিকির",
            body = "যাচাইকৃত সূত্রসহ দোয়া যুক্ত হওয়ার পর এখানে প্রতিদিন একটি করে দেখানো হবে (পর্যায় ৪)।"
        )

        InfoCard(
            title = "আজকের আমল ও অগ্রগতি",
            body = "আমল ট্র্যাকার তৈরি হলে এখানে আপনার দৈনিক সারাংশ দেখা যাবে (পর্যায় ৮)।"
        )

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
            Text(
                gregorian,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                hijri,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "হিজরি তারিখ গণনা করা। স্থানীয় চাঁদ দেখার ওপর নির্ভর করে এক দিন পর্যন্ত পার্থক্য হতে পারে।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun QuickButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier) { Text(label) }
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
    Text(
        "এই বিষয়গুলোর কনটেন্ট পরের পর্যায়গুলোতে যুক্ত হবে।",
        style = MaterialTheme.typography.bodySmall
    )
}
