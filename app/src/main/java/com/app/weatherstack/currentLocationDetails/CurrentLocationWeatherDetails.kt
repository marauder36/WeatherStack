package com.app.weatherstack.currentLocationDetails

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.app.weatherstack.R
import com.app.weatherstack.WeatherList
import com.app.weatherstack.adapters.FiveThreeHoursForecastAdapter
import com.app.weatherstack.repositories.WeatherRepository
import com.app.weatherstack.retrofit.WeatherRetrofitInstance
import com.app.weatherstack.utils.Constants
import com.app.weatherstack.utils.DateTimeValsUtils
import com.app.weatherstack.viewmodelfactories.ViewModelFactory
import com.app.weatherstack.viewmodels.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class CurrentLocationWeatherDetails : AppCompatActivity() {

    private lateinit var cityDetailsName                : TextView
    private lateinit var cityDetailsWeatherIcon         : ImageView
    private lateinit var cityDetailsWeatherDesc         : TextView
    private lateinit var cityDetailsTodayDate           : TextView
    private lateinit var cityDetailsForecastRV          : RecyclerView
    private lateinit var cityDetailsForecastRVAdapter   : FiveThreeHoursForecastAdapter
    private lateinit var cityDetailsTemp                : TextView
    private lateinit var cityDetailsWindSpeed           : TextView
    private lateinit var cityDetailsHumidity            : TextView
    private lateinit var cityDetailsPressure            : TextView
    private lateinit var backArrow                      : ImageView
    private lateinit var weatherVM                      : WeatherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_location_weather_details)

        WindowCompat.setDecorFitsSystemWindows(window,false)

        val service = WeatherRetrofitInstance.api
        val repository= WeatherRepository(service)
        weatherVM = ViewModelProvider(this, ViewModelFactory(repository)).get(WeatherViewModel::class.java)

        initUI()
        getData()
        setupObservers()
        
    }

    private fun setupObservers() {
        weatherVM.closeToOrExactlySameWeatherData.observe(this, Observer { weatherList->

            if(weatherList!=null)
            {
                setMainImage(weatherList.weather[0].icon)

                cityDetailsWeatherDesc.text = weatherList.weather[0].description.toString()[0].uppercaseChar()+
                        weatherList.weather[0].description.toString().substring(1)

                cityDetailsTodayDate.text   = formatDate_d_MM_yyyy(weatherList.dtTxt)

                if(getLastTempSelectedUnit()=="C")
                    cityDetailsTemp.text        = DateTimeValsUtils.getTempInCelsius(weatherList.main?.temp!!)
                else
                    cityDetailsTemp.text        = DateTimeValsUtils.getTempInFahrenheit(weatherList.main?.temp!!)

                if(getLastWindSelectedUnit()=="Km")
                    cityDetailsWindSpeed.text   = DateTimeValsUtils.getWindSpeedInKmPerH(weatherList.wind?.speed)
                else
                {
                    cityDetailsWindSpeed.text   = DateTimeValsUtils.getWindSpeedInMilesPerH(weatherList.wind?.speed)
                }

                cityDetailsHumidity.text    = "${ weatherList.main?.humidity.toString() } %"

                if(getLastPressureSelectedUnit()=="mmHg")
                    cityDetailsPressure.text    = DateTimeValsUtils.getPressureInmmHg(weatherList.main!!.pressure)
                else
                    cityDetailsPressure.text    = DateTimeValsUtils.getPressureInmBar(weatherList.main!!.pressure)

            }
            
        })

        weatherVM.cityCountryCode.observe(this, Observer {
            cityDetailsName.text = it
        })

        weatherVM.closestFiveThreeHoursForecast.observe(this, Observer {
            val setNewList = it as List<WeatherList>
            val nextDatePattern = DateTimeValsUtils.getNextDatePattern()
            val isTomorrowPresent=setNewList.filter { weatherList -> weatherList.dtTxt?.startsWith(nextDatePattern)==true }
            cityDetailsForecastRVAdapter.setList(setNewList)
            cityDetailsForecastRV.adapter=cityDetailsForecastRVAdapter
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

    private fun tempTransform(temp: Double):String{
        val temperatureKelvin = temp
        val temperatureCelsius = (temperatureKelvin.minus(273.15))
        val temperatureFahrenheit = (temperatureCelsius.times(9).div(5).plus(32))
        val temperatureFormated = temperatureCelsius.roundToInt().toString()
        return if (getTempUnit()=="C")
            "$temperatureFormated°C"
        else
            "$temperatureFahrenheit.roundToInt().toString()°F"
    }

    private fun getTempUnit(): String {
        val sharedPreferences = getSharedPreferences("TempUnit", Context.MODE_PRIVATE)
        val tempUnits = sharedPreferences.getString("LastSelected", null)
        return tempUnits ?: "C"
    }

    private fun formatDate_d_MM_yyyy(dtTxt: String?): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = dtTxt?.let { inputFormat.parse(it) }
        val outputFormatDate= SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val dateAndDayName= outputFormatDate.format(date!!)
        val outputFormatDayName= SimpleDateFormat("EEEE", Locale.getDefault())
        val dayName= outputFormatDayName.format(date)
        val dateAndDayNameEdited =dayName+", "+ dateAndDayName.split(" ")[0]+ "th "+dateAndDayName.split(" ")[1]+" "+dateAndDayName.split(" ")[2]
        return dateAndDayNameEdited
    }

    private fun setMainImage(icon:String?){
        when (icon) {
            "01d" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.sunclear)
            }
            "01n" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.moonclear)
            }
            "02d" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.suncloud)
            }
            "02n" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.mooncloud)
            }
            "03d", "03n" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.singlecloud)
            }
            "04d", "04n" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.brokenclouds)
            }
            "09d", "09n" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.showerrain)
            }
            "10d" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.suncloudrain)
            }
            "10n" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.mooncloudrain)
            }
            "11d", "11n" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.lightningstorm)
            }
            "13d", "13n" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.snow)
            }
            "14d" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.sunmist)
            }
            "14n" -> {
                cityDetailsWeatherIcon.setImageResource(R.drawable.moonmist)
            }
        }
    }

    private fun getData() {
        val cityFromMain = intent.getStringExtra(Constants.cityFromMainActivity)
        if(cityFromMain!=null && cityFromMain!="null")
        {
            weatherVM.getWeatherMVVM(cityFromMain)
            weatherVM.getForeCastUpcomingMVVM(cityFromMain)
        }
        else
        {
            weatherVM.getWeatherMVVM(city = "Bucharest")
            weatherVM.getForeCastUpcomingMVVM(city = "Bucharest")
        }
    }

    private fun initUI() {
        cityDetailsForecastRV           = findViewById(R.id.cityDetailsForecastRV)
        cityDetailsForecastRVAdapter    = FiveThreeHoursForecastAdapter()
        cityDetailsPressure             = findViewById(R.id.detailsUVindex)
        cityDetailsName                 = findViewById(R.id.city_details_name)
        cityDetailsWeatherIcon          = findViewById(R.id.weatherIconDetails)
        cityDetailsWeatherDesc          = findViewById(R.id.detailsWeatherDesc)
        cityDetailsTodayDate            = findViewById(R.id.detailsTodayDate)
        cityDetailsTemp                 = findViewById(R.id.detailsTempInDegrees)
        cityDetailsWindSpeed            = findViewById(R.id.detailsWindSpeed)
        cityDetailsHumidity             = findViewById(R.id.detailsHumidity)
        backArrow                       = findViewById(R.id.backArrowDetails)
        backArrow.setOnClickListener {
            finish()
        }
    }
}