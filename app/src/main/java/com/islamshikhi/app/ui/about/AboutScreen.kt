package com.islamshikhi.app.ui.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.MenuCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.WarningCard
import com.islamshikhi.app.ui.navigation.Routes
import com.islamshikhi.app.util.DateFormatBn

/** অ্যাপ সম্পর্কে: গোপনীয়তা, কনটেন্টের যাচাই-অবস্থা, উৎস ও লাইসেন্স এবং পরিচিত সীমাবদ্ধতা। */
@Composable
fun AboutScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val version = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "-"

    ScreenColumn("অ্যাপ সম্পর্কে", onBack) {
        InfoCard("ইসলাম শিখি", "সংস্করণ ${DateFormatBn.digits(version)}\nদৈনন্দিন ইবাদত, কুরআন, দোয়া, মাসআলা ও ব্যক্তিগত উন্নতির জন্য অফলাইন-ফার্স্ট অ্যাপ।")

        SectionTitle("গোপনীয়তা")
        InfoCard(
            "সব কিছু আপনার ফোনে",
            "বিজ্ঞাপন নেই, অ্যাকাউন্ট বাধ্যতামূলক নয়। প্রোফাইল, বুকমার্ক, নোট ও হিসাব ডিফল্টে শুধু এই ফোনে থাকে; আপনি নিজে Google দিয়ে সাইন-ইন করে \"ক্লাউডে রাখুন\" চাপলে তবেই সেগুলো Google Firebase-এ আপনার অ্যাকাউন্টের অধীনে জমা হয়। ইন্টারনেট কেবল আপনি চাইলে ব্যবহার হয়: কাছের মসজিদ খোঁজায় আনুমানিক স্থানাঙ্ক OpenStreetMap-ভিত্তিক সার্ভারে যায়, তিলাওয়াত EveryAyah.com থেকে শোনানো হয়, কনটেন্ট আপডেট GitHub থেকে আসে। এছাড়া সব কিছু অফলাইনে চলে। " +
                "প্রোফাইল ট্যাবের \"সব ডেটা মুছুন\" দিয়ে সব মুছে ফেলা যায়।"
        )

        SectionTitle("কনটেন্ট")
        InfoCard(
            "মতভেদের বিষয়ে",
            "ফিকহি মতভেদের বিষয়ে একাধিক মত দেখানো হয়, একটি মতকে সর্বসম্মত বলা হয় না। জটিল ও ব্যক্তিগত বিষয়ে যোগ্য আলেমের পরামর্শ নিন।"
        )

        SectionTitle("উৎস ও লাইসেন্স")
        MenuCard("কুরআনের টেক্সট ও অনুবাদ", "Tanzil (আরবি) ও QuranEnc (বাংলা অনুবাদ)-এর শর্ত ও কপিরাইট নোটিশ") { onNavigate(Routes.QURAN_NOTICE) }
        InfoCard(
            "হাদীসের আরবি টেক্সট",
            "সহীহ বুখারী ও সহীহ মুসলিমের আরবি লেখা ওপেন ডেটাসেট (AhmedBaset/hadith-json, sunnah.com থেকে সংগৃহীত) থেকে হুবহু নেওয়া।"
        )
        InfoCard(
            "প্রোগ্রামিং লাইব্রেরি",
            "AndroidX, Jetpack Compose ও Material 3 (Apache 2.0)। নামাজের সময় গণনায় adhan2 (MIT)।"
        )

        SectionTitle("জেনে রাখুন")
        InfoCard(
            "সীমাবদ্ধতা",
            "• নামাজের সময় গণনা করা; স্থান ও হিসাব-পদ্ধতির পছন্দের ওপর নির্ভর করে, মসজিদের জামাতের সময়ের সঙ্গে ভিন্ন হতে পারে।\n" +
                "• কিবলা কম্পাস ফোনের সেন্সরের ওপর নির্ভর করে; ধাতব বস্তু বা চুম্বকের কাছে ভুল দিক দেখাতে পারে।\n" +
                "• হিজরি তারিখ গণনা করা; স্থানীয় চাঁদ দেখার কারণে এক দিনের পার্থক্য হতে পারে।\n" +
                "• যাকাতের হিসাব আনুমানিক; স্বর্ণ-রূপার দর আপনাকে নিজে দিতে হয়।\n" +
                "• ইংরেজি ভাষার UI এখনও তৈরি হয়নি।"
        )
    }
}
