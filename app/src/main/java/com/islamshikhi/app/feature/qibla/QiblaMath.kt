package com.islamshikhi.app.feature.qibla

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** কাবার দিক ও দূরত্ব গণনা (বৃহৎ বৃত্ত বা great-circle সূত্র)। */
object QiblaMath {
    const val KAABA_LAT = 21.4225
    const val KAABA_LON = 39.8262
    private const val EARTH_RADIUS_KM = 6371.0

    /** উত্তর থেকে ঘড়ির কাঁটার দিকে ডিগ্রি (০–৩৬০) — কাবার দিকের প্রকৃত (true north) বেয়ারিং। */
    fun bearing(lat: Double, lon: Double): Double {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val dLon = Math.toRadians(KAABA_LON - lon)
        val y = sin(dLon) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    fun distanceKm(lat: Double, lon: Double): Double {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val dPhi = phi2 - phi1
        val dLam = Math.toRadians(KAABA_LON - lon)
        val a = sin(dPhi / 2) * sin(dPhi / 2) + cos(phi1) * cos(phi2) * sin(dLam / 2) * sin(dLam / 2)
        return 2 * EARTH_RADIUS_KM * atan2(sqrt(a), sqrt(1 - a))
    }
}
