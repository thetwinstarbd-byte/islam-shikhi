package com.islamshikhi.app.ui.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.data.content.QuizQuestion
import com.islamshikhi.app.ui.common.ContentGate
import com.islamshikhi.app.ui.common.ScreenHeader
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.ui.common.rememberLoaded
import com.islamshikhi.app.util.DateFormatBn
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val BEST_KEY = "quiz:best"
private const val ROUND_SIZE = 10

private class Shuffled(val q: QuizQuestion, val options: List<String>, val answer: Int)

private fun prepare(all: List<QuizQuestion>, seed: Long): List<Shuffled> =
    all.shuffled(Random(seed)).take(ROUND_SIZE).map { q ->
        val order = q.options.indices.shuffled(Random(seed + q.id.hashCode()))
        Shuffled(q, order.map { q.options[it] }, order.indexOf(q.answerIndex))
    }

/** দশটি এলোমেলো প্রশ্নের কুইজ। প্রতিটি উত্তরের পর ব্যাখ্যা ও সূত্র দেখায়; সর্বোচ্চ স্কোর ফোনে থাকে। */
@Composable
fun QuizScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val result = rememberLoaded { container.content.quiz() }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("ইসলামিক কুইজ", onBack)
        ContentGate(result) { all -> QuizRound(all) }
    }
}

@Composable
private fun QuizRound(all: List<QuizQuestion>) {
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val best by container.userData.int(BEST_KEY, -1).collectAsState(initial = -1)

    var seed by rememberSaveable { mutableLongStateOf(0L) }       // 0 = এখনও শুরু হয়নি
    var index by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var picked by rememberSaveable { mutableIntStateOf(-1) }

    val questions = remember(seed) { if (seed == 0L) emptyList() else prepare(all, seed) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            seed == 0L -> {
                Text(
                    "${DateFormatBn.digits(minOf(ROUND_SIZE, all.size))}টি প্রশ্ন। প্রতিটি উত্তরের পর ব্যাখ্যা ও সূত্র দেখানো হবে। প্রশ্নগুলো অ্যাপের যাচাই-করা কনটেন্ট থেকে নেওয়া।",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (best >= 0) Text("আপনার সর্বোচ্চ স্কোর: ${DateFormatBn.digits(best)}", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { seed = System.currentTimeMillis(); index = 0; score = 0; picked = -1 }) { Text("শুরু করুন") }
            }

            index >= questions.size -> {
                Text("কুইজ শেষ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "আপনার স্কোর: ${DateFormatBn.digits(score)}/${DateFormatBn.digits(questions.size)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(onClick = { seed = System.currentTimeMillis(); index = 0; score = 0; picked = -1 }) { Text("আবার খেলুন") }
                OutlinedButton(onClick = { seed = 0L }) { Text("শুরুর পাতায় যান") }
            }

            else -> {
                val cur = questions[index]
                Text(
                    "প্রশ্ন ${DateFormatBn.digits(index + 1)}/${DateFormatBn.digits(questions.size)}  •  ${cur.q.topic}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(cur.q.questionBn, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                cur.options.forEachIndexed { i, text ->
                    val answered = picked >= 0
                    val cardColor = when {
                        !answered -> MaterialTheme.colorScheme.surfaceVariant
                        i == cur.answer -> MaterialTheme.colorScheme.primaryContainer
                        i == picked -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (!answered) {
                                picked = i
                                if (i == cur.answer) score += 1
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }

                if (picked >= 0) {
                    Text(
                        if (picked == cur.answer) "সঠিক!" else "ভুল। সঠিক উত্তর: ${cur.options[cur.answer]}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(cur.q.explanationBn, style = MaterialTheme.typography.bodyMedium)
                    cur.q.sourceRef?.let { Text("সূত্র: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                    val last = index == questions.size - 1
                    Button(onClick = {
                        if (last && score > best) scope.launch { container.userData.putInt(BEST_KEY, score) }
                        picked = -1
                        index += 1
                    }) { Text(if (last) "ফলাফল দেখুন" else "পরের প্রশ্ন") }
                }
            }
        }
    }
}
