package com.islamshikhi.app.util

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * অ্যাপ ক্র্যাশ করলে কারণটি ফোনের ভেতরে একটি ফাইলে রেখে দেয়। পরের বার অ্যাপ খুললে
 * [com.islamshikhi.app.MainActivity] সেই লেখা দেখায়, যেন আপনি কপি করে আমাকে পাঠাতে পারেন।
 * কিছু ইন্টারনেটে পাঠানো হয় না; লেখা শুধু ফোনেই থাকে।
 */
object CrashReporter {

    private fun file(context: Context) = File(context.applicationContext.filesDir, "last_crash.txt")

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                val trace = StringWriter().also { error.printStackTrace(PrintWriter(it)) }.toString()
                file(appContext).writeText(
                    "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}), ${Build.MANUFACTURER} ${Build.MODEL}\n" +
                        "Thread: ${thread.name}\n\n" + trace.take(6000)
                )
            }
            previous?.uncaughtException(thread, error)
        }
    }

    /** গত ক্র্যাশের লেখা, না থাকলে null। */
    fun read(context: Context): String? =
        runCatching { file(context).takeIf { it.exists() }?.readText() }.getOrNull()

    fun clear(context: Context) {
        runCatching { file(context).delete() }
    }
}
