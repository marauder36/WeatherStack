package com.app.weatherstack.viewmodelfactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.weatherstack.repositories.LocationRepository
import com.app.weatherstack.viewmodels.LocationCheckViewModel

class LocationCheckViewModelFactory(private val repository: LocationRepository) : ViewModelProvider.Factory {

     override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationCheckViewModel::class.java)) {
            return LocationCheckViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}