package com.islamshikhi.app.ui.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * ভিজুয়াল ইফেক্টের কেন্দ্র: অ্যাপের সব বিশেষ ইফেক্ট এই একটি ফাইলে।
 * কোনো ইফেক্ট বদলাতে বা বন্ধ করতে চাইলে শুধু এখানে হাত দিলেই হবে।
 *
 *  ১. islamicPattern()   — ৮ কোণার তারার ইসলামি জ্যামিতিক নকশা (হোমের আকাশ ও স্বাগত পাতার ওপর)
 *  ২. ShimmerText        — সোনালি আলো চলে-যাওয়া লেখা (অ্যাপের নাম)
 *  ৩. rememberTwinkle    — তারার মিটমিট (Sky.kt-এর আকাশে)
 *  ৪. glowPulse()        — ধীরে জ্বলা-নেভা আভা (চাঁদ, পরবর্তী ওয়াক্ত)
 *  ৫. luxClickable()     — চাপলে হালকা ছোট হয়ে আবার ফিরে আসা (সব কার্ড ও বাটন)
 *  ৬. reveal()           — একের পর এক ভেসে ওঠা (হোমের গ্রিড ও কার্ড)
 *  ৭. goldBorder(), LuxCard, GoldButton, GoldOrnament, LuxSectionTitle — সোনালি সীমারেখার কার্ড, বাটন ও বিভাজক
 *
 * মনে রাখুন: ফোনের "অ্যানিমেশন বন্ধ" সেটিং চালু থাকলে অ্যানিমেশনগুলো নিজে থেকেই থেমে যায়; অ্যাপ তখনও ঠিক দেখায়।
 */

// ------------------------------------------------------------------------------------------------
// ১. জ্যামিতিক নকশা
// ------------------------------------------------------------------------------------------------

/**
 * পটভূমিতে ৮ কোণার তারার সারি আঁকে (সরু সোনালি রেখা)। ওপরে স্পষ্ট, নিচে মিলিয়ে যায়।
 * [alpha] বাড়ালে নকশা গাঢ় হয়, [cell] বাড়ালে তারা বড় হয়।
 */
fun Modifier.islamicPattern(
    color: Color = Palette.GoldMid,
    alpha: Float = 0.13f,
    cell: Dp = 46.dp
): Modifier = drawBehind {
    val c = cell.toPx()
    val outer = c * 0.38f
    val inner = outer * 0.74f
    val path = Path()
    var row = 0
    var cy = c / 2f
    while (cy < size.height + c) {
        var cx = if (row % 2 == 0) c / 2f else 0f
        while (cx < size.width + c) {
            for (k in 0 until 16) {
                val angle = (PI.toFloat() / 8f) * k - PI.toFloat() / 2f
                val r = if (k % 2 == 0) outer else inner
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (k == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            cx += c
        }
        cy += c * 0.5f
        row++
    }
    drawPath(
        path,
        brush = Brush.verticalGradient(
            0f to color.copy(alpha = alpha),
            0.75f to color.copy(alpha = alpha * 0.35f),
            1f to Color.Transparent
        ),
        style = Stroke(width = 0.8.dp.toPx())
    )
}

// ------------------------------------------------------------------------------------------------
// ২. সোনালি শিমার লেখা
// ------------------------------------------------------------------------------------------------

/** সোনালি রঙের লেখা, যার ওপর দিয়ে একটি উজ্জ্বল আলো ধীরে বয়ে যায়। */
@Composable
fun ShimmerText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX"
    )
    val start = x * 1500f - 450f
    val brush = Brush.linearGradient(
        colors = listOf(Palette.GoldMid, Palette.GoldHigh, Color.White, Palette.GoldHigh, Palette.GoldMid),
        start = Offset(start, 0f),
        end = Offset(start + 520f, 160f)
    )
    Text(text, modifier = modifier, style = style.copy(brush = brush), textAlign = textAlign)
}

// ------------------------------------------------------------------------------------------------
// ৩. তারার মিটমিট
// ------------------------------------------------------------------------------------------------

/**
 * ০ থেকে ২π পর্যন্ত ঘুরতে থাকা একটি মান। প্রতিটি তারা এর সঙ্গে নিজের আলাদা অংশ যোগ করে sin() নিয়ে মিটমিট করে।
 * মানটি শুধু আঁকার সময় পড়া হয় (Canvas-এর ভেতরে), তাই পুরো স্ক্রিন বারবার নতুন করে সাজানো হয় না।
 */
@Composable
fun rememberTwinkle(): State<Float> {
    val transition = rememberInfiniteTransition(label = "twinkle")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart),
        label = "twinkleValue"
    )
}

// ------------------------------------------------------------------------------------------------
// ৪. আভা
// ------------------------------------------------------------------------------------------------

