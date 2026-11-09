package com.islamshikhi.app.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.report.ReportSender
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.i18n.Text
import kotlinx.coroutines.launch

private val CATEGORIES = listOf("কনটেন্টে ভুল (দোয়া, আয়াত, মাসআলা, প্রবন্ধ)", "নামাজের সময়ে ভুল", "অ্যাপে সমস্যা", "অন্য কিছু")

/** "ভুল জানান": লিখুন, চাইলে একটি ছবি/ফাইল দিন, একটি বাটনে পাঠান — ইমেইল ডেভেলপারের কাছে নিজে চলে যায়। */
@Composable
fun ReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var category by remember { mutableStateOf(CATEGORIES[0]) }
    var message by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }
    val configured = remember { ReportSender.isConfigured() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val att = try { ReportSender.readAttachment(context, uri) } catch (e: Exception) { null }
            if (att == null) { status = "ফাইলটি খোলা যায়নি বা ৪ MB-এর বেশি বড়।" }
            else { fileUri = uri; fileName = att.name; status = null }
        }
    }

    ScreenColumn("ভুল জানান", onBack) {
        if (!configured) {
            InfoCard(
                title = "শীঘ্রই আসছে",
                body = "এই সুবিধা এখনো চালু করা হয়নি। কিছুদিনের মধ্যে চালু হবে।"
            )
            return@ScreenColumn
        }
        if (done) {
            InfoCard(title = "ধন্যবাদ", body = status ?: "আপনার বার্তা পাঠানো হয়েছে।")
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("ঠিক আছে") }
            return@ScreenColumn
        }
        Text("কোনো ভুল চোখে পড়লে এখানে জানান। আমরা দেখে ঠিক করব।", style = MaterialTheme.typography.bodyMedium)
        Text("কোন ধরনের ভুল?", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CATEGORIES.forEach { c ->
                FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) }, modifier = Modifier.fillMaxWidth())
            }
        }
        OutlinedTextField(
            value = message, onValueChange = { if (it.length <= ReportSender.MAX_TEXT) message = it },
            label = { Text("কী ভুল পেয়েছেন? (কোন পাতায়, কী লেখা আছে, কী হওয়া উচিত)") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp).padding(top = 8.dp)
        )
        OutlinedButton(onClick = { picker.launch("*/*") }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text(if (fileName == null) "ছবি বা ফাইল যোগ করুন (ঐচ্ছিক)" else "ফাইল: $fileName")
        }
        if (fileName != null) TextButton(onClick = { fileUri = null; fileName = null }) { Text("ফাইল সরান") }
        OutlinedTextField(
            value = contact, onValueChange = { contact = it.take(120) },
            label = { Text("আপনার ফোন বা ইমেইল (চাইলে, উত্তর পেতে)") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "আপনার লেখা ও ফাইল শুধু ডেভেলপারের কাছে ইমেইলে যাবে। অ্যাপের সংস্করণ ও ফোনের মডেল সঙ্গে যায়, আর কোনো ব্যক্তিগত তথ্য নয়।",
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = {
                scope.launch {
                    busy = true; status = null
                    when (val r = ReportSender.send(context, category, message, contact, fileUri)) {
                        ReportSender.Result.Sent -> { status = "আপনার বার্তা পাঠানো হয়েছে।"; done = true }
                        ReportSender.Result.Queued -> { status = "এখন ইন্টারনেট পাওয়া যায়নি। বার্তাটি ফোনে রাখা হলো; ইন্টারনেট পেলে অ্যাপ নিজেই পাঠিয়ে দেবে।"; done = true }
                        ReportSender.Result.NotConfigured -> status = "এই সুবিধা এখনো চালু হয়নি।"
                        is ReportSender.Result.Invalid -> status = if (r.reason == "file") "ফাইলটি পড়া যায়নি বা বেশি বড়।" else "কিছু লিখুন বা একটি ফাইল দিন।"
                    }
                    busy = false
                }
            },
            enabled = !busy && (message.isNotBlank() || fileUri != null),
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
        ) { Text(if (busy) "পাঠানো হচ্ছে…" else "পাঠান") }
        status?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}
