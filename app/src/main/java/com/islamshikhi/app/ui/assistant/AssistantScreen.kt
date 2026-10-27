package com.islamshikhi.app.ui.assistant

import androidx.compose.runtime.Composable
import com.islamshikhi.app.ui.common.FeatureItem
import com.islamshikhi.app.ui.common.FeatureListScreen

@Composable
fun AssistantScreen() {
    FeatureListScreen(
        title = "ইসলামিক সহকারী",
        intro = "মাসআলার উত্তর শুধু যাচাইকৃত, সূত্রসহ কনটেন্ট থেকে দেখানো হবে। " +
            "জটিল বিষয়ে যোগ্য আলেমের পরামর্শ নেওয়ার কথা মনে করিয়ে দেওয়া হবে।",
        items = listOf(
            FeatureItem("মাসআলা", "ওজু, নামাজ, রোজা, হজসহ বিভাগভিত্তিক প্রশ্নোত্তর।", 7),
            FeatureItem("নারী ও পুরুষের নির্দেশনা", "প্রাসঙ্গিক বিষয় আলাদা করে সাজানো।", 7),
            FeatureItem("যাকাত ক্যালকুলেটর", "আনুমানিক হিসাব, চূড়ান্ত ফতোয়া নয়।", 8),
            FeatureItem("জরুরি গাইড", "অসুস্থতা, সফর ও জানাজার প্রাথমিক নির্দেশনা।", 9),
            FeatureItem("ইসলামিক লাইব্রেরি", "যাচাইকৃত বই ও নিবন্ধ।", 9)
        )
    )
}
