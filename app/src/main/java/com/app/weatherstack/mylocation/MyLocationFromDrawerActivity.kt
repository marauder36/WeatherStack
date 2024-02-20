package com.app.weatherstack.mylocation

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.app.weatherstack.R
import com.app.weatherstack.WeatherList
import com.app.weatherstack.adapters.FiveDayForeCastAdapter
import com.app.weatherstack.adapters.FiveThreeHoursForecastAdapter
import com.app.weatherstack.repositories.WeatherRepository
import com.app.weatherstack.retrofit.WeatherRetrofitInstance
import com.app.weatherstack.utils.DateTimeValsUtils
import com.app.weatherstack.viewmodelfactories.ViewModelFactory
import com.app.weatherstack.viewmodels.WeatherViewModel

class MyLocationFromDrawerActivity : AppCompatActivity() {

    private lateinit var toolbar                        : Toolbar
    private lateinit var backArrowCurrent               : ImageView
    private lateinit var currentLocationWeatherImage    : ImageView
    private lateinit var currentLocationCityCountry     : TextView
    private lateinit var currentWeatherDesc             : TextView
    private lateinit var currentMainTemp                : TextView
    private lateinit var current53ForecastRV            : RecyclerView
    private lateinit var current53ForecastRVAdapter     : FiveThreeHoursForecastAdapter
    private lateinit var monthDayYearCurrent            : TextView
    private lateinit var current5DaysForecastRV         : RecyclerView
    private lateinit var current5DaysForecastRVAdapter  : FiveDayForeCastAdapter
    private lateinit var currentWindSpeed               : TextView
    private lateinit var currentPressure                : TextView

    private lateinit var weatherVM                      : WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_location_from_drawer)

        initUI()

        val service = WeatherRetrofitInstance.api
        val repository= WeatherRepository(service)
        weatherVM = ViewModelProvider(this, ViewModelFactory(repository)).get(WeatherViewModel::class.java)

        weatherVM.getWeatherMVVM(getLastGPSLocationFromSharedPrefs())
        weatherVM.getForeCastUpcomingMVVM(getLastGPSLocationFromSharedPrefs())
        setupObservers()



    }



    private fun initUI() {
        toolbar                      = findViewById(R.id.currentToolbar)
        backArrowCurrent             = findViewById(R.id.backArrowDarkCurrent)
        backArrowCurrent.setOnClickListener {
            finish()
        }
        currentLocationWeatherImage  = findViewById(R.id.currentLocationImage)
        currentLocationCityCountry   = findViewById(R.id.currentLocationCityCountry)
        currentWeatherDesc           = findViewById(R.id.currentWeatherDesc)
        currentMainTemp              = findViewById(R.id.currentMainTemp)
        monthDayYearCurrent          = findViewById(R.id.day_month_year_current)
        currentWindSpeed             = findViewById(R.id.currentWindSpeed)
        currentPressure              = findViewById(R.id.currentAtmPressure)

        current53ForecastRV          = findViewById(R.id.cityCurrentDetailsForecastRV)
        current53ForecastRVAdapter   = FiveThreeHoursForecastAdapter()
        current53ForecastRV.adapter  = current53ForecastRVAdapter

        current5DaysForecastRV       = findViewById(R.id.forecastRecyclerViewCurrent)
        current5DaysForecastRVAdapter= FiveDayForeCastAdapter()
        current5DaysForecastRV.adapter=current5DaysForecastRVAdapter


    }

    private fun setupObservers() {

        weatherVM.cityCountryCode.observe(this, Observer {
            currentLocationCityCountry.text = it
        })

        weatherVM.closeToOrExactlySameWeatherData.observe(this, Observer {

            if(getLastTempSelectedUnit()=="C")
                currentMainTemp.text = DateTimeValsUtils.getTempInCelsius(it?.main?.temp?: 25.0)
            else
                currentMainTemp.text = DateTimeValsUtils.getTempInFahrenheit(it?.main?.temp?: 25.0)

            if(getLastWindSelectedUnit()=="Km")
                currentWindSpeed.text= DateTimeValsUtils.getWindSpeedInKmPerH(it?.wind?.speed?: 0.0)
            else
                currentWindSpeed.text= DateTimeValsUtils.getWindSpeedInMilesPerH(it?.wind?.speed?: 0.0)

            if(getLastPressureSelectedUnit()=="mmHg")
                currentPressure.text = DateTimeValsUtils.getPressureInmmHg(it?.main?.pressure?: 1021)
            else
                currentPressure.text = DateTimeValsUtils.getPressureInmBar(it?.main?.pressure?: 1021)


            monthDayYearCurrent.text=DateTimeValsUtils.formatDate_dayofweek_d_MM_yyyy(it?.dtTxt)

            val desc = it!!.weather[0].description.toString()
            val upperFirstChar = desc[0].uppercaseChar()
            currentWeatherDesc.text= upperFirstChar + desc.substring(1)

        })

        weatherVM.closestFiveThreeHoursForecast.observe(this, Observer {
            val setNewList = it as List<WeatherList>
            val nextDatePattern = DateTimeValsUtils.getNextDatePattern()
            val isTomorrowPresent=setNewList.filter { weatherList -> weatherList.dtTxt?.startsWith(nextDatePattern)==true }
            current53ForecastRVAdapter.setList(setNewList)
            current53ForecastRVAdapter.notifyDataSetChanged()

        })

        weatherVM.forecastWeatherLiveData.observe(this, Observer {

            val setNewList = it as List<WeatherList>

            current5DaysForecastRVAdapter.setList(setNewList)
            current5DaysForecastRVAdapter.notifyDataSetChanged()

        })

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

    private fun getLastGPSLocationFromSharedPrefs():String {
        val sharedPreferences = getSharedPreferences("LastGPSLocation", Context.MODE_PRIVATE)
        val lastCityCountry = sharedPreferences.getString("LastCityCountryCode", null)
//        Toast.makeText(this,"LastCity $lastCityCountry", Toast.LENGTH_SHORT).show()
        return lastCityCountry ?: "Bucharest, RO"
    }
}