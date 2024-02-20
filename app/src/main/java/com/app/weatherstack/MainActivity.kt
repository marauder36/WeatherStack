package com.app.weatherstack

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.app.weatherstack.adapters.FiveDayForeCastAdapter
import com.app.weatherstack.adapters.FiveThreeHoursForecastAdapter
import com.app.weatherstack.adapters.NavHeaderRVAdapter
import com.app.weatherstack.adapters.WeatherToday
import com.app.weatherstack.addlocation.AddLocationActivity
import com.app.weatherstack.addlocation.GeocodingResult
import com.app.weatherstack.addlocation.MapViewModel
import com.app.weatherstack.alllocations.AllLocationsActivity
import com.app.weatherstack.broadcastreceiver.LocationSwitchStateReceiver
import com.app.weatherstack.currentLocationDetails.CurrentLocationWeatherDetails
import com.app.weatherstack.databinding.ActivityMainBinding
import com.app.weatherstack.interfaces.LocationSettingsListener
import com.app.weatherstack.mylocation.MyLocationFromDrawerActivity
import com.app.weatherstack.news.NewsActivity
import com.app.weatherstack.repositories.LocationRepository
import com.app.weatherstack.repositories.NewsRepository
import com.app.weatherstack.repositories.WeatherRepository
import com.app.weatherstack.retrofit.WeatherRetrofitInstance
import com.app.weatherstack.settings.SettingsActivity
import com.app.weatherstack.utils.Constants
import com.app.weatherstack.utils.DateTimeValsUtils
import com.app.weatherstack.utils.SharedPrefs
import com.app.weatherstack.viewmodelfactories.LocationCheckViewModelFactory
import com.app.weatherstack.viewmodelfactories.NewsViewModelFactory
import com.app.weatherstack.viewmodelfactories.ViewModelFactory
import com.app.weatherstack.viewmodels.LocationCheckViewModel
import com.app.weatherstack.viewmodels.NewsViewModel
import com.app.weatherstack.viewmodels.WeatherViewModel
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
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

