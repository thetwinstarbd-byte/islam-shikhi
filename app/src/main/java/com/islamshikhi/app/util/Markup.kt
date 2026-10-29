package com.islamshikhi.app.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

/**
 * অনুবাদের টীকায় থাকা <b>…</b> ট্যাগকে বোল্ড লেখায় বদলায়, যেন ট্যাগের চিহ্ন স্ক্রিনে না দেখায়।
 * লেখার একটি অক্ষরও বদলানো বা বাদ দেওয়া হয় না (অনুবাদের লাইসেন্স-শর্ত অনুযায়ী)।
 */
fun boldMarkup(text: String): AnnotatedString = buildAnnotatedString {
    var depth = 0
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("<b>", i) -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                depth++
                i += 3
            }
            text.startsWith("</b>", i) -> {
                if (depth > 0) {
                    pop()
                    depth--
                }
                i += 4
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
    while (depth > 0) {
        pop()
        depth--
    }
}
