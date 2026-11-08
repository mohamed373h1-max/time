package com.example.util

import java.util.Calendar
import java.util.Locale
import kotlin.math.*

data class Governorate(
    val id: String,
    val nameAr: String,
    val lat: Double,
    val lng: Double,
    val timezone: Double = 2.0 // Egypt Standard Time (UTC+2 or UTC+3 during DST)
)

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val timeRemaining: String
)

object PrayerTimesCalculator {
    val egyptianGovernorates = listOf(
        Governorate("cairo", "القاهرة", 30.0444, 31.2357),
        Governorate("alexandria", "الإسكندرية", 31.2001, 29.9187),
        Governorate("giza", "الجيزة", 30.0131, 31.2089),
        Governorate("qalyubia", "القليوبية (بنها)", 30.4660, 31.1853),
        Governorate("sharqia", "الشرقية (الزقازيق)", 30.5877, 31.5020),
        Governorate("dakahlia", "الدقهلية (المنصورة)", 31.0409, 31.3785),
        Governorate("monufia", "المنوفية (شبين الكوم)", 30.5526, 31.0094),
        Governorate("gharbia", "الغربية (طنطا)", 30.7865, 31.0004),
        Governorate("kafr_el_sheikh", "كفر الشيخ", 31.1107, 30.9388),
        Governorate("damietta", "دمياط", 31.4175, 31.8144),
        Governorate("port_said", "بورسعيد", 31.2653, 32.3019),
        Governorate("ismailia", "الإسماعيلية", 30.6043, 32.2723),
        Governorate("suez", "السويس", 29.9668, 32.5498),
        Governorate("beheira", "البحيرة (دمنهور)", 31.0434, 30.4700),
        Governorate("faiyum", "الفيوم", 29.3084, 30.8428),
        Governorate("beni_suef", "بني سويف", 29.0661, 31.0994),
        Governorate("minya", "المنيا", 28.0871, 30.7618),
        Governorate("assiut", "أسيوط", 27.1801, 31.1837),
        Governorate("sohag", "سوهاج", 26.5590, 31.6957),
        Governorate("qena", "قنا", 26.1551, 32.7160),
        Governorate("luxor", "الأقصر", 25.6872, 32.6396),
        Governorate("aswan", "أسوان", 24.0889, 32.8998),
        Governorate("red_sea", "البحر الأحمر (الغردقة)", 27.2579, 33.8116),
        Governorate("new_valley", "الوادي الجديد (الخارجة)", 25.4514, 30.5464),
        Governorate("matruh", "مطروح", 31.3543, 27.2373),
        Governorate("north_sinai", "شمال سيناء (العريش)", 31.1316, 33.7984),
        Governorate("south_sinai", "جنوب سيناء (شرم الشيخ)", 27.9158, 34.3299)
    )

    fun calculateTimes(governorate: Governorate, calendar: Calendar = Calendar.getInstance()): PrayerTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian date
        val a = floor((14 - month) / 12.0)
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        val julianDate = day + floor((153 * m + 2) / 5.0) + 365 * y + floor(y / 4.0) - floor(y / 100.0) + floor(y / 400.0) - 32045

        val d = julianDate - 2451545.0
        val g = (357.529 + 0.98560028 * d) % 360
        val q = (280.459 + 0.98564736 * d) % 360
        val l = (q + 1.915 * sin(toRadians(g)) + 0.020 * sin(toRadians(2 * g))) % 360
        val e = 23.439 - 0.00000036 * d
        val ra = toDegrees(atan2(cos(toRadians(e)) * sin(toRadians(l)), cos(toRadians(l)))) / 15.0
        val decl = asin(sin(toRadians(e)) * sin(toRadians(l)))

        val eqTime = (q / 15.0) - (ra % 24)

        // Timezone adjustment
        val tz = calendar.timeZone.getOffset(calendar.timeInMillis) / 3600000.0
        val noon = (12.0 + tz - (governorate.lng / 15.0) - eqTime)

        // Egyptian General Authority of Survey: Fajr angle 19.5, Isha angle 17.5
        val fajrAngle = 19.5
        val ishaAngle = 17.5

