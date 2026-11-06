package com.islamshikhi.app.ui.ibadah

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.runtime.Composable
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.navigation.Routes

/** ইবাদত ট্যাবের হাব: প্রতিটি কার্ডে চাপলে সংশ্লিষ্ট পাতা খোলে। */
@Composable
fun IbadahScreen(onNavigate: (String) -> Unit) {
    ScreenColumn("ইবাদত") {
        MenuCard("নামাজের সময়", "পাঁচ ওয়াক্ত ও রিমাইন্ডার", Icons.Filled.AccessTime) { onNavigate(Routes.PRAYER) }
        MenuCard("দোয়া ও যিকির", "অর্থ ও সূত্রসহ", imageRes = com.islamshikhi.app.R.drawable.dua_hands) { onNavigate(Routes.DUA) }
        MenuCard("ডিজিটাল তাসবিহ", "গণনা ও দৈনিক লক্ষ্য", Icons.Filled.TouchApp) { onNavigate(Routes.TASBIH) }
        MenuCard("কিবলা", "কাবার দিক", Icons.Filled.Explore) { onNavigate(Routes.QIBLA) }
        MenuCard("কাছের মসজিদ", "মানচিত্রে নিকটবর্তী মসজিদ (ইন্টারনেট লাগে)", Icons.Filled.Place) { onNavigate(Routes.MOSQUES) }
        MenuCard("হিজরি ক্যালেন্ডার", "হিজরি মাস ও তারিখ", Icons.Filled.CalendarMonth) { onNavigate(Routes.CALENDAR) }
        MenuCard("আমল ট্র্যাকার", "দৈনিক আমলের হিসাব", Icons.Filled.TaskAlt) { onNavigate(Routes.TRACKER) }
        MenuCard("রমযান মোড", "সেহরি-ইফতার, চেকলিস্ট ও তারাবীহ", imageRes = com.islamshikhi.app.R.drawable.ic_ramadan) { onNavigate(Routes.RAMADAN) }
        MenuCard("হজ্জ-উমরা গাইড", "ধাপে ধাপে নির্দেশনা ও চেকলিস্ট", imageRes = com.islamshikhi.app.R.drawable.ic_hajj) { onNavigate(Routes.HAJJ) }
        MenuCard("কাযা নামাজ ও রোযা", "ছুটে যাওয়া নামাজ-রোযার হিসাব", imageRes = com.islamshikhi.app.R.drawable.ic_qada) { onNavigate(Routes.QADA) }
    }
}
