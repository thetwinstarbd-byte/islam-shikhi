package com.islamshikhi.app.feature.prayer

import com.batoulapps.adhan2.CalculationMethod

enum class Prayer(val id: String, val labelBn: String, val isObligation: Boolean) {
    FAJR("fajr", "ফজর", true),
    SUNRISE("sunrise", "সূর্যোদয়", false),
    DHUHR("dhuhr", "যোহর", true),
    ASR("asr", "আসর", true),
    MAGHRIB("maghrib", "মাগরিব", true),
    ISHA("isha", "এশা", true);

    companion object {
        val obligations: List<Prayer> = entries.filter { it.isObligation }
        fun byId(id: String): Prayer? = entries.firstOrNull { it.id == id }
    }
}

data class City(val id: String, val nameBn: String, val lat: Double, val lon: Double)

object Cities {
    const val DEFAULT_ID = "dhaka"

    val all: List<City> = listOf(
        City("dhaka", "ঢাকা", 23.8103, 90.4125),
        City("chattogram", "চট্টগ্রাম", 22.3569, 91.7832),
        City("rajshahi", "রাজশাহী", 24.3745, 88.6042),
        City("khulna", "খুলনা", 22.8456, 89.5403),
        City("barishal", "বরিশাল", 22.7010, 90.3535),
        City("sylhet", "সিলেট", 24.8949, 91.8687),
        City("rangpur", "রংপুর", 25.7439, 89.2752),
        City("mymensingh", "ময়মনসিংহ", 24.7471, 90.4203),
        City("cumilla", "কুমিল্লা", 23.4607, 91.1809),
        City("narayanganj", "নারায়ণগঞ্জ", 23.6238, 90.5000),
        City("gazipur", "গাজীপুর", 23.9999, 90.4203),
        City("bogura", "বগুড়া", 24.8465, 89.3773),
        City("jashore", "যশোর", 23.1667, 89.2167),
        City("coxsbazar", "কক্সবাজার", 21.4272, 92.0058),
        City("makkah", "মক্কা", 21.4225, 39.8262),
        City("madinah", "মদিনা", 24.4672, 39.6111),
        City("riyadh", "রিয়াদ", 24.7136, 46.6753),
        City("dubai", "দুবাই", 25.2048, 55.2708),
        City("kualalumpur", "কুয়ালালামপুর", 3.1390, 101.6869),
        City("london", "লন্ডন", 51.5074, -0.1278),
        City("newyork", "নিউ ইয়র্ক", 40.7128, -74.0060)
    )

    fun byId(id: String?): City = all.firstOrNull { it.id == id } ?: all.first { it.id == DEFAULT_ID }
}

/** গণনা পদ্ধতি। অ্যাপে এগুলোর নাম বাংলায় দেখানো হয়। */
enum class CalcMethodOption(val id: String, val labelBn: String, val noteBn: String, val adhan: CalculationMethod) {
    KARACHI("KARACHI", "করাচি (ইউনিভার্সিটি অব ইসলামিক সায়েন্সেস)", "দক্ষিণ এশিয়ায় বহুল প্রচলিত", CalculationMethod.KARACHI),
    MWL("MWL", "মুসলিম ওয়ার্ল্ড লীগ", "ইউরোপ, দূর প্রাচ্য ও আমেরিকার কিছু অংশে প্রচলিত", CalculationMethod.MUSLIM_WORLD_LEAGUE),
    EGYPTIAN("EGYPTIAN", "মিশরীয় সাধারণ জরিপ কর্তৃপক্ষ", "আফ্রিকা, সিরিয়া, লেবানন, মালয়েশিয়ায় প্রচলিত", CalculationMethod.EGYPTIAN),
    UMM_AL_QURA("UMM_AL_QURA", "উম্মুল কুরা (মক্কা)", "আরব উপদ্বীপে প্রচলিত", CalculationMethod.UMM_AL_QURA),
    ISNA("ISNA", "উত্তর আমেরিকা (ISNA)", "যুক্তরাষ্ট্র ও কানাডায় প্রচলিত", CalculationMethod.NORTH_AMERICA);

    companion object {
        fun byId(id: String?): CalcMethodOption = entries.firstOrNull { it.id == id } ?: KARACHI
    }
}

data class PrayerSettings(
    val cityId: String = Cities.DEFAULT_ID,
    val useGps: Boolean = false,
    val gpsLat: Double? = null,
    val gpsLon: Double? = null,
    val method: CalcMethodOption = CalcMethodOption.KARACHI,
    val hanafiAsr: Boolean = true,
    val notify: Set<String> = Prayer.obligations.map { it.id }.toSet()
) {
    val city: City get() = Cities.byId(cityId)

    /** গণনার স্থানাঙ্ক: GPS চালু ও পাওয়া গেলে সেটি, নইলে নির্বাচিত শহর। */
    val lat: Double get() = if (useGps && gpsLat != null) gpsLat else city.lat
    val lon: Double get() = if (useGps && gpsLon != null) gpsLon else city.lon
    val placeLabelBn: String get() = if (useGps && gpsLat != null && gpsLon != null) "আপনার বর্তমান অবস্থান" else city.nameBn
}
