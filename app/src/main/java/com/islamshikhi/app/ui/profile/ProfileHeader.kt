package com.islamshikhi.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.cloud.CloudSync
import com.islamshikhi.app.ui.i18n.Text
import kotlinx.coroutines.launch

/**
 * প্রোফাইলের একদম উপরের অংশ: সাইন-ইন না থাকলে "অ্যাকাউন্ট খুলুন" ও "সাইন ইন" (দুটোই Google দিয়ে);
 * সাইন-ইন থাকলে নাম, ইমেইল ও ম্যানেজ-বাটন। সাইন-ইন সম্পূর্ণ ঐচ্ছিক; না করলেও অ্যাপের সবকিছু চলে।
 * Google-এ একই ধাপে নতুন অ্যাকাউন্ট খোলা ও পুরনোটিতে ঢোকা হয়, তাই দুই বাটনের কাজ এক।
 */
@Composable
fun ProfileHeader(onManage: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf(CloudSync.currentUser(context)) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var askConsent by remember { mutableStateOf(false) }
    val u = user

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (u != null) {
                Text(u.name?.takeIf { it.isNotBlank() } ?: "Google অ্যাকাউন্ট", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                u.email?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            if (u == null) {
                Button(
                    onClick = { askConsent = true },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) { Text("Google দিয়ে অ্যাকাউন্ট খুলুন") }
                OutlinedButton(
                    onClick = { askConsent = true },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) { Text("Google দিয়ে সাইন ইন করুন") }
            } else {
                OutlinedButton(onClick = onManage, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text("ক্লাউড ব্যাকআপ ও অ্যাকাউন্ট")
                }
            }
            if (busy) Text("অপেক্ষা করুন…", style = MaterialTheme.typography.bodySmall)
            message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }

    if (askConsent) {
        AlertDialog(
            onDismissRequest = { askConsent = false },
            title = { Text("সাইন ইনের আগে জেনে নিন") },
            text = {
                Text(
                    "সাইন ইন করলে আপনার নাম ও ইমেইল Google-এর Firebase সেবায় থাকবে। আপনি \"ক্লাউডে রাখুন\" চাপলে বুকমার্ক, নোট, আমল ও তাসবিহের হিসাব সেখানে জমা হবে। " +
                        "অন্য কেউ তা দেখতে পাবে না, বিজ্ঞাপনে ব্যবহার হবে না। যেকোনো সময় সব মুছে ফেলতে পারবেন।"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    askConsent = false
                    scope.launch {
                        busy = true; message = null
                        try {
                            user = CloudSync.signInWithGoogle(context)
                            message = "সাইন ইন হয়েছে।"
                        } catch (e: Exception) {
                            message = if (!CloudSync.isConfigured(context)) "ক্লাউড-সুবিধা এখনো চালু হয়নি।" else "সাইন ইন হয়নি। আবার চেষ্টা করুন।"
                        }
                        busy = false
                    }
                }) { Text("সম্মত, এগিয়ে যান") }
            },
            dismissButton = { TextButton(onClick = { askConsent = false }) { Text("বাতিল") } }
        )
    }
}
