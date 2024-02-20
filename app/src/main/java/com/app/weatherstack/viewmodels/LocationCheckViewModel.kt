package com.app.weatherstack.viewmodels

import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.weatherstack.repositories.LocationRepository

class LocationCheckViewModel(private val locationRepository: LocationRepository):ViewModel() {

    private val _locationEnabled = MutableLiveData<Boolean>()
    val locationEnabled: LiveData<Boolean> get() = _locationEnabled

    private val _locationPermissionGranted = MutableLiveData<Boolean>()
    val locationPermissionGranted: LiveData<Boolean> get() = _locationPermissionGranted

    private val _locationData = MutableLiveData<Location?>()
    val locationData : LiveData<Location?> get()= _locationData

    fun checkLocationServices() {
        Log.d("LocationIsEnabled","checkLocationServicesCalled")
        val isEnabled = locationRepository.isLocationEnabled()
        _locationEnabled.postValue(isEnabled)
    }
    fun updateLocationStatus(isEnabled: Boolean) {
        Log.d("LocationIsEnabled","updateLocationStatusCalled")
        _locationEnabled.postValue(isEnabled)
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        Log.d("LocationIsEnabled","onLocationPermissionResultCalled")
        _locationPermissionGranted.value = isGranted
//        if(isGranted)
//            checkLocationServices()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchCurrentLocation() {
        Log.d("LocationIsEnabled","fetchCurrentLocationCalled")
        val location = locationRepository.getCurrentLocation()
        _locationData.postValue(location)
    }
    // Other logic and LiveData for the ViewModel

}