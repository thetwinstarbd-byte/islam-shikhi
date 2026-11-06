package com.islamshikhi.app.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.islamshikhi.app.data.backup.BackupCodec
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Instant
import java.time.LocalDate

private fun readText(input: InputStream, max: Int): String {
    val out = ByteArrayOutputStream()
    val buf = ByteArray(8192)
    var total = 0
    while (true) {
        val n = input.read(buf)
        if (n < 0) break
        total += n
        if (total > max) throw IllegalArgumentException("ফাইলটি খুব বড়")
        out.write(buf, 0, n)
    }
    return out.toString("UTF-8")
}

/**
 * ব্যাকআপ ও রিস্টোর: বুকমার্ক, নোট, ট্র্যাকার, তাসবিহ ও নামাজ-সেটিং একটি ফাইলে রাখা, নতুন ফোনে ফেরত আনা।
 * ফাইল কোথায় রাখবেন তা ব্যবহারকারী নিজে বাছেন (ফোনের ফাইল-পিকার); অ্যাপ এটি ইন্টারনেটে পাঠায় না।
 */
@Composable
fun BackupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<Map<String, Any>?>(null) }

    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            message = try {
                val json = BackupCodec.encode(container.userData.snapshot(), Instant.now().toString())
                withContext(Dispatchers.IO) {
                    val out = context.contentResolver.openOutputStream(uri) ?: throw IllegalStateException("ফাইল খোলা যায়নি")
                    out.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                }
                "ব্যাকআপ ফাইল সংরক্ষিত হয়েছে।"
            } catch (e: Exception) {
                "ব্যাকআপ সংরক্ষণ করা যায়নি।"
            }
        }
    }

    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    val input = context.contentResolver.openInputStream(uri) ?: throw IllegalStateException("ফাইল খোলা যায়নি")
                    input.use { readText(it, 8_000_000) }
                }
                pending = BackupCodec.decode(text)
                message = null
            } catch (e: IllegalArgumentException) {
                message = e.message ?: "ফাইলটি ব্যাকআপ ফাইল নয়।"
            } catch (e: Exception) {
                message = "ফাইলটি পড়া যায়নি।"
            }
        }
    }

    ScreenColumn("ব্যাকআপ ও রিস্টোর", onBack) {
        InfoCard(
            title = "আপনার তথ্য ফাইলে রাখুন",
            body = "বুকমার্ক, নোট, আমল ও কাযা-খতমের হিসাব, তাসবিহ গণনা এবং নামাজের সেটিং একটি ফাইলে সংরক্ষণ হয়। " +
                "নতুন ফোনে সেই ফাইল থেকে ফেরত আনা যায়। প্রোফাইল-সেটিং (থিম, ভাষা, লেখার আকার) এতে থাকে না।"
        )
        Button(
            onClick = { create.launch("islam-shikhi-backup-${LocalDate.now()}.json") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("ব্যাকআপ তৈরি করুন") }
        OutlinedButton(
            onClick = { open.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("ফাইল থেকে ফেরত আনুন") }
        message?.let { Text(it) }
        InfoCard(
            title = "নিরাপত্তা",
            body = "ফাইলে আপনার ব্যক্তিগত নোট থাকে, তাই এটি নিরাপদ জায়গায় রাখুন। অ্যাপ এই ফাইল ইন্টারনেটে পাঠায় না; কোথায় রাখবেন তা আপনিই ঠিক করেন।"
        )
    }

    pending?.let { data ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("ফেরত আনবেন?") },
            text = { Text("ফাইলে ${DateFormatBn.digits(data.size)}টি তথ্য আছে। একই নামের বর্তমান তথ্য বদলে যাবে; বাকিগুলো যেমন আছে তেমনই থাকবে।") },
            confirmButton = {
                TextButton(onClick = {
                    val snapshot = data
                    pending = null
                    scope.launch {
                        message = try {
                            container.userData.restore(snapshot)
                            runCatching { container.scheduler.rescheduleNext() }
                            "তথ্য ফেরত আনা হয়েছে।"
                        } catch (e: Exception) {
                            "ফেরত আনা যায়নি।"
                        }
                    }
                }) { Text("ফেরত আনুন") }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("বাতিল") } }
        )
    }
}
