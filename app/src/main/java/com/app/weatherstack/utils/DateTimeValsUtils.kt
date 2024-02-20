package com.app.weatherstack.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

object DateTimeValsUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDatePattern(): String {
        val currentDateTime = LocalDateTime.now()
        return currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getNextDatePattern(): String {
        val currentDateTime = LocalDate.now()
        val tomorrow = currentDateTime.plusDays(1)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return tomorrow.format(formatter)
    }

    fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    fun getTimeAgo(publishedAt: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT") // The date from API is in GMT

        return try {
            val time = sdf.parse(publishedAt)?.time ?: return ""
            val now = System.currentTimeMillis()
            val diff = now - time

            when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
                diff < 48 * 60 * 60 * 1000 -> "Yesterday"
                else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            "Unknown date"
        }
    }

    fun getTempInCelsius(temp: Double?): String
    {
        val temperatureCelsius = (temp?.minus(273.15))
        val temperatureFormated = temperatureCelsius?.roundToInt().toString()
        return "$temperatureFormated°C"
    }

    fun getTempInFahrenheit(temp: Double?): String
    {
        val temperatureCelsius = (temp?.minus(273.15))
        val temperatureFahrenheit = (temperatureCelsius?.times(9)?.div(5)?.plus(32))
        val temperatureFormated = temperatureFahrenheit?.roundToInt().toString()
        return "$temperatureFormated°F"
    }

    fun getWindSpeedInKmPerH(speed:Double?): String
    {
        return (speed!!*3.6).roundToInt().toString()+" km/h"
    }

    fun getWindSpeedInMilesPerH(speed:Double?): String
    {
        return (speed!!*3.6*0.621371192).roundToInt().toString()+" M/h"
    }

    fun getPressureInmBar(pressure:Int?): String
    {
        return (pressure!!).toString()+" mBar"
    }

    fun getPressureInmmHg(pressure:Int?): String
    {
        return (pressure!! * 0.75006).roundToInt().toString()+" mmHg"
    }

    fun formatDate_d_MM_yyyy(dtTxt: String? = "2024-01-28 18:00:00"): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dtTxt)
        val outputFormat= SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val dateAndDayName= outputFormat.format(date!!)
        val dateAndDayNameEdited = dateAndDayName.split(" ")[0]+"th "+dateAndDayName.split(" ")[1]+" "+dateAndDayName.split(" ")[2]
        return dateAndDayNameEdited
    }

    fun formatDate_dayofweek_d_MM_yyyy(dtTxt: String?): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = dtTxt?.let { inputFormat.parse(it) }
        val outputFormatDate= SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val dateAndDayName= outputFormatDate.format(date!!)
        val outputFormatDayName= SimpleDateFormat("EEEE", Locale.getDefault())
        val dayName= outputFormatDayName.format(date)
        val dateAndDayNameEdited =dayName+", "+ dateAndDayName.split(" ")[0]+ "th "+dateAndDayName.split(" ")[1]+" "+dateAndDayName.split(" ")[2]
        return dateAndDayNameEdited
    }

}
