package com.islamshikhi.app.ui.quran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.SectionTitle
import com.islamshikhi.app.ui.common.rememberContainer

/**
 * কুরআনের আরবি ও অনুবাদের উৎস ও লাইসেন্স। Tanzil ও QuranEnc-এর শর্ত অনুযায়ী উৎসের নাম, সংস্করণ
 * ও লিংক দেখাতে হয়; তাই এই পাতা আবশ্যক।
 */
@Composable
fun QuranNoticeScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val uriHandler = LocalUriHandler.current
    var notice by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { notice = runCatching { container.content.quranNotice() }.getOrDefault("") }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("উৎস ও লাইসেন্স", onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle("আরবি টেক্সট")
            InfoCard(
                "Tanzil Project — Uthmani, সংস্করণ ১.১",
                "লাইসেন্স: Creative Commons Attribution 3.0। টেক্সট অপরিবর্তিত; পরিবর্তন অনুমোদিত নয়।"
            )
            OutlinedButton(onClick = { uriHandler.openUri("https://tanzil.net") }) { Text("tanzil.net খুলুন") }

            SectionTitle("বাংলা অনুবাদ ও টীকা")
            InfoCard(
                "আবু বকর যাকারিয়া — QuranEnc.com, সংস্করণ ১.১.১",
                "অনুবাদ ও টীকা অপরিবর্তিত। কোনো ভুল বা মন্তব্য থাকলে QuranEnc.com-এ জানানো যায়।"
            )
            OutlinedButton(onClick = { uriHandler.openUri("https://quranenc.com") }) { Text("quranenc.com খুলুন") }

            SectionTitle("মূল কপিরাইট নোটিশ (Tanzil)")
            if (notice.isNotBlank()) {
                Text(notice, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
