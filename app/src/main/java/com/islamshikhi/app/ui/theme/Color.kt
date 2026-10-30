package com.islamshikhi.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * রঙের ভাষা: নামাজের ওয়াক্ত সূর্যের অবস্থানে বাঁধা, তাই অ্যাপের রং আসে আকাশ থেকে।
 * রাতের নীল (প্রধান রং), সূর্যের সোনালি (জোর দেওয়ার রং), ভোরের গোলাপি (হালকা ছোঁয়া)।
 * হোমের "আকাশ" কার্ডটিই একমাত্র উজ্জ্বল জায়গা; বাকি সব শান্ত।
 */
object Palette {
    // আলো
    val PaperLight = Color(0xFFF3F5FA)
    val InkLight = Color(0xFF151B2E)
    val IndigoLight = Color(0xFF2A3F85)
    val IndigoContainerLight = Color(0xFFDDE4F8)
    val GoldLight = Color(0xFF8A5A00)
    val GoldContainerLight = Color(0xFFFCEBC2)
    val RoseLight = Color(0xFFA7443B)
    val RoseContainerLight = Color(0xFFFADCD7)

    // অন্ধকার
    val PaperDark = Color(0xFF0B0F1F)
    val SurfaceDark = Color(0xFF121830)
    val InkDark = Color(0xFFE4E8F5)
    val IndigoDark = Color(0xFFA9BBFF)
    val GoldDark = Color(0xFFF0C25A)

    // আকাশের আলো (সূর্য ও চাঁদ)
    val Sun = Color(0xFFFFD27A)
    val SunGlow = Color(0xFFFFB84D)
    val Moon = Color(0xFFF4EBCF)
}

/** দিনের কোন অংশ চলছে, তা অনুযায়ী আকাশের রং (ওপর থেকে নিচে)। সাদা লেখা পড়া যায়, এমন গাঢ়তা রাখা হয়েছে। */
enum class SkyPhase(val colors: List<Color>, val starAlpha: Float) {
    NIGHT(listOf(Color(0xFF0A1033), Color(0xFF1A2760), Color(0xFF2B3A7C)), 1f),
    DAWN(listOf(Color(0xFF2E3A80), Color(0xFF8E5A93), Color(0xFFD9806F)), 0.35f),
    MORNING(listOf(Color(0xFF2B5FB3), Color(0xFF4D86CF), Color(0xFF7FB0E4)), 0f),
    NOON(listOf(Color(0xFF1E5DB5), Color(0xFF2F78C9), Color(0xFF58A0DB)), 0f),
    AFTERNOON(listOf(Color(0xFF30509C), Color(0xFF7A6BA0), Color(0xFFC0794C)), 0f),
    DUSK(listOf(Color(0xFF242A6A), Color(0xFF8A4A86), Color(0xFFD0654E)), 0.5f)
}
