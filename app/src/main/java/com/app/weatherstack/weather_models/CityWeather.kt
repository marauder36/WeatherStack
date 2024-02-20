package com.app.weatherstack.weather_models

data class CityWeather(
    val cityName: String,
    val countryName: String, // e.g., "Sunny", "Rainy"
    val weatherIconId: String, // Resource ID or URL of the weather icon
    val temp: String
    )
