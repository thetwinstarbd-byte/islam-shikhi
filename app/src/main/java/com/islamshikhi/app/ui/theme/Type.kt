package com.islamshikhi.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.islamshikhi.app.R

/** বাংলা UI-এর ফন্ট: Hind Siliguri (OFL)। বাংলা যুক্তাক্ষর ও অঙ্ক পরিষ্কার দেখায়। */
val HindSiliguri = FontFamily(
    Font(R.font.hind_siliguri_regular, FontWeight.Normal),
    Font(R.font.hind_siliguri_medium, FontWeight.Medium),
    Font(R.font.hind_siliguri_semibold, FontWeight.SemiBold),
    Font(R.font.hind_siliguri_bold, FontWeight.Bold)
)

/** কুরআন ও দোয়ার আরবির ফন্ট: Amiri Quran (OFL)। উসমানী লেখার সব চিহ্ন আছে। */
val AmiriQuran = FontFamily(Font(R.font.amiri_quran_regular, FontWeight.Normal))

private fun TextStyle.hind() = copy(fontFamily = HindSiliguri)

private val base = Typography()

/** বাংলার জন্য লাইনের উচ্চতা একটু বেশি রাখা হয়েছে (ওপর-নিচের মাত্রা চিহ্ন কাটা না পড়তে)। */
val AppTypography = Typography(
    displayLarge = base.displayLarge.hind(),
    displayMedium = base.displayMedium.hind(),
    displaySmall = base.displaySmall.hind().copy(fontSize = 40.sp, lineHeight = 54.sp, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.hind(),
    headlineMedium = base.headlineMedium.hind().copy(fontSize = 30.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.hind().copy(fontSize = 24.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.hind().copy(fontSize = 21.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.hind().copy(fontSize = 17.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium),
    titleSmall = base.titleSmall.hind().copy(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    bodyLarge = base.bodyLarge.hind().copy(fontSize = 17.sp, lineHeight = 28.sp),
    bodyMedium = base.bodyMedium.hind().copy(fontSize = 15.sp, lineHeight = 24.sp),
    bodySmall = base.bodySmall.hind().copy(fontSize = 13.sp, lineHeight = 20.sp),
    labelLarge = base.labelLarge.hind().copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = base.labelMedium.hind().copy(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    labelSmall = base.labelSmall.hind().copy(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
)
