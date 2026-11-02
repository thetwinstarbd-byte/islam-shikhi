package com.islamshikhi.app.ui.update

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.islamshikhi.app.data.update.ContentUpdater
import com.islamshikhi.app.data.update.UpdateResult
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch

/** কনটেন্ট আপডেট: লেখা/প্রবন্ধ/কুইজ নতুন করে ইনস্টল ছাড়াই হালনাগাদ। ব্যবহারকারী বাটন চাপলেই কেবল ইন্টারনেট ব্যবহার হয়। */
@Composable
fun UpdateScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var version by remember { mutableStateOf(ContentUpdater.currentVersion(context)) }

    ScreenColumn("কনটেন্ট আপডেট", onBack) {
        InfoCard(
            "বর্তমান কনটেন্ট সংস্করণ: ${DateFormatBn.digits(version)}",
            "জ্ঞান-প্রবন্ধ, মাসআলা, দোয়া, কুইজ ও অন্যান্য লেখা অ্যাপ নতুন করে ইনস্টল না করেই হালনাগাদ করা যায়। " +
                "প্রতিটি ফাইল যাচাই করে তবেই বসানো হয়। কুরআনের আরবি ও অনুবাদ এভাবে বদলায় না।"
        )
        Button(
            onClick = {
                scope.launch {
                    busy = true
                    message = when (val r = ContentUpdater.check(context)) {
                        UpdateResult.UpToDate -> "আপনার কনটেন্ট সবচেয়ে নতুন।"
                        is UpdateResult.Applied -> { version = r.version; "আপডেট হয়েছে (সংস্করণ ${DateFormatBn.digits(r.version)})। নতুন লেখা অ্যাপ আবার চালু করলে দেখা যাবে।" }
                        is UpdateResult.Failed -> r.reason
                    }
                    busy = false
                }
            },
            enabled = !busy
        ) { Text(if (busy) "দেখা হচ্ছে…" else "আপডেট আছে কি না দেখুন") }
        message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        Text(
            "বাটন চাপলে শুধু GitHub থেকে একটি ছোট তালিকা ও ফাইল নামানো হয়; আপনার কোনো তথ্য পাঠানো হয় না।",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
