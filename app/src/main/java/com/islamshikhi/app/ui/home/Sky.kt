package com.islamshikhi.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.feature.prayer.DayTimes
import com.islamshikhi.app.feature.prayer.Prayer
import com.islamshikhi.app.ui.theme.Palette
import com.islamshikhi.app.ui.theme.SkyPhase
import kotlin.math.PI
import kotlin.math.sin

/**
 * আকাশের অবস্থা: দিনের কোন অংশ চলছে এবং সূর্য (দিনে) বা চাঁদ (রাতে) আকাশের চাপে কোথায়।
 * [progress] ০ = বাম দিগন্ত, ১ = ডান দিগন্ত।
 */
data class SkyState(val phase: SkyPhase, val progress: Float, val isSun: Boolean)

/**
 * গতকাল, আজ ও আগামীকালের নামাজের সময় থেকে হিসাব করা হয়। সূর্য চলে সূর্যোদয় থেকে মাগরিব পর্যন্ত,
 * চাঁদ চলে মাগরিব থেকে পরের সূর্যোদয় পর্যন্ত।
 */
fun computeSky(yesterday: DayTimes, today: DayTimes, tomorrow: DayTimes, now: Long): SkyState {
    val phase = when {
        now < today[Prayer.FAJR] -> SkyPhase.NIGHT
        now < today[Prayer.SUNRISE] -> SkyPhase.DAWN
        now < today[Prayer.DHUHR] -> SkyPhase.MORNING
        now < today[Prayer.ASR] -> SkyPhase.NOON
        now < today[Prayer.MAGHRIB] -> SkyPhase.AFTERNOON
        now < today[Prayer.ISHA] -> SkyPhase.DUSK
        else -> SkyPhase.NIGHT
    }
    val sunUp = now >= today[Prayer.SUNRISE] && now < today[Prayer.MAGHRIB]
    val (start, end) = when {
        sunUp -> today[Prayer.SUNRISE] to today[Prayer.MAGHRIB]
        now >= today[Prayer.MAGHRIB] -> today[Prayer.MAGHRIB] to tomorrow[Prayer.SUNRISE]
        else -> yesterday[Prayer.MAGHRIB] to today[Prayer.SUNRISE]
    }
    val span = (end - start).coerceAtLeast(1L)
    val progress = ((now - start).toFloat() / span).coerceIn(0f, 1f)
    return SkyState(phase, progress, sunUp)
}

/** আকাশের গ্রেডিয়েন্ট পটভূমি, তারা ও সূর্য/চাঁদের চাপ। লেখা এর ওপরে বসে। */
@Composable
fun SkyBackdrop(sky: SkyState, modifier: Modifier = Modifier, showArc: Boolean = true) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(sky.phase.colors))

        // তারা: নির্দিষ্ট জায়গায় (প্রতিবার একই), রাতে সবচেয়ে উজ্জ্বল
        if (sky.phase.starAlpha > 0f) {
            var seed = 7L
            fun next(): Float {
                seed = (seed * 1103515245L + 12345L) and 0x7fffffffL
                return (seed % 10000L) / 10000f
            }
            repeat(34) {
                val x = next() * w
                val y = next() * h * 0.62f
                val r = (0.7f + next() * 1.4f).dp.toPx()
                drawCircle(Color.White.copy(alpha = sky.phase.starAlpha * (0.35f + next() * 0.55f)), r, Offset(x, y))
            }
        }

        if (showArc) {
            val padX = 28.dp.toPx()
            // দিগন্তরেখা নিচ থেকে ৫৮dp ওপরে: তার নিচে সূর্যোদয়/সূর্যাস্তের সময়, আর সবার নিচে ওয়াক্ত-সারি ঢুকে পড়ে
            val baseY = h - 58.dp.toPx()
            val arcH = 78.dp.toPx()
            val x0 = padX
            val x1 = w - padX

            // দিগন্তরেখা
            drawLine(Color.White.copy(alpha = 0.28f), Offset(x0 - 8.dp.toPx(), baseY), Offset(x1 + 8.dp.toPx(), baseY), 1.dp.toPx())

            // সূর্য/চাঁদের পথ (ড্যাশ-করা চাপ)
            val path = Path()
            val steps = 48
            for (i in 0..steps) {
                val p = i / steps.toFloat()
                val x = x0 + (x1 - x0) * p
                val y = baseY - arcH * sin(PI.toFloat() * p)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path,
                Color.White.copy(alpha = 0.32f),
                style = Stroke(width = 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 7.dp.toPx())))
            )

            val cx = x0 + (x1 - x0) * sky.progress
            val cy = baseY - arcH * sin(PI.toFloat() * sky.progress)
            if (sky.isSun) {
                drawCircle(
                    Brush.radialGradient(
                        listOf(Palette.SunGlow.copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(cx, cy), radius = 40.dp.toPx()
                    ),
                    radius = 40.dp.toPx(), center = Offset(cx, cy)
                )
                drawCircle(Palette.Sun, radius = 12.dp.toPx(), center = Offset(cx, cy))
            } else {
                // চাঁদ: একটি বৃত্ত থেকে আরেকটি বৃত্ত কেটে বাঁকা চাঁদ
                val r = 12.dp.toPx()
                val full = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx, cy), r)) }
                val cut = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx + r * 0.55f, cy - r * 0.25f), r * 0.9f)) }
                val crescent = Path.combine(PathOperation.Difference, full, cut)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Palette.Moon.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(cx, cy), radius = 34.dp.toPx()
                    ),
                    radius = 34.dp.toPx(), center = Offset(cx, cy)
                )
                drawPath(crescent, Palette.Moon)
            }
        }

        // নিচের দিকে হালকা ছায়া, যেন ওপরের সাদা লেখা ও নিচের সময়ের লেখা সব আকাশেই পড়া যায়
        drawRect(
            Brush.verticalGradient(
                0.55f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.22f)
            )
        )
    }
}

/** বাঁকা চাঁদ (স্বাগত পাতার জন্য)। */
@Composable
fun Crescent(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        val full = Path().apply { addOval(androidx.compose.ui.geometry.Rect(c, r)) }
        val cut = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(c.x + r * 0.5f, c.y - r * 0.22f), r * 0.86f)) }
        drawCircle(
            Brush.radialGradient(listOf(Palette.Moon.copy(alpha = 0.30f), Color.Transparent), center = c, radius = r * 2.1f),
            radius = r * 2.1f, center = c
        )
        drawPath(Path.combine(PathOperation.Difference, full, cut), Palette.Moon)
    }
}
