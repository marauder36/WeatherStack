package com.app.weatherstack.retrofit

import com.example.example.GeocodingResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("maps/api/geocode/json?")
    fun getCoordinates(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): Call<GeocodingResponse>

    @GET("maps/api/geocode/json?")
    fun getLocation(
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String
    ): Call<GeocodingResponse>
}

