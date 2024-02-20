package com.app.weatherstack.datausage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataUsageViewModel : ViewModel() {
    private val _dataUsage = MutableLiveData<Long>()
    val dataUsage: LiveData<Long> = _dataUsage

    fun updateDataUsage(usage: Long) {
        _dataUsage.value = usage
    }

    private val apiService1 = ServiceLocator.retrofitApi1
    private val apiService2 = ServiceLocator.retrofitApi2
    private val apiService3 = ServiceLocator.retrofitApi3
    // Add more logic for updating and handling data usage limits
}
