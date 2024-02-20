package com.app.weatherstack.adapters

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.app.weatherstack.R
import com.app.weatherstack.SharedPrefsHelper
import com.app.weatherstack.WeatherList
import com.app.weatherstack.utils.DateTimeValsUtils
import java.text.SimpleDateFormat
import java.util.Locale

class FiveThreeHoursForecastAdapter :RecyclerView.Adapter<FiveThreeHoursForecastHolder>(){

    private var listOfForecast = listOf<WeatherList>()
    fun setList(newList:List<WeatherList>){
        this.listOfForecast=newList
    }

    fun convertTemp(temperatureKelvin:Double?):String{
        if(SharedPrefsHelper.getSelectedUnitType()=="F")
            return DateTimeValsUtils.getTempInFahrenheit(temperatureKelvin)
        else
            return DateTimeValsUtils.getTempInCelsius(temperatureKelvin)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FiveThreeHoursForecastHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fivethreehoursforecast,parent,false)
        return FiveThreeHoursForecastHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfForecast.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: FiveThreeHoursForecastHolder, position: Int) {
        val forecastObject = listOfForecast[position]

        val nextDatePattern = DateTimeValsUtils.getNextDatePattern()
        if(forecastObject.dtTxt!!.startsWith(nextDatePattern))
            holder.todayOrTomorrow.visibility=View.VISIBLE

        holder.tempFiveThree.text=convertTemp(forecastObject.main?.temp)
        Log.d("Temperature","tempFiveThree: ${forecastObject.main?.temp}")
        Log.d("Temperature","${forecastObject.main.toString()}")
        Log.d("Temperature","${forecastObject.dtTxt.toString()}")

        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = inputFormat.parse(forecastObject.dtTxt!!)
        val outputFormat= SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateAndDayName= outputFormat.format(date!!)
        holder.hourThreeFive.text = dateAndDayName

        for(i in forecastObject.weather){

            if(i.icon=="01d"){
                holder.imageGraphic.setImageResource(R.drawable.sunclear)

            }
            else if (i.icon=="01n"){
                holder.imageGraphic.setImageResource(R.drawable.moonclear)


            }
            else if (i.icon=="02d"){
                holder.imageGraphic.setImageResource(R.drawable.suncloud)

            }
            else if (i.icon=="02n"){
                holder.imageGraphic.setImageResource(R.drawable.mooncloud)

            }
            else if (i.icon=="03d" || i.icon=="03n"){
                holder.imageGraphic.setImageResource(R.drawable.singlecloud)

            }
            else if (i.icon=="04d" || i.icon=="04n"){
                holder.imageGraphic.setImageResource(R.drawable.brokenclouds)

            }
            else if (i.icon=="09d" || i.icon=="09n"){
                holder.imageGraphic.setImageResource(R.drawable.showerrain)

            }
            else if (i.icon=="10d"){
                holder.imageGraphic.setImageResource(R.drawable.suncloudrain)

            }
            else if (i.icon=="10n"){
                holder.imageGraphic.setImageResource(R.drawable.mooncloudrain)

            }
            else if (i.icon=="11d" || i.icon=="11n"){
                holder.imageGraphic.setImageResource(R.drawable.lightningstorm)

            }
            else if (i.icon=="13d" || i.icon=="13n"){
                holder.imageGraphic.setImageResource(R.drawable.snow)

            }
            else if (i.icon=="14d"){
                holder.imageGraphic.setImageResource(R.drawable.sunmist)

            }
            else if (i.icon=="14n"){
                holder.imageGraphic.setImageResource(R.drawable.moonmist)

            }
        }
    }


}
class FiveThreeHoursForecastHolder(itemView: View):ViewHolder(itemView){
    val imageGraphic: ImageView= itemView.findViewById(R.id.imageGraphic)
    val tempFiveThree : TextView = itemView.findViewById(R.id.tempDisplayfivethree)
    val hourThreeFive : TextView = itemView.findViewById(R.id.hourthreefive)
    val todayOrTomorrow:TextView = itemView.findViewById(R.id.today_or_tomorrow)
}