package com.islamshikhi.app.feature.daily

import com.islamshikhi.app.data.content.ContentRepository
import com.islamshikhi.app.data.content.QuranRef
import com.islamshikhi.app.util.DateFormatBn

/**
 * দৈনিক আয়াত/হাদীসের নোটিফিকেশনের লেখা। আয়াত অ্যাপের ভেতরের কুরআন-টেক্সট (Tanzil + অনুবাদ) থেকে আসে,
 * হাদীস আসে জ্ঞান-প্রবন্ধে থাকা যাচাই-করা হাদীসগুলো থেকে। নিজে থেকে কোনো আয়াত বা হাদীস বানানো হয় না।
 * জোড় দিনে আয়াত, বিজোড় দিনে হাদীস; প্রতিটি তালিকা ঘুরে ঘুরে আসে।
 */
object DailyVerse {
    const val DEFAULT_MINUTES = 7 * 60

    /** স্বয়ংসম্পূর্ণ অর্থবহ আয়াত (সূরা, শুরু, শেষ)। */
    val AYAHS: List<QuranRef> = listOf(
        QuranRef(2, 152, 152), QuranRef(2, 153, 153), QuranRef(2, 186, 186), QuranRef(2, 255, 255),
        QuranRef(2, 286, 286), QuranRef(3, 139, 139), QuranRef(13, 28, 28), QuranRef(14, 7, 7),
        QuranRef(16, 90, 90), QuranRef(17, 23, 23), QuranRef(39, 53, 53), QuranRef(40, 60, 60),
        QuranRef(49, 10, 10), QuranRef(49, 13, 13), QuranRef(51, 56, 56), QuranRef(65, 3, 3),
        QuranRef(94, 5, 6), QuranRef(103, 1, 3), QuranRef(112, 1, 4)
    )

    sealed class Pick {
        data class AyahPick(val index: Int) : Pick()
        data class HadithPick(val index: Int) : Pick()
    }

    fun choose(epochDay: Long, ayahCount: Int, hadithCount: Int): Pick {
        val slot = Math.floorDiv(epochDay, 2L)
        return if (epochDay % 2L == 0L || hadithCount <= 0) {
            Pick.AyahPick(Math.floorMod(slot, ayahCount.toLong()).toInt())
        } else {
            Pick.HadithPick(Math.floorMod(slot, hadithCount.toLong()).toInt())
        }
    }

    private fun cut(s: String, n: Int): String = if (s.length <= n) s else s.take(n).trimEnd() + "…"

    /** (শিরোনাম, লেখা) */
    suspend fun message(content: ContentRepository, epochDay: Long): Pair<String, String> {
        val hadiths = content.ilm().articles
            .flatMap { a -> a.sections.flatMap { it.hadiths } }
            .filter { it.ref.isNotBlank() && it.meaningBn.isNotBlank() }
            .distinctBy { it.ref + it.meaningBn }
        return when (val p = choose(epochDay, AYAHS.size, hadiths.size)) {
            is Pick.AyahPick -> {
                val ref = AYAHS[p.index]
                val chapter = content.chapters().first { it.id == ref.chapter }
                val text = content.ayahRange(ref).joinToString(" ") { it.bengali }
                val num = if (ref.from == ref.to) DateFormatBn.digits(ref.from) else "${DateFormatBn.digits(ref.from)}-${DateFormatBn.digits(ref.to)}"
                "আজকের আয়াত: সূরা ${chapter.bn} ${DateFormatBn.digits(ref.chapter)}:$num" to cut(text, 600)
            }
            is Pick.HadithPick -> {
                val h = hadiths[p.index]
                "আজকের হাদীস" to cut(h.meaningBn, 500) + "\n— " + h.ref
            }
        }
    }
}
