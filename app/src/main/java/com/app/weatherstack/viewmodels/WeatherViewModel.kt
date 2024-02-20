package com.app.weatherstack.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.weatherstack.WeatherList
import com.app.weatherstack.context.MyApplication
import com.app.weatherstack.repositories.WeatherRepository
import com.app.weatherstack.retrofit.WeatherRetrofitInstance
import com.app.weatherstack.utils.DateTimeValsUtils
import com.app.weatherstack.utils.SharedPrefs
import com.app.weatherstack.weather_models.CityWeather
import com.app.weatherstack.weather_models.CityWeatherList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    val todayWeatherLiveData            = MutableLiveData<List<WeatherList>>()
    val forecastWeatherLiveData         = MutableLiveData<List<WeatherList>>()
    val closeToOrExactlySameWeatherData = MutableLiveData<WeatherList?>()
    val cityName                        = MutableLiveData<String?>()
    val countryName                     = MutableLiveData<String?>()
    val cityCountryCode                 = MutableLiveData<String?>()
    val closestFiveThreeHoursForecast   = MutableLiveData<List<WeatherList?>>()
    val cityWeather                     = MutableLiveData<CityWeatherList>()
    val cityWeathersLiveData = MutableLiveData<List<CityWeather>>()
    val cityCountryGPS       = MutableLiveData<String?>()

    val context = MyApplication.instance


        // Use this pattern to encapsulate errors and loading states as well
        val weatherDataState = MutableLiveData<WeatherDataState>()
    val weatherDataStateGPS = MutableLiveData<WeatherDataState>()

    fun getWeatherGPSMVVM(city: String? = null,lat:String?=null,lon:String?=null) = viewModelScope.launch(Dispatchers.IO) {
        Log.d("GPSMVVMResponse0", "Entered function")
        weatherDataStateGPS.postValue(WeatherDataState.Loading)

        try {
            Log.d("GPSMVVMResponse0", "Entered try")
            val response = repository.getWeatherData(city,lat,lon) // Use a method from the repository
            Log.d("GPSMVVMResponse1", response.toString())
            if (response.isSuccessful) {

                response.body()?.let { forecast ->





                    var cityName    =forecast.city?.name
                    var countryName =forecast.city?.country

                    if(cityName!!.isEmpty())
                        cityName="No data"
                    if (countryName!!.isEmpty())
                        countryName="No data"

                    cityCountryGPS.postValue("${cityName}, ${countryName}")

                }

                weatherDataStateGPS.postValue(WeatherDataState.Success)

            } else {
                weatherDataStateGPS.postValue(WeatherDataState.Error("Error fetching weather data"))
            }
        } catch (e: Exception) {
            weatherDataStateGPS.postValue(WeatherDataState.Error(e.message ?: "Unknown error"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeatherMVVM(city: String? = null,lat:String?=null,lon:String?=null) = viewModelScope.launch(Dispatchers.IO) {
        Log.d("MVVMResponse0", "Entered function")
        weatherDataState.postValue(WeatherDataState.Loading)

        try {
            Log.d("MVVMResponse0", "Entered try")
            val response = repository.getWeatherData(city,lat,lon) // Use a method from the repository
            Log.d("MVVMResponse1", response.toString())
            if (response.isSuccessful) {

                response.body()?.let { forecast ->



                    cityName.postValue(forecast.city?.name ?: "No data")
                    Log.d("MVVMResponse2", "CityName: ${forecast.city?.name}")

                    Log.d("MVVMResponse7", "currentDatePattern: ${forecast.weatherList}")

                    countryName.postValue(forecast.city?.country)

                    var cityName    =forecast.city?.name
                    var countryName =forecast.city?.country

                    if(cityName!!.isEmpty())
                        cityName="No data"
                    if (countryName!!.isEmpty())
                        countryName="No data"

                    cityCountryCode.postValue("${cityName}, ${countryName}")
                    val todayWeather = filterTodayWeather(forecast.weatherList)
                    Log.d("MVVMResponse3", "TodayWeather: $todayWeather")

                    todayWeatherLiveData.postValue(todayWeather)

                    Log.d("MVVMResponse4", "ExactWeather: ${findClosestWeatherMVVM(todayWeather)}")
                    cityWeather.postValue(CityWeatherList(forecast.city?.name!!,findClosestWeather(todayWeather)!!))

                    closeToOrExactlySameWeatherData.postValue(findClosestWeatherMVVM(todayWeather))
                    Log.d("MVVMResponse4", "ExactWeather: ${findClosestWeatherMVVM(todayWeather)}")

                    val fiveThreeHoursForecast = getFiveThreeHoursForecast(forecast.weatherList)
                    Log.d("MVVMResponse10", "FiveThreeHoursForecast: ${fiveThreeHoursForecast}")
                    closestFiveThreeHoursForecast.postValue(fiveThreeHoursForecast)
                }

                weatherDataState.postValue(WeatherDataState.Success)

            } else {
                weatherDataState.postValue(WeatherDataState.Error("Error fetching weather data"))
            }
        } catch (e: Exception) {
            weatherDataState.postValue(WeatherDataState.Error(e.message ?: "Unknown error"))
        }
    }

    fun fetchWeatherForCities(cities: List<String>) =
        viewModelScope.launch(Dispatchers.IO) {
            val cityWeathers = mutableListOf<CityWeather>()
            for (cityName in cities) {
                try {
                    val response = repository.getWeatherData(cityName)
                    if (response.isSuccessful) {
                        response.body()?.let { weatherData ->
                            // Assuming you have a method to parse the response and get the required fields
                            val cityWeather = CityWeather(
                                cityName = cityName,
                                countryName = weatherData.city?.country!!, // Replace with actual field from your response
                                weatherIconId = weatherData.weatherList[0].weather[0].icon!!, // Replace with actual field from your response
                                temp=weatherData.weatherList[0].main?.temp.toString()
                            )
                            cityWeathers.add(cityWeather)
                        }
                    }
                } catch (e: Exception) {
                    // Handle exception (e.g., log it or add an error entry to cityWeathers)
                }
            }
            cityWeathersLiveData.postValue(cityWeathers)
        }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFiveThreeHoursForecast(weatherList: List<WeatherList>): List<WeatherList> {
        Log.d("todayWeatherListSize","AdditionalWeatherListSize=${weatherList}")


        val currentDatePattern = DateTimeValsUtils.getCurrentDatePattern()
        Log.d("todayWeatherListSize","AdditionalWeatherListSize=${currentDatePattern}")

        val nextDatePattern = DateTimeValsUtils.getNextDatePattern() // This function should return the pattern for the next day.
        Log.d("todayWeatherListSize","AdditionalWeatherListSize=${nextDatePattern}")

        // Filter for today's forecasts.
        val todayWeatherList = weatherList.filter { weather -> weather.dtTxt?.startsWith(currentDatePattern) == true }
        Log.d("todayWeatherListSize","WeatherListSize=${todayWeatherList.size}")
        // If we already have 5 forecasts for today, return them.
        if (todayWeatherList.size >= 5) {
            Log.d("todayWeatherListSize","${todayWeatherList.size}")
            return todayWeatherList.take(5)
        }

        // Otherwise, add forecasts from the next day.
        val additionalWeatherList = weatherList.filter { weather -> weather.dtTxt?.startsWith(nextDatePattern) == true }
        Log.d("todayWeatherListSize","AdditionalWeatherListSize=${additionalWeatherList.size}")

        val totalWeatherList = todayWeatherList + additionalWeatherList.take(5 - todayWeatherList.size)

        return totalWeatherList.take(5)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getForeCastUpcomingMVVM(city: String? = null) = viewModelScope.launch(Dispatchers.IO) {

        Log.d("MVVMResponse0", "Entered function")
        weatherDataState.postValue(WeatherDataState.Loading)

        try {
            Log.d("MVVMResponse00", "Entered try")

            val response = repository.getWeatherData(city) // Use a method from the repository
            Log.d("MVVMResponse11", response.toString())
            if (response.isSuccessful) {

                response.body()?.let { forecast ->

                    cityName.postValue(forecast.city?.name ?: "No data")
                    Log.d("MVVMResponse22", "CityName: ${forecast.city?.name}")

                    countryName.postValue(forecast.city?.country)
                    Log.d("MVVMResponse44", "TodayWeather: ${forecast.city?.country}")

                    val todayWeather = filterTodayWeatherExclude(forecast.weatherList)
                    Log.d("MVVMResponse33", "TodayWeather: $todayWeather")

                    forecastWeatherLiveData.postValue(todayWeather)

                }

                weatherDataState.postValue(WeatherDataState.Success)

            } else {
                weatherDataState.postValue(WeatherDataState.Error("Error fetching weather data"))
            }
        } catch (e: Exception) {
            weatherDataState.postValue(WeatherDataState.Error(e.message ?: "Unknown error"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterTodayWeatherExclude(weatherList: List<WeatherList>): List<WeatherList> {
        val currentDatePattern = DateTimeValsUtils.getCurrentDatePattern()
        return weatherList.filterNot { weather ->weather.dtTxt?.startsWith(currentDatePattern) == true}
                          .filter    { weather ->weather.dtTxt!!.endsWith("12:00:00")}

        }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterTodayWeather(weatherList: List<WeatherList>): List<WeatherList> {
        val currentDatePattern = DateTimeValsUtils.getCurrentDatePattern()
        Log.d("MVVMResponse5", "currentDatePattern: ${currentDatePattern}")
        Log.d("MVVMResponse6", "currentDatePattern: ${weatherList}")
        val todayWeather = weatherList.filter { weather ->
            weather.dtTxt?.startsWith(currentDatePattern) == true
        }
        if(todayWeather.isNotEmpty()){
            return todayWeather
        }
        else
        {
            val tomorrowZi=currentDatePattern.split("-")[2].toInt()+1
            Log.d("MVVMResponse8", "tomorrowZI: ${tomorrowZi}")
            val tomorrowDatePatter=currentDatePattern.split("-")[0]+"-"+currentDatePattern.split("-")[1]+"-"+tomorrowZi
            Log.d("MVVMResponse9", "TomorrowDatepattern: ${tomorrowDatePatter}")
            return weatherList.filter { weather ->
                weather.dtTxt?.startsWith(tomorrowDatePatter) == true
            }.filter { weather ->weather.dtTxt!!.substring(11,16)=="00:00"   }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun findClosestWeatherMVVM(weatherList: List<WeatherList>): WeatherList? {
        val systemTime = DateTimeValsUtils.timeToMinutes(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        )
        Log.d("MVVMResponse4InFUNCtion", "$weatherList")
        return weatherList.minBy { weather ->
            val weatherTime = DateTimeValsUtils.timeToMinutes(weather.dtTxt!!.substring(11, 16))
            abs(systemTime - weatherTime)
        }
    }


    //luam date despre vreme in background folosind coroutines

//    fun getWeather(city: String?=null)=viewModelScope.launch(Dispatchers.IO){
//
//        val todayWeatherList = mutableListOf<WeatherList>()
//
//        val currentDateTime     = LocalDateTime.now()
//        val currentDatePattern  = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//
//        val sharedPrefs = SharedPrefs.getInstance(context)
//        val lat= sharedPrefs.getValue("lat").toString()
//        val lon= sharedPrefs.getValue("lon").toString()
//
//        val call = if(city!=null){WeatherRetrofitInstance.api.getWeatherByCityOG(city)}
//                   else{WeatherRetrofitInstance.api.getCurrentWeatherOG(lat,lon)}
//
//        val response = call.execute()
//        Log.d("ResponseTest1",response.toString())
//        if(response.isSuccessful){
//
//            val weatherList = response.body()?.weatherList
//            Log.d("ResponseTest2","WeatherList: $weatherList")
//            cityName.postValue(response.body()?.city!!.name!!)
//            Log.d("ResponseTest3","City Name: $cityName")
//            val presentDate = currentDatePattern
//            Log.d("ResponseTest4","Present Date: $presentDate")
//            //separam toate obiectele de tip weather care au data de azi
//            weatherList?.forEach {weather->
//                if(weather.dtTxt!!.split("\\s".toRegex()).contains(presentDate)){
//                    Log.d("ResponseTest5","Weather: $weather")
//                    todayWeatherList.add(weather)}
//                Log.d("ResponseTest6","TodayWeatherList: $todayWeatherList")
//            }
//
//            //ne dorim sa afisam cea mai apropiata prognoza meteo de timpul curent al sistemului
//            val closestWeather = findClosestWeather(todayWeatherList)
//            Log.d("ResponseTest7","Closest Weather: $closestWeather")
//            closeToOrExactlySameWeatherData.postValue(closestWeather)
//
//            todayWeatherLiveData.postValue(todayWeatherList)
//        }
//
//    }
//    fun getForeCastUpcoming(city: String?=null)=viewModelScope.launch(Dispatchers.IO){
//
//        val forecastWeatherList = mutableListOf<WeatherList>()
//
//        val currentDateTime     = LocalDateTime.now()
//        val currentDatePattern  = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//
//        val sharedPrefs = SharedPrefs.getInstance(context)
//        val lat= sharedPrefs.getValue("lat").toString()
//        val lon= sharedPrefs.getValue("lon").toString()
//
//        val call = if(city!=null){WeatherRetrofitInstance.api.getWeatherByCityOG(city)}
//                   else{WeatherRetrofitInstance.api.getCurrentWeatherOG(lat,lon)}
//
//        val response = call.execute()
//        if(response.isSuccessful){
//
//            val weatherList = response.body()?.weatherList
//
//            cityName.postValue(response.body()?.city!!.name!!)
//
//            val presentDate = currentDatePattern
//
//            //separam toate obiectele de tip weather care NU au data de azi
//            weatherList?.forEach {weather->
//                if(!weather.dtTxt!!.split("\\s".toRegex()).contains(presentDate)){
//                    if(weather.dtTxt!!.substring(11,16)=="12:00"){
//
//                        forecastWeatherList.add(weather)}
//                }
//            }
//
//
//
//            forecastWeatherLiveData.postValue(forecastWeatherList)
//        }
//
//    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun findClosestWeather(weatherList: List<WeatherList>): WeatherList? {
        val systemTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        var closestWeather:WeatherList?=null
        var minTimeDifference = Int.MAX_VALUE

        for(weather in weatherList){
            val weatherTime = weather.dtTxt!!.substring(11,16)
            val timeDifference = Math.abs(timeToMinutes(weatherTime)-timeToMinutes(systemTime))
            if(timeDifference<minTimeDifference){
                minTimeDifference=timeDifference
                closestWeather=weather
            }
        }
        return closestWeather

    }
    private fun timeToMinutes(time:String):Int{
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}

sealed class WeatherDataState {
    object Loading : WeatherDataState()
    object Success : WeatherDataState()
    data class Error(val message: String) : WeatherDataState()
}