import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity(),LocationSettingsListener,
    NavHeaderRVAdapter.OnItemClickListener,
    OnMapReadyCallback {

    private val locationViewModel: LocationCheckViewModel by viewModels {
        LocationCheckViewModelFactory(LocationRepository(applicationContext))
    }

    private lateinit var newsViewModel: NewsViewModel
    private lateinit var settingsButton: TextView
    private lateinit var availableCountriesForNews: List<String>
    //private lateinit var window:Window

    private var isFirstMessageDisplayed = false
    private var useCurrentLocationForDrawer=false

    private var locationServicesAndPermissionsEnabled = false
    private var latCurrentLocation                      : Double = 0.0
    private var lonCurrentLocation                      : Double = 0.0

    private lateinit var googleMap                      : GoogleMap // Declare this at the class level
    private lateinit var geocodingVM                    : MapViewModel

    private lateinit var toolbar                        : Toolbar
    //private lateinit var cardView                     : CardView
    //private lateinit var mainLinearLayout             : LinearLayout
    private lateinit var drawerLayout                   : DrawerLayout
    private lateinit var navView                        : NavigationView

    private lateinit var drawerLocationCityCountry      : TextView
    private lateinit var locationLinearLayout           : LinearLayout
    private lateinit var addLocationTextView            : TextView
    private lateinit var addLocationImageView           : ImageView
    private lateinit var addLocationLinearLayout        : LinearLayout
    private lateinit var openDrawerImage                : ImageView
    private lateinit var viewAllLocationsTV             : TextView
    private lateinit var cityFromMapFragment            : TextView
    private lateinit var windType                       : TextView
    private lateinit var mainWindSpeed                  : TextView
    private lateinit var mainAtmPressure                : TextView

    private lateinit var navHeaderRV                    : RecyclerView
    private lateinit var navHeaderRVAdapter             : NavHeaderRVAdapter

    private lateinit var binding                        : ActivityMainBinding

    private lateinit var weatherVM                      : WeatherViewModel

    private lateinit var mainForecastRV                 : RecyclerView
    private lateinit var todayForecastRV                : RecyclerView
    private lateinit var mainForecastAdapter            : WeatherToday
    private lateinit var fiveDaysForeCastAdapter        : FiveDayForeCastAdapter
    private lateinit var fiveThreeHoursForecastAdapter  : FiveThreeHoursForecastAdapter
    private lateinit var mapFragmentConLayout    : ConstraintLayout
    private var isLocationPromptShown = false

    private var lastLocationCheckTime = 0L
    private val LOCATION_CHECK_DEBOUNCE = 1000 // milliseconds


    private lateinit var locationSwitchStateReceiver    : LocationSwitchStateReceiver

    private val LOCATION_PERMISSION_REQUEST_CODE = 69
    private val REQUEST_CHECK_SETTINGS = 44

//    private lateinit var adViewBanner1 : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window,true)

        initUI()
        initDrawerLayout()
        overrideDrawerSlide()
        adapterInit()
        requestLocationPermissions()
        setupDrawer()
//        setupAds()
        val sharedPreferences = getSharedPreferences("TempUnit", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("LastSelected", "C")
        editor.apply()

        window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )

        val citiesAndCountryMap= loadAllLocations()
        val citiesAndCountryList=citiesAndCountryMap.keys.toList().takeLast(3)

        updateDrawerRV(citiesAndCountryList)

        openDrawerImage.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        geocodingVM = ViewModelProvider(this).get(MapViewModel::class.java)

        geocodingVM.locationData.observe(this, Observer {locationResult->
            handleLocationResult(locationResult)
        })

        val service = WeatherRetrofitInstance.api
        val repository=WeatherRepository(service)
        weatherVM = ViewModelProvider(this,ViewModelFactory(repository)).get(WeatherViewModel::class.java)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.currentCityMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        locationViewModel.fetchCurrentLocation()
//
//        locationViewModel.locationData.observe(this, Observer { location ->
//            Log.d("LocationDataMVVM","$location")
//        })
        locationViewModel.checkLocationServices()
        locationViewModel.locationPermissionGranted.observe(this, Observer { isGranted ->
            if (isGranted) {
//                Toast.makeText(this, "MVVM Coarse and Fine location permissions granted", Toast.LENGTH_SHORT).show()

                locationViewModel.locationEnabled.observe(this, Observer { isEnabled ->
                    if (isEnabled) {
//                        Toast.makeText(this, "MVVM Location Enabled", Toast.LENGTH_SHORT).show()
                        locationServicesAndPermissionsEnabled=true
                        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                        useCurrentLocationForDrawer=true
                        requestLocationData(mFusedLocationClient)

                    }

                    else {
                        getWeatherDataOnStart()
                        getLastCityCoordinates()
//                        Toast.makeText(this, "MVVM Location Disabled", Toast.LENGTH_SHORT).show()
                        if(!isLocationPromptShown){
                            promptUserToEnableLocation()
                            isLocationPromptShown=true
                        }
                    }
                })

            } else {
                getWeatherDataOnStart()
                getLastCityCoordinates()
//                Toast.makeText(this, "MVVM Coarse and fine location permissions NOT granted", Toast.LENGTH_SHORT).show()
            }
        })

//        locationViewModel.fetchCurrentLocation()
//        locationViewModel.locationData.observe(this, Observer {location->
//            Log.d("LocationMVVM","$location")
//        })


        //binding.lifecycleOwner = this
        //binding.vm = weatherVM

        //weatherVM.getWeather()



        //de cate ori pornim app-ul, putem sa stergem orasul salvat, sa folosim locatia curenta
        val savedSharedPrefs = SharedPrefs.getInstance(this@MainActivity)
        //sharedPrefs.clearCityValue()

        weatherVM.cityName.observe(this, Observer {
            binding.cityName.text=it.toString()
            if (it!=null && it!="No data" && it.isNotEmpty())
            {
                saveLastSearchedOrSelectedCityToSharedPrefs(it)
                getCityCoordinates(it)



                binding.mainWeather.setOnClickListener {_->
                    val intent = Intent(this,CurrentLocationWeatherDetails::class.java)
                    intent.putExtra(Constants.cityFromMainActivity,it.toString())
                    startActivity(intent)
                }

            }


        })

        weatherVM.countryName.observe(this, Observer {
            saveLastSearchedOrSelectedCountryToSharedPrefs(it.toString())
            binding.countryName.text=it.toString()
            locationLinearLayout.setOnClickListener {_->
//                Toast.makeText(this,"Pressed current location",Toast.LENGTH_SHORT).show()
                val intent = Intent(this,MyLocationFromDrawerActivity::class.java)
                intent.putExtra(Constants.cityFromMainActivity,it)
                startActivity(intent)
            }
            if(availableCountriesForNews.contains(it?.lowercase()))
                newsViewModel.getTopHeadlines("$it", Constants.newsAPIKey)
            else
                newsViewModel.getTopHeadlines("us", Constants.newsAPIKey)
        })

        weatherVM.cityCountryCode.observe(this, Observer {
            binding.cityCountryQuery.text=it.toString()
            cityFromMapFragment.text=it.toString()
            drawerLocationCityCountry.text = getLastGPSLocationFromSharedPrefs()

            saveLastSearchedOrSelectedCityCountryForWidgetToSharedPrefs(it?:"Bucharest, RO")
            updateWidget()
            mapFragmentConLayout.setOnClickListener {_->
                val intent = Intent(this,AddLocationActivity::class.java)
                intent.putExtra(Constants.cityFromMainActivity,it.toString())
                startActivity(intent)
            }

        })

        weatherVM.cityCountryGPS.observe(this, Observer {
            saveLastGPSLocationToSharedPrefs(it?:"Bucharest, RO")

        })

        weatherVM.closestFiveThreeHoursForecast.observe(this, Observer {
            val setNewList = it as List<WeatherList>
            val nextDatePattern = DateTimeValsUtils.getNextDatePattern()
            val isTomorrowPresent=setNewList.filter { weatherList -> weatherList.dtTxt?.startsWith(nextDatePattern)==true }
            if (isTomorrowPresent.isNotEmpty())
            {
                binding.todayForecast.text="Today and tomorrow's forecast"
            }
            else
            {
                binding.todayForecast.text="Today's forecast"
            }
            fiveThreeHoursForecastAdapter.setList(setNewList)
            todayForecastRV.adapter=fiveThreeHoursForecastAdapter
        })

        weatherVM.forecastWeatherLiveData.observe(this, Observer {

            val setNewList = it as List<WeatherList>

            mainForecastAdapter.setList(setNewList)
            fiveDaysForeCastAdapter.setList(setNewList)
            //mainForecastRV.adapter=mainForecastAdapter
            mainForecastRV.adapter=fiveDaysForeCastAdapter

        })

        //Pentru peste 11:30
//        weatherVM.closeToOrExactlySameWeatherData.observe(this, Observer { it ->
//            val temperatureKelvin = it?.main?.temp
//            val temperatureCelsius = temperatureKelvin?.minus(273.15)
//            val temperatureFahrenheit = temperatureCelsius?.times(9)?.div(5)?.plus(32)
//            val temperatureFormatted = String.format("%.2f", temperatureCelsius)
//            binding.tempMain.text = "$temperatureFormatted°C"
//
//            it?.weather?.firstOrNull()?.let { weatherItem ->
//                val desc = weatherItem.description?.capitalize() ?: ""
//                binding.descMain.text = desc
//            }
//
//            binding.humidity.text = it?.main?.humidity.toString()
//            binding.windSpeed.text = it?.wind?.speed.toString()
//            binding.chanceOfRain.text = "${it?.pop?.times(100).toString()}%"
//
//            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
//            //val date = inputFormat.parse(it?.dtTxt ?: "")
//            val outputFormat = SimpleDateFormat("d MMMM EEEE", Locale.getDefault())
//            //val dateAndDayName = outputFormat.format(date ?: Date())
//            //binding.dateDayMain.text = dateAndDayName
//
//            it?.weather?.firstOrNull()?.let { weatherItem ->
//                setMainImage(weatherItem.icon)
//            }
//        })

        weatherVM.closeToOrExactlySameWeatherData.observe(this, Observer {

            if(getLastTempSelectedUnit()=="C") {
                saveLastSearchedOrSelectedCityCountryTempForWidgetToSharedPrefs(DateTimeValsUtils.getTempInCelsius(it?.main?.temp?:298.15))
            }
            else {
                saveLastSearchedOrSelectedCityCountryTempForWidgetToSharedPrefs(DateTimeValsUtils.getTempInFahrenheit(it?.main?.temp?:298.15))
            }

            if(it!=null) {

                if(getLastTempSelectedUnit()=="C") {
                    binding.tempMain.text = DateTimeValsUtils.getTempInCelsius(it.main?.temp)
                }
                else {
                    binding.tempMain.text = DateTimeValsUtils.getTempInFahrenheit(it.main?.temp)
                }

                for (i in it.weather) {
                    val desc = i.description.toString()
                    val upperFirstChar = desc[0].uppercaseChar()

                    binding.descMain.text = upperFirstChar + desc.substring(1)
                    binding.todayDesc5DayRV.text = upperFirstChar + desc.substring(1)
                    binding.todayDesc53HoursRV.text = upperFirstChar + desc.substring(1)
                }

                binding.humidity.text = it.main!!.humidity.toString()

                if (it.wind?.speed!=null) {
                    if(getLastWindSelectedUnit()=="Km") {
                        binding.windSpeed.text = DateTimeValsUtils.getWindSpeedInKmPerH(it.wind?.speed!!)
                        mainWindSpeed.text = DateTimeValsUtils.getWindSpeedInKmPerH(it.wind?.speed!!)
                    }
                    else
                    {
                        binding.windSpeed.text = DateTimeValsUtils.getWindSpeedInMilesPerH(it.wind?.speed!!)
                        mainWindSpeed.text = DateTimeValsUtils.getWindSpeedInMilesPerH(it.wind?.speed!!)
                    }
                }

                binding.chanceOfRain.text = "${it.pop?.times(100)?.toInt().toString()}%"

                if(it.main?.pressure!=null) {

                    if(getLastPressureSelectedUnit()=="mmHg") {
                        binding.pressure.text =
                            DateTimeValsUtils.getPressureInmmHg(it.main!!.pressure!!)
                        mainAtmPressure.text =
                            DateTimeValsUtils.getPressureInmmHg(it.main!!.pressure!!)
                    }
                    else
                    {
                        binding.pressure.text =
                            DateTimeValsUtils.getPressureInmBar(it.main!!.pressure!!)
                        mainAtmPressure.text =
                            DateTimeValsUtils.getPressureInmBar(it.main!!.pressure!!)
                    }
                }


                binding.dayMonthYear.text = formatDate_d_MM_yyyy(it.dtTxt)


                for (i in it.weather) {
                    setMainImage(i.icon)
                    saveLastSearchedOrSelectedCityCountryIconForWidgetToSharedPrefs(i.icon?:"01d")
                }
            }
        })

//        binding.searchView.setOnClickListener {
//            binding.cityCountryQuery.visibility=View.INVISIBLE
//        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                if(!query.isNullOrEmpty()){
                    //weatherVM.getWeather(query)
                    weatherVM.getWeatherMVVM(query)
                    weatherVM.getForeCastUpcomingMVVM(query)
                    binding.searchView.setQuery("",false)
                    binding.searchView.clearFocus()
                    binding.searchView.isIconified=true
                    binding.cityCountryQuery.visibility=View.VISIBLE
                    isFirstMessageDisplayed=false
                    geocodingVM.getCoordinates(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(!newText.isNullOrEmpty())
                    binding.cityCountryQuery.visibility=View.INVISIBLE
                else
                    binding.cityCountryQuery.visibility=View.VISIBLE
                return true
            }

        })

        binding.searchView.setOnCloseListener {
            binding.cityCountryQuery.visibility=View.VISIBLE
            false
        }

        val newsRepository = NewsRepository()
        val viewModelFactory = NewsViewModelFactory(newsRepository)
        newsViewModel = ViewModelProvider(this, viewModelFactory).get(NewsViewModel::class.java)


        newsViewModel.news.observe(this, Observer{ response ->

            if (response!=null){

                for(article in response.articles) {

                    if(article.source?.name!="[Removed]" && article.title!="[Removed]" && article.description!="[Removed]"&& article.content!="[Removed]")
                    {
                        Log.d("NewsResponse", "$response")
                        Glide.with(this@MainActivity)
                            .load(article.urlToImage)
                            .into(binding.newsImage)
                        binding.newsTitle.text = article.title
                        binding.newsSource.text = article.source?.name ?: "News Source"
                        binding.newsTimePosted.text =
                            DateTimeValsUtils.getTimeAgo(article.publishedAt!!)
                        break
                    }
                }


            }
        })

        binding.newsCardView.setOnClickListener {
            val intent = Intent(this,NewsActivity::class.java)
            startActivity(intent)
        }

        availableCountriesForNews = listOf("ae","ar","at","au","be","bg","br","ca","ch",
            "cn","co","cu","cz","de","eg","fr","gb","gr","hk","hu","id","ie","il","in","it","jp","kr","lt","lv","ma",
            "mx","my","ng","nl","no","nz","ph","pl","pt","ro","rs","ru","sa","se","sg","si","sk","th","tr","tw","ua","us","ve","za")
        //getNewsForCurrentCountry()

        val sharedPreferencesMain = getSharedPreferences("LastSelectedTempUnit", Context.MODE_PRIVATE)
        SharedPrefsHelper.init(sharedPreferencesMain)

    MobileAds.initialize(this)
    }

