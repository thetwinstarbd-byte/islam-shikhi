package com.islamshikhi.app.feature.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Reciter(val id: String, val nameBn: String, val folder: String)

/**
 * আয়াত ধরে ধরে তিলাওয়াত স্ট্রিম করে (EveryAyah-এর সরাসরি লিংক)। ফোনে কিছু সংরক্ষণ বা পুনর্বিতরণ হয় না।
 * ইন্টারনেট না থাকলে/ত্রুটি হলে [error] সেট হয়; বাকি অ্যাপ অফলাইনেই চলে।
 */
class RecitationController {
    companion object {
        val reciters = listOf(
            Reciter("alafasy", "মিশারি রাশিদ আল-আফাসি", "Alafasy_128kbps"),
            Reciter("husary", "মাহমুদ খলিল আল-হুসারি", "Husary_128kbps"),
            Reciter("basit", "আবদুল বাসিত (মুরাত্তাল)", "Abdul_Basit_Murattal_192kbps"),
            Reciter("sudais", "আবদুর রহমান আস-সুদাইস", "Abdurrahmaan_As-Sudais_192kbps")
        )
        fun url(folder: String, chapter: Int, ayah: Int) =
            "https://everyayah.com/data/$folder/%03d%03d.mp3".format(chapter, ayah)
    }

    /** বর্তমানে চলা/লোড হওয়া আয়াত নম্বর; চলছে না হলে null। */
    var current by mutableStateOf<Int?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var player: MediaPlayer? = null
    private var chapter = 0
    private var total = 0
    private var folder = reciters[0].folder
    private var continuous = false
    private var token = 0

    fun play(chapter: Int, ayah: Int, total: Int, folder: String, continuous: Boolean) {
        release()
        this.chapter = chapter; this.total = total; this.folder = folder; this.continuous = continuous
        start(ayah)
    }

    private fun start(ayah: Int) {
        val my = ++token
        error = null
        current = ayah
        loading = true
        try {
            val mp = MediaPlayer()
            player = mp
            mp.setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).setUsage(AudioAttributes.USAGE_MEDIA).build()
            )
            mp.setDataSource(url(folder, chapter, ayah))
            mp.setOnPreparedListener { if (my == token) { loading = false; it.start() } }
            mp.setOnCompletionListener {
                if (my != token) return@setOnCompletionListener
                if (continuous && ayah < total) { dispose(); start(ayah + 1) } else stop()
            }
            mp.setOnErrorListener { _, _, _ ->
                if (my == token) { fail() }
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            fail()
        }
    }

    private fun fail() {
        dispose()
        current = null
        loading = false
        error = "তিলাওয়াত চালানো যায়নি — ইন্টারনেট সংযোগ দেখুন।"
    }

    private fun dispose() {
        player?.let { runCatching { it.setOnCompletionListener(null); it.setOnErrorListener(null); it.release() } }
        player = null
    }

    fun stop() {
        token++
        dispose()
        current = null
        loading = false
    }

    fun release() { stop() }
}
