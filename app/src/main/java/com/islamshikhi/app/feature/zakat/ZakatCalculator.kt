package com.islamshikhi.app.feature.zakat

/** নিসাব কোন ধাতুর হিসাবে ধরা হবে। আলেমদের মধ্যে এ নিয়ে মতভেদ আছে; ব্যবহারকারী নিজে বেছে নেন। */
enum class NisabBasis { SILVER, GOLD }

/**
 * যাকাতের হিসাবের ইনপুট। সব টাকার অঙ্ক একই মুদ্রায় (যেমন টাকা) এবং ওজন গ্রামে।
 * স্বর্ণ-রূপার বাজারদর ব্যবহারকারী নিজে দেন; অ্যাপ ইন্টারনেট থেকে দর আনে না।
 */
data class ZakatInput(
    val cash: Double = 0.0,                 // নগদ ও ব্যাংকে জমা
    val goldGrams: Double = 0.0,
    val silverGrams: Double = 0.0,
    val goldPricePerGram: Double = 0.0,
    val silverPricePerGram: Double = 0.0,
    val businessAssets: Double = 0.0,       // ব্যবসার পণ্য ও পাওনা
    val receivables: Double = 0.0,          // অন্যের কাছে আদায়যোগ্য পাওনা
    val liabilities: Double = 0.0,          // এখনই পরিশোধযোগ্য ঋণ
    val basis: NisabBasis = NisabBasis.SILVER
)

data class ZakatResult(
    val goldValue: Double,
    val silverValue: Double,
    val totalAssets: Double,
    val liabilities: Double,
    val netWealth: Double,
    val nisabGrams: Double,
    val nisabValue: Double,
    val priceMissing: Boolean,       // নিসাবের ধাতুর দর না দিলে সত্য; তখন সিদ্ধান্ত দেওয়া যায় না
    val reachesNisab: Boolean,
    val zakatDue: Double
)

object ZakatCalculator {
    /** সাধারণভাবে প্রচলিত মান: ৭.৫ ভরি স্বর্ণ ও ৫২.৫ ভরি রূপা (১ ভরি ≈ ১১.৬৬৪ গ্রাম)। কেউ কেউ স্বর্ণের নিসাব ৮৫ গ্রাম ধরেন। */
    const val GOLD_NISAB_GRAMS = 87.48
    const val SILVER_NISAB_GRAMS = 612.36
    const val RATE = 0.025

    fun calculate(input: ZakatInput): ZakatResult {
        val goldValue = input.goldGrams * input.goldPricePerGram
        val silverValue = input.silverGrams * input.silverPricePerGram
        val total = input.cash + goldValue + silverValue + input.businessAssets + input.receivables
        val net = (total - input.liabilities).coerceAtLeast(0.0)

        val grams = if (input.basis == NisabBasis.SILVER) SILVER_NISAB_GRAMS else GOLD_NISAB_GRAMS
        val price = if (input.basis == NisabBasis.SILVER) input.silverPricePerGram else input.goldPricePerGram
        val missing = price <= 0.0
        val nisabValue = grams * price
        val reaches = !missing && net >= nisabValue
        val due = if (reaches) net * RATE else 0.0

        return ZakatResult(goldValue, silverValue, total, input.liabilities, net, grams, nisabValue, missing, reaches, due)
    }

    /** "১২৩.৫" বা "123.5" দুটোই বোঝে। ফাঁকা বা ভুল হলে ০। কমা বাদ। */
    fun parse(text: String): Double {
        val ascii = text.map { c ->
            when (c) {
                in '০'..'৯' -> ('0' + (c - '০'))
                '।', '٫' -> '.'
                else -> c
            }
        }.joinToString("").replace(",", "").trim()
        return ascii.toDoubleOrNull()?.takeIf { it >= 0.0 } ?: 0.0
    }
}
