package com.app.weatherstack.adapters

import android.os.Build
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

class ForeCastAdapter :RecyclerView.Adapter<ForeCastHolder>(){

    private var listOfForecast = listOf<WeatherList>()
    fun setList(newList:List<WeatherList>){
        this.listOfForecast=newList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForeCastHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.upcomingforecastlist,parent,false)
        return ForeCastHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfForecast.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ForeCastHolder, position: Int) {
        val forecastObject = listOfForecast[position]

        for(i in forecastObject.weather){
            holder.description.text=i.description!!
        }

        holder.humidity.text = forecastObject.main!!.humidity.toString()
        holder.windSpeed.text= forecastObject.wind?.speed.toString()

        if(SharedPrefsHelper.getSelectedUnitType()=="F")
            holder.temp.text= DateTimeValsUtils.getTempInFahrenheit(forecastObject.main?.temp)
        else
            holder.temp.text= DateTimeValsUtils.getTempInCelsius(forecastObject.main?.temp)

        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = inputFormat.parse(forecastObject.dtTxt!!)
        val outputFormat= SimpleDateFormat("d MMMM EEEE", Locale.getDefault())
        val dateAndDayName= outputFormat.format(date!!)
        holder.dateDayName.text = dateAndDayName

        for(i in forecastObject.weather){

            if(i.icon=="01d"){
                holder.imageGraphic.setImageResource(R.drawable.sunclear)
                holder.smallIcon.setImageResource(R.drawable.sunclear)
            }
            else if (i.icon=="01n"){
                holder.imageGraphic.setImageResource(R.drawable.moonclear)
                holder.smallIcon.setImageResource(R.drawable.moonclear)

            }
            else if (i.icon=="02d"){
                holder.imageGraphic.setImageResource(R.drawable.suncloud)
                holder.smallIcon.setImageResource(R.drawable.suncloud)
            }
            else if (i.icon=="02n"){
                holder.imageGraphic.setImageResource(R.drawable.mooncloud)
                holder.smallIcon.setImageResource(R.drawable.mooncloud)
            }
            else if (i.icon=="03d" || i.icon=="03n"){
                holder.imageGraphic.setImageResource(R.drawable.singlecloud)
                holder.smallIcon.setImageResource(R.drawable.singlecloud)
            }
            else if (i.icon=="04d" || i.icon=="04n"){
                holder.imageGraphic.setImageResource(R.drawable.brokenclouds)
                holder.smallIcon.setImageResource(R.drawable.brokenclouds)
            }
            else if (i.icon=="09d" || i.icon=="09n"){
                holder.imageGraphic.setImageResource(R.drawable.showerrain)
                holder.smallIcon.setImageResource(R.drawable.showerrain)
            }
            else if (i.icon=="10d"){
                holder.imageGraphic.setImageResource(R.drawable.suncloudrain)
                holder.smallIcon.setImageResource(R.drawable.suncloudrain)
            }
            else if (i.icon=="10n"){
                holder.imageGraphic.setImageResource(R.drawable.mooncloudrain)
                holder.smallIcon.setImageResource(R.drawable.mooncloudrain)
            }
            else if (i.icon=="11d" || i.icon=="11n"){
                holder.imageGraphic.setImageResource(R.drawable.lightningstorm)
                holder.smallIcon.setImageResource(R.drawable.lightningstorm)
            }
            else if (i.icon=="13d" || i.icon=="13n"){
                holder.imageGraphic.setImageResource(R.drawable.snow)
                holder.smallIcon.setImageResource(R.drawable.snow)
            }
            else if (i.icon=="14d"){
                holder.imageGraphic.setImageResource(R.drawable.sunmist)
                holder.smallIcon.setImageResource(R.drawable.sunmist)
            }
            else if (i.icon=="14n"){
                holder.imageGraphic.setImageResource(R.drawable.moonmist)
                holder.smallIcon.setImageResource(R.drawable.moonmist)
            }
        }
    }


}
class ForeCastHolder(itemView: View):ViewHolder(itemView){
    val imageGraphic: ImageView= itemView.findViewById(R.id.imageGraphic)
    val description : TextView = itemView.findViewById(R.id.weatherDesc)
    val humidity    : TextView = itemView.findViewById(R.id.humidity)
    val windSpeed   : TextView = itemView.findViewById(R.id.windSpeed)
    val temp        : TextView = itemView.findViewById(R.id.tempDisplayForecast)
    val smallIcon   : ImageView= itemView.findViewById(R.id.smallIcon)
    val dateDayName : TextView = itemView.findViewById(R.id.dayDateText)
}