package com.islamshikhi.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamshikhi.app.AppContainer
import com.islamshikhi.app.IslamShikhiApp
import com.islamshikhi.app.ui.effects.GoldOrnament
import com.islamshikhi.app.ui.effects.LuxCard
import com.islamshikhi.app.ui.effects.goldBorder
import com.islamshikhi.app.ui.theme.AmiriQuran

@Composable
fun rememberContainer(): AppContainer {
    val ctx = LocalContext.current
    return remember { (ctx.applicationContext as IslamShikhiApp).container }
}

/** উপরের শিরোনাম; [onBack] থাকলে সোনালি-রিংয়ের গোল পিছনে-বাটন দেখায়। নিচে ছোট সোনালি অলংকার। */
@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.goldBorder(CircleShape, alpha = 0.7f),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পিছনে")
                }
                Spacer(Modifier.width(12.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(6.dp))
        GoldOrnament(Modifier.padding(horizontal = 4.dp))
    }
}

/** শিরোনামসহ স্ক্রল-যোগ্য পাতা। */
@Composable
fun ScreenColumn(
    title: String,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title, onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

/** গোল আইকন-চিপ: সোনালি রিং ও গাঢ় পান্না ভরাটের ওপর সোনালি আইকন। তালিকা ও কার্ডে বিষয় চেনানোর জন্য। */
@Composable
fun IconChip(icon: ImageVector, size: Int = 44, tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondary,
             container: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer) {
    Box(
        modifier = Modifier.size(size.dp).background(container, CircleShape).goldBorder(CircleShape, alpha = 0.75f),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.48f).dp))
    }
}

@Composable
fun MenuCard(title: String, subtitle: String, icon: ImageVector? = null, onClick: () -> Unit) {
    LuxCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                IconChip(icon, size = 48)
                Spacer(Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )
}

/** যাচাই-অবস্থার ব্যাজ। docs/CONTENT_VERIFICATION.md-এর তিন অবস্থার বাংলা রূপ। */
@Composable
fun StatusBadge(status: String) {
    val (text, strong) = when (status) {
        "SCHOLAR_REVIEWED" -> "আলেম দ্বারা পর্যালোচিত" to true
        "SOURCE_CHECKED" -> "সূত্র মেলানো হয়েছে • আলেম দ্বারা পর্যালোচিত নয়" to false
        else -> "খসড়া • যাচাই বাকি" to false
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = if (strong) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (strong) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/** আরবি লেখা: ডান থেকে বামে, Amiri Quran ফন্টে, বড় অক্ষরে। */
@Composable
fun ArabicText(text: String, sizeSp: Int = 26) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.headlineSmall.copy(
            fontFamily = AmiriQuran,
            fontWeight = FontWeight.Normal,
            fontSize = sizeSp.sp,
            lineHeight = (sizeSp * 1.95f).sp,
            textDirection = TextDirection.Rtl
        ),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Start
    )
}

@Composable
fun InfoCard(title: String, body: String) {
    LuxCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** সতর্কতা/দ্রষ্টব্য: সোনালি রঙের হালকা পটভূমিতে তথ্যের আইকনসহ (লাল নয়, কারণ এটা বিপদ নয়, মনে করিয়ে দেওয়া)। */
@Composable
fun WarningCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 3.dp).size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/** নোট লেখার ডায়ালগ। */
@Composable
fun NoteDialog(initial: String, title: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("নোট শুধু আপনার ফোনে থাকে।", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = text, onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(), minLines = 3
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(text); onDismiss() }) { Text("সংরক্ষণ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}
