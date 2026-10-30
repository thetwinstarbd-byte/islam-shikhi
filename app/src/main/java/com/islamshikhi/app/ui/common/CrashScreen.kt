package com.islamshikhi.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** গত বার অ্যাপ ক্র্যাশ করলে সেটার কারণ দেখায়, যেন ব্যবহারকারী কপি করে ডেভেলপারকে পাঠাতে পারেন। */
@Composable
fun CrashScreen(report: String, onContinue: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("গত বার অ্যাপ বন্ধ হয়ে গিয়েছিল", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "নিচের লেখাটি কপি করে ডেভেলপারকে পাঠালে সমস্যাটি ঠিক করা যাবে। লেখাটি শুধু এই ফোনেই আছে, কোথাও পাঠানো হয়নি।",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { clipboard.setText(AnnotatedString(report)) }, modifier = Modifier.fillMaxWidth()) {
                Text("লেখাটি কপি করুন")
            }
            OutlinedButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text("মুছে অ্যাপে যান")
            }
            SelectionContainer {
                Text(report, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    }
}
