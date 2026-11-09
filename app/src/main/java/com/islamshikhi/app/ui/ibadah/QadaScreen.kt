package com.islamshikhi.app.ui.ibadah

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.islamshikhi.app.ui.i18n.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.islamshikhi.app.feature.tracker.QadaItem
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.effects.LuxCard
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch

/** কাযা নামাজ ও রোযার হিসাব: বাকি সংখ্যা রেখে একে একে কমানো। শুধু ফোনে সংরক্ষিত। */
@Composable
fun QadaScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val prefs by container.userData.data.collectAsState(initial = emptyPreferences())
    var editing by remember { mutableStateOf<QadaItem?>(null) }

    fun countOf(item: QadaItem): Int = prefs[intPreferencesKey(item.key)] ?: 0
    val total = QadaItem.entries.sumOf { countOf(it) }

    ScreenColumn("কাযা নামাজ ও রোযা", onBack) {
        InfoCard(
            title = "মোট বাকি: ${DateFormatBn.digits(total)}",
            body = "যে সংখ্যা বাকি আছে তা বসান। একটি আদায় করলে \"আদায় −১\" চাপুন; সংখ্যা এক করে কমবে।"
        )
        QadaItem.entries.forEach { item ->
            val count = countOf(item)
            LuxCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.labelBn, style = MaterialTheme.typography.titleMedium)
                        Text(
                            DateFormatBn.digits(count),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { scope.launch { container.userData.incrementInt(item.key, -1) } },
                            enabled = count > 0,
                            modifier = Modifier.weight(1f)
                        ) { Text("আদায় −১") }
                        OutlinedButton(
                            onClick = { scope.launch { container.userData.incrementInt(item.key, 1) } },
                            modifier = Modifier.weight(1f)
                        ) { Text("+১") }
                        OutlinedButton(
                            onClick = { scope.launch { container.userData.incrementInt(item.key, 10) } },
                            modifier = Modifier.weight(1f)
                        ) { Text("+১০") }
                    }
                    TextButton(onClick = { editing = item }) { Text("সংখ্যা বসান") }
                }
            }
        }
    }

    editing?.let { item ->
        var text by remember(item) { mutableStateOf(countOf(item).toString()) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("${item.labelBn}: মোট বাকি") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { v -> text = v.filter { it.isDigit() }.take(6) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val n = text.toIntOrNull() ?: 0
                    scope.launch { container.userData.putInt(item.key, n) }
                    editing = null
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("বাতিল") } }
        )
    }
}
