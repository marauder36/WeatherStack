package com.app.weatherstack

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.app.weatherstack.viewmodels.WeatherViewModel

/**
 * Implementation of App Widget functionality.
 */
class WeatherStackWidget : AppWidgetProvider() {
    private lateinit var weatherVM                      : WeatherViewModel

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    val sharedPreferencesCity = context.getSharedPreferences("LastCityCountrySearchedOrSelectedForWidget", Context.MODE_PRIVATE)
    val lastCityCountry = sharedPreferencesCity.getString("LastCountryForWidget", "Bucharest, RO")
    Log.d("LastCountryForWidget","$lastCityCountry")

    val sharedPreferencesTemp = context.getSharedPreferences("LastCityCountrySearchedOrSelectedTempForWidget", Context.MODE_PRIVATE)
    val temp = sharedPreferencesTemp.getString("LastCountryForWidgetTemp", "25°C")
    Log.d("LastCountryForWidget","$temp")

    val sharedPreferencesIcon = context.getSharedPreferences("LastCityCountrySearchedOrSelectedIconForWidget", Context.MODE_PRIVATE)
    val icon = sharedPreferencesIcon.getString("LastCountryForWidgetIcon", "01d")
    Log.d("LastCountryForWidget","$icon")
//        Toast.makeText(this,"LastCity $lastCityCountry",Toast.LENGTH_SHORT).show()
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.weather_stack_widget)
    views.setTextViewText(R.id.cityCountryTVWidget, lastCityCountry)
    views.setTextViewText(R.id.tempTVWidget,temp)
    views.setImageViewResource(R.id.widgetWeatherIcon, setMainImage(icon))

    val intent = Intent(context, MainActivity::class.java)
    intent.action = Intent.ACTION_MAIN
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
    views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent) // Replace R.id.widget_button with your widget view's ID

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)



}
private fun setMainImage(icon:String?):Int{
    if(icon=="01d"){
        return R.drawable.sunclear
    }
    else if (icon=="01n"){
        return R.drawable.moonclear
    }
    else if (icon=="02d"){
        return R.drawable.suncloud
    }
    else if (icon=="02n"){
        return R.drawable.mooncloud
    }
    else if (icon=="03d" || icon=="03n"){
        return R.drawable.singlecloud
    }
    else if (icon=="04d" || icon=="04n"){
        return R.drawable.brokenclouds
    }
    else if (icon=="09d" || icon=="09n"){
        return R.drawable.showerrain
    }
    else if (icon=="10d"){
        return R.drawable.suncloudrain
    }
    else if (icon=="10n"){
        return R.drawable.mooncloudrain
    }
    else if (icon=="11d" || icon=="11n"){
        return R.drawable.lightningstorm
    }
    else if (icon=="13d" || icon=="13n"){
        return R.drawable.snow
    }
    else if (icon=="14d"){
        return R.drawable.sunmist
    }
    else if (icon=="14n"){
        return R.drawable.moonmist
    }
    else
        return R.drawable.sunclear
}