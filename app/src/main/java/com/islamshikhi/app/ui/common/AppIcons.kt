package com.islamshikhi.app.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * নিজের আঁকা আইকন। Material-এর আইকনে "মোনাজাতের হাত" নেই, তাই এখানে রেখা দিয়ে আঁকা হয়েছে।
 * ২৪×২৪ ক্যানভাসে: নিচে দুই হাতের মিলিত অর্ধগোলাকার পাত্র (তালু), ওপরে দুই পাশে আঙুল।
 * আকার বা রং Icon(tint = ...) দিয়েই বদলায়, তাই অন্য আইকনের মতোই ব্যবহার করা যায়।
 */
val MunajatIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Munajat",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        val stroke = SolidColor(Color.Black)
        // তালু: দুই হাতের মিলিত পাত্রের আকার
        path(stroke = stroke, strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(3.2f, 11.2f)
            curveTo(3.6f, 16.2f, 7.4f, 19.6f, 12f, 19.6f)
            curveTo(16.6f, 19.6f, 20.4f, 16.2f, 20.8f, 11.2f)
        }
        // মাঝের সংযোগ (দুই বুড়ো আঙুল মিলেছে)
        path(stroke = stroke, strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(12f, 19.6f)
            lineTo(12f, 14.4f)
        }
        // বাম হাতের চার আঙুল (বাইরের দিকে হেলানো)
        path(stroke = stroke, strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(3.2f, 11.2f); lineTo(2.6f, 6.4f)
            moveTo(5.9f, 12.6f); lineTo(5.4f, 4.9f)
            moveTo(8.6f, 13.6f); lineTo(8.4f, 4.2f)
            moveTo(10.9f, 14.2f); lineTo(11f, 5.6f)
        }
        // ডান হাতের চার আঙুল (প্রতিসম)
        path(stroke = stroke, strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(20.8f, 11.2f); lineTo(21.4f, 6.4f)
            moveTo(18.1f, 12.6f); lineTo(18.6f, 4.9f)
            moveTo(15.4f, 13.6f); lineTo(15.6f, 4.2f)
            moveTo(13.1f, 14.2f); lineTo(13f, 5.6f)
        }
    }.build()
}
