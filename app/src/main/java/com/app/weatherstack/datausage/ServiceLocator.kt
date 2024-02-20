package com.app.weatherstack.datausage

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceLocator {

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context
    }

    private val dataRepository: DataRepository by lazy {
        DataRepository(appContext)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(DataUsageInterceptor(dataRepository))
            .build()
    }



    // Retrofit instance for API 1
    val retrofitApi1 = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Retrofit instance for API 2
    val retrofitApi2 = Retrofit.Builder()
        .baseUrl("https://newsapi.org/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitApi3 = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
// Add more Retrofit instances as needed


    // Add more services as needed
}
