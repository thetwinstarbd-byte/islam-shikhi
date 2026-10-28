package com.islamshikhi.app.ui.assistant

import androidx.compose.runtime.Composable
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn

@Composable
fun AssistantScreen(onNavigate: (String) -> Unit) {
    ScreenColumn("ইসলামিক সহকারী") {
        InfoCard("শীঘ্রই আসছে", "মাসআলা, যাকাত ও অন্যান্য অংশ পরের ধাপে যুক্ত হচ্ছে।")
    }
}
