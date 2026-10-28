package com.islamshikhi.app.ui.ibadah

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.islamshikhi.app.feature.prayer.PrayerSettings
import com.islamshikhi.app.feature.qibla.QiblaMath
import com.islamshikhi.app.ui.common.InfoCard
import com.islamshikhi.app.ui.common.ScreenColumn
import com.islamshikhi.app.ui.common.WarningCard
import com.islamshikhi.app.ui.common.rememberContainer
import com.islamshikhi.app.util.DateFormatBn
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val context = LocalContext.current
    val settings by container.prayerSettings.settings.collectAsState(initial = PrayerSettings())

    val qibla = QiblaMath.bearing(settings.lat, settings.lon)
    val distance = QiblaMath.distanceKm(settings.lat, settings.lon)
    val declination = remember(settings.lat, settings.lon) {
        GeomagneticField(settings.lat.toFloat(), settings.lon.toFloat(), 0f, System.currentTimeMillis()).declination
    }

    var heading by remember { mutableFloatStateOf(Float.NaN) }   // সত্যিকারের উত্তর থেকে ডিগ্রি
    var accuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE) }
    var hasSensor by remember { mutableFloatStateOf(1f) }

    DisposableEffect(declination) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensor == null) {
            hasSensor = 0f
            onDispose { }
        } else {
            val rotation = FloatArray(9)
            val orientation = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotation, event.values)
                    SensorManager.getOrientation(rotation, orientation)
                    val azimuthMagnetic = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    val azimuthTrue = (azimuthMagnetic + declination + 360f) % 360f
                    heading = if (heading.isNaN()) azimuthTrue else smoothAngle(heading, azimuthTrue, 0.15f)
                }
                override fun onAccuracyChanged(sensor: Sensor?, acc: Int) { accuracy = acc }
            }
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sm.unregisterListener(listener) }
        }
    }

    ScreenColumn("কিবলা", onBack) {
        InfoCard(
            title = "কাবার দিক",
            body = "${settings.placeLabelBn} থেকে কাবা উত্তর দিক থেকে ঘড়ির কাঁটার দিকে ${DateFormatBn.digits(qibla.roundToInt())}° " +
                "এবং দূরত্ব প্রায় ${DateFormatBn.digits(distance.roundToInt())} কিমি।"
        )

        if (hasSensor == 0f) {
            WarningCard("এই ফোনে কম্পাস সেন্সর নেই, তাই লাইভ কম্পাস চলবে না। ওপরের ডিগ্রি দেখে অন্য কম্পাস বা মানচিত্রের সাহায্যে দিক ঠিক করুন।")
        } else if (heading.isNaN()) {
            Text("কম্পাস চালু হচ্ছে…", style = MaterialTheme.typography.bodyMedium)
        } else {
            val arrow = (qibla.toFloat() - heading + 360f) % 360f
            val aligned = arrow < 5f || arrow > 355f
            Text(
                if (aligned) "আপনি কিবলার দিকে মুখ করে আছেন।" else "ফোন সমতলে ধরে ঘুরুন, যতক্ষণ না তীর ওপরের দিকে আসে।",
                style = MaterialTheme.typography.titleMedium
            )
            CompassDial(arrowAngle = arrow, northAngle = (360f - heading) % 360f)
        }

        if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW && hasSensor == 1f && !heading.isNaN()) {
            WarningCard("সেন্সরের নির্ভুলতা কম। ফোনটি আকাশে \"৮\" আকৃতিতে কয়েকবার ঘোরান এবং চুম্বক, ধাতব বস্তু ও ইলেকট্রনিক যন্ত্র থেকে দূরে রাখুন।")
        }

        WarningCard(
            "কম্পাস ফোনের সেন্সরের ওপর নির্ভর করে, তাই কয়েক ডিগ্রি ভুল হতে পারে (ধাতব বস্তু, কেস বা ঘরের কাঠামোর কারণেও)। " +
                "মসজিদে বা পরিচিত কিবলার দিকে একবার মিলিয়ে নিন। শহর ঠিক না থাকলে \"নামাজের সময়\" পাতা থেকে অবস্থান ঠিক করুন।"
        )
    }
}

/** দুটি কোণের মাঝে ধীরে ধীরে গড়ানো (০/৩৬০ সীমায় লাফ এড়িয়ে)। */
private fun smoothAngle(old: Float, new: Float, alpha: Float): Float {
    var diff = new - old
    while (diff > 180f) diff -= 360f
    while (diff < -180f) diff += 360f
    return (old + diff * alpha + 360f) % 360f
}

@Composable
private fun CompassDial(arrowAngle: Float, northAngle: Float) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth(0.85f).aspectRatio(1f)) {
            val r = size.minDimension / 2f
            val c = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = outline, radius = r, center = c, style = Stroke(width = 4f))
            // উত্তর চিহ্ন (লাল বিন্দু): ফোন ঘোরালে ঘোরে
            rotate(degrees = northAngle, pivot = c) {
                drawCircle(color = Color(0xFFB3261E), radius = r * 0.06f, center = Offset(c.x, c.y - r * 0.92f))
            }
            // কিবলার তীর: ওপরের দিকে এলে ঠিক দিক
            rotate(degrees = arrowAngle, pivot = c) {
                val path = Path().apply {
                    moveTo(c.x, c.y - r * 0.78f)
                    lineTo(c.x - r * 0.14f, c.y + r * 0.1f)
                    lineTo(c.x, c.y - r * 0.02f)
                    lineTo(c.x + r * 0.14f, c.y + r * 0.1f)
                    close()
                }
                drawPath(path, color = primary)
            }
            drawCircle(color = onSurface, radius = r * 0.03f, center = c)
        }
        Text("কিবলা", modifier = Modifier.align(Alignment.TopCenter), style = MaterialTheme.typography.labelLarge)
    }
    Text("লাল বিন্দু = উত্তর দিক। সবুজ তীর যখন ওপরের দিকে, তখন সেটিই কিবলা।", style = MaterialTheme.typography.bodySmall)
}
