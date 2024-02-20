package com.app.weatherstack.addlocation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.weatherstack.retrofit.GeocodingService
import com.example.example.GeocodingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapViewModel : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(GeocodingService::class.java)

    val locationData: MutableLiveData<GeocodingResult> = MutableLiveData()

    fun getCoordinates(cityName: String) {
        Log.d("FunctionGetcoordinates","got called")
        val call = service.getCoordinates(cityName, "A")
        call.enqueue(object : Callback<GeocodingResponse> {
            override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
                val body = response.body()
                Log.d("FunctionGetcoordinates","Response Body: $body")
                if (body != null && body.results.isNotEmpty()) {
                    Log.d("FunctionGetcoordinates","Succes")
                    val location = body.results[0].geometry?.location
                    locationData.postValue(location?.let { GeocodingResult.Success(it) })
                } else {
                    Log.d("FunctionGetcoordinates","No location found")
                    locationData.postValue(GeocodingResult.Error("No location found"))
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                Log.d("FunctionGetcoordinates","failed")
                locationData.postValue(GeocodingResult.Error("API call failed"))
            }
        })
    }

    fun getLocationFromCoordinates(latitude: Double, longitude: Double) {
        val call = service.getLocation("$latitude,$longitude", "A")
        call.enqueue(object : Callback<GeocodingResponse> {
            override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
                val body = response.body()
                if (body != null && body.results.isNotEmpty()) {
                    val address = body.results[0].formattedAddress
                    locationData.postValue(address?.let { GeocodingResult.Success(it) })
                } else {
                    locationData.postValue(GeocodingResult.Error("No address found"))
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                locationData.postValue(GeocodingResult.Error("API call failed"))
            }
        })
    }
}

sealed class GeocodingResult {
    data class Success(val data: Any) : GeocodingResult()
    data class Error(val message: String) : GeocodingResult()
}
