package com.app.weatherstack.retrofit

import com.app.weatherstack.ForeCast
import com.app.weatherstack.utils.Constants.APP_ID
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("forecast?")
    suspend fun getCurrentWeatherOG(
        @Query("lat")
        lat:String,
        @Query("lon")
        long:String,
        @Query("appid")
        appid:String=APP_ID
    ):Response<ForeCast>

    @GET("forecast?")
    suspend fun getWeatherByCityOG(
        @Query("q")
        city:String,
        @Query("appid")
        appid:String=APP_ID
    ):Response<ForeCast>

    @GET("forecast?")
    suspend fun getCurrentWeather(
        @Query("lat")
        lat:String,
        @Query("lon")
        lon:String
    ): Response<ForeCast>

    @GET("forecast?")
    suspend fun getWeatherByCity(
        @Query("q")
        city:String
    ): Response<ForeCast>
}