        val latRad = toRadians(governorate.lat)
        val declVal = decl

        fun sunAngleTime(angle: Double, isDirectionUp: Boolean): Double {
            val num = -sin(toRadians(angle)) - sin(latRad) * sin(declVal)
            val den = cos(latRad) * cos(declVal)
            val cosH = num / den
            if (cosH < -1.0 || cosH > 1.0) return Double.NaN
            val h = toDegrees(acos(cosH)) / 15.0
            return if (isDirectionUp) noon - h else noon + h
        }

        // Sunrise & Sunset (approx 0.833 deg)
        val sunrise = sunAngleTime(0.833, true)
        val sunset = sunAngleTime(0.833, false)

        val fajr = sunAngleTime(fajrAngle, true)
        val isha = sunAngleTime(ishaAngle, false)

        // Asr (Shafi'i/Standard: shadow length = 1)
        val asrAngle = toDegrees(atan(1.0 + tan(abs(latRad - declVal))))
        val asrCos = (sin(toRadians(90 - asrAngle)) - sin(latRad) * sin(declVal)) / (cos(latRad) * cos(declVal))
        val asrH = if (asrCos in -1.0..1.0) toDegrees(acos(asrCos)) / 15.0 else 3.0
        val asr = noon + asrH

        val dhuhr = noon

        fun formatHours(hours: Double): String {
            if (hours.isNaN()) return "--:--"
            var normalized = hours % 24.0
            if (normalized < 0) normalized += 24.0
            val h = floor(normalized).toInt()
            val min = floor((normalized - h) * 60).toInt()
            val amPm = if (h >= 12) "م" else "ص"
            val displayH = if (h % 12 == 0) 12 else h % 12
            return String.format(Locale.getDefault(), "%02d:%02d %s", displayH, min, amPm)
        }

        fun toMinutes(hours: Double): Int {
            if (hours.isNaN()) return 0
            var normalized = hours % 24.0
            if (normalized < 0) normalized += 24.0
            return (normalized * 60).toInt()
        }

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMin = calendar.get(Calendar.MINUTE)
        val currentMinutes = currentHour * 60 + currentMin

        val prayerMins = listOf(
            "الفجر" to toMinutes(fajr),
            "الشروق" to toMinutes(sunrise),
            "الظهر" to toMinutes(dhuhr),
            "العصر" to toMinutes(asr),
            "المغرب" to toMinutes(sunset),
            "العشاء" to toMinutes(isha)
        )

        var nextName = "الفجر"
        var nextTime = formatHours(fajr)
        var diffMinutes = 0

        val nextEntry = prayerMins.firstOrNull { it.second > currentMinutes }
        if (nextEntry != null) {
            nextName = nextEntry.first
            diffMinutes = nextEntry.second - currentMinutes
            nextTime = when (nextName) {
                "الفجر" -> formatHours(fajr)
                "الشروق" -> formatHours(sunrise)
                "الظهر" -> formatHours(dhuhr)
                "العصر" -> formatHours(asr)
                "المغرب" -> formatHours(sunset)
                else -> formatHours(isha)
            }
        } else {
            // After Isha, next prayer is Fajr tomorrow
            nextName = "الفجر"
            nextTime = formatHours(fajr)
            diffMinutes = (24 * 60 - currentMinutes) + prayerMins[0].second
        }

        val remainingHours = diffMinutes / 60
        val remainingMins = diffMinutes % 60
        val remainingFormatted = if (remainingHours > 0) {
            "متبقي $remainingHours ساعة و $remainingMins دقيقة"
        } else {
            "متبقي $remainingMins دقيقة"
        }

        return PrayerTimes(
            fajr = formatHours(fajr),
            sunrise = formatHours(sunrise),
            dhuhr = formatHours(dhuhr),
            asr = formatHours(asr),
            maghrib = formatHours(sunset),
            isha = formatHours(isha),
            nextPrayerName = nextName,
            nextPrayerTime = nextTime,
            timeRemaining = remainingFormatted
        )
    }

    private fun toRadians(deg: Double): Double = deg * PI / 180.0
    private fun toDegrees(rad: Double): Double = rad * 180.0 / PI
}
