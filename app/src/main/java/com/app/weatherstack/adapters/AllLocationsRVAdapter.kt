package com.app.weatherstack.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.weatherstack.R
import com.app.weatherstack.SharedPrefsHelper
import com.app.weatherstack.utils.DateTimeValsUtils
import com.app.weatherstack.weather_models.CityWeather

class AllLocationsRVAdapter( private val itemClickListener: OnItemClickListener) :RecyclerView.Adapter<AllLocationsRVAdapter.CityViewHolder>(){


    private var listOfCityAndCountryName = listOf<CityWeather>()
    private val selectedItems            = mutableSetOf<CityWeather>()


    interface OnItemClickListener {
        fun onItemClick(city: CityWeather)
    }

    inner class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.cityAndCountryNameAllCities)
        val pinImageView:ImageView=view.findViewById(R.id.locationPinCL)
        val mainConstraintLayout:ConstraintLayout=view.findViewById(R.id.mainConstraintLayoutAL)
        val temp : TextView=view.findViewById(R.id.weatherAllCities)

        val imageView:ImageView= view.findViewById(R.id.allCitiesWeatherIcon)

        fun bind(city: CityWeather, clickListener: OnItemClickListener) {
            textView.text = city.cityName+", "+city.countryName
            itemView.setOnClickListener {
                clickListener.onItemClick(city)
                if (city in selectedItems) {
                    selectedItems.remove(city)
                    mainConstraintLayout.setBackgroundResource((if(city in selectedItems) R.color.secondaru else R.color.white))
                    textView.setTextColor(if(city in selectedItems) Color.WHITE else Color.BLACK)
                    pinImageView.setImageResource(if(city in selectedItems) R.drawable.gradient_transparent_location_pin else R.drawable.location_pin_dark)

                }
            }
                if(city.weatherIconId=="01d"){
                    imageView.setImageResource(R.drawable.sunclear)
                }
                else if (city.weatherIconId=="01n"){
                    imageView.setImageResource(R.drawable.moonclear)
                }
                else if (city.weatherIconId=="02d"){
                    imageView.setImageResource(R.drawable.suncloud)
                }
                else if (city.weatherIconId=="02n"){
                    imageView.setImageResource(R.drawable.mooncloud)
                }
                else if (city.weatherIconId=="03d" || city.weatherIconId=="03n"){
                    imageView.setImageResource(R.drawable.singlecloud)
                }
                else if (city.weatherIconId=="04d" || city.weatherIconId=="04n"){
                    imageView.setImageResource(R.drawable.brokenclouds)
                }
                else if (city.weatherIconId=="09d" || city.weatherIconId=="09n"){
                    imageView.setImageResource(R.drawable.showerrain)
                }
                else if (city.weatherIconId=="10d"){
                    imageView.setImageResource(R.drawable.suncloudrain)
                }
                else if (city.weatherIconId=="10n"){
                    imageView.setImageResource(R.drawable.mooncloudrain)
                }
                else if (city.weatherIconId=="11d" || city.weatherIconId=="11n"){
                    imageView.setImageResource(R.drawable.lightningstorm)
                }
                else if (city.weatherIconId=="13d" || city.weatherIconId=="13n"){
                    imageView.setImageResource(R.drawable.snow)
                }
                else if (city.weatherIconId=="14d"){
                    imageView.setImageResource(R.drawable.sunmist)
                }
                else if (city.weatherIconId=="14n"){
                    imageView.setImageResource(R.drawable.moonmist)
                }

            mainConstraintLayout.setBackgroundResource((if(city in selectedItems) R.color.secondaru else R.color.white))
            textView.setTextColor(if(city in selectedItems) Color.WHITE else Color.BLACK)
            pinImageView.setImageResource(if(city in selectedItems) R.drawable.gradient_transparent_location_pin else R.drawable.location_pin_dark)

            if(SharedPrefsHelper.getSelectedUnitType()=="F")
                temp.text=DateTimeValsUtils.getTempInFahrenheit(city.temp.toDouble())
            else
                temp.text=DateTimeValsUtils.getTempInCelsius(city.temp.toDouble())

            itemView.setOnLongClickListener {
                // Toggle selection
                if (city in selectedItems) {
                    selectedItems.remove(city)
                } else {
                    selectedItems.add(city)
                }
                notifyItemChanged(adapterPosition)
                true
            }
        }
    }

    fun getSelectedItems(): Set<CityWeather> = selectedItems

    fun clearSelections() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

//    private var itemClickListener: ((Int) -> Unit)? = null
//
//    fun setOnItemClickListener(listener: (Int) -> Unit) {
//        itemClickListener = listener
//    }



    fun setList(newList:List<CityWeather>){
        this.listOfCityAndCountryName=newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.all_locations_rv_item_layout,parent,false)
        return CityViewHolder(view)
    }


    override fun getItemCount(): Int {
        return listOfCityAndCountryName.size
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val cityAndCountryObject = listOfCityAndCountryName[position]

        holder.bind(cityAndCountryObject,itemClickListener)
//
//        holder.itemView.setOnClickListener{
//            itemClickListener?.invoke(holder.adapterPosition)
//        }
    }


}