package com.islamshikhi.app.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

/** কনটেন্ট ফাইল পড়ার অবস্থা: লোড হচ্ছে, তৈরি, অথবা ব্যর্থ। */
sealed class LoadResult<out T> {
    data object Loading : LoadResult<Nothing>()
    data object Failed : LoadResult<Nothing>()
    data class Ready<T>(val value: T) : LoadResult<T>()
}

/** [block] একবার চালিয়ে ফল রাখে। ফাইল পড়তে ব্যর্থ হলে অ্যাপ ক্র্যাশ না করে [LoadResult.Failed] দেয়। */
@Composable
fun <T> rememberLoaded(block: suspend () -> T): LoadResult<T> {
    val state = produceState<LoadResult<T>>(initialValue = LoadResult.Loading) {
        value = try {
            LoadResult.Ready(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Failed
        }
    }
    return state.value
}

/** [result]-এর অবস্থা অনুযায়ী বার্তা বা কনটেন্ট দেখায়। */
@Composable
fun <T> ContentGate(result: LoadResult<T>, content: @Composable (T) -> Unit) {
    when (result) {
        is LoadResult.Ready -> content(result.value)
        is LoadResult.Loading -> Text("লোড হচ্ছে…", modifier = Modifier.padding(20.dp))
        is LoadResult.Failed -> Text(
            "কনটেন্ট খোলা যায়নি। অ্যাপটি বন্ধ করে আবার খুলে দেখুন।",
            modifier = Modifier.padding(20.dp)
        )
    }
}
