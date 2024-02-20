package com.app.weatherstack.addlocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.app.weatherstack.R
import com.app.weatherstack.WeatherList
import com.app.weatherstack.broadcastreceiver.LocationSwitchStateReceiver
import com.app.weatherstack.interfaces.LocationSettingsListener
import com.app.weatherstack.repositories.LocationRepository
import com.app.weatherstack.repositories.WeatherRepository
import com.app.weatherstack.retrofit.WeatherRetrofitInstance
import com.app.weatherstack.utils.Constants
import com.app.weatherstack.utils.DateTimeValsUtils
import com.app.weatherstack.viewmodelfactories.LocationCheckViewModelFactory
import com.app.weatherstack.viewmodelfactories.ViewModelFactory
import com.app.weatherstack.viewmodels.LocationCheckViewModel
import com.app.weatherstack.viewmodels.WeatherViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class AddLocationActivity : AppCompatActivity(), OnMapReadyCallback, LocationSettingsListener {

    private lateinit var googleMap: GoogleMap // Declare this at the class level


    private lateinit var weatherVM: WeatherViewModel

    private var lastLocationCheckTime = 0L
    private val LOCATION_CHECK_DEBOUNCE = 1000 // milliseconds
    private var lastSearchTime = 0L
    private val searchDebouncePeriod = 1000 // 1 second

    private val debounceDelay = 2000L
    private val handler = Handler(Looper.getMainLooper())
    private var isFirstMessageDisplayed = false
    private var shownErrorMessage       = true
    private var shownCustomToast        = false

    private lateinit var toolbar: Toolbar
    private lateinit var addCitySV:SearchView
    private lateinit var addCityCountry:TextView
    private lateinit var addCityImageWeather:ImageView
    private lateinit var addCityTemp:TextView
    private lateinit var addCityWindSpeed:TextView
    private lateinit var addCityLatLong: TextView
    private lateinit var backArrowDark: ImageView
    private lateinit var addCitySave: ImageView
    private lateinit var bottomLLForCV:LinearLayout
    private lateinit var myLocationImageView: ImageView
    private lateinit var geocodingVM: MapViewModel

    private var userWantedGPSLocation = false
    private var shownUserGPSLocation = false

    private lateinit var locationSwitchStateReceiver    : LocationSwitchStateReceiver

    private val LOCATION_PERMISSION_REQUEST_CODE = 769
    private val REQUEST_CHECK_SETTINGS = 987

    private val locationViewModel: LocationCheckViewModel by viewModels {
        LocationCheckViewModelFactory(LocationRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        WindowCompat.setDecorFitsSystemWindows(window,true)

        initUI()
        requestLocationPermissions()
        geocodingVM = ViewModelProvider(this).get(MapViewModel::class.java)



        val service = WeatherRetrofitInstance.api
        val repository= WeatherRepository(service)
        weatherVM = ViewModelProvider(this, ViewModelFactory(repository)).get(WeatherViewModel::class.java)

        bottomLLForCV.visibility = View.INVISIBLE

//        addCitySV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//
//            override fun onQueryTextSubmit(query: String): Boolean {
//                shownCustomToast=false
//                isFirstMessageDisplayed =false
//                shownErrorMessage       =false
//                if(shownCustomToast==false) {
//                    geocodingVM.getCoordinates(query)
//                    geocodingVM.locationData.observe(
//                        this@AddLocationActivity,
//                        Observer { locationResult ->
//                            handleLocationResult(locationResult)
//                        })
//                }
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String): Boolean {
//                return false
//            }
//        })

        geocodingVM.locationData.observe(
            this@AddLocationActivity,
            Observer { locationResult ->

                handleLocationResult(locationResult)
                Log.d("FunctionGetcoordinates","HandleLocationResult got called")
            })

        addCitySV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            private val handler = Handler(Looper.getMainLooper())
            private val debounceRunnable = object : Runnable {
                var lastQuery: String = ""

                override fun run() {
                    isFirstMessageDisplayed = false
                    Log.d("FunctionGetcoordinates","Runnable got called")
                    geocodingVM.getCoordinates(lastQuery)

                }
            }

            private val debounceDelay = 200L // debounceDelay/1000 seconds debounce period

            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d("FunctionGetcoordinates","Query got called")

                handler.removeCallbacks(debounceRunnable)
                debounceRunnable.lastQuery = query
                handler.postDelayed(debounceRunnable, debounceDelay)
                addCitySV.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        if(intent.getStringExtra(Constants.cityFromMainActivity)!=null
            && intent.getStringExtra(Constants.cityFromMainActivity)!=null
            && intent.getStringExtra(Constants.cityFromMainActivity)!= "No data")
        {
            addCitySV.setQuery(intent.getStringExtra(Constants.cityFromMainActivity),true)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.addCityMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Log.d("LocationIsEnabled","BeforeCall")

        myLocationImageView.setOnClickListener {

            userWantedGPSLocation=true
//            shownUserGPSLocation=false
            forMyLocationFunc()
            locationViewModel.checkLocationServices()

        }

        locationViewModel.locationPermissionGranted.observe(this, Observer { isGranted ->
            Log.d("LocationIsEnabled","Permissions")
            if (isGranted) {
//                Toast.makeText(this, "MVVM Coarse and Fine location permissions granted", Toast.LENGTH_SHORT).show()

                locationViewModel.locationEnabled.observe(this, Observer { isEnabled ->
                    Log.d("LocationIsEnabled","GPS")
                    if (isEnabled) {
//                        Toast.makeText(this, "MVVM Location Enabled", Toast.LENGTH_SHORT).show()
//                        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//                        requestLocationData(mFusedLocationClient)
                        Log.d("LocationIsEnabled","GPSEnabled")
                    }

                    else {

//                        Toast.makeText(this, "MVVM Location Disabled", Toast.LENGTH_SHORT).show()

                        //promptUserToEnableLocation()

                    }
                })

            } else {

            }
        })



}

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun hideKeyboardOnOutsideTouch(activity: Activity) {
        val rootView = activity.findViewById<View>(android.R.id.content)
        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val inputMethodManager =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("In order to use current location , please open the app settings and allow for coarse and fine location permissions.")
            .setPositiveButton("Settings") { dialog, which ->
                // Prompt the user once the explanation has been shown
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onStart() {
        super.onStart()
        locationSwitchStateReceiver = LocationSwitchStateReceiver(this)
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(locationSwitchStateReceiver, filter)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            val isGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            locationViewModel.onLocationPermissionResult(isGranted)
        }

    }

    override fun onResume() {
        super.onResume()
        locationViewModel.checkLocationServices()

        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // User denied permissions and selected "Don't ask again," show a dialog to guide them to app settings

            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                promptUserToEnableLocation()
            }
            else{
                val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                requestLocationData(mFusedLocationClient)
            }
        }


    }

    private fun forMyLocationFunc() {

        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // User denied permissions and selected "Don't ask again," show a dialog to guide them to app settings
                showPermissionSettingsDialog()

        }
        else
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
         promptUserToEnableLocation()
        }
        else{
                val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                requestLocationData(mFusedLocationClient)
            }


    }

    private fun promptUserToEnableLocation() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Location settings are adequate, and we can start location requests
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                    exception.startResolutionForResult(this@AddLocationActivity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun handleLocationResult(locationResult: GeocodingResult) {
        when (locationResult) {
            is GeocodingResult.Success -> {
                // Update the map based on the result
                // If result is LatLng, move the map camera and add marker
                // If result is String (address), show it in a Toast or UI element
                //handler.removeCallbacksAndMessages(null)
                if(!isFirstMessageDisplayed) {
                    Log.d("LocationResult",locationResult.data.toString())

                    val locationResultString = locationResult.data.toString()
                    val locationResultLatitude =
                        locationResultString.substringAfter("lat").removePrefix("=")
                            .substringBefore(",")
                    val locationResultLongitude =
                        locationResultString.substringAfter("lng").removePrefix("=")
                            .substringBefore(")")

                    Log.d("LocationResult","Lat=$locationResultLatitude, Lon=$locationResultLongitude")
                    bottomLLForCV.visibility = View.VISIBLE
                    moveMapMarker(locationResultLatitude,locationResultLongitude)

                    val latitudeForWeatherAPI:String=((locationResultLatitude.toDouble()*100).toInt()/100.0).toString()

                    weatherVM.getWeatherMVVM(city = null,lat = latitudeForWeatherAPI, lon = locationResultLongitude)

                    addCityLatLong.text = "$locationResultLatitude, $locationResultLongitude"

                    weatherVM.closeToOrExactlySameWeatherData.observe(this, Observer {
                        setDataToViews(it)
                    })

                    weatherVM.cityCountryCode.observe(this, Observer {
                        addCityCountry.text=it
                        addCitySV.setQuery(it,false)
                    })

                    isFirstMessageDisplayed=true
                }

//                handler.postDelayed({
//                    isFirstMessageDisplayed=false
//                },debounceDelay)
            }
            is GeocodingResult.Error -> {
                // Show error message
                    showCustomToast(this)
//                    makeCustomPopup(locationResult.message)

                //Toast.makeText(this, locationResult.message, Toast.LENGTH_SHORT).show()
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                    requestLocationData(mFusedLocationClient)
                } else {
                    // The user did not agree to change the location settings
//                    Toast.makeText(this@MainActivity, "The user did not agree to change the location settings", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData(mFusedLocationClient: FusedLocationProviderClient){

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).build()
        mFusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if(userWantedGPSLocation==true) {
                    val latitude = locationResult.lastLocation?.latitude!!.toString()
                    val longitude = locationResult.lastLocation?.longitude!!.toString()
                    onMapReadyWorkGPS(latitude, longitude)
                    weatherVM.getWeatherMVVM(city = null, lat = latitude, lon = longitude)
                    weatherVM.cityCountryCode.observe(this@AddLocationActivity, Observer {
                        addCitySV.setQuery(it, false)
                        userWantedGPSLocation = false
                    })
                }
//                Toast.makeText(this@MainActivity,"Latitude: ${locationResult.lastLocation?.latitude!!} \n " +
//                      "Longitude: ${locationResult.lastLocation?.longitude!!}",Toast.LENGTH_SHORT).show()
            }
        }, Looper.myLooper())



