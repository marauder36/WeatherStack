package com.app.weatherstack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.weatherstack.R

class NavHeaderRVAdapter( private val itemClickListener: OnItemClickListener) :RecyclerView.Adapter<NavHeaderRVAdapter.CityViewHolder>(){

    interface OnItemClickListener {
        fun onItemClick(city: String)
    }

    class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.cityAndCountryName)

        fun bind(city: String, clickListener: OnItemClickListener) {
            textView.text = city
            itemView.setOnClickListener {
                clickListener.onItemClick(city)
            }
        }
    }

    private var listOfCityAndCountryName = listOf<String>()

//    private var itemClickListener: ((Int) -> Unit)? = null
//
//    fun setOnItemClickListener(listener: (Int) -> Unit) {
//        itemClickListener = listener
//    }



    fun setList(newList:List<String>){
        this.listOfCityAndCountryName=newList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.drawer_locations_rv_layout,parent,false)
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