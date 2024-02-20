package com.app.weatherstack.datausage

import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException

class DataUsageInterceptor(private val repository: DataRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val currentUsage = repository.getDataUsage()
        val dataLimit = repository.getDataLimit()

        if (currentUsage >= dataLimit) {
            throw IOException("Data limit reached")
        }

        return chain.proceed(chain.request())
    }
}
