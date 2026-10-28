package com.islamshikhi.app.ui.ibadah

import androidx.compose.runtime.Composable
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.navigation.Routes

/** ইবাদত ট্যাবের হাব: প্রতিটি কার্ডে চাপলে সংশ্লিষ্ট পাতা খোলে। */
@Composable
fun IbadahScreen(onNavigate: (String) -> Unit) {
    ScreenColumn("ইবাদত") {
        MenuCard("নামাজের সময়", "পাঁচ ওয়াক্ত, কাউন্টডাউন, নোটিফিকেশন ও রিমাইন্ডার") { onNavigate(Routes.PRAYER) }
        MenuCard("দোয়া ও যিকির", "আরবি, উচ্চারণ, অর্থ ও সূত্রসহ; বুকমার্ক ও নোট") { onNavigate(Routes.DUA) }
        MenuCard("ডিজিটাল তাসবিহ", "গণনা ও দৈনিক লক্ষ্য") { onNavigate(Routes.TASBIH) }
        MenuCard("কিবলা", "কাবার দিক নির্দেশনা") { onNavigate(Routes.QIBLA) }
        MenuCard("হিজরি ক্যালেন্ডার", "হিজরি মাস ও তারিখ") { onNavigate(Routes.CALENDAR) }
        MenuCard("আমল ট্র্যাকার", "নামাজ ও দৈনিক আমলের হিসাব, সাপ্তাহিক সারাংশ") { onNavigate(Routes.TRACKER) }
    }
}
