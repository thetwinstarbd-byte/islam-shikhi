package com.islamshikhi.app.ui.common

import com.islamshikhi.app.domain.model.AgeGroup
import com.islamshikhi.app.domain.model.AppLanguage
import com.islamshikhi.app.domain.model.Gender
import com.islamshikhi.app.domain.model.LearningGoal
import com.islamshikhi.app.domain.model.LearningInterest
import com.islamshikhi.app.domain.model.ThemeMode

/** enum-এর বাংলা নাম এক জায়গায় রাখা হয়েছে, যেন সব স্ক্রিনে একই শব্দ দেখায়। */

fun Gender.labelBn(): String = when (this) {
    Gender.MALE -> "পুরুষ"
    Gender.FEMALE -> "নারী"
}

fun AgeGroup.labelBn(): String = when (this) {
    AgeGroup.CHILD_TEEN -> "শিশু/কিশোর (১৭ বছর পর্যন্ত)"
    AgeGroup.ADULT -> "প্রাপ্তবয়স্ক"
    AgeGroup.SENIOR -> "বয়স্ক"
}

/** মাসআলা/কনটেন্টের audience তালিকা থেকে ছোট লেবেল; সবার জন্য হলে null (কিছু দেখানো হয় না)। */
fun audienceLabelBn(audience: List<String>): String? = when {
    "ALL" in audience || audience.isEmpty() -> null
    "FEMALE" in audience && "MALE" in audience -> null
    "FEMALE" in audience -> "নারীদের জন্য প্রাসঙ্গিক"
    "MALE" in audience -> "পুরুষদের জন্য প্রাসঙ্গিক"
    else -> null
}

fun AppLanguage.labelBn(): String = when (this) {
    AppLanguage.BANGLA -> "বাংলা"
    AppLanguage.ENGLISH -> "English"
}

fun ThemeMode.labelBn(): String = when (this) {
    ThemeMode.SYSTEM -> "ফোনের সেটিং অনুযায়ী"
    ThemeMode.LIGHT -> "লাইট"
    ThemeMode.DARK -> "ডার্ক"
}

fun LearningInterest.labelBn(): String = when (this) {
    LearningInterest.QURAN -> "কুরআন"
    LearningInterest.DUA_ZIKR -> "দোয়া ও যিকির"
    LearningInterest.SALAH -> "নামাজ"
    LearningInterest.MASAIL -> "মাসআলা"
    LearningInterest.ZAKAT -> "যাকাত ও অর্থনীতি"
    LearningInterest.FAMILY -> "পরিবার ও শিশুদের শিক্ষা"
    LearningInterest.SELF_IMPROVEMENT -> "আখলাক ও আত্মশুদ্ধি"
}

fun LearningGoal.labelBn(): String = when (this) {
    LearningGoal.DAILY_HABIT -> "প্রতিদিনের অভ্যাস গড়া"
    LearningGoal.LEARN_BASICS -> "মৌলিক বিষয় শেখা"
    LearningGoal.DEEPEN_KNOWLEDGE -> "জ্ঞান আরও গভীর করা"
}
