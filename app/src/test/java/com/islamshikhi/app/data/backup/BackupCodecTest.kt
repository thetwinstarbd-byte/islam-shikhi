package com.islamshikhi.app.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackupCodecTest {

    @Test fun roundTrip_keepsAllTypes() {
        val src: Map<String, Any> = mapOf(
            "note:dua:1" to "আমার নোট \"উদ্ধৃতি\"\nদ্বিতীয় লাইন",
            "tasbih" to 33,
            "big" to 5_000_000_000L,
            "flag" to true,
            "lat" to 23.8103,
            "bookmarks" to setOf("dua:a", "ayah:1:1"),
            "f" to 1.5f
        )
        val out = BackupCodec.decode(BackupCodec.encode(src, "2026-11-06T00:00:00Z"))
        assertEquals(src.keys, out.keys)
        assertEquals(src["note:dua:1"], out["note:dua:1"])
        assertEquals(33, out["tasbih"])
        assertEquals(5_000_000_000L, out["big"])
        assertEquals(true, out["flag"])
        assertEquals(23.8103, out["lat"] as Double, 0.0)
        assertEquals(setOf("dua:a", "ayah:1:1"), out["bookmarks"])
        assertEquals(1.5f, out["f"] as Float, 0f)
    }

    private fun assertRejected(json: String) {
        try {
            BackupCodec.decode(json)
            fail("প্রত্যাখ্যাত হওয়ার কথা ছিল: $json")
        } catch (e: IllegalArgumentException) {
            assertTrue(!e.message.isNullOrBlank())
        }
    }

    @Test fun decode_rejectsForeignOrBrokenFiles() {
        assertRejected("not json")
        assertRejected("""{"format":"other","version":1,"entries":[]}""")
        assertRejected("""{"format":"islam-shikhi-backup","version":99,"entries":[]}""")
        assertRejected("""{"format":"islam-shikhi-backup","version":1}""")
        assertRejected("""{"format":"islam-shikhi-backup","version":1,"entries":[{"k":"x","t":"zzz","v":1}]}""")
        assertRejected("""{"format":"islam-shikhi-backup","version":1,"entries":[{"k":"","t":"i","v":1}]}""")
    }

    @Test fun encode_isDeterministicAndSorted() {
        val a = BackupCodec.encode(mapOf("b" to 1, "a" to 2), "t")
        val b = BackupCodec.encode(mapOf("a" to 2, "b" to 1), "t")
        assertEquals(a, b)
    }
}
