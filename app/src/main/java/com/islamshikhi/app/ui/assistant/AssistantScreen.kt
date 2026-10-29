package com.islamshikhi.app.ui.assistant

import androidx.compose.runtime.Composable
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.WarningCard
import com.islamshikhi.app.ui.navigation.Routes

/** সহকারী ট্যাবের হাব: মাসআলা, যাকাত, শিশুদের শিক্ষা, কুইজ, জরুরি গাইড ও লাইব্রেরি। */
@Composable
fun AssistantScreen(onNavigate: (String) -> Unit) {
    ScreenColumn("ইসলামিক সহকারী") {
        WarningCard(
            "এই অ্যাপের উত্তর ফতোয়া নয়। সব উত্তর অ্যাপের ভেতরের সূত্রসহ ডেটা থেকে দেখানো হয় এবং কোনোটিই " +
                "এখনও আলেম দ্বারা পর্যালোচিত নয়। জটিল বা ব্যক্তিগত বিষয়ে যোগ্য আলেমের পরামর্শ নিন।"
        )
        MenuCard("মাসআলা", "ওজু, নামাজ, রোজা, যাকাত, হজসহ বিভাগভিত্তিক প্রশ্নোত্তর") { onNavigate(Routes.MASAIL) }
    }
}
