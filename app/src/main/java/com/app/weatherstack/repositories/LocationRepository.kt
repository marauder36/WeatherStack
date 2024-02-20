package com.app.weatherstack.repositories

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.app.weatherstack.utils.SharedPrefs

class LocationRepository(private val context: Context) {

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentLocation(): Location?{

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(ActivityCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            val location : Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            Log.d("CurrentLocation","Location=$location")

            val testPrefs= SharedPrefs.getInstance(context)
            Log.d("CurrentLocation","Other method obtained" +
                    "but we also saved them to sharedPrefs: " +
                    "SLat=${testPrefs.getValue("lat")} and SLon=${testPrefs.getValue("lon")}")

            if(location!=null)
            {
                val latitude = location.latitude
                val longitude= location.longitude

                val myPrefs = SharedPrefs.getInstance(context)
                myPrefs.setValue("lat",latitude.toString())
                myPrefs.setValue("lon",longitude.toString())

                Log.d("CurrentLocation","Other method obtained Lat= $latitude and Lon=$longitude,\n " +
                        "but we also saved them to sharedPrefs: " +
                        "SLat=${myPrefs.getValue("lat")} and SLon=${myPrefs.getValue("lon")}")
            }
            return location
        }
        return null

    }

    // Include other location-related methods here
}
