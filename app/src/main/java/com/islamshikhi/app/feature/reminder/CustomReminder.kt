package com.islamshikhi.app.feature.reminder

import org.json.JSONObject

/** ব্যবহারকারীর নিজের তৈরি দৈনিক রিমাইন্ডার (যেমন: কুরআন পড়া, সকালের যিকির)। */
data class CustomReminder(
    val id: String,
    val label: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true
) {
    fun toJson(): String = JSONObject()
        .put("id", id).put("label", label).put("hour", hour).put("minute", minute).put("enabled", enabled)
        .toString()

    companion object {
        fun fromJson(raw: String): CustomReminder? = runCatching {
            val o = JSONObject(raw)
            CustomReminder(o.getString("id"), o.getString("label"), o.getInt("hour"), o.getInt("minute"), o.optBoolean("enabled", true))
        }.getOrNull()
    }
}
