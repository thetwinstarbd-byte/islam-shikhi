package com.islamshikhi.app.ui.assistant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.runtime.Composable
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.navigation.Routes

/** সহকারী ট্যাবের হাব: ইসলামি জ্ঞান, যাকাত, শিশুদের শিক্ষা, কুইজ, জরুরি গাইড ও মাসআলা। */
@Composable
fun AssistantScreen(onNavigate: (String) -> Unit) {
    ScreenColumn("ইসলামিক সহকারী") {
        MenuCard("ইসলামি জ্ঞান", "ঈমান ও আকীদা: বিস্তারিত প্রবন্ধ", Icons.Filled.Lightbulb) { onNavigate(Routes.ILM) }
        MenuCard("যাকাত ক্যালকুলেটর", "আনুমানিক হিসাব", Icons.Filled.Calculate) { onNavigate(Routes.ZAKAT) }
        MenuCard("শিশুদের শিক্ষা", "বয়সোপযোগী শিক্ষা", Icons.Filled.ChildCare) { onNavigate(Routes.KIDS) }
        MenuCard("ইসলামিক কুইজ", "নিজেকে যাচাই করুন", Icons.Filled.Quiz) { onNavigate(Routes.QUIZ) }
        MenuCard("জরুরি গাইড", "অসুস্থতা, সফর ও জানাজা", Icons.Filled.MedicalServices) { onNavigate(Routes.EMERGENCY) }
        MenuCard("মাসআলা-মাসায়েল", "প্রশ্ন ও উত্তরে দৈনন্দিন বিধান", Icons.Filled.LocalLibrary) { onNavigate(Routes.MASAIL) }
    }
}
