package com.islamshikhi.app.ui.i18n

import android.content.Context
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.islamshikhi.app.domain.model.AppLanguage
import org.json.JSONObject

/**
 * ইংরেজি ইন্টারফেসের অনুবাদক।
 * নিয়ম: লেখার সব বাংলা অংশ অভিধানে মিললে তবেই ইংরেজি দেখানো হয়; একটি অংশও না মিললে মূল বাংলা লেখা অপরিবর্তিত থাকে
 * (যাতে কুরআন, দোয়া, প্রবন্ধ ইত্যাদি কনটেন্ট কখনো আধা-অনুবাদ হয়ে না যায়)।
 */
object En {
    private val BN_LETTER = Regex("[ঀ-৥ৰ-৿]")
    private const val DIGITS_BN = "০১২৩৪৫৬৭৮৯"

    @Volatile private var ready = false
    private var exact: Map<String, String> = emptyMap()
    private var parts: List<Pair<String, String>> = emptyList()
    private val cache = object : LinkedHashMap<String, String>(512, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?) = size > 3000
    }

    fun ensure(ctx: Context) {
        if (ready) return
        synchronized(this) {
            if (ready) return
            val m = HashMap<String, String>(EnglishDict.map)
            try {
                val json = ctx.applicationContext.assets.open("i18n_en.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
                val o = JSONObject(json)
                for (k in o.keys()) m[k] = o.getString(k)
            } catch (_: Exception) { /* অভিধান না পেলে বাংলাই দেখাবে */ }
            exact = m
            parts = m.entries.filter { it.key.isNotBlank() }.sortedByDescending { it.key.length }.map { it.key to it.value }
            ready = true
        }
    }

    fun digits(s: String): String {
        val sb = StringBuilder(s.length)
        for (c in s) { val i = DIGITS_BN.indexOf(c); sb.append(if (i >= 0) ('0' + i) else c) }
        return sb.toString()
    }

    fun translate(s: String): String {
        if (s.isEmpty()) return s
        synchronized(cache) { cache[s]?.let { return it } }
        val out = compute(s)
        synchronized(cache) { cache[s] = out }
        return out
    }

    private fun compute(s: String): String {
        exact[s]?.let { return it }
        val st = s.trim()
        if (st != s) exact[st]?.let { return s.replace(st, it) }
        if (!BN_LETTER.containsMatchIn(s)) return digits(s).replace('।', '.')
        if (s.length > 700) return s
        var r = s
        for ((k, v) in parts) if (r.contains(k)) r = r.replace(k, v)
        return if (BN_LETTER.containsMatchIn(r)) s else digits(r).replace('।', '.').replace("  ", " ")
    }
}

@Composable
fun trAuto(text: String): String {
    val lang = LocalLang.current
    if (lang != AppLanguage.ENGLISH) return text
    val ctx = LocalContext.current
    En.ensure(ctx)
    return remember(text) { En.translate(text) }
}

/** material3 Text-এর বদলি: ইংরেজি ভাষা বাছা থাকলে UI-র লেখা অনুবাদ করে দেখায়। */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    androidx.compose.material3.Text(
        text = trAuto(text), modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
        fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration,
        textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap, maxLines = maxLines,
        minLines = minLines, onTextLayout = onTextLayout, style = style,
    )
}

@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    androidx.compose.material3.Text(
        text = text, modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
        fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration,
        textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap, maxLines = maxLines,
        minLines = minLines, onTextLayout = onTextLayout, style = style,
    )
}
