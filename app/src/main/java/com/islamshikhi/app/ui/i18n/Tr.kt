package com.islamshikhi.app.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.islamshikhi.app.domain.model.AppLanguage

/** অ্যাপের বর্তমান ইন্টারফেস-ভাষা। MainActivity এটি সরবরাহ করে। */
val LocalLang = staticCompositionLocalOf { AppLanguage.BANGLA }

/**
 * মেনু, বাটন ও শিরোনামের ইংরেজি রূপ। ইংরেজি বেছে নিলে অভিধানে থাকা লেখা ইংরেজিতে দেখায়; না থাকলে বাংলাই থাকে।
 * কুরআন, দোয়া, প্রবন্ধ ও মাসআলার কনটেন্ট সব সময় বাংলায় থাকে।
 */
@Composable
fun tr(bn: String): String = if (LocalLang.current == AppLanguage.ENGLISH) trEn(bn) else bn

fun trEn(bn: String): String = EnglishDict.map[bn] ?: bn

object EnglishDict {
    val map: Map<String, String> = mapOf(
        // ট্যাব
        "হোম" to "Home", "ইবাদত" to "Worship", "কুরআন" to "Quran", "সহকারী" to "Assistant", "প্রোফাইল" to "Profile",
        // হোম
        "সব সেবা" to "All services", "নামাজ" to "Prayer", "দোয়া" to "Dua", "তাসবিহ" to "Tasbih", "কিবলা" to "Qibla",
        "মসজিদ" to "Mosques", "ক্যালেন্ডার" to "Calendar", "আমল" to "Deeds", "অভ্যাস" to "Habits", "জ্ঞান" to "Knowledge",
        "যাকাত" to "Zakat", "কুইজ" to "Quiz", "শিশুশিক্ষা" to "Kids", "মুখস্থ" to "Memorize", "মাসআলা" to "Rulings",
        "জরুরি" to "Emergency", "বুকমার্ক" to "Bookmarks", "খুঁজুন" to "Search", "রমযান" to "Ramadan", "কাযা" to "Qadha",
        "খতম" to "Khatam", "হজ্জ-উমরা" to "Hajj & Umrah",
        "আজকের দোয়া" to "Today's dua", "শেষ পড়া" to "Last read",
        // ইবাদত ট্যাব
        "নামাজের সময়" to "Prayer times", "পাঁচ ওয়াক্ত ও রিমাইন্ডার" to "Five prayers and reminders",
        "দোয়া ও যিকির" to "Dua & Dhikr", "অর্থ ও সূত্রসহ" to "With meaning and source",
        "ডিজিটাল তাসবিহ" to "Digital tasbih", "গণনা ও দৈনিক লক্ষ্য" to "Counter and daily goal",
        "কাবার দিক" to "Direction of the Kaaba", "কাছের মসজিদ" to "Nearby mosques",
        "মানচিত্রে নিকটবর্তী মসজিদ (ইন্টারনেট লাগে)" to "Nearby mosques on the map (needs internet)",
        "হিজরি ক্যালেন্ডার" to "Hijri calendar", "হিজরি মাস ও তারিখ" to "Hijri months and dates",
        "আমল ট্র্যাকার" to "Deeds tracker", "দৈনিক আমলের হিসাব" to "Daily deeds record",
        "রমযান মোড" to "Ramadan mode", "সেহরি-ইফতার, চেকলিস্ট ও তারাবীহ" to "Suhoor/iftar, checklist and taraweeh",
        "কাযা নামাজ ও রোযা" to "Qadha prayers & fasts", "ছুটে যাওয়া নামাজ-রোযার হিসাব" to "Count of missed prayers and fasts",
        "হজ্জ-উমরা গাইড" to "Hajj & Umrah guide", "ধাপে ধাপে নির্দেশনা ও চেকলিস্ট" to "Step-by-step guide and checklist",
        // কুরআন
        "কুরআন খতম ট্র্যাকার" to "Quran khatam tracker", "লক্ষ্য ঠিক করুন, বাকি পরিমাণ দেখুন" to "Set a goal and see what is left",
        "কুরআনে খুঁজুন" to "Search the Quran", "মুখস্থ পরিকল্পনা" to "Memorization plan",
        // সহকারী
        "ইসলামিক সহকারী" to "Islamic assistant", "ইসলামি জ্ঞান" to "Islamic knowledge",
        "ঈমান ও আকীদা: বিস্তারিত প্রবন্ধ" to "Faith and creed: detailed articles",
        "যাকাত ক্যালকুলেটর" to "Zakat calculator", "আনুমানিক হিসাব" to "Approximate calculation",
        "শিশুদের শিক্ষা" to "Children's learning", "বয়সোপযোগী শিক্ষা" to "Age-appropriate lessons",
        "ইসলামিক কুইজ" to "Islamic quiz", "নিজেকে যাচাই করুন" to "Test yourself",
        "জরুরি গাইড" to "Emergency guide", "অসুস্থতা, সফর ও জানাজা" to "Illness, travel and funeral",
        "মাসআলা-মাসায়েল" to "Rulings Q&A", "প্রশ্ন ও উত্তরে দৈনন্দিন বিধান" to "Everyday rulings in Q&A form",
        "মাসআলা দেখুন" to "See rulings",
        // সার্চ
        "সার্চ" to "Search", "দোয়া, মাসআলা, প্রবন্ধ ও সূরা একসঙ্গে খুঁজুন" to "Search duas, rulings, articles and surahs together",
        // প্রোফাইল
        "প্রোফাইল ও সেটিংস" to "Profile & settings", "সংরক্ষিত দোয়া, আয়াত, মাসআলা ও পাঠ" to "Saved duas, verses, rulings and lessons",
        "প্রতিদিনের আমল ও সাপ্তাহিক অগ্রগতি" to "Daily deeds and weekly progress",
        "অভ্যাস ও মনোযোগ" to "Habits & focus", "ব্যক্তিগত লক্ষ্য, স্ক্রিন টাইমের নোট ও সাপ্তাহিক সারাংশ" to "Personal goals, screen-time notes and weekly summary",
        "কনটেন্ট আপডেট" to "Content update", "নতুন প্রবন্ধ ও লেখা আনুন (ইন্টারনেট লাগে)" to "Get new articles and texts (needs internet)",
        "অ্যাপ সম্পর্কে" to "About the app", "গোপনীয়তা, কনটেন্টের যাচাই-অবস্থা, উৎস ও লাইসেন্স" to "Privacy, content status, sources and licences",
        "ব্যাকআপ ও রিস্টোর" to "Backup & restore", "বুকমার্ক, নোট ও হিসাব ফাইলে রাখুন বা ফেরত আনুন" to "Save or restore bookmarks, notes and records",
        "লিঙ্গ" to "Gender", "বয়সের শ্রেণি" to "Age group", "ভাষা" to "Language", "থিম" to "Theme", "লেখার আকার" to "Text size",
        "নোটিফিকেশন" to "Notifications", "গোপনীয়তা" to "Privacy", "সব ডেটা মুছুন" to "Delete all data",
        "দৈনিক আয়াত/হাদীস" to "Daily verse / hadith", "প্রতিদিন সকালে একটি করে আয়াত বা হাদীস" to "One verse or hadith every morning",
        "বাংলা" to "Bangla", "ইংরেজি" to "English", "পুরুষ" to "Male", "নারী" to "Female",
        "সিস্টেম অনুযায়ী" to "Follow system", "হালকা" to "Light", "গাঢ়" to "Dark",
        // সাধারণ
        "পিছনে" to "Back", "সংরক্ষণ" to "Save", "বাতিল" to "Cancel", "ঠিক আছে" to "OK", "মুছুন" to "Delete",
        "নামাজ ও রিমাইন্ডার" to "Prayer & reminders", "আজকের সময়সূচি" to "Today's schedule",
        "ফজর" to "Fajr", "সূর্যোদয়" to "Sunrise", "যোহর" to "Dhuhr", "আসর" to "Asr", "মাগরিব" to "Maghrib", "এশা" to "Isha",
        // নতুন স্ক্রিন
        "সেহরির শেষ সময়" to "Suhoor ends", "ইফতারের সময়" to "Iftar time", "পরের সেহরি" to "Next suhoor",
        "আজকের চেকলিস্ট" to "Today's checklist", "তারাবীহ" to "Taraweeh", "গত ৩০ দিনের সারাংশ" to "Last 30 days",
        "ব্যাকআপ তৈরি করুন" to "Create backup", "ফাইল থেকে ফেরত আনুন" to "Restore from file",
        "পড়ার অগ্রগতি" to "Reading progress", "রাতের মোড" to "Night mode"
    )
}
