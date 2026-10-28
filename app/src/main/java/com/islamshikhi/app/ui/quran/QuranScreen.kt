package com.islamshikhi.app.ui.quran

import androidx.compose.runtime.Composable
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn

@Composable
fun QuranScreen(onNavigate: (String) -> Unit) {
    ScreenColumn("কুরআন") {
        InfoCard("শীঘ্রই আসছে", "কুরআন পাঠের পাতা পরের ধাপে যুক্ত হচ্ছে।")
    }
}
