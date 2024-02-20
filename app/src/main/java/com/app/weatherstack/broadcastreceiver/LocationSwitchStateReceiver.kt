package com.app.weatherstack.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.app.weatherstack.interfaces.LocationSettingsListener

class LocationSwitchStateReceiver(private val listener: LocationSettingsListener) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            listener.onLocationSettingsChanged(isGpsEnabled || isNetworkEnabled)

//            if (isGpsEnabled || isNetworkEnabled) {
//                Toast.makeText(context, "Location services got Enabled", Toast.LENGTH_LONG).show()
//            } else {
//                Toast.makeText(context, "Location services still disabled", Toast.LENGTH_LONG).show()
//            }
        }
    }
}