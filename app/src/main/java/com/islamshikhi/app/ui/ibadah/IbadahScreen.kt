package com.islamshikhi.app.ui.ibadah

import androidx.compose.runtime.Composable
import com.islamshikhi.app.ui.common.FeatureItem
import com.islamshikhi.app.ui.common.FeatureListScreen

@Composable
fun IbadahScreen() {
    FeatureListScreen(
        title = "ইবাদত",
        intro = "দৈনন্দিন ইবাদতের সব সুবিধা এখানে একসঙ্গে থাকবে।",
        items = listOf(
            FeatureItem("নামাজের সময়", "পাঁচ ওয়াক্তের সময়, কাউন্টডাউন ও নোটিফিকেশন।", 5),
            FeatureItem("দোয়া ও যিকির", "আরবি, বাংলা উচ্চারণ, অর্থ ও সূত্রসহ।", 4),
            FeatureItem("ডিজিটাল তাসবিহ", "গণনা ও দৈনিক লক্ষ্য।", 4),
            FeatureItem("কিবলা কম্পাস", "কাবার দিক নির্দেশনা।", 5),
            FeatureItem("হিজরি ক্যালেন্ডার", "গুরুত্বপূর্ণ দিন ও মাস।", 5),
            FeatureItem("আমল ট্র্যাকার", "প্রতিদিনের আমলের হিসাব ও অগ্রগতি।", 8)
        )
    )
}
