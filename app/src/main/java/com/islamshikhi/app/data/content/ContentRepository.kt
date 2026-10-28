package com.islamshikhi.app.data.content

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * assets/ ফোল্ডারের JSON কনটেন্ট পড়ে। সব ডেটা অ্যাপের ভেতরেই থাকে, ইন্টারনেট লাগে না।
 * একবার পড়া ডেটা মেমোরিতে রাখা হয় (cache)।
 */
class ContentRepository(private val context: Context) {

    private val lock = Mutex()
    private var duaCache: DuaData? = null
    private var masailCache: MasalaData? = null
    private var chaptersCache: List<Chapter>? = null
    private var emergencyCache: InfoData? = null
    private var libraryCache: InfoData? = null
    private var kidsCache: InfoData? = null
    private var quizCache: List<QuizQuestion>? = null
    private var searchIndex: List<Pair<Int, List<String>>>? = null   // (সূরা, আয়াতের হরকতহীন আরবি)
    private var bnIndex: List<Pair<Int, List<String>>>? = null

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun JSONObject.str(name: String): String? =
        if (isNull(name)) null else optString(name).takeIf { it.isNotBlank() }

    private fun JSONArray.strings(): List<String> = List(length()) { getString(it) }

    // ---------------- দোয়া ----------------

    suspend fun duas(): DuaData = lock.withLock {
        duaCache ?: withContext(Dispatchers.IO) {
            val root = JSONObject(readAsset("content/duas.json"))
            val cats = root.getJSONArray("categories").let { a ->
                List(a.length()) { DuaCategory(a.getJSONObject(it).getString("id"), a.getJSONObject(it).getString("titleBn")) }
            }
            val entries = root.getJSONArray("entries").let { a ->
                List(a.length()) { i ->
                    val o = a.getJSONObject(i)
                    DuaEntry(
                        id = o.getString("id"),
                        category = o.getString("category"),
                        titleBn = o.getString("titleBn"),
                        arabic = o.getString("arabic"),
                        transliterationBn = o.str("transliterationBn"),
                        meaningBn = o.getString("meaningBn"),
                        usageBn = o.str("usageBn"),
                        suggestedCount = if (o.isNull("suggestedCount")) null else o.optInt("suggestedCount"),
                        sourceRef = o.getString("sourceRef"),
                        hadithGrade = o.str("hadithGrade"),
                        verificationStatus = o.getString("verificationStatus"),
                        checkNoteBn = o.str("checkNoteBn"),
                        meaningCreditBn = o.str("meaningCreditBn"),
                        quranRef = if (o.isNull("quranRef")) null else o.getJSONObject("quranRef").let {
                            QuranRef(it.getInt("chapter"), it.getInt("from"), it.getInt("to"))
                        }
                    )
                }
            }
            // নিয়ম: সূত্র ফাঁকা থাকলে এন্ট্রি দেখানো হবে না (docs/CONTENT_VERIFICATION.md)
            DuaData(cats, entries.filter { it.sourceRef.isNotBlank() })
        }.also { duaCache = it }
    }

    // ---------------- কুরআন ----------------

    suspend fun chapters(): List<Chapter> = lock.withLock {
        chaptersCache ?: withContext(Dispatchers.IO) {
            val a = JSONArray(readAsset("quran/chapters.json"))
            List(a.length()) {
                val o = a.getJSONObject(it)
                Chapter(o.getInt("id"), o.getString("ar"), o.getString("bn"), o.getString("en"),
                    o.getString("meaning"), o.getString("type"), o.getInt("verses"))
            }
        }.also { chaptersCache = it }
    }

    suspend fun chapterText(chapter: Int): ChapterText = withContext(Dispatchers.IO) {
        val ar = JSONArray(readAsset("quran/ar/$chapter.json")).strings().toMutableList()
        val bn = JSONArray(readAsset("quran/bn/$chapter.json")).strings()
        val fnObj = JSONObject(readAsset("quran/bnfn/$chapter.json"))
        require(ar.size == bn.size) { "আরবি ও বাংলার আয়াত সংখ্যা মেলেনি: সূরা $chapter" }

        // সূরা ফাতিহায় বিসমিল্লাহ ১ম আয়াত; সূরা তাওবায় নেই। বাকিগুলোতে ১ম আয়াতের শুরুর ৪ শব্দ বিসমিল্লাহ।
        var bismillah: String? = null
        if (chapter != 1 && chapter != 9) {
            val tokens = ar[0].split(" ")
            if (tokens.size > 4 && normalizeArabic(tokens.take(4).joinToString(" ")).replace('\u0671', '\u0627') == BISMILLAH_PLAIN) {
                bismillah = tokens.take(4).joinToString(" ")
                ar[0] = tokens.drop(4).joinToString(" ")
            }
        }
        ChapterText(
            bismillah,
            List(ar.size) { i -> Ayah(i + 1, ar[i], bn[i], fnObj.str((i + 1).toString())) }
        )
    }

