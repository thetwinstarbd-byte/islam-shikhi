package com.islamshikhi.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * রঙের ভাষা (লাক্সারি): গভীর পান্না-সবুজ (প্রায় কালো) + শ্যাম্পেন সোনালি।
 * - গাঢ় থিমে: পান্না-কালো পটভূমি, সোনালি জোর, পান্না-মিন্ট প্রধান রং।
 * - হালকা থিমে: হাতির-দাঁতের মতো ক্রিম পটভূমি, গাঢ় পান্না প্রধান রং, গাঢ় সোনালি জোর।
 * সোনালি রং শুধু জোর দেওয়ার জায়গায় (সীমারেখা, আইকন, বাটন); সবখানে ছড়ালে দামি লাগে না।
 */
object Palette {
    // ---- হালকা থিম ----
    val PaperLight = Color(0xFFFBF8F1)            // ক্রিম পটভূমি
    val InkLight = Color(0xFF12201B)
    val EmeraldLight = Color(0xFF0B5A45)          // প্রধান রং
    val EmeraldContainerLight = Color(0xFFD5EEE3)
    val GoldLight = Color(0xFF8F6A12)             // সাদা/ক্রিমের ওপর পড়ার মতো গাঢ় সোনালি
    val GoldContainerLight = Color(0xFFF6EBCB)
    val RoseLight = Color(0xFFA7443B)
    val RoseContainerLight = Color(0xFFFADCD7)
    val SurfaceVariantLight = Color(0xFFF2EDDF)

    // ---- গাঢ় থিম ----
    val PaperDark = Color(0xFF07110E)             // পান্না-কালো
    val SurfaceDark = Color(0xFF0E1C17)
    val SurfaceVariantDark = Color(0xFF14261F)
    val InkDark = Color(0xFFEDEBE2)
    val EmeraldDark = Color(0xFF6FD6AB)           // মিন্ট-পান্না
    val EmeraldContainerDark = Color(0xFF16382B)
    val GoldDark = Color(0xFFE6C77C)              // শ্যাম্পেন সোনালি

    // ---- সোনালি গ্রেডিয়েন্টের ধাপ (বাটন, সীমারেখা, শিমার) ----
    val GoldHigh = Color(0xFFF6E3A8)
    val GoldMid = Color(0xFFD9B35F)
    val GoldDeep = Color(0xFFA9812F)

    // ---- আকাশের আলো (সূর্য ও চাঁদ) ----
    val Sun = Color(0xFFFFD27A)
    val SunGlow = Color(0xFFFFB84D)
    val Moon = Color(0xFFF4EBCF)

    /** সোনালি বাটনের গ্রেডিয়েন্ট (বাম থেকে ডানে)। */
    val GoldBrush: Brush get() = Brush.linearGradient(listOf(GoldHigh, GoldMid, GoldDeep))

    /** সোনালি হেয়ারলাইন সীমারেখা; কোণায় উজ্জ্বল, মাঝখানে ম্লান। */
    fun goldHairline(alpha: Float = 1f): Brush = Brush.linearGradient(
        listOf(GoldHigh.copy(alpha = 0.85f * alpha), GoldMid.copy(alpha = 0.18f * alpha), GoldDeep.copy(alpha = 0.55f * alpha))
    )
}

/**
 * দিনের কোন অংশ চলছে, তা অনুযায়ী আকাশের রং (ওপর থেকে নিচে)।
 * সব আকাশই একটু পান্না/টিলের দিকে ঝোঁকানো, যেন অ্যাপের রঙের সঙ্গে মিলে যায়। সাদা লেখা পড়া যায়, এমন গাঢ়তা রাখা হয়েছে।
 */
enum class SkyPhase(val colors: List<Color>, val starAlpha: Float) {
    NIGHT(listOf(Color(0xFF030B09), Color(0xFF0A2A22), Color(0xFF13453A)), 1f),
    DAWN(listOf(Color(0xFF0E2A33), Color(0xFF6B5A72), Color(0xFFD08F62)), 0.35f),
    MORNING(listOf(Color(0xFF0B4A55), Color(0xFF1F7A78), Color(0xFF5FB0A0)), 0f),
    NOON(listOf(Color(0xFF0A4E63), Color(0xFF14707F), Color(0xFF3AA1A4)), 0f),
    AFTERNOON(listOf(Color(0xFF15434B), Color(0xFF7A6A48), Color(0xFFC0894A)), 0f),
    DUSK(listOf(Color(0xFF111C36), Color(0xFF5E3A58), Color(0xFFC0664B)), 0.5f)
}