//    private fun setupAds() {
//        val adRequest = AdRequest.Builder().build()
//        adViewBanner1.loadAd(adRequest)
//        adViewBanner1.adListener = object : AdListener(){
//            override fun onAdClicked() {
//                // Code to be executed when the user clicks on an ad.
//                Log.d("AdOperations","Ad was clicked")
//            }
//
//            override fun onAdClosed() {
//                // Code to be executed when the user is about to return
//                // to the app after tapping on an ad.
//                Log.d("AdOperations","Ad was clicked and i am coming back to app")
//            }
//
//            override fun onAdFailedToLoad(adError : LoadAdError) {
//                // Code to be executed when an ad request fails.
//                Log.d("AdOperations","Ad add failed to load: $adError")
//            }
//
//            override fun onAdImpression() {
//                // Code to be executed when an impression is recorded
//                // for an ad.
//                Log.d("AdOperations","Ad impression")
//            }
//
//            override fun onAdLoaded() {
//                // Code to be executed when an ad finishes loading.
//                Log.d("AdOperations","Ad was loaded")
//            }
//
//            override fun onAdOpened() {
//                // Code to be executed when an ad opens an overlay that
//                // covers the screen.
//                Log.d("AdOperations","Ad was opened")
//            }
//        }
//    }

    private fun updateWidget() {

        val intent = Intent(applicationContext,WeatherStackWidget::class.java)
        intent.action=AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(applicationContext)
            .getAppWidgetIds(ComponentName(applicationContext,WeatherStackWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids)
        sendBroadcast(intent)
    }

//    private fun getNewsForCurrentCountry() {
//        newsViewModel.getTopHeadlines("us", "YOUR_API_KEY")
//    }

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

    private fun getCityCoordinates(city:String?){
        Log.d("FunctionCityCoords","got called with city=${city.toString()}")
        if(city!=null && city!="No data")
        {
            Log.d("FunctionCityCoords","got called inside if")
            geocodingVM.getCoordinates(city)
        }
    }

    private fun getLastCityCoordinates() {
        Log.d("FunctionLastCity","got called")
        geocodingVM.getCoordinates(getLastLocationFromSharedPrefs())

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

                    onMapReadyWork(locationResultLatitude,locationResultLongitude)

                    isFirstMessageDisplayed=true

                }
            }
            is GeocodingResult.Error -> {
                // Show error message
//                Toast.makeText(this, locationResult.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getWeatherDataOnStart() {
        if(getLastLocationFromSharedPrefs()!="Bucharest")
        {
            weatherVM.getWeatherMVVM(city=getLastLocationFromSharedPrefs())
            weatherVM.getForeCastUpcomingMVVM(city=getLastLocationFromSharedPrefs())
        }
        else
        {
            weatherVM.getWeatherMVVM(city="Bucharest")
            weatherVM.getForeCastUpcomingMVVM("Bucharest")
        }
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

    private fun saveLastGPSLocationToSharedPrefs(cityCountryCode:String) {
        val sharedPreferences = getSharedPreferences("LastGPSLocation", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("LastCityCountryCode", "$cityCountryCode")
        editor.apply()
    }

    private fun getLastGPSLocationFromSharedPrefs():String {
        val sharedPreferences = getSharedPreferences("LastGPSLocation", Context.MODE_PRIVATE)
        val lastCityCountry = sharedPreferences.getString("LastCityCountryCode", null)
//        Toast.makeText(this,"LastCity $lastCityCountry",Toast.LENGTH_SHORT).show()
        return lastCityCountry ?: "Bucharest, RO"
    }

    private fun saveLastSearchedOrSelectedCountryToSharedPrefs(country:String) {
        val sharedPreferences = getSharedPreferences("LastCountrySearchedOrSelected", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("LastCountry", "$country")
        editor.apply()
    }

    private fun saveLastSearchedOrSelectedCityCountryForWidgetToSharedPrefs(country:String) {
        val sharedPreferences = getSharedPreferences("LastCityCountrySearchedOrSelectedForWidget", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("LastCountryForWidget", "$country")
        editor.apply()
    }

    private fun saveLastSearchedOrSelectedCityCountryTempForWidgetToSharedPrefs(temp:String) {
        val sharedPreferences = getSharedPreferences("LastCityCountrySearchedOrSelectedTempForWidget", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("LastCountryForWidgetTemp", temp)
        editor.apply()
    }

    private fun saveLastSearchedOrSelectedCityCountryIconForWidgetToSharedPrefs(iconCode:String) {
        val sharedPreferences = getSharedPreferences("LastCityCountrySearchedOrSelectedIconForWidget", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("LastCountryForWidgetIcon", "$iconCode")
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

    private fun updateDrawerRV(citiesAndCountryList:List<String>) {

        navHeaderRVAdapter.setList(citiesAndCountryList)

        navHeaderRV.adapter=navHeaderRVAdapter
    }

    private fun setupDrawer() {

        val headerView = navView.getHeaderView(0)
        drawerLocationCityCountry = headerView.findViewById(R.id.currentLocationDrawer)

        locationLinearLayout   = headerView.findViewById(R.id.locationLinearLayout)


        addLocationTextView       = headerView.findViewById(R.id.addLocationTV)
        addLocationImageView      = headerView.findViewById(R.id.addLocationIV)
        addLocationLinearLayout   = headerView.findViewById(R.id.addLocationLinearLayout)
        addLocationLinearLayout.setOnClickListener {

//            if(drawerLayout.isDrawerOpen(GravityCompat.START))
//                drawerLayout.closeDrawer(GravityCompat.START)

            startActivity(Intent(this@MainActivity,AddLocationActivity::class.java))

        }

        navHeaderRV = headerView.findViewById(R.id.drawerLocationsRV)
        navHeaderRVAdapter = NavHeaderRVAdapter(this)

        viewAllLocationsTV=headerView.findViewById(R.id.viewAllLocationsTV)
        viewAllLocationsTV.setOnClickListener {
            //            if(drawerLayout.isDrawerOpen(GravityCompat.START))
            //            drawerLayout.closeDrawer(GravityCompat.START)

            startActivity(Intent(this@MainActivity,AllLocationsActivity::class.java))
        }

        settingsButton = headerView.findViewById(R.id.drawerSettings)
        settingsButton.setOnClickListener {
            val intent = Intent(this,SettingsActivity::class.java)
            startActivity(intent)
        }

    }

    private fun overrideDrawerSlide() {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Scale the drawer offset to vary how far the content should move
                val moveFactor = (drawerView.width * slideOffset)

                // Translate the main content view to the right as the drawer opens
                binding.mainCardView.translationX = moveFactor
                binding.mainCardView.translationY = moveFactor*0.1f

                // Adjust transparency of the main content view
                // You can adjust the 0.7f to more or less transparency as you see fit
                val contentAlpha = 1 - slideOffset * 0.2f
                binding.mainContentView.alpha = contentAlpha
                binding.mainCardView.alpha = contentAlpha

                val colorStart = R.color.gray_bkg  // assuming the original color is white
                val colorEnd = R.color.drawerStartTopColor   // the target color you want

//                // Blend the colors based on the slideOffset
                val blendedColor = ColorUtils.blendARGB(getColor(colorStart), getColor(colorEnd), slideOffset)
                window.statusBarColor=blendedColor
                val cornerRadius = slideOffset * resources.getDimension(R.dimen.max_corner_radius)

                // Apply the corner radius to the card view
                binding.mainCardView.radius = cornerRadius
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onDrawerOpened(drawerView: View)
            {
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
//                val newCornerRadius = resources.getDimension(R.dimen.max_corner_radius)
//                cardView.radius = newCornerRadius
//                cardView.requestLayout()
//                cardView.invalidate()
//                val currentRadius = cardView.radius
//                val newRadius = resources.getDimension(R.dimen.max_corner_radius)
                // Apply the corner radius to the card view
                //cardView.radius = cornerRadius
                //cardView.invalidate()

                // Animate the corner radius change
//                ValueAnimator.ofFloat(currentRadius, newRadius).apply {
//                    addUpdateListener { animator ->
//                        val value = animator.animatedValue as Float
//                        cardView.radius = value
//                    }
//                    duration = 300 // Duration in milliseconds
//                    start()
//                }

                //cardView.setRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, applicationContext.resources.displayMetrics))

//                cardView.radius = resources.getDimension(R.dimen.max_corner_radius)

                //window.statusBarColor=getColor(R.color.drawerStartTopColor)
            }

            override fun onDrawerClosed(drawerView: View)
            {
                window.insetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
                // Reset the alpha back to fully opaque when the drawer is closed
                binding.mainContentView.alpha = 1.0f

            }

            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    private fun initDrawerLayout() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            // Handle menu item selected
            drawerLayout.closeDrawers()
            true
        }
        drawerLayout.setScrimColor(Color.TRANSPARENT)
    }

    private fun adapterInit() {
        mainForecastAdapter = WeatherToday()
        fiveDaysForeCastAdapter=FiveDayForeCastAdapter()
        fiveThreeHoursForecastAdapter=FiveThreeHoursForecastAdapter()
    }

    private fun formatDate_d_MM_yyyy(dtTxt: String?): String? {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dtTxt)
        val outputFormat= SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val dateAndDayName= outputFormat.format(date!!)
        val dateAndDayNameEdited = dateAndDayName.split(" ")[0]+"th "+dateAndDayName.split(" ")[1]+" "+dateAndDayName.split(" ")[2]
        return dateAndDayNameEdited
    }

    private fun formatDate_d_MM_EEEE(dtTxt: String?):String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dtTxt)
        val outputFormat= SimpleDateFormat("d MMMM EEEE", Locale.getDefault())
        val dateAndDayName= outputFormat.format(date!!)
        return dateAndDayName
    }

    override fun onStart() {
        super.onStart()
        locationSwitchStateReceiver = LocationSwitchStateReceiver(this)
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(locationSwitchStateReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(locationSwitchStateReceiver)
    }

    override fun onResume() {
        super.onResume()
        val citiesAndCountryMap= loadAllLocations()
        val citiesAndCountryList=citiesAndCountryMap.keys.toList().takeLast(3)

        updateDrawerRV(citiesAndCountryList)
//        locationViewModel.checkLocationServices()
        Log.d("getLastLocationFromSharedPrefs","${getLastLocationFromSharedPrefs()}")
        binding.searchView.setQuery(getLastLocationFromSharedPrefs(),true)
    }

    private fun initUI(){
        toolbar             = findViewById(R.id.mainToolbar)
        setSupportActionBar(toolbar)
//        adViewBanner1       = findViewById(R.id.adViewBanner1)
        drawerLayout        = findViewById(R.id.drawer_layout)
        navView             = findViewById(R.id.nav_view)
        mainForecastRV      = findViewById(R.id.forecastRecyclerView)
        todayForecastRV     = findViewById(R.id.todayForecastRecyclerView)
        openDrawerImage     = findViewById(R.id.openDrawerImage)
        cityFromMapFragment = findViewById(R.id.cityFromMapFramgentMain)
        windType            = findViewById(R.id.windType)
        mainWindSpeed       = findViewById(R.id.mainWindSpeed)
        mainAtmPressure     = findViewById(R.id.mainAtmPressure)
        mapFragmentConLayout= findViewById(R.id.mapFragmentConstrainedLayout)
    }

    private fun setMainImage(icon:String?){
        if(icon=="01d"){
            binding.imageMain.setImageResource(R.drawable.sunclear)
        }
        else if (icon=="01n"){
            binding.imageMain.setImageResource(R.drawable.moonclear)
        }
        else if (icon=="02d"){
            binding.imageMain.setImageResource(R.drawable.suncloud)
        }
        else if (icon=="02n"){
            binding.imageMain.setImageResource(R.drawable.mooncloud)
        }
        else if (icon=="03d" || icon=="03n"){
            binding.imageMain.setImageResource(R.drawable.singlecloud)
        }
        else if (icon=="04d" || icon=="04n"){
            binding.imageMain.setImageResource(R.drawable.brokenclouds)
        }
        else if (icon=="09d" || icon=="09n"){
            binding.imageMain.setImageResource(R.drawable.showerrain)
        }
        else if (icon=="10d"){
            binding.imageMain.setImageResource(R.drawable.suncloudrain)
        }
        else if (icon=="10n"){
            binding.imageMain.setImageResource(R.drawable.mooncloudrain)
        }
        else if (icon=="11d" || icon=="11n"){
            binding.imageMain.setImageResource(R.drawable.lightningstorm)
        }
        else if (icon=="13d" || icon=="13n"){
            binding.imageMain.setImageResource(R.drawable.snow)
        }
        else if (icon=="14d"){
            binding.imageMain.setImageResource(R.drawable.sunmist)
        }
        else if (icon=="14n"){
            binding.imageMain.setImageResource(R.drawable.moonmist)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData(mFusedLocationClient:FusedLocationProviderClient){
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 3600000
        ).build()
        mFusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                val latitude = locationResult.lastLocation?.latitude!!.toString()
                val longitude= locationResult.lastLocation?.longitude!!.toString()

                onMapReadyWork(latitude,longitude)
                if(useCurrentLocationForDrawer==true){
                    weatherVM.getWeatherGPSMVVM(city = null,lat = latitude, lon = longitude)
                    useCurrentLocationForDrawer=false}

                weatherVM.getWeatherMVVM(city = null,lat = latitude, lon = longitude)

//                Toast.makeText(this@MainActivity,"Latitude: ${locationResult.lastLocation?.latitude!!} \n " +
//                      "Longitude: ${locationResult.lastLocation?.longitude!!}",Toast.LENGTH_SHORT).show()
            }
        }, Looper.myLooper())
    }

    private fun onMapReadyWork(lat:String, lon:String) {

        Log.d("LatLonForMap","\nLat=$lat \n Lon=$lon")

        val latLng = LatLng(lat.toDouble(),lon.toDouble())

        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(latLng))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,12f))

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
                    exception.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    // The user agreed to change the location settings
