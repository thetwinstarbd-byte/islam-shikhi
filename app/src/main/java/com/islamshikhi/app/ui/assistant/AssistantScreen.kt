package com.islamshikhi.app.ui.assistant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.LocalLibrary
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
        MenuCard("মাসআলা", "ওজু, নামাজ, রোজা, যাকাত, হজসহ বিভাগভিত্তিক প্রশ্নোত্তর", Icons.Filled.QuestionAnswer) { onNavigate(Routes.MASAIL) }
        MenuCard("যাকাত ক্যালকুলেটর", "আনুমানিক হিসাব; চূড়ান্ত ফতোয়া নয়", Icons.Filled.Calculate) { onNavigate(Routes.ZAKAT) }
        MenuCard("শিশুদের শিক্ষা", "বয়সোপযোগী ইসলামিক শিক্ষা ও গল্প", Icons.Filled.ChildCare) { onNavigate(Routes.KIDS) }
        MenuCard("ইসলামিক কুইজ", "নিজেকে যাচাই করুন", Icons.Filled.Quiz) { onNavigate(Routes.QUIZ) }
        MenuCard("জরুরি গাইড", "অসুস্থতা, সফর ও জানাজার প্রাথমিক নির্দেশনা", Icons.Filled.MedicalServices) { onNavigate(Routes.EMERGENCY) }
        MenuCard("ইসলামিক লাইব্রেরি", "সংক্ষিপ্ত নিবন্ধ ও শেখার গাইড", Icons.Filled.LocalLibrary) { onNavigate(Routes.LIBRARY) }
    }
}
