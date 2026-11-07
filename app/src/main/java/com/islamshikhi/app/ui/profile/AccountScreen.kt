package com.islamshikhi.app.ui.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.backup.BackupCodec
import com.islamshikhi.app.data.cloud.CloudLimits
import com.islamshikhi.app.data.cloud.CloudSync
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * অ্যাকাউন্ট ও ক্লাউড (ঐচ্ছিক): Google দিয়ে সাইন-ইন করে বুকমার্ক, নোট ও আমলের হিসাব ক্লাউডে রাখা ও নতুন ফোনে ফেরত আনা।
 * সাইন-ইন না করলেও অ্যাপের সবকিছু আগের মতোই চলে।
 */
@Composable
fun AccountScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val configured = remember { CloudSync.isConfigured(context) }
    var user by remember { mutableStateOf(CloudSync.currentUser(context)) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var askConsent by remember { mutableStateOf(false) }
    var askDelete by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf<Map<String, Any>?>(null) }

    fun run(errorText: String, block: suspend () -> Unit) {
        scope.launch {
            busy = true
            message = null
            try { block() } catch (e: Exception) { message = errorText }
            busy = false
        }
    }

    ScreenColumn("অ্যাকাউন্ট ও ক্লাউড", onBack) {
        if (!configured) {
            InfoCard(
                title = "ক্লাউড-সুবিধা শীঘ্রই আসছে",
                body = "এখন অ্যাপের সবকিছু আপনার ফোনেই চলছে। ফোন বদলালে \\"ব্যাকআপ ও রিস্টোর\\" থেকে ফাইলে তথ্য রেখে নিতে পারেন।"
            )
            return@ScreenColumn
        }
        InfoCard(
            title = "ঐচ্ছিক সুবিধা",
            body = "সাইন-ইন না করলেও অ্যাপের সব কিছু চলবে। সাইন-ইন করলে আপনার বুকমার্ক, নোট ও আমলের হিসাব ক্লাউডে রেখে নতুন ফোনে ফেরত আনতে পারবেন।"
        )
        val u = user
        if (u == null) {
            Button(
                onClick = { askConsent = true },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text("Google দিয়ে সাইন-ইন করুন") }
        } else {
            InfoCard(title = "সাইন-ইন করা আছে", body = listOfNotNull(u.name, u.email).joinToString("\n").ifBlank { "Google অ্যাকাউন্ট" })
            Button(
                onClick = {
                    run("ক্লাউডে রাখা যায়নি। ইন্টারনেট দেখে আবার চেষ্টা করুন।") {
                        val json = BackupCodec.encode(container.userData.snapshot(), Instant.now().toString())
                        if (!CloudLimits.fits(json)) { message = "তথ্য ক্লাউডে রাখার জন্য বেশি বড়। ফাইলে ব্যাকআপ নিন।"; return@run }
                        CloudSync.upload(u.uid, json)
                        message = "ক্লাউডে রাখা হয়েছে।"
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text("এখনই ক্লাউডে রাখুন") }
            OutlinedButton(
                onClick = {
                    run("ক্লাউড থেকে আনা যায়নি। ইন্টারনেট দেখে আবার চেষ্টা করুন।") {
                        val json = CloudSync.download(u.uid)
                        if (json == null) message = "ক্লাউডে এখনো কিছু রাখা হয়নি।"
                        else pendingRestore = BackupCodec.decode(json)
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) { Text("ক্লাউড থেকে ফেরত আনুন") }
            OutlinedButton(
                onClick = { run("সাইন-আউট করা যায়নি।") { CloudSync.signOut(context); user = null; message = "সাইন-আউট হয়েছে।" } },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("সাইন-আউট") }
            TextButton(onClick = { askDelete = true }, enabled = !busy) { Text("আমার অ্যাকাউন্ট ও ক্লাউডের তথ্য মুছুন") }
        }
        if (busy) Text("অপেক্ষা করুন…")
        message?.let { Text(it) }
    }

    if (askConsent) {
        AlertDialog(
            onDismissRequest = { askConsent = false },
            title = { Text("সাইন-ইনের আগে জেনে নিন") },
            text = {
                Text(
                    "সাইন-ইন করলে আপনার নাম ও ইমেইল Google-এর Firebase সেবায় থাকবে। আপনি \\"ক্লাউডে রাখুন\\" চাপলে বুকমার্ক, নোট, আমল ও তাসবিহের হিসাব সেখানে আপনার অ্যাকাউন্টের অধীনে জমা হবে। " +
                        "অন্য কেউ তা দেখতে পাবে না, বিজ্ঞাপনে ব্যবহার হবে না। যেকোনো সময় এখান থেকেই সব মুছে ফেলতে পারবেন।"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    askConsent = false
                    run("সাইন-ইন হয়নি। আবার চেষ্টা করুন।") {
                        user = CloudSync.signInWithGoogle(context)
                        message = "সাইন-ইন হয়েছে।"
                    }
                }) { Text("সম্মত, এগিয়ে যান") }
            },
            dismissButton = { TextButton(onClick = { askConsent = false }) { Text("বাতিল") } }
        )
    }

    if (askDelete) {
        AlertDialog(
            onDismissRequest = { askDelete = false },
            title = { Text("সব মুছে ফেলবেন?") },
            text = { Text("ক্লাউডে রাখা আপনার তথ্য ও এই অ্যাকাউন্ট মুছে যাবে। ফোনে থাকা তথ্য মুছবে না। এটি ফেরানো যাবে না।") },
            confirmButton = {
                TextButton(onClick = {
                    askDelete = false
                    val u = user
                    if (u != null) run("মোছা যায়নি। সাইন-আউট করে আবার সাইন-ইন করে চেষ্টা করুন।") {
                        CloudSync.deleteAccount(context, u.uid)
                        user = null
                        message = "অ্যাকাউন্ট ও ক্লাউডের তথ্য মুছে ফেলা হয়েছে।"
                    }
                }) { Text("মুছে ফেলুন") }
            },
            dismissButton = { TextButton(onClick = { askDelete = false }) { Text("বাতিল") } }
        )
    }

    pendingRestore?.let { data ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("ফেরত আনবেন?") },
            text = { Text("ক্লাউডে ${DateFormatBn.digits(data.size)}টি তথ্য আছে। একই নামের বর্তমান তথ্য বদলে যাবে; বাকিগুলো থাকবে।") },
            confirmButton = {
                TextButton(onClick = {
                    val snapshot = data
                    pendingRestore = null
                    run("ফেরত আনা যায়নি।") {
                        container.userData.restore(snapshot)
                        runCatching { container.scheduler.rescheduleNext() }
                        message = "তথ্য ফেরত আনা হয়েছে।"
                    }
                }) { Text("ফেরত আনুন") }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("বাতিল") } }
        )
    }
}
