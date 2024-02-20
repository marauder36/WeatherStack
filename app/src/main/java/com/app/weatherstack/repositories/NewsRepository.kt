package com.app.weatherstack.repositories

import com.app.weatherstack.NewsResponse
import com.app.weatherstack.retrofit.NewsRetrofitInstance
import retrofit2.Response

class NewsRepository {

    suspend fun getTopHeadlines(country: String, apiKey: String): Response<NewsResponse> {
        return NewsRetrofitInstance.api.getTopHeadlines(country, apiKey)
    }

}