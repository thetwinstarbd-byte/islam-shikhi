package com.islamshikhi.app.ui.quran

import androidx.compose.runtime.Composable
import com.islamshikhi.app.ui.common.FeatureItem
import com.islamshikhi.app.ui.common.FeatureListScreen

@Composable
fun QuranScreen() {
    FeatureListScreen(
        title = "কুরআন",
        intro = "কুরআনের কোনো অনুবাদ বা তথ্য লাইসেন্স যাচাই ছাড়া যুক্ত করা হবে না।",
        items = listOf(
            FeatureItem("সূরার তালিকা", "১১৪টি সূরা, আয়াত অনুযায়ী নেভিগেশন।", 6),
            FeatureItem("আরবি ও বাংলা অনুবাদ", "অনুবাদকের নাম ও সংস্করণসহ।", 6),
            FeatureItem("অনুসন্ধান", "সূরা ও আয়াত খোঁজা।", 6),
            FeatureItem("পড়ার অগ্রগতি", "বুকমার্ক এবং শেষবার যেখানে পড়েছেন সেখান থেকে শুরু।", 6),
            FeatureItem("তাজবিদ ও কুইজ", "ধাপে ধাপে শেখা ও পরীক্ষা।", 8)
        )
    )
}
