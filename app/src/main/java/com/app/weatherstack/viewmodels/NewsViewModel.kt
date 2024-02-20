package com.app.weatherstack.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.weatherstack.NewsResponse
import com.app.weatherstack.repositories.NewsRepository
import kotlinx.coroutines.launch

class NewsViewModel(private val newsRepository: NewsRepository) : ViewModel() {

    val news: MutableLiveData<NewsResponse> = MutableLiveData()

    fun getTopHeadlines(country: String, apiKey: String) {
        viewModelScope.launch {
            val response = newsRepository.getTopHeadlines(country, apiKey)
            if (response.isSuccessful) {
                news.postValue(response.body())
            } else {
                // Handle error
            }
        }
    }
}