/** পিছনে নরম আভা, যার উজ্জ্বলতা ধীরে ওঠানামা করে। [radius] হলো আভার ব্যাসার্ধ। */
fun Modifier.glowPulse(color: Color = Palette.GoldMid, radius: Dp = 60.dp, periodMs: Int = 3600): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "glow")
    val a by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )
    drawBehind {
        val r = radius.toPx()
        drawCircle(
            Brush.radialGradient(listOf(color.copy(alpha = a), Color.Transparent), center = center, radius = r),
            radius = r,
            center = center
        )
    }
}

// ------------------------------------------------------------------------------------------------
// ৫. চাপলে ছোট হওয়া
// ------------------------------------------------------------------------------------------------

/**
 * সাধারণ clickable-এর মতোই (স্ক্রিন-রিডার ও টেস্ট একইভাবে কাজ করে), কিন্তু চেপে ধরলে ৪% ছোট হয়
 * এবং ছাড়লে স্প্রিং-এর মতো ফিরে আসে। ঢেউ (ripple) বাদ দিয়ে এটিই চাপের অনুভূতি দেয়।
 */
fun Modifier.luxClickable(onClick: () -> Unit, role: Role = Role.Button): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(interactionSource = source, indication = null, role = role, onClick = onClick)
}

// ------------------------------------------------------------------------------------------------
// ৬. একের পর এক ভেসে ওঠা
// ------------------------------------------------------------------------------------------------

/** পাতা খুললে উপাদানটি নিচ থেকে ভেসে ওঠে। [index] যত বড়, তত দেরিতে শুরু (সর্বোচ্চ প্রায় ০.৭ সেকেন্ড)। */
fun Modifier.reveal(index: Int, stepMs: Int = 80): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((index.coerceAtMost(9) * stepMs).toLong())
        progress.animateTo(1f, tween(480, easing = FastOutSlowInEasing))
    }
    this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 26.dp.toPx()
    }
}

// ------------------------------------------------------------------------------------------------
// ৭. সোনালি সীমারেখা, কার্ড, বাটন, বিভাজক
// ------------------------------------------------------------------------------------------------

/** সরু সোনালি হেয়ারলাইন সীমারেখা। */
fun Modifier.goldBorder(shape: Shape, width: Dp = 1.dp, alpha: Float = 1f): Modifier =
    this.border(width, Palette.goldHairline(alpha), shape)

/**
 * প্রিমিয়াম কার্ড: হালকা গ্রেডিয়েন্ট পটভূমি + সোনালি হেয়ারলাইন। [onClick] দিলে চাপা যায় (চাপলে ছোট হয়)।
 * সাধারণ Card-এর বদলে সব জায়গায় এটি ব্যবহার করলে অ্যাপ এক রকম দেখায়।
 */
@Composable
fun LuxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val fill = Brush.verticalGradient(
        if (dark) listOf(scheme.surfaceVariant, scheme.surface) else listOf(scheme.surface, scheme.surfaceContainerLow)
    )
    val clickMod = if (onClick != null) Modifier.luxClickable(onClick) else Modifier
    Column(
        modifier = modifier
            .then(clickMod)
            .shadow(if (dark) 0.dp else 5.dp, shape, ambientColor = Palette.GoldDeep, spotColor = Palette.GoldDeep)
            .clip(shape)
            .background(fill)
            .goldBorder(shape, alpha = if (dark) 0.55f else 0.5f),
        content = content
    )
}

/** সোনালি গ্রেডিয়েন্টের প্রধান বাটন (গাঢ় লেখা)। অ্যাপের সবচেয়ে গুরুত্বপূর্ণ বাটনে ব্যবহারের জন্য। */
@Composable
fun GoldButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .luxClickable(onClick)
            .shadow(10.dp, shape, ambientColor = Palette.GoldMid, spotColor = Palette.GoldMid)
            .clip(shape)
            .background(Palette.GoldBrush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF2A1E00)
        )
    }
}

/** মাঝখানে ছোট হিরের চিহ্নসহ সোনালি বিভাজক রেখা। */
@Composable
fun GoldOrnament(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(14.dp)) {
        val y = size.height / 2f
        val cx = size.width / 2f
        val gap = 10.dp.toPx()
        val left = Brush.horizontalGradient(listOf(Color.Transparent, Palette.GoldMid.copy(alpha = 0.8f)), 0f, cx - gap)
        val right = Brush.horizontalGradient(listOf(Palette.GoldMid.copy(alpha = 0.8f), Color.Transparent), cx + gap, size.width)
        drawLine(left, Offset(0f, y), Offset(cx - gap, y), 1.dp.toPx())
        drawLine(right, Offset(cx + gap, y), Offset(size.width, y), 1.dp.toPx())
        val d = 4.dp.toPx()
        val diamond = Path().apply {
            moveTo(cx, y - d); lineTo(cx + d, y); lineTo(cx, y + d); lineTo(cx - d, y); close()
        }
        drawPath(diamond, Palette.GoldMid)
    }
}

/** বিভাগের শিরোনাম: লেখা + ডানে মিলিয়ে-যাওয়া সোনালি রেখা। */
@Composable
fun LuxSectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(Palette.GoldMid.copy(alpha = 0.6f), Color.Transparent)))
        )
    }
}
