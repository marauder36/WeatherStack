package com.app.weatherstack.repositories

import com.app.weatherstack.ForeCast
import com.app.weatherstack.retrofit.WeatherService
import retrofit2.Response

class WeatherRepository(private val weatherService: WeatherService) {

        // Assuming you have some sort of API call setup in your Service interface for fetching weather.
        suspend fun getWeatherData(city: String? = null, lat: String? = null, lon: String? = null): Response<ForeCast> {
            return if (city != null) {
                weatherService.getWeatherByCity(city)
            } else {
                weatherService.getCurrentWeather(lat ?: "", lon ?: "")
            }
        }

        // ... Other repository methods ...


    suspend fun getCurrentWeather(lat: String, lon: String) = weatherService.getCurrentWeather(lat, lon)
    suspend fun getWeatherByCity(city: String) = weatherService.getWeatherByCity(city)
}
