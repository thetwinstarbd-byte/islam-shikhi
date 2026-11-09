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
        City("narsingdi", "নরসিংদী", 23.9322, 90.7151),
        City("manikganj", "মানিকগঞ্জ", 23.8617, 90.0003),
        City("munshiganj", "মুন্সিগঞ্জ", 23.5422, 90.5305),
        City("tangail", "টাঙ্গাইল", 24.2513, 89.9167),
        City("kishoreganj", "কিশোরগঞ্জ", 24.4449, 90.7766),
        City("faridpur", "ফরিদপুর", 23.6070, 89.8429),
        City("gopalganj", "গোপালগঞ্জ", 23.0050, 89.8266),
        City("madaripur", "মাদারীপুর", 23.1641, 90.1896),
        City("rajbari", "রাজবাড়ী", 23.7574, 89.6446),
        City("shariatpur", "শরীয়তপুর", 23.2423, 90.4348),
        City("brahmanbaria", "ব্রাহ্মণবাড়িয়া", 23.9608, 91.1115),
        City("chandpur", "চাঁদপুর", 23.2333, 90.6713),
        City("feni", "ফেনী", 23.0159, 91.3976),
        City("noakhali", "নোয়াখালী", 22.8696, 91.0995),
        City("lakshmipur", "লক্ষ্মীপুর", 22.9425, 90.8412),
        City("khagrachhari", "খাগড়াছড়ি", 23.1193, 91.9847),
        City("rangamati", "রাঙ্গামাটি", 22.7324, 92.2985),
        City("bandarban", "বান্দরবান", 22.1953, 92.2184),
        City("moulvibazar", "মৌলভীবাজার", 24.4829, 91.7774),
        City("habiganj", "হবিগঞ্জ", 24.3745, 91.4156),
        City("sunamganj", "সুনামগঞ্জ", 25.0715, 91.3992),
        City("joypurhat", "জয়পুরহাট", 25.0968, 89.0227),
        City("naogaon", "নওগাঁ", 24.7936, 88.9318),
        City("natore", "নাটোর", 24.4206, 89.0000),
        City("chapainawabganj", "চাঁপাইনবাবগঞ্জ", 24.5965, 88.2775),
        City("pabna", "পাবনা", 24.0064, 89.2372),
        City("sirajganj", "সিরাজগঞ্জ", 24.4534, 89.7007),
        City("satkhira", "সাতক্ষীরা", 22.7185, 89.0705),
        City("bagerhat", "বাগেরহাট", 22.6516, 89.7860),
        City("narail", "নড়াইল", 23.1725, 89.5127),
        City("magura", "মাগুরা", 23.4873, 89.4199),
        City("jhenaidah", "ঝিনাইদহ", 23.5448, 89.1539),
        City("kushtia", "কুষ্টিয়া", 23.9013, 89.1205),
        City("chuadanga", "চুয়াডাঙ্গা", 23.6402, 88.8418),
        City("meherpur", "মেহেরপুর", 23.7622, 88.6318),
        City("bhola", "ভোলা", 22.6859, 90.6482),
        City("patuakhali", "পটুয়াখালী", 22.3596, 90.3299),
        City("pirojpur", "পিরোজপুর", 22.5841, 89.9720),
        City("jhalokathi", "ঝালকাঠি", 22.6406, 90.1987),
        City("barguna", "বরগুনা", 22.1592, 90.1121),
        City("dinajpur", "দিনাজপুর", 25.6217, 88.6354),
        City("thakurgaon", "ঠাকুরগাঁও", 26.0337, 88.4616),
        City("panchagarh", "পঞ্চগড়", 26.3411, 88.5541),
        City("nilphamari", "নীলফামারী", 25.9310, 88.8560),
        City("lalmonirhat", "লালমনিরহাট", 25.9923, 89.2847),
        City("kurigram", "কুড়িগ্রাম", 25.8054, 89.6361),
        City("gaibandha", "গাইবান্ধা", 25.3297, 89.5430),
        City("jamalpur", "জামালপুর", 24.9375, 89.9372),
        City("sherpur", "শেরপুর", 25.0204, 90.0152),
        City("netrokona", "নেত্রকোনা", 24.8700, 90.7279),
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
    val notify: Set<String> = Prayer.obligations.map { it.id }.toSet(),
    /** false: ফোনের হিসাব করা ডিফল্ট সময়। true: ব্যবহারকারীর বসানো আজান/জামাতের সময় (যেগুলো বসানো নেই সেগুলো ডিফল্ট)। */
    val useCustom: Boolean = false,
    /** ওয়াক্তের id -> দিনের শুরু থেকে মিনিট (০–১৪৩৯)। */
    val adhanMin: Map<String, Int> = emptyMap(),
    val jamaatMin: Map<String, Int> = emptyMap()
) {
    val city: City get() = Cities.byId(cityId)

    /** গণনার স্থানাঙ্ক: GPS চালু ও পাওয়া গেলে সেটি, নইলে নির্বাচিত শহর। */
    val lat: Double get() = if (useGps && gpsLat != null) gpsLat else city.lat
    val lon: Double get() = if (useGps && gpsLon != null) gpsLon else city.lon
    val placeLabelBn: String get() = if (useGps && gpsLat != null && gpsLon != null) "আপনার বর্তমান অবস্থান" else city.nameBn
}

/** ব্যবহারকারীর বসানো সময় সংরক্ষণের সহায়ক: "fajr=330,dhuhr=750" আকারে। */
object CustomTimes {
    fun encode(m: Map<String, Int>): String = m.entries.sortedBy { it.key }.joinToString(",") { "${it.key}=${it.value}" }

    fun decode(s: String?): Map<String, Int> = s.orEmpty().split(",").mapNotNull { part ->
        val kv = part.split("=")
        if (kv.size != 2) return@mapNotNull null
        val v = kv[1].trim().toIntOrNull()?.takeIf { it in 0..1439 } ?: return@mapNotNull null
        val id = kv[0].trim().takeIf { Prayer.byId(it)?.isObligation == true } ?: return@mapNotNull null
        id to v
    }.toMap()
}
