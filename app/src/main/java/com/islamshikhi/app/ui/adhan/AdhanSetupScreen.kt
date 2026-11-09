package com.islamshikhi.app.ui.adhan

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import com.islamshikhi.app.ui.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.islamshikhi.app.feature.adhan.AdhanPrefs
import com.islamshikhi.app.feature.adhan.AdhanService
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.rememberContainer
import kotlinx.coroutines.launch

/** যেসব ফোন-ব্র্যান্ডে আলাদা "অটো-স্টার্ট" অনুমতি থাকে। */
private val AUTOSTART_BRANDS = setOf("xiaomi", "redmi", "poco", "oppo", "realme", "oneplus", "vivo", "iqoo", "huawei", "honor", "tecno", "infinix", "itel", "asus")

/** ব্র্যান্ড অনুযায়ী অটো-স্টার্ট পাতার সম্ভাব্য ঠিকানা; কোনোটি না মিললে অ্যাপের সাধারণ তথ্য-পাতা। */
private fun autostartIntents(): List<Intent> = listOf(
    ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
    ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
    ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
    ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
    ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
    ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
    ComponentName("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity"),
    ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity")
).map { Intent().setComponent(it) }

private fun openAutostart(context: Context) {
    for (i in autostartIntents()) {
        if (i.resolveActivity(context.packageManager) != null) {
            try { context.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return } catch (_: Exception) {}
        }
    }
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

private fun requestIgnoreBattery(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

@Composable
private fun StatusCard(ok: Boolean, title: String, body: String, actionLabel: String?, onAction: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    if (ok) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = if (ok) "ঠিক আছে" else "ব্যবস্থা দরকার",
                    tint = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(body, style = MaterialTheme.typography.bodyMedium)
            if (!ok && actionLabel != null) {
                Button(onClick = onAction, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(actionLabel) }
            }
        }
    }
}

/**
 * আজান ও অনুমতি: আজান ঠিক সময়ে বাজার জন্য দরকারি ব্যবস্থাগুলো এক জায়গায়, প্রতিটির অবস্থাসহ।
 * অনুমতি চাওয়ার আগে কেন লাগে তা লেখা আছে; ব্যবহারকারী না দিলেও অ্যাপ চলে (শুধু আজান দেরিতে বা না বাজতে পারে)।
 */
@Composable
fun AdhanSetupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val adhanOn by container.userData.bool(AdhanPrefs.KEY_ON, true).collectAsState(initial = true)
    val autostartDone by container.userData.bool(AdhanPrefs.KEY_AUTOSTART_DONE, false).collectAsState(initial = false)

    // সেটিং-পাতা থেকে ফিরলে অবস্থা আবার দেখা
    var tick by remember { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) tick++ }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { tick++ }

    @Suppress("UNUSED_VARIABLE") val stateVersion = tick
    val notifOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val batteryOk = pm.isIgnoringBatteryOptimizations(context.packageName)
    val brand = Build.MANUFACTURER.lowercase()
    val needsAutostart = AUTOSTART_BRANDS.any { brand.contains(it) }

    ScreenColumn("আজান ও অনুমতি", onBack) {
        InfoCard(
            title = "আজান ঠিক সময়ে বাজানোর জন্য",
            body = "ব্যাটারি-সেভার চালু থাকলে অনেক ফোন অ্যাপকে ঘুম পাড়িয়ে দেয়, তখন আজান দেরিতে বাজে বা বাজেই না। নিচের ব্যবস্থাগুলো একবার করে নিলে এই সমস্যা হয় না। এই অনুমতিগুলো শুধু আজান ও রিমাইন্ডারের জন্য ব্যবহার হয়।"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("নামাজের সময় আজান বাজবে", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Switch(checked = adhanOn, onCheckedChange = { v -> scope.launch { container.userData.putBool(AdhanPrefs.KEY_ON, v) } })
        }
        StatusCard(
            ok = notifOk,
            title = "নোটিফিকেশন",
            body = if (notifOk) "চালু আছে।" else "আজান ও নামাজের খবর দেখাতে নোটিফিকেশনের অনুমতি লাগে।",
            actionLabel = "অনুমতি দিন",
            onAction = { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        )
        StatusCard(
            ok = batteryOk,
            title = "ব্যাটারি-সীমা থেকে অ্যাপকে মুক্ত রাখুন",
            body = if (batteryOk) "অ্যাপ ব্যাটারি-সীমা থেকে মুক্ত আছে।"
            else "অনুরোধ এলে \"অনুমতি দিন\" বা \"Allow\" চাপুন। এতে ব্যাটারি-সেভারেও আজান ঠিক সময়ে বাজবে।",
            actionLabel = "ব্যবস্থা করুন",
            onAction = { requestIgnoreBattery(context) }
        )
        if (needsAutostart) {
            StatusCard(
                ok = autostartDone,
                title = "অটো-স্টার্ট চালু করুন",
                body = "আপনার ফোনে (${Build.MANUFACTURER}) অ্যাপকে নিজে চালু হতে দেওয়ার আলাদা অনুমতি থাকে। খোলা পাতায় \"ইসলাম শিখি\"-এর পাশে সুইচ চালু করে ফিরে এসে নিচের বাটন চাপুন।",
                actionLabel = "অটো-স্টার্টের পাতা খুলুন",
                onAction = { openAutostart(context) }
            )
            if (!autostartDone) {
                OutlinedButton(
                    onClick = { scope.launch { container.userData.putBool(AdhanPrefs.KEY_AUTOSTART_DONE, true) } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("আমি চালু করেছি") }
            }
        }
        InfoCard(
            title = "ভলিউম",
            body = "আজান ফোনের অ্যালার্ম-ভলিউমে বাজে। অ্যালার্ম-ভলিউম বন্ধ থাকলে শোনা যাবে না। নিচে শুনে দেখুন।"
        )
        Button(
            onClick = { AdhanService.start(context, "fajr", preview = true) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text("ফজরের আজান শুনে দেখুন") }
        Button(
            onClick = { AdhanService.start(context, "dhuhr", preview = true) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text("অন্য ওয়াক্তের আজান শুনে দেখুন") }
        OutlinedButton(onClick = { AdhanService.stop(context) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("আজান থামান") }
    }
}
