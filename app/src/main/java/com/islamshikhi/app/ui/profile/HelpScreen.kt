package com.islamshikhi.app.ui.profile

import androidx.compose.runtime.Composable
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.navigation.Routes

/** সাহায্য কেন্দ্র: সাধারণ প্রশ্নের সংক্ষিপ্ত উত্তর এবং "ভুল জানান"-এ যাওয়ার পথ। */
@Composable
fun HelpScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    ScreenColumn("সাহায্য কেন্দ্র", onBack) {
        MenuCard("ভুল জানান", "কোনো ভুল পেলে লিখে বা ফাইল দিয়ে আমাদের জানান") { onNavigate(Routes.REPORT) }
        InfoCard("নামাজের সময় কীভাবে ঠিক করব?", "\"ইবাদত\" ট্যাবে \"নামাজের সময়\" খুলুন। শহর বেছে নিন বা \"আমার বর্তমান অবস্থান ব্যবহার করুন\" চাপুন। মসজিদের জামাতের সময় আলাদা হলে \"আমার নিজের সময়\" বেছে নিজের সময় বসান।")
        InfoCard("আজান বাজছে না বা দেরিতে বাজছে", "\"নামাজের সময়\" পাতায় \"আজান ও অনুমতি ঠিক আছে কি না দেখুন\" চাপুন এবং দেখানো ব্যবস্থাগুলো একবার করে নিন। ফোনের অ্যালার্ম-ভলিউমও চালু রাখুন।")
        InfoCard("ভাষা বদলাব কীভাবে?", "\"প্রোফাইল\" ট্যাবে \"ভাষা\" থেকে English বেছে নিন। মেনু ও বাটন ইংরেজিতে হবে; কুরআন, দোয়া, প্রবন্ধ ও মাসআলা বাংলাতেই থাকবে।")
        InfoCard("লেখা ছোট বা বড় করব কীভাবে?", "\"প্রোফাইল\" ট্যাবে \"লেখার আকার\" থেকে বেছে নিন।")
        InfoCard("তথ্য হারাতে না চাইলে", "\"ব্যাকআপ ও রিস্টোর\" থেকে ফাইলে তথ্য রাখুন, অথবা \"অ্যাকাউন্ট ও ক্লাউড\" থেকে Google দিয়ে সাইন-ইন করে ক্লাউডে রাখুন (ঐচ্ছিক)।")
        InfoCard("নতুন লেখা পাব কীভাবে?", "\"কনটেন্ট আপডেট\" খুলে বাটন চাপুন। ইন্টারনেট লাগবে।")
    }
}
