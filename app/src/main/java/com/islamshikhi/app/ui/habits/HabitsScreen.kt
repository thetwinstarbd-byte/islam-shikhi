package com.islamshikhi.app.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.islamshikhi.app.feature.zakat.ZakatCalculator
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.navigation.Routes
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val GOALS = "habit:goals"
private const val PLAN = "habit:plan"
private fun screenKey(d: LocalDate) = "habit:screen:$d"
private fun doneKey(d: LocalDate) = "habit:done:$d"

/**
 * অভ্যাস ও মনোযোগ: ব্যক্তিগত লক্ষ্য, স্ক্রিন টাইমের হাতে লেখা নোট ও সাপ্তাহিক সারাংশ।
 * এই অ্যাপ ফোনের অন্য অ্যাপ পর্যবেক্ষণ বা নিয়ন্ত্রণ করে না; সবকিছু ব্যবহারকারীর নিজের লেখা, ঐচ্ছিক এবং ফোনেই থাকে।
 */
@Composable
fun HabitsScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val prefs by container.userData.data.collectAsState(initial = emptyPreferences())
    val goals by container.userData.jsonList(GOALS).collectAsState(initial = emptyList())
    val today = remember { LocalDate.now() }
    val week = remember(today) { (0..6).map { today.minusDays(it.toLong()) } }   // আজ থেকে পেছনে ৭ দিন

    var newGoal by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        plan = container.userData.string(PLAN).first().orEmpty()
        loaded = true
    }

    fun screenMinutes(d: LocalDate): Int? = prefs[intPreferencesKey(screenKey(d))]
    fun doneSet(d: LocalDate): Set<String> = prefs[stringSetPreferencesKey(doneKey(d))] ?: emptySet()

    ScreenColumn("অভ্যাস ও মনোযোগ", onBack) {
        InfoCard(
            "আপনার নিজের জন্য",
            "এখানে সবকিছু ঐচ্ছিক। অ্যাপ আপনার ফোনের অন্য অ্যাপ দেখে না বা নিয়ন্ত্রণ করে না। যা লিখবেন তা শুধু এই ফোনে থাকে।"
        )

        SectionTitle("আজকের ব্যক্তিগত লক্ষ্য")
        if (goals.isEmpty()) {
            Text("এখনও কোনো লক্ষ্য নেই। যেমন: ফজরের পর ৫ মিনিট কুরআন পড়া, রাতে ফোন রেখে ঘুমানো।", style = MaterialTheme.typography.bodySmall)
        }
        goals.forEach { goal ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = goal in doneSet(today),
                    onCheckedChange = { scope.launch { container.userData.toggleInSet(doneKey(today), goal) } }
                )
                Text(goal, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = { scope.launch { container.userData.putJsonList(GOALS, goals - goal) } }) {
                    Icon(Icons.Filled.Delete, contentDescription = "লক্ষ্য মুছুন")
                }
            }
        }
        OutlinedTextField(
            value = newGoal,
            onValueChange = { newGoal = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("নতুন লক্ষ্য") },
            singleLine = true
        )
        Button(onClick = {
            val text = newGoal.trim()
            if (text.isNotEmpty() && text !in goals) scope.launch { container.userData.putJsonList(GOALS, goals + text) }
            newGoal = ""
        }) { Text("লক্ষ্য যোগ করুন") }

        SectionTitle("আজকের স্ক্রিন টাইম (নিজে লিখুন)")
        Text(
            "ফোনের সেটিংস থেকে আজ কত মিনিট ফোন চালিয়েছেন দেখে এখানে লিখে রাখতে পারেন।",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedTextField(
            value = minutes,
            onValueChange = { minutes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (screenMinutes(today) != null) "আজ: ${DateFormatBn.digits(screenMinutes(today)!!)} মিনিট (বদলাতে লিখুন)" else "মিনিট") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(onClick = {
            val m = ZakatCalculator.parse(minutes).toInt()
            if (minutes.isNotBlank()) scope.launch { container.userData.putInt(screenKey(today), m) }
            minutes = ""
        }) { Text("সংরক্ষণ") }

        SectionTitle("ক্ষতিকর অভ্যাস এড়ানোর আমার পরিকল্পনা")
        if (loaded) {
            OutlinedTextField(
                value = plan,
                onValueChange = {
                    plan = it
                    scope.launch { container.userData.putString(PLAN, it.ifBlank { null }) }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("যেমন: কখন, কোথায় সমস্যা হয়, তখন কী করব") },
                minLines = 3
            )
        }

        SectionTitle("সাপ্তাহিক সারাংশ (আজ সহ গত ৭ দিন)")
        val minuteDays = week.mapNotNull { screenMinutes(it) }
        Text(
            if (minuteDays.isEmpty()) "স্ক্রিন টাইম: এখনও কিছু লেখা হয়নি।"
            else "স্ক্রিন টাইম: গড় ${DateFormatBn.digits(minuteDays.sum() / minuteDays.size)} মিনিট (${DateFormatBn.digits(minuteDays.size)} দিনের নোট থেকে)।",
            style = MaterialTheme.typography.bodyMedium
        )
        if (goals.isNotEmpty()) {
            goals.forEach { goal ->
                val days = week.count { goal in doneSet(it) }
                Text("$goal: ${DateFormatBn.digits(days)}/${DateFormatBn.digits(7)} দিন", style = MaterialTheme.typography.bodyMedium)
            }
        }

        SectionTitle("রিমাইন্ডার")
        MenuCard("নামাজ ও রিমাইন্ডার", "নামাজের সময়ে নোটিফিকেশন ও নিজের রিমাইন্ডার") { onNavigate(Routes.PRAYER) }
        MenuCard("আমল ট্র্যাকার", "প্রতিদিনের আমল ও সাপ্তাহিক অগ্রগতি") { onNavigate(Routes.TRACKER) }
    }
}
