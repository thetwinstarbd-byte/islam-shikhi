package com.islamshikhi.app.data.content

data class QuranRef(val chapter: Int, val from: Int, val to: Int)

data class DuaCategory(val id: String, val titleBn: String)

/** দোয়া/যিকিরের একটি এন্ট্রি। docs/CONTENT_VERIFICATION.md-এর কাঠামো অনুযায়ী। */
data class DuaEntry(
    val id: String,
    val category: String,
    val titleBn: String,
    val arabic: String,
    val transliterationBn: String?,
    val meaningBn: String,
    val usageBn: String?,
    val suggestedCount: Int?,
    val sourceRef: String,
    val hadithGrade: String?,
    val verificationStatus: String,
    val checkNoteBn: String?,
    val meaningCreditBn: String?,
    val quranRef: QuranRef?
)

data class DuaData(val categories: List<DuaCategory>, val entries: List<DuaEntry>)

data class Chapter(
    val id: Int,
    val ar: String,
    val bn: String,
    val en: String,
    val meaning: String,
    val type: String,
    val verses: Int
) {
    val typeBn: String get() = if (type == "meccan") "মাক্কী" else "মাদানী"
}

data class Ayah(val number: Int, val arabic: String, val bengali: String, val footnote: String?)

/** সূরার পাঠ। ডেটাসেটে ১ম আয়াতের সঙ্গে জোড়া 'বিসমিল্লাহ' আলাদা করে [bismillah]-এ রাখা হয় (লেখা অপরিবর্তিত)। */
data class ChapterText(val bismillah: String?, val ayahs: List<Ayah>)

data class SearchHit(val chapter: Int, val ayah: Int, val snippet: String)

/** মাসআলার সূত্র/দলিল: কুরআনের আয়াত বা হাদীস। */
data class Evidence(
    val kind: String,          // "quran" | "hadith"
    val ref: String,
    val arabic: String?,
    val meaningBn: String?,
    val creditBn: String?
)

data class Viewpoint(val titleBn: String, val textBn: String, val sourceRef: String?)

data class MasalaCategory(val id: String, val titleBn: String)

data class Masala(
    val id: String,
    val category: String,
    val audience: List<String>,        // "ALL" | "FEMALE" | "MALE"
    val questionBn: String,
    val shortAnswerBn: String,
    val detailBn: String,
    val evidence: List<Evidence>,
    val viewpoints: List<Viewpoint>,
    val related: List<String>,
    val askScholar: Boolean,
    val verificationStatus: String,
    val checkNoteBn: String?
)

data class MasalaData(val categories: List<MasalaCategory>, val entries: List<Masala>)

/** জরুরি গাইড, লাইব্রেরি ও শিশুদের শিক্ষার জন্য একই সাধারণ কাঠামো। */
data class InfoSection(val titleBn: String, val bodyBn: String, val sourceRef: String?)

data class InfoItem(
    val id: String,
    val category: String,
    val titleBn: String,
    val summaryBn: String,
    val sections: List<InfoSection>,
    val audience: List<String>,
    val minAgeGroup: String?,
    val verificationStatus: String,
    val checkNoteBn: String?
)

data class InfoData(val categories: List<MasalaCategory>, val items: List<InfoItem>)

data class QuizQuestion(
    val id: String,
    val topic: String,
    val questionBn: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanationBn: String,
    val sourceRef: String?
)


// ---------------- ইসলামি জ্ঞান (ঈমান ও আকীদা ইত্যাদি বিস্তারিত প্রবন্ধ) ----------------

data class IlmCategory(val id: String, val titleBn: String, val introBn: String)

/** হাদীসের উদ্ধৃতি: আরবি অংশ (ডেটাসেট থেকে অপরিবর্তিত, না থাকলে null), বাংলা অর্থ, সূত্র ও মান। */
data class IlmHadith(
    val ref: String,
    val narratorBn: String,
    val arabic: String?,
    val meaningBn: String,
    val gradeBn: String
)

/** প্রবন্ধের একটি অংশ: শিরোনাম, অনুচ্ছেদ, কুরআনের আয়াত (অ্যাপের কুরআন-ডেটা থেকে), হাদীস ও আলেমদের মতভেদ। */
data class IlmSection(
    val headingBn: String,
    val paragraphs: List<String>,
    val ayahs: List<QuranRef>,
    val hadiths: List<IlmHadith>,
    val viewpoints: List<Viewpoint>
)

data class IlmArticle(
    val id: String,
    val category: String,
    val titleBn: String,
    val summaryBn: String,
    val levelBn: String,
    val sections: List<IlmSection>,
    val readingBn: List<String>,
    val verificationStatus: String,
    val checkNoteBn: String?
)

data class IlmData(val categories: List<IlmCategory>, val articles: List<IlmArticle>)
