package com.app.weatherstack.retrofit

import com.app.weatherstack.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherRetrofitInstance {

    // Assuming Constants.APP_ID is defined in your Constants object
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.newBuilder().addQueryParameter("appid", Constants.APP_ID).build()
                chain.proceed(request.newBuilder().url(url).build())
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val api: WeatherService by lazy { retrofit.create(WeatherService::class.java) }
}
