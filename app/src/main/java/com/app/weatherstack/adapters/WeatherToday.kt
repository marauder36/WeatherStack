package com.app.weatherstack.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.app.weatherstack.R
import com.app.weatherstack.SharedPrefsHelper
import com.app.weatherstack.WeatherList
import com.app.weatherstack.utils.DateTimeValsUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeatherToday:RecyclerView.Adapter<TodayHolder>() {

    private var listOfTodayWeather= listOf<WeatherList>()

    fun setList(listOfToday:List<WeatherList>){
        this.listOfTodayWeather=listOfToday
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.todayhourlylist,parent,false)
        return TodayHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfTodayWeather.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TodayHolder, position: Int) {
        val todayForeCast = listOfTodayWeather[position]
        holder.timeDisplay.text = todayForeCast.dtTxt!!.substring(11,16).toString()

        if(SharedPrefsHelper.getSelectedUnitType()=="F")
            holder.tempDisplay.text= DateTimeValsUtils.getTempInFahrenheit(todayForeCast.main?.temp)
        else
            holder.tempDisplay.text= DateTimeValsUtils.getTempInCelsius(todayForeCast.main?.temp)

        val calendar = Calendar.getInstance()
        val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formatedTime=hourFormat.format(calendar.time)

        val timeOfAPI = todayForeCast.dtTxt!!.split(" ")
        val splitSection = timeOfAPI[1]

        Log.e("time", "$formatedTime:${formatedTime}, time from API: ${splitSection}")

        for(i in todayForeCast.weather){

            if(i.icon=="01d"){
                holder.imageDisplay.setImageResource(R.drawable.sunclear)
            }
            else if (i.icon=="01n"){
                holder.imageDisplay.setImageResource(R.drawable.moonclear)
            }
            else if (i.icon=="02d"){
                holder.imageDisplay.setImageResource(R.drawable.suncloud)
            }
            else if (i.icon=="02n"){
                holder.imageDisplay.setImageResource(R.drawable.mooncloud)
            }
            else if (i.icon=="03d" || i.icon=="03n"){
                holder.imageDisplay.setImageResource(R.drawable.singlecloud)
            }
            else if (i.icon=="04d" || i.icon=="04n"){
                holder.imageDisplay.setImageResource(R.drawable.brokenclouds)
            }
            else if (i.icon=="09d" || i.icon=="09n"){
                holder.imageDisplay.setImageResource(R.drawable.showerrain)
            }
            else if (i.icon=="10d"){
                holder.imageDisplay.setImageResource(R.drawable.suncloudrain)
            }
            else if (i.icon=="10n"){
                holder.imageDisplay.setImageResource(R.drawable.mooncloudrain)
            }
            else if (i.icon=="11d" || i.icon=="11n"){
                holder.imageDisplay.setImageResource(R.drawable.lightningstorm)
            }
            else if (i.icon=="13d" || i.icon=="13n"){
                holder.imageDisplay.setImageResource(R.drawable.snow)
            }
            else if (i.icon=="14d"){
                holder.imageDisplay.setImageResource(R.drawable.sunmist)
            }
            else if (i.icon=="14n"){
                holder.imageDisplay.setImageResource(R.drawable.moonmist)
            }
        }
    }


}

class TodayHolder(itemview:View):RecyclerView.ViewHolder(itemview){

    val imageDisplay: ImageView = itemView.findViewById(R.id.imageDisplay)
    val tempDisplay: TextView = itemview.findViewById(R.id.tempDisplay)
    val timeDisplay: TextView = itemview.findViewById(R.id.timeDisplay)

}