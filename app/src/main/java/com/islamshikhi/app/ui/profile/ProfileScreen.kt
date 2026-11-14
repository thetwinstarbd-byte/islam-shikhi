package com.islamshikhi.app.ui.profile

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.islamshikhi.app.ui.i18n.tr
import com.islamshikhi.app.data.user.UserKeys
import com.islamshikhi.app.feature.daily.DailyVerse
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.TimeFormatBn
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import com.islamshikhi.app.ui.i18n.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamshikhi.app.domain.model.AgeGroup
import com.islamshikhi.app.domain.model.AppLanguage
import com.islamshikhi.app.domain.model.Gender
import com.islamshikhi.app.domain.model.ThemeMode
import com.islamshikhi.app.domain.model.UserProfile
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.labelBn
import com.islamshikhi.app.ui.navigation.Routes

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onNavigate: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("প্রোফাইল", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "সব তথ্য শুধু এই ফোনে সংরক্ষিত। লিঙ্গ ও বয়স শুধু প্রাসঙ্গিক বিষয় সাজাতে ব্যবহৃত হয়, পরিচয় যাচাইয়ের জন্য নয়।",
            style = MaterialTheme.typography.bodySmall
        )

        MenuCard("বুকমার্ক", "সংরক্ষিত দোয়া, আয়াত, মাসআলা ও পাঠ") { onNavigate(Routes.BOOKMARKS) }
        MenuCard("আমল ট্র্যাকার", "প্রতিদিনের আমল ও সাপ্তাহিক অগ্রগতি") { onNavigate(Routes.TRACKER) }
        MenuCard("অভ্যাস ও মনোযোগ", "ব্যক্তিগত লক্ষ্য, স্ক্রিন টাইমের নোট ও সাপ্তাহিক সারাংশ") { onNavigate(Routes.HABITS) }
        MenuCard("সেটিংস", "ভাষা, থিম, লেখার আকার, নোটিফিকেশন, কুরআন পড়ার ও আরও অনেক কিছু") { onNavigate(Routes.SETTINGS) }
        MenuCard("অ্যাকাউন্ট ও ক্লাউড", "Google দিয়ে সাইন-ইন (ঐচ্ছিক), ক্লাউডে ব্যাকআপ") { onNavigate(Routes.ACCOUNT) }
        MenuCard("সাহায্য কেন্দ্র", "ব্যবহারের সহজ টিপস ও সমস্যার সমাধান") { onNavigate(Routes.HELP) }
        MenuCard("ভুল জানান", "কোনো ভুল পেলে লিখে বা ফাইল দিয়ে আমাদের জানান") { onNavigate(Routes.REPORT) }
        MenuCard("অ্যাপ সম্পর্কে", "গোপনীয়তা, কনটেন্টের যাচাই-অবস্থা, উৎস ও লাইসেন্স") { onNavigate(Routes.ABOUT) }

        SectionTitle("আপনার তথ্য")
        Text("লিঙ্গ", style = MaterialTheme.typography.bodyMedium)
        Gender.entries.forEach { item ->
            RadioRow(item.labelBn(), profile.gender == item) { viewModel.setGender(item) }
        }

        Text("বয়সের শ্রেণি", style = MaterialTheme.typography.bodyMedium)
        AgeGroup.entries.forEach { item ->
            RadioRow(item.labelBn(), profile.ageGroup == item) { viewModel.setAgeGroup(item) }
        }
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = 12.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
internal fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
    }
}
