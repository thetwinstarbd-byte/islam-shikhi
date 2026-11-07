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
import androidx.compose.material3.Text
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
    onResetData: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    var showResetDialog by remember { mutableStateOf(false) }
    // স্লাইডার টানার সময় শুধু স্থানীয় মান বদলায়; ছেড়ে দিলে সংরক্ষণ হয়।
    var fontScale by remember(profile.fontScale) { mutableFloatStateOf(profile.fontScale) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("প্রোফাইল ও সেটিংস", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "সব তথ্য শুধু এই ফোনে সংরক্ষিত। লিঙ্গ শুধু প্রাসঙ্গিক বিষয় সাজাতে ব্যবহৃত হয়, পরিচয় যাচাইয়ের জন্য নয়।",
            style = MaterialTheme.typography.bodySmall
        )

        MenuCard("বুকমার্ক", "সংরক্ষিত দোয়া, আয়াত, মাসআলা ও পাঠ") { onNavigate(Routes.BOOKMARKS) }
        MenuCard("আমল ট্র্যাকার", "প্রতিদিনের আমল ও সাপ্তাহিক অগ্রগতি") { onNavigate(Routes.TRACKER) }
        MenuCard("অভ্যাস ও মনোযোগ", "ব্যক্তিগত লক্ষ্য, স্ক্রিন টাইমের নোট ও সাপ্তাহিক সারাংশ") { onNavigate(Routes.HABITS) }
        MenuCard("কনটেন্ট আপডেট", "নতুন প্রবন্ধ ও লেখা আনুন (ইন্টারনেট লাগে)") { onNavigate(Routes.UPDATE) }
        MenuCard("ব্যাকআপ ও রিস্টোর", "বুকমার্ক, নোট ও হিসাব ফাইলে রাখুন বা ফেরত আনুন", imageRes = com.islamshikhi.app.R.drawable.ic_backup) { onNavigate(Routes.BACKUP) }
        MenuCard("অ্যাপ সম্পর্কে", "গোপনীয়তা, কনটেন্টের যাচাই-অবস্থা, উৎস ও লাইসেন্স") { onNavigate(Routes.ABOUT) }

        SectionTitle("লিঙ্গ")
        Gender.entries.forEach { item ->
            RadioRow(item.labelBn(), profile.gender == item) { viewModel.setGender(item) }
        }

        SectionTitle("বয়সের শ্রেণি")
        AgeGroup.entries.forEach { item ->
            RadioRow(item.labelBn(), profile.ageGroup == item) { viewModel.setAgeGroup(item) }
        }

        SectionTitle("সহজ মোড")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "বড় লেখা ও বড় ছবির বাটন, শুধু দরকারি কাজ। পড়তে কষ্ট হলে চালু করুন।",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(checked = profile.simpleMode, onCheckedChange = viewModel::setSimpleMode)
        }

        SectionTitle("ভাষা")
        AppLanguage.entries.forEach { item ->
            RadioRow(item.labelBn(), profile.language == item) { viewModel.setLanguage(item) }
        }
        Text(
            "ইংরেজি বেছে নিলে মেনু ও বাটন ইংরেজিতে দেখাবে। কুরআন, দোয়া, প্রবন্ধ ও মাসআলা সবসময় বাংলায় থাকবে।",
            style = MaterialTheme.typography.bodySmall
        )

        SectionTitle("থিম")
        ThemeMode.entries.forEach { item ->
            RadioRow(item.labelBn(), profile.themeMode == item) { viewModel.setThemeMode(item) }
        }

        SectionTitle("লেখার আকার")
        Slider(
            value = fontScale,
            onValueChange = { fontScale = it },
            onValueChangeFinished = { viewModel.setFontScale(fontScale) },
            valueRange = 0.85f..1.5f
        )
        Text("নমুনা লেখা: আসসালামু আলাইকুম", style = MaterialTheme.typography.bodyLarge)

        SectionTitle("নোটিফিকেশন")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "নামাজ ও আমলের রিমাইন্ডার (ফোনের অনুমতি পরে আলাদাভাবে চাওয়া হবে)",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(checked = profile.notificationsEnabled, onCheckedChange = viewModel::setNotifications)
        }

        val context = LocalContext.current
        val container = rememberContainer()
        val scope = rememberCoroutineScope()
        val verseOn by container.userData.bool(UserKeys.DAILY_VERSE_ON, false).collectAsState(initial = false)
        val verseMinutes by container.userData.int(UserKeys.DAILY_VERSE_MIN, DailyVerse.DEFAULT_MINUTES)
            .collectAsState(initial = DailyVerse.DEFAULT_MINUTES)
        val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tr("দৈনিক আয়াত/হাদীস"), style = MaterialTheme.typography.bodyMedium)
                Text(tr("প্রতিদিন সকালে একটি করে আয়াত বা হাদীস"), style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = verseOn, onCheckedChange = { on ->
                scope.launch {
                    container.userData.putBool(UserKeys.DAILY_VERSE_ON, on)
                    container.scheduler.rescheduleNext()
                }
                if (on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            })
        }
        if (verseOn) {
            OutlinedButton(onClick = {
                TimePickerDialog(context, { _, h, m ->
                    scope.launch {
                        container.userData.putInt(UserKeys.DAILY_VERSE_MIN, h * 60 + m)
                        container.scheduler.rescheduleNext()
                    }
                }, verseMinutes / 60, verseMinutes % 60, false).show()
            }) { Text("সময়: ${TimeFormatBn.format(verseMinutes / 60, verseMinutes % 60)}") }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        SectionTitle("গোপনীয়তা")
        Text(
            "কোনো অ্যাকাউন্ট নেই। আপনার প্রোফাইল, নোট ও হিসাব শুধু এই ফোনে থাকে। ইন্টারনেট ব্যবহার হয় কেবল আপনি চাইলে (মসজিদ খোঁজা, তিলাওয়াত শোনা, কনটেন্ট আপডেট)। নিচের বাটনে চাপলে এই ফোন থেকে সব সংরক্ষিত তথ্য মুছে যাবে।",
            style = MaterialTheme.typography.bodySmall
        )
        Button(onClick = { showResetDialog = true }) { Text("সব ডেটা মুছুন") }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("সব ডেটা মুছবেন?") },
            text = { Text("প্রোফাইল ও সব সেটিংস মুছে যাবে এবং আবার প্রথম সেটআপ দেখাবে। এটি ফেরানো যাবে না।") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    onResetData()
                }) { Text("মুছে ফেলুন") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = 12.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
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
