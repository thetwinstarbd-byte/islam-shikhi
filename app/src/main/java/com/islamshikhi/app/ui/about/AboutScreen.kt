package com.islamshikhi.app.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.islamshikhi.app.data.cloud.AppRemote
import com.islamshikhi.app.data.update.ContentUpdater
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

    val pageUrl by AppRemote.pageUrl.collectAsState()
    val contentVersion = remember { ContentUpdater.currentVersion(context) }
    fun open(url: String) { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }

    ScreenColumn("অ্যাপ সম্পর্কে", onBack) {
        InfoCard(
            "ইসলাম শিখি",
            "সংস্করণ ${DateFormatBn.digits(version)} · কনটেন্ট ${DateFormatBn.digits(contentVersion)}\n" +
                "সহজ বাংলায় ইসলাম শেখার একটি সাথী: কুরআন, দোয়া, নামাজের সময় ও দৈনন্দিন মাসআলা এক জায়গায়। ইন্টারনেট না থাকলেও প্রায় সব কিছু চলে।"
        )
        InfoCard(
            "কেন এই অ্যাপ",
            "দ্বীনের প্রয়োজনীয় বিষয়গুলো যেন সবাই নিজের ভাষায়, সহজে ও নির্ভরযোগ্য উৎসসহ জানতে পারে, সেই চেষ্টা থেকেই এটি তৈরি। বিজ্ঞাপন নেই, সাইন-ইন করতে বাধ্য করা হয় না। " +
                "কোথাও ভুল চোখে পড়লে \"ভুল জানান\" থেকে আমাদের জানান। ভুল শুধরে দেওয়াই আমাদের সবচেয়ে বড় সহায়তা। আল্লাহ এই কাজ কবুল করুন এবং আমাদের ভুলত্রুটি ক্ষমা করুন, আমিন।"
        )
        if (pageUrl.isNotBlank()) MenuCard("আমাদের ফেসবুক পেজ", "খবর, নতুন সুবিধা ও যোগাযোগ") { open(pageUrl) }
        MenuCard("ভুল জানান", "কোনো ভুল পেলে লিখে বা ফাইল দিয়ে জানান") { onNavigate(Routes.REPORT) }
        MenuCard("সাহায্য কেন্দ্র", "ব্যবহারের সহজ টিপস ও সমস্যার সমাধান") { onNavigate(Routes.HELP) }

        SectionTitle("গোপনীয়তা")
        InfoCard(
            "সব কিছু আপনার ফোনে",
            "বিজ্ঞাপন নেই, অ্যাকাউন্ট বাধ্যতামূলক নয়। প্রোফাইল, বুকমার্ক, নোট ও হিসাব ডিফল্টে শুধু এই ফোনে থাকে; আপনি নিজে Google দিয়ে সাইন-ইন করে \"ক্লাউডে রাখুন\" চাপলে তবেই সেগুলো Google Firebase-এ আপনার অ্যাকাউন্টের অধীনে জমা হয়। ইন্টারনেট কেবল আপনি চাইলে ব্যবহার হয়: কাছের মসজিদ খোঁজায় আনুমানিক স্থানাঙ্ক OpenStreetMap-ভিত্তিক সার্ভারে যায়, তিলাওয়াত EveryAyah.com থেকে শোনানো হয়, কনটেন্ট আপডেট GitHub থেকে আসে। এছাড়া সব কিছু অফলাইনে চলে। " +
                "প্রোফাইল ট্যাবের \"সব ডেটা মুছুন\" দিয়ে সব মুছে ফেলা যায়।"
        )

        MenuCard("গোপনীয়তা নীতি", "কোন তথ্য কীভাবে ব্যবহার হয়, পুরোটা পড়ুন") {
            open("https://github.com/thetwinstarbd-byte/islam-shikhi/blob/main/docs/PRIVACY.md")
        }

        SectionTitle("কনটেন্ট")
        InfoCard(
            "উৎস ও যাচাই",
            "প্রতিটি আয়াত ও হাদীসের সঙ্গে উৎসের নাম দেওয়া আছে। আরবি লেখা সরাসরি নির্ভরযোগ্য উৎস থেকে নেওয়া, নিজে লেখা নয়। হাদীসের ক্ষেত্রে সহীহ বুখারী ও সহীহ মুসলিম থেকে নেওয়া হয়েছে।"
        )
        InfoCard(
            "মতভেদের বিষয়ে",
            "ফিকহি মতভেদের বিষয়ে একাধিক মত দেখানো হয়, একটি মতকে সর্বসম্মত বলা হয় না। জটিল ও ব্যক্তিগত বিষয়ে যোগ্য আলেমের পরামর্শ নিন।"
        )

        SectionTitle("উৎস ও লাইসেন্স")
        MenuCard("কুরআনের টেক্সট ও অনুবাদ", "Tanzil (আরবি) ও QuranEnc (বাংলা অনুবাদ)-এর শর্ত ও কপিরাইট নোটিশ") { onNavigate(Routes.QURAN_NOTICE) }
        InfoCard(
            "হাদীসের আরবি টেক্সট",
            "সহীহ বুখারী ও সহীহ মুসলিমের আরবি লেখা AhmedBaset/hadith-json (sunnah.com থেকে সংগৃহীত) উৎস থেকে নেওয়া।"
        )
        InfoCard(
            "প্রোগ্রামিং লাইব্রেরি",
            "AndroidX, Jetpack Compose ও Material 3 (Apache 2.0)। নামাজের সময় গণনায় adhan2 (MIT)। সাইন-ইন, ক্লাউড ব্যাকআপ ও ঘোষণার জন্য Google Firebase। কাছের মসজিদ খোঁজায় OpenStreetMap-এর ডেটা। তিলাওয়াত EveryAyah.com থেকে।"
        )

        SectionTitle("জেনে রাখুন")
        InfoCard(
            "সীমাবদ্ধতা",
            "• নামাজের সময় গণনা করা; স্থান ও হিসাব-পদ্ধতির পছন্দের ওপর নির্ভর করে, মসজিদের জামাতের সময়ের সঙ্গে ভিন্ন হতে পারে।\n" +
                "• কিবলা কম্পাস ফোনের সেন্সরের ওপর নির্ভর করে; ধাতব বস্তু বা চুম্বকের কাছে ভুল দিক দেখাতে পারে।\n" +
                "• হিজরি তারিখ গণনা করা; স্থানীয় চাঁদ দেখার কারণে এক দিনের পার্থক্য হতে পারে।\n" +
                "• যাকাতের হিসাব আনুমানিক; স্বর্ণ-রূপার দর আপনাকে নিজে দিতে হয়।"
        )
    }
}