//                    Toast.makeText(this@MainActivity, "The user agreed to change the location settings", Toast.LENGTH_LONG).show()
                } else {
                    // The user did not agree to change the location settings
//                    Toast.makeText(this@MainActivity, "The user did not agree to change the location settings", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onLocationSettingsChanged(isEnabled: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLocationCheckTime > LOCATION_CHECK_DEBOUNCE) {
            lastLocationCheckTime = currentTime
            locationViewModel.updateLocationStatus(isEnabled)
        }
    }
    override fun onBackPressed() {
        // Override onBackPressed to handle the back button press

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else if (binding.searchView!=null&&!binding.searchView.isIconified) {
//            binding.searchView.isIconified = true
//            binding.searchView.isIconified = true
//            binding.searchView.setQuery("", false)
//            binding.searchView.clearFocus()
//            binding.searchView.onActionViewCollapsed()
//            // If the SearchView is not collapsed, collapse it and show the TextView
            binding.searchView.isIconified = true
            binding.cityCountryQuery.visibility = View.VISIBLE
        }
//        else if (binding.searchView.query.isNotEmpty()) {
//            // If the SearchView is already collapsed but the query is not empty, clear the query
//            binding.searchView.setQuery("", false)}
        else {
            super.onBackPressed()
        }
    }

    override fun onItemClick(city: String) {
//        Toast.makeText(this, "Clicked on ${city}", Toast.LENGTH_SHORT).show()
        saveLastSearchedOrSelectedCityToSharedPrefs(city)
        Log.d("LastSetCity","${getLastLocationFromSharedPrefs()}")
        binding.searchView.setQuery(city.substringBefore(","),true)
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.uiSettings.setAllGesturesEnabled(false)
    }
}