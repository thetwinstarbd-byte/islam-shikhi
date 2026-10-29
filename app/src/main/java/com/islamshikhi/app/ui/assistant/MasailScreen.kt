package com.islamshikhi.app.ui.assistant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.content.Masala
import com.islamshikhi.app.data.content.MasalaData
import com.islamshikhi.app.domain.model.Gender
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.ui.common.ContentGate
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.audienceLabelBn
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.common.rememberLoaded

/**
 * মাসআলার তালিকা। বিভাগ বা শব্দ দিয়ে খোঁজা যায়।
 * প্রোফাইলের লিঙ্গ অনুযায়ী প্রাসঙ্গিক মাসআলা ও বিভাগ আগে দেখায়, কিন্তু কোনো মাসআলা লুকায় না।
 */
@Composable
fun MasailScreen(profile: UserProfile, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val container = rememberContainer()
    val result = rememberLoaded { container.content.masail() }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("মাসআলা", onBack)
        ContentGate(result) { data -> MasailList(data, profile, onOpen) }
    }
}

@Composable
private fun MasailList(data: MasalaData, profile: UserProfile, onOpen: (String) -> Unit) {
    var selected by remember { mutableStateOf("all") }
    var query by remember { mutableStateOf("") }

    val categories = remember(data, profile.gender) {
        val first = when (profile.gender) {
            Gender.FEMALE -> "women"
            Gender.MALE -> "men"
            null -> null
        }
        data.categories.sortedBy { if (it.id == first) 0 else 1 }
    }
    val titles = remember(data) { data.categories.associate { it.id to it.titleBn } }

    val shown = remember(data, selected, query, profile.gender) {
        val q = query.trim()
        data.entries
            .filter { selected == "all" || it.category == selected }
            .filter {
                q.isEmpty() || it.questionBn.contains(q, ignoreCase = true) ||
                    it.shortAnswerBn.contains(q, ignoreCase = true) ||
                    it.detailBn.contains(q, ignoreCase = true)
            }
            .sortedBy { relevance(it, profile.gender) }
    }

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        label = { Text("প্রশ্ন বা শব্দ খুঁজুন") },
        singleLine = true
    )
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item { FilterChip(selected = selected == "all", onClick = { selected = "all" }, label = { Text("সব") }) }
        items(categories, key = { it.id }) { c ->
            FilterChip(selected = selected == c.id, onClick = { selected = c.id }, label = { Text(c.titleBn) })
        }
    }

    LazyColumn(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "সব উত্তরের সূত্র দেওয়া আছে। কোনোটিই আলেম দ্বারা পর্যালোচিত নয়, তাই এগুলো ফতোয়া নয়।",
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (shown.isEmpty()) {
            item { Text("কোনো মাসআলা পাওয়া যায়নি।", style = MaterialTheme.typography.bodyMedium) }
        }
        items(shown, key = { it.id }) { m ->
            MasalaRow(m, titles[m.category].orEmpty()) { onOpen(m.id) }
        }
    }
}

/** ছোট সংখ্যা মানে আগে দেখানো। শুধু সাজানোর জন্য; কিছুই বাদ যায় না। */
private fun relevance(m: Masala, gender: Gender?): Int = when {
    gender == Gender.FEMALE && "FEMALE" in m.audience -> 0
    gender == Gender.MALE && "MALE" in m.audience -> 0
    "ALL" in m.audience -> 1
    else -> 2
}

@Composable
private fun MasalaRow(m: Masala, categoryTitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(m.questionBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(m.shortAnswerBn, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            val audience = audienceLabelBn(m.audience)
            Text(
                if (audience != null) "$categoryTitle • $audience" else categoryTitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