//        mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
//            if (location != null) {
//                val latitude = location.latitude.toString()
//                val longitude = location.longitude.toString()
//
//                // Use this single location update as needed
//                onMapReadyWorkGPS(latitude, longitude)
//                weatherVM.getWeatherMVVM(city = null, lat = latitude, lon = longitude)
//                weatherVM.cityCountryCode.observe(this, Observer {
//                    addCitySV.setQuery(it,false)
//                })
//            }
//        }.addOnFailureListener {
//            // Handle failure
//        }

    }

    private fun onMapReadyWorkGPS(lat:String="0.0",lon:String="0.0"){
        val latLng = LatLng(lat.toDouble(),lon.toDouble())
        // Add a marker and move the map's camera to the clicked location
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
        bottomLLForCV.visibility = View.VISIBLE
        weatherVM.getWeatherMVVM(city = null, lat = latLng.latitude.toString(), lon = latLng.longitude.toString())

        //val latitudeForWeatherAPI:String=((latLng.latitude*100).toInt()/100.0).toString()
        //weatherVM.getWeatherMVVM(city = null,lat = latitudeForWeatherAPI, lon = latLng.longitude.toString())

        val displayLat = (latLng.latitude*1000000).toInt()/1000000.0
        val displayLon = (latLng.longitude*1000000).toInt()/1000000.0
        addCityLatLong.text = "${displayLat}, ${displayLon}"

        weatherVM.closeToOrExactlySameWeatherData.observe(this, Observer {
            setDataToViews(it)
        })

        weatherVM.cityCountryCode.observe(this, Observer {
            addCityCountry.text=it
        })

        // Display the coordinates in a Toast
        //Toast.makeText(this, "Coordinates: ${latLng.latitude}, ${latLng.longitude}", Toast.LENGTH_LONG).show()
    }

    private fun showCustomToast(context: Context) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast_no_find, null)

        val toast = Toast(context).apply {
            duration = Toast.LENGTH_LONG
            view = layout
            setGravity(Gravity.CENTER_HORIZONTAL, 0, -600)
        }

        toast.show()

    }

    private fun makeCustomPopup(message: String) {
        // Create a custom view for the popup
        if(shownErrorMessage==false){
        Log.d("TimesCalledCustomPop", "CalledAgain")
        val customView = layoutInflater.inflate(R.layout.custom_toast, null)
//        customView.findViewById<TextView>(R.id.textViewCustomToast)
// Initialize the PopupWindow
        val popupWindow = PopupWindow(
            customView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

// Set animation style (optional)
        popupWindow.animationStyle = R.style.PopupAnimation

// Show the PopupWindow at a specific location
        popupWindow.showAtLocation(View(this), Gravity.TOP, 0, 450)

// Dismiss the PopupWindow after a certain duration (e.g., 2000 milliseconds)
        Handler().postDelayed({
            popupWindow.dismiss()
        }, 2000)
//        shownErrorMessage=true
        popupWindow.setOnDismissListener {
            shownErrorMessage=true
        }
        }
    }

    private fun moveMapMarker(locationResultLatitude: String, locationResultLongitude: String) {
        val lat = locationResultLatitude.toDouble()
        val lng = locationResultLongitude.toDouble()
        val newLocation = LatLng(lat, lng)

        // Clear existing markers
        googleMap.clear()

        // Add new marker
        googleMap.addMarker(MarkerOptions().position(newLocation).title("New Location"))

        // Move the camera to the new location with zoom level
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 12f))

    }

    private fun setDataToViews(it: WeatherList?) {

        if(getLastTempSelectedUnit()=="C")
            addCityTemp.text = DateTimeValsUtils.getTempInCelsius(it?.main?.temp)
        else
            addCityTemp.text = DateTimeValsUtils.getTempInFahrenheit(it?.main?.temp)

        if(getLastWindSelectedUnit()=="Km")
            addCityWindSpeed.text=DateTimeValsUtils.getWindSpeedInKmPerH(it?.wind?.speed)
        else
            addCityWindSpeed.text=DateTimeValsUtils.getWindSpeedInMilesPerH(it?.wind?.speed)


        for(i in it?.weather!!)
            setMainImage(i.icon)
    }

    private fun getLastTempSelectedUnit():String {
        val sharedPreferences = getSharedPreferences("LastSelectedTempUnit", Context.MODE_PRIVATE)
        val unit = sharedPreferences.getString("LastTempUnit", null)
        return unit ?: "C"
    }

    private fun getLastWindSelectedUnit():String {
        val sharedPreferences = getSharedPreferences("LastSelectedWindUnit", Context.MODE_PRIVATE)
        val unit = sharedPreferences.getString("LastWindUnit", null)
        return unit ?: "Km"
    }

    private fun getLastPressureSelectedUnit():String {
        val sharedPreferences = getSharedPreferences("LastSelectedPressureUnit", Context.MODE_PRIVATE)
        val unit = sharedPreferences.getString("LastPressureUnit", null)
        return unit ?: "mmHg"
    }

    private fun setMainImage(icon:String?){
            if(icon=="01d"){
                addCityImageWeather.setImageResource(R.drawable.sunclear)
            }
            else if (icon=="01n"){
                addCityImageWeather.setImageResource(R.drawable.moonclear)
            }
            else if (icon=="02d"){
                addCityImageWeather.setImageResource(R.drawable.suncloud)
            }
            else if (icon=="02n"){
                addCityImageWeather.setImageResource(R.drawable.mooncloud)
            }
            else if (icon=="03d" || icon=="03n"){
                addCityImageWeather.setImageResource(R.drawable.singlecloud)
            }
            else if (icon=="04d" || icon=="04n"){
                addCityImageWeather.setImageResource(R.drawable.brokenclouds)
            }
            else if (icon=="09d" || icon=="09n"){
                addCityImageWeather.setImageResource(R.drawable.showerrain)
            }
            else if (icon=="10d"){
                addCityImageWeather.setImageResource(R.drawable.suncloudrain)
            }
            else if (icon=="10n"){
                addCityImageWeather.setImageResource(R.drawable.mooncloudrain)
            }
            else if (icon=="11d" || icon=="11n"){
                addCityImageWeather.setImageResource(R.drawable.lightningstorm)
            }
            else if (icon=="13d" || icon=="13n"){
                addCityImageWeather.setImageResource(R.drawable.snow)
            }
            else if (icon=="14d"){
                addCityImageWeather.setImageResource(R.drawable.sunmist)
            }
            else if (icon=="14n"){
                addCityImageWeather.setImageResource(R.drawable.moonmist)
            }
        }


    private fun initUI() {
        toolbar             = findViewById(R.id.addCityToolbar)
        setSupportActionBar(toolbar)
        myLocationImageView = findViewById(R.id.myLocationImageView)
        bottomLLForCV       = findViewById(R.id.bottomLLforCV)
        addCitySV           = findViewById(R.id.addCitySearchView)
        addCityCountry      = findViewById(R.id.addCityCountry)
        addCityTemp         = findViewById(R.id.addCityTemperature)
        addCityImageWeather = findViewById(R.id.addCityImageWeather)
        addCityWindSpeed    = findViewById(R.id.addCityWindSpeed)
        addCityLatLong      = findViewById(R.id.addCityLatLong)
        backArrowDark       = findViewById(R.id.bacArrowDark)
        backArrowDark.setOnClickListener{
            finish()
        }
        addCitySave         = findViewById(R.id.addCitySave)
        addCitySave.setOnClickListener {
            if(weatherVM.cityCountryCode.isInitialized) {
                checkIfCityIsValid()
                //showSaveDialog()
            }
            else {
                showNoCitySelectedDialog()
            }

        }
    }

    private fun checkIfCityIsValid() {
        var cityCountryNamesObserver:Observer<String?>?=null

        cityCountryNamesObserver= Observer<String?> { cityCountryNames->
            if (cityCountryNames!="No data, No data")
                showSaveDialog()
            else
                showInvalidCityDialog()

        }

        weatherVM.cityCountryCode.observe(this, cityCountryNamesObserver )
        weatherVM.cityCountryCode.removeObserver(cityCountryNamesObserver)
    }

    private fun showInvalidCityDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.invalid_city_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)

        dialogBuilder.setView(dialogView)

        val yesButton       = dialogView.findViewById<TextView>(R.id.saveCityYes)


        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(true)

        yesButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showSaveDialog() {

        // Inflate the custom layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.save_city_dialog_layout, null)
        val dialogBuilder = AlertDialog.Builder(this)

        // Set the custom layout as dialog view
        dialogBuilder.setView(dialogView)

        // Initialize the elements of your custom layout
        val title       = dialogView.findViewById<TextView>(R.id.saveCityDialogTitle)
        val cityCountry = dialogView.findViewById<TextView>(R.id.saveCityDialogCityCountry)
        val yesButton   = dialogView.findViewById<TextView>(R.id.saveCityYes)
        val cancelButton= dialogView.findViewById<TextView>(R.id.saveCityCancel)

        weatherVM.cityCountryCode.observe(this, Observer { cityCountryNames->

            cityCountry.text=cityCountryNames

        })

        // Create the AlertDialog object
        val alertDialog = dialogBuilder.create()


        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(false)

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        yesButton.setOnClickListener {

            observeCityCountryNames()

            Log.d("SharedPrefsLocs","${loadAllLocations()}")
            alertDialog.dismiss()
            finish()
            }

        // Display the custom AlertDialog
        alertDialog.show()
    }

    private fun observeCityCountryNames() {
        var cityCountryNamesObserver:Observer<String?>?=null

        cityCountryNamesObserver= Observer<String?> { cityCountryNames->
            saveLocationToSharedPrefs(cityCountryNames, cityCountryNames)
            getLocationFromSharedPrefs(cityCountryNames)
            weatherVM.cityCountryCode.removeObserver(Observer {  })
            saveLastSearchedOrSelectedCityToSharedPrefs(cityCountryNames.toString())
        }

        weatherVM.cityCountryCode.observe(this, cityCountryNamesObserver )
        weatherVM.cityCountryCode.removeObserver(cityCountryNamesObserver)

    }

    private fun getLastLocationFromSharedPrefs():String {
        val sharedPreferences = getSharedPreferences("LastCitySearchedOrSelected", Context.MODE_PRIVATE)
        val lastCity = sharedPreferences.getString("LastCity", null)
//        Toast.makeText(this,"LastCity $lastCity",Toast.LENGTH_SHORT).show()
        return lastCity ?: "Bucharest"
    }

    private fun saveLastSearchedOrSelectedCityToSharedPrefs(city:String) {
        val sharedPreferences = getSharedPreferences("LastCitySearchedOrSelected", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("LastCity", "$city")
        editor.apply()
    }

    private fun loadAllLocations(): Map<String, String> {
        val sharedPreferences = getSharedPreferences("CityCountryNames", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all
        val locationsMap = mutableMapOf<String, String>()

        for ((key, value) in allEntries) {
            if (value is String) {
                locationsMap[key] = value
            }
        }

        return locationsMap
    }


    private fun getLocationFromSharedPrefs(key: String?) {
        val sharedPreferences = getSharedPreferences("CityCountryNames", Context.MODE_PRIVATE)
        val cityCountryNames = sharedPreferences.getString(key, null)
        showCustomToast(this,cityCountryNames)

    }

    private fun showCustomToast(context: Context, cityCountryNames: String?) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast_delete_locations, null)

        // Find the ImageView and TextView in the custom layout
        val image = layout.findViewById<ImageView>(R.id.custom_toast_image)
        val text = layout.findViewById<TextView>(R.id.custom_toast_message)

        // Set the text and image as needed
        text.text = "Saved $cityCountryNames"
        // You can dynamically set the image here if you like:
        image.setImageResource(R.drawable.save_white)
        // Create the Toast object and set the custom view
        val toast = Toast(context).apply {
            duration = Toast.LENGTH_LONG
            view = layout
            setGravity(Gravity.CENTER_HORIZONTAL, 0, 900)
        }

        toast.show()
    }

    private fun saveLocationToSharedPrefs(key: String?, cityCountryNames: String?) {
        val sharedPreferences = getSharedPreferences("CityCountryNames", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString(key, "$cityCountryNames")
        editor.apply()
    }

    private fun showNoCitySelectedDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.no_city_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)

        dialogBuilder.setView(dialogView)

        val yesButton       = dialogView.findViewById<TextView>(R.id.saveCityYes)


        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(true)

        yesButton.setOnClickListener {
                alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap=googleMap
        onMapReadyWork()

//        googleMap.addMarker(
//            MarkerOptions()
//                .position(LatLng(0.0, 0.0))
//                .title("Marker")
//        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(locationSwitchStateReceiver)
    }

    private fun onMapReadyWork() {
        googleMap.setOnMapClickListener { latLng ->
            // Add a marker and move the map's camera to the clicked location
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            bottomLLForCV.visibility = View.VISIBLE
            weatherVM.getWeatherMVVM(city = null, lat = latLng.latitude.toString(), lon = latLng.longitude.toString())
            hideKeyboard()
            //val latitudeForWeatherAPI:String=((latLng.latitude*100).toInt()/100.0).toString()
            //weatherVM.getWeatherMVVM(city = null,lat = latitudeForWeatherAPI, lon = latLng.longitude.toString())

            val displayLat = (latLng.latitude*1000000).toInt()/1000000.0
            val displayLon = (latLng.longitude*1000000).toInt()/1000000.0
            addCityLatLong.text = "${displayLat}, ${displayLon}"

            weatherVM.closeToOrExactlySameWeatherData.observe(this, Observer {
                setDataToViews(it)
            })

            weatherVM.cityCountryCode.observe(this, Observer {
                addCityCountry.text=it
            })

            // Display the coordinates in a Toast
            //Toast.makeText(this, "Coordinates: ${latLng.latitude}, ${latLng.longitude}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onLocationSettingsChanged(isEnabled: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLocationCheckTime > LOCATION_CHECK_DEBOUNCE) {
            lastLocationCheckTime = currentTime
            locationViewModel.updateLocationStatus(isEnabled)
        }
    }
}