    suspend fun ayahs(chapter: Int): List<Ayah> = chapterText(chapter).ayahs

    /** সূরার একটি আয়াতের পরিসর (দোয়ার সঙ্গে সংযুক্ত কুরআনি অংশ দেখাতে)। */
    suspend fun ayahRange(ref: QuranRef): List<Ayah> =
        ayahs(ref.chapter).filter { it.number in ref.from..ref.to }

    suspend fun quranNotice(): String = withContext(Dispatchers.IO) {
        readAsset("quran/TANZIL_NOTICE.txt").lines().joinToString("\n") { it.trimStart('#').trimEnd() }.trim()
    }

    // ---------------- অনুসন্ধান ----------------

    private suspend fun arabicIndex(): List<Pair<Int, List<String>>> = lock.withLock {
        searchIndex ?: withContext(Dispatchers.IO) {
            (1..114).map { ch ->
                val v = JSONArray(readAsset("quran/search_ar/$ch.json")).strings().toMutableList()
                // ১ম আয়াতের সঙ্গে জোড়া বিসমিল্লাহ বাদ, নইলে প্রতিটি সূরাই "রহমান রহীম" খুঁজলে আসত
                if (ch != 1 && ch != 9 && v.isNotEmpty() && v[0].startsWith("$BISMILLAH_PLAIN ")) {
                    v[0] = v[0].removePrefix("$BISMILLAH_PLAIN ").trim()
                }
                ch to v
            }
        }.also { searchIndex = it }
    }

    private suspend fun bengaliIndex(): List<Pair<Int, List<String>>> = lock.withLock {
        bnIndex ?: withContext(Dispatchers.IO) {
            (1..114).map { it to JSONArray(readAsset("quran/bn/$it.json")).strings() }
        }.also { bnIndex = it }
    }

    /** আরবি বা বাংলা শব্দ দিয়ে আয়াত খোঁজে। সর্বোচ্চ [limit]টি ফল। */
    suspend fun searchAyahs(query: String, limit: Int = 60): List<SearchHit> {
        val q = query.trim()
        if (q.length < 2) return emptyList()
        val isArabic = q.any { it in '؀'..'ۿ' }
        val needle = if (isArabic) normalizeArabic(q) else q
        val index = if (isArabic) arabicIndex() else bengaliIndex()
        return withContext(Dispatchers.Default) {
            val out = ArrayList<SearchHit>()
            loop@ for ((chapter, verses) in index) {
                for ((i, text) in verses.withIndex()) {
                    if (text.contains(needle)) {
                        out += SearchHit(chapter, i + 1, snippet(text, 140))
                        if (out.size >= limit) break@loop
                    }
                }
            }
            out
        }
    }

    // ---------------- মাসআলা, জরুরি গাইড, লাইব্রেরি, শিশু ----------------

    suspend fun masail(): MasalaData = lock.withLock {
        masailCache ?: withContext(Dispatchers.IO) { parseMasail(JSONObject(readAsset("content/masail.json"))) }
            .also { masailCache = it }
    }

    suspend fun emergency(): InfoData = lock.withLock {
        emergencyCache ?: withContext(Dispatchers.IO) { parseInfo(JSONObject(readAsset("content/emergency.json"))) }
            .also { emergencyCache = it }
    }

    suspend fun library(): InfoData = lock.withLock {
        libraryCache ?: withContext(Dispatchers.IO) { parseInfo(JSONObject(readAsset("content/library.json"))) }
            .also { libraryCache = it }
    }

    suspend fun kids(): InfoData = lock.withLock {
        kidsCache ?: withContext(Dispatchers.IO) { parseInfo(JSONObject(readAsset("content/kids.json"))) }
            .also { kidsCache = it }
    }

