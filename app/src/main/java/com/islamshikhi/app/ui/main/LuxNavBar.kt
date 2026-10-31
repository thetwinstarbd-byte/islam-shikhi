package com.islamshikhi.app.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.ui.effects.goldBorder
import com.islamshikhi.app.ui.effects.luxClickable
import com.islamshikhi.app.ui.navigation.AppTab
import com.islamshikhi.app.ui.theme.Palette

/**
 * ভাসমান নিচের ট্যাব-বার: স্ক্রিনের কিনারা থেকে সরানো, গোলাকার, সোনালি হেয়ারলাইনসহ।
 * নির্বাচিত ট্যাবে সোনালি আভার গোলাকার দাগ। সব ট্যাবেই নাম লেখা থাকে (চিনতে সুবিধা)।
 */
@Composable
fun LuxNavBar(currentRoute: String?, onSelect: (AppTab) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val shape = RoundedCornerShape(30.dp)
    val barFill = Brush.verticalGradient(
        if (dark) listOf(scheme.surfaceContainerHigh, scheme.surface) else listOf(Color.White, scheme.surfaceContainerLow)
    )

    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 10.dp, top = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(14.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(shape)
                .background(barFill)
                .goldBorder(shape, alpha = 0.7f)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.entries.forEach { tab ->
                val selected = currentRoute?.startsWith(tab.route) == true
                val tint by animateColorAsState(
                    if (selected) Color(0xFF2A1E00) else scheme.onSurfaceVariant,
                    label = "navTint"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .luxClickable({ onSelect(tab) }, role = Role.Tab)
                        .clip(RoundedCornerShape(24.dp))
                        .then(if (selected) Modifier.background(Palette.GoldBrush) else Modifier)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(tab.icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                    Text(tab.label, style = MaterialTheme.typography.labelSmall, color = tint, maxLines = 1)
                }
            }
        }
    }
}
