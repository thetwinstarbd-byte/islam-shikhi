package com.islamshikhi.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.islamshikhi.app.domain.model.AgeGroup
import com.islamshikhi.app.domain.model.AppLanguage
import com.islamshikhi.app.domain.model.Gender
import com.islamshikhi.app.domain.model.LearningGoal
import com.islamshikhi.app.domain.model.LearningInterest
import com.islamshikhi.app.ui.common.labelBn

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {

            if (state.step > 0) {
                LinearProgressIndicator(
                    progress = { state.step / OnboardingViewModel.LAST_STEP.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                when (state.step) {
                    0 -> WelcomeStep()
                    1 -> GenderStep(state.gender, viewModel::selectGender)
                    2 -> AgeStep(state.ageGroup, viewModel::selectAgeGroup)
                    3 -> LanguageStep(state.language, viewModel::selectLanguage)
                    else -> PreferencesStep(
                        notificationsEnabled = state.notificationsEnabled,
                        onNotifications = viewModel::setNotifications,
                        interests = state.interests,
                        onToggleInterest = viewModel::toggleInterest,
                        goal = state.goal,
                        onGoal = viewModel::selectGoal
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.step > 0) {
                    OutlinedButton(onClick = viewModel::back, modifier = Modifier.weight(1f)) {
                        Text("পিছনে")
                    }
                }
                Button(
                    onClick = viewModel::next,
                    enabled = state.canGoNext && !state.saving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        when (state.step) {
                            0 -> "শুরু করি"
                            OnboardingViewModel.LAST_STEP -> "সম্পন্ন"
                            else -> "পরবর্তী"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String? = null) {
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    if (subtitle != null) {
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun WelcomeStep() {
    Spacer(Modifier.height(48.dp))
    Text(
        "ইসলাম শিখি",
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(12.dp))
    Text(
        "দৈনন্দিন ইবাদত, কুরআন, দোয়া ও মাসআলা শেখার একটি সহজ সঙ্গী।",
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.height(16.dp))
    Text(
        "আপনার তথ্য শুধু আপনার ফোনে থাকবে। কোনো অ্যাকাউন্ট লাগবে না। " +
            "কয়েকটি প্রশ্নের উত্তর দিলে আপনার জন্য প্রাসঙ্গিক বিষয়গুলো সাজিয়ে দেওয়া হবে।",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun GenderStep(selected: Gender?, onSelect: (Gender) -> Unit) {
    StepTitle(
        "আপনি কে?",
        "এটি শুধু আপনার জন্য প্রাসঙ্গিক বিষয় সাজাতে ব্যবহৃত হবে, পরিচয় যাচাইয়ের জন্য নয়। " +
            "পরে প্রোফাইল থেকে বদলাতে পারবেন।"
    )
    OptionCard("পুরুষ", selected == Gender.MALE) { onSelect(Gender.MALE) }
    Spacer(Modifier.height(12.dp))
    OptionCard("নারী", selected == Gender.FEMALE) { onSelect(Gender.FEMALE) }
}

@Composable
private fun AgeStep(selected: AgeGroup?, onSelect: (AgeGroup) -> Unit) {
    StepTitle("বয়সের শ্রেণি", "বয়স অনুযায়ী বিষয় ও ভাষা সহজ করে দেখানো হবে।")
    OptionCard("শিশু/কিশোর (১৭ বছর পর্যন্ত)", selected == AgeGroup.CHILD_TEEN) {
        onSelect(AgeGroup.CHILD_TEEN)
    }
    Spacer(Modifier.height(12.dp))
    OptionCard("প্রাপ্তবয়স্ক", selected == AgeGroup.ADULT) { onSelect(AgeGroup.ADULT) }
    Spacer(Modifier.height(12.dp))
    OptionCard("বয়স্ক", selected == AgeGroup.SENIOR) { onSelect(AgeGroup.SENIOR) }
}

@Composable
private fun LanguageStep(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    StepTitle("ভাষা", "পরে সেটিংস থেকে বদলানো যাবে।")
    OptionCard("বাংলা", selected == AppLanguage.BANGLA) { onSelect(AppLanguage.BANGLA) }
    Spacer(Modifier.height(12.dp))
    OptionCard("English", selected == AppLanguage.ENGLISH) { onSelect(AppLanguage.ENGLISH) }
}

@Composable
private fun PreferencesStep(
    notificationsEnabled: Boolean,
    onNotifications: (Boolean) -> Unit,
    interests: Set<LearningInterest>,
    onToggleInterest: (LearningInterest) -> Unit,
    goal: LearningGoal?,
    onGoal: (LearningGoal) -> Unit
) {
    StepTitle("আপনার পছন্দ", "সবগুলো ঐচ্ছিক। পরে বদলানো যাবে।")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("নোটিফিকেশন", style = MaterialTheme.typography.titleMedium)
            Text(
                "নামাজ ও আমলের রিমাইন্ডারের জন্য। ফোনের অনুমতি পরে আলাদাভাবে চাওয়া হবে।",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(checked = notificationsEnabled, onCheckedChange = onNotifications)
    }

    Spacer(Modifier.height(24.dp))
    Text("যেসব বিষয়ে জানতে চান", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    LearningInterest.entries.forEach { item ->
        OptionCard(item.labelBn(), item in interests) { onToggleInterest(item) }
        Spacer(Modifier.height(8.dp))
    }

    Spacer(Modifier.height(16.dp))
    Text("শেখার লক্ষ্য", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    LearningGoal.entries.forEach { item ->
        OptionCard(item.labelBn(), item == goal) { onGoal(item) }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun OptionCard(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) colors.primaryContainer else colors.surface
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.primary else colors.outline
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