    suspend fun quiz(): List<QuizQuestion> = lock.withLock {
        quizCache ?: withContext(Dispatchers.IO) {
            val a = JSONObject(readAsset("content/quiz.json")).getJSONArray("questions")
            List(a.length()) {
                val o = a.getJSONObject(it)
                QuizQuestion(
                    o.getString("id"), o.getString("topic"), o.getString("questionBn"),
                    o.getJSONArray("options").strings(), o.getInt("answerIndex"),
                    o.getString("explanationBn"), o.str("sourceRef")
                )
            }
        }.also { quizCache = it }
    }

    private fun parseMasail(root: JSONObject): MasalaData {
        val cats = root.getJSONArray("categories").let { a ->
            List(a.length()) { MasalaCategory(a.getJSONObject(it).getString("id"), a.getJSONObject(it).getString("titleBn")) }
        }
        val entries = root.getJSONArray("entries").let { a ->
            List(a.length()) { i ->
                val o = a.getJSONObject(i)
                Masala(
                    id = o.getString("id"),
                    category = o.getString("category"),
                    audience = o.getJSONArray("audience").strings(),
                    questionBn = o.getString("questionBn"),
                    shortAnswerBn = o.getString("shortAnswerBn"),
                    detailBn = o.getString("detailBn"),
                    evidence = o.getJSONArray("evidence").let { e ->
                        List(e.length()) { j ->
                            val x = e.getJSONObject(j)
                            Evidence(x.getString("kind"), x.getString("ref"), x.str("arabic"), x.str("meaningBn"), x.str("creditBn"))
                        }
                    },
                    viewpoints = o.getJSONArray("viewpoints").let { v ->
                        List(v.length()) { j ->
                            val x = v.getJSONObject(j)
                            Viewpoint(x.getString("titleBn"), x.getString("textBn"), x.str("sourceRef"))
                        }
                    },
                    related = o.getJSONArray("related").strings(),
                    askScholar = o.optBoolean("askScholar", false),
                    verificationStatus = o.getString("verificationStatus"),
                    checkNoteBn = o.str("checkNoteBn")
                )
            }
        }
        // নিয়ম: সূত্র/দলিল ছাড়া মাসআলা দেখানো হবে না
        return MasalaData(cats, entries.filter { it.evidence.isNotEmpty() })
    }

    private fun parseInfo(root: JSONObject): InfoData {
        val cats = root.getJSONArray("categories").let { a ->
            List(a.length()) { MasalaCategory(a.getJSONObject(it).getString("id"), a.getJSONObject(it).getString("titleBn")) }
        }
        val items = root.getJSONArray("items").let { a ->
            List(a.length()) { i ->
                val o = a.getJSONObject(i)
                InfoItem(
                    id = o.getString("id"),
                    category = o.getString("category"),
                    titleBn = o.getString("titleBn"),
                    summaryBn = o.getString("summaryBn"),
                    sections = o.getJSONArray("sections").let { s ->
                        List(s.length()) { j ->
                            val x = s.getJSONObject(j)
                            InfoSection(x.getString("titleBn"), x.getString("bodyBn"), x.str("sourceRef"))
                        }
                    },
                    audience = o.getJSONArray("audience").strings(),
                    minAgeGroup = o.str("minAgeGroup"),
                    verificationStatus = o.getString("verificationStatus"),
                    checkNoteBn = o.str("checkNoteBn")
                )
            }
        }
        return InfoData(cats, items)
    }

    companion object {
        private const val BISMILLAH_PLAIN = "بسم الله الرحمن الرحيم"

        /** আরবি অনুসন্ধানের জন্য শুধু হরকত ও টান (তাতউইল) বাদ দেয়; search_ar ফাইলের বর্ণরূপ অপরিবর্তিত। */
        fun normalizeArabic(s: String): String {
            val sb = StringBuilder()
            for (ch in s) {
                val code = ch.code
                val isMark = code in 0x064B..0x065F || code == 0x0670 || code in 0x06D6..0x06ED || code == 0x0640
                if (!isMark) sb.append(ch)
            }
            return sb.toString().trim()
        }

        private fun snippet(text: String, max: Int): String =
            if (text.length <= max) text else text.take(max).trimEnd() + "…"
    }
}
