package com.app.weatherstack.alllocations

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.app.weatherstack.R
import com.app.weatherstack.adapters.AllLocationsRVAdapter
import com.app.weatherstack.repositories.WeatherRepository
import com.app.weatherstack.retrofit.WeatherRetrofitInstance
import com.app.weatherstack.viewmodelfactories.ViewModelFactory
import com.app.weatherstack.viewmodels.WeatherViewModel
import com.app.weatherstack.weather_models.CityWeather
import com.app.weatherstack.weather_models.CityWeatherList

class AllLocationsActivity : AppCompatActivity(), AllLocationsRVAdapter.OnItemClickListener {

    private lateinit var weatherVM: WeatherViewModel

    private lateinit var allLocationsRV: RecyclerView
    private lateinit var allLocationsRVAdapter: AllLocationsRVAdapter
    private lateinit var deleteButton: ImageView
    private lateinit var backButton: ImageView
    private lateinit var allCitiesSearchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_locations)

        initUI()

        val service = WeatherRetrofitInstance.api
        val repository= WeatherRepository(service)
        weatherVM = ViewModelProvider(this, ViewModelFactory(repository)).get(WeatherViewModel::class.java)

        val locationsMap = loadAllLocations()
        val locationsList = locationsMap.keys.toList()
        Log.d("locationsList", "$locationsList")

        val locationsIcons: MutableList<CityWeatherList> = mutableListOf()
        val locationsNames: MutableList<String>          = mutableListOf()
        for (str in locationsList) {
            val parts = str.split(",") // Split the string by comma
            if (parts.isNotEmpty()) {
                val firstPart = parts[0] // Get the first part (before the comma)
                locationsNames.add(firstPart) // Add it to the result list
            }
        }

        Log.d("LocationsNameFinal","$locationsNames")
        var cnt = 0

        weatherVM.fetchWeatherForCities(locationsNames)
        weatherVM.cityWeathersLiveData.observe(this, Observer {
            Log.d("LocationsNameFinal","${it}")
            allLocationsRVAdapter.setList(it)
            allLocationsRVAdapter.notifyDataSetChanged()
            val modifiedList=it.toMutableList()

            deleteButton.setOnClickListener {

                val  selectedItems = allLocationsRVAdapter.getSelectedItems()
                if(selectedItems.isNotEmpty())
                    showDataLimitDialog(selectedItems,modifiedList)
                else
                    showCustomToast(this)
            }

            allCitiesSearchView.setOnQueryTextListener(object : OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if(query!=null)
                        modifyListForRV(query,modifiedList)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if(newText!=null)
                        modifyListForRV(newText,modifiedList)
                    return false
                }

            })

        })

    }

    private fun showCustomToast(context: Context) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast_delete_locations, null)

        // Find the ImageView and TextView in the custom layout
        val image = layout.findViewById<ImageView>(R.id.custom_toast_image)
        val text = layout.findViewById<TextView>(R.id.custom_toast_message)

        // Set the text and image as needed
        text.text = "Long press on a saved location to select it, then press trash bin again to delete it."
        // You can dynamically set the image here if you like:
        image.setImageResource(R.drawable.long_click_white)
        // Create the Toast object and set the custom view
        val toast = Toast(context).apply {
            duration = Toast.LENGTH_LONG
            view = layout
            setGravity(Gravity.CENTER_HORIZONTAL, 0, -600)
        }

        toast.show()
    }

    private fun showDataLimitDialog(selectedItems: Set<CityWeather>, modifiedList: MutableList<CityWeather>) {

        // Inflate the custom layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.delete_location_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)

        // Set the custom layout as dialog view
        dialogBuilder.setView(dialogView)

        // Initialize the elements of your custom layout
        var selectedItemsCount = 0
        var shownItems = mutableListOf<CityWeather>()
        val currentSelectedCities   = dialogView.findViewById<TextView>(R.id.citiesTV)
        val yesButtonDeleteDialog   = dialogView.findViewById<TextView>(R.id.yesButtonDeleteDialog)
        val cancelButtonDeleteDialog= dialogView.findViewById<TextView>(R.id.cancelButtonDeleteDialog)




        // Create the AlertDialog object
        val alertDialog = dialogBuilder.create()

        for (city in selectedItems)
        {
            if(selectedItemsCount<=2) {
                shownItems.add(city)
                selectedItemsCount++
            }
            else
                break
        }

        Log.d("ShownItems","$shownItems")

        if(selectedItemsCount<=2)
        {
            for (city in shownItems)
            {
                val currentText = currentSelectedCities.text.toString()
                currentSelectedCities.text ="$currentText\n${city.cityName}, ${city.countryName}"
            }
        }
        else
        {
            for (city in shownItems.take(2))
            {
                val currentText = currentSelectedCities.text.toString()
                Log.d("currentText1", "$currentText")
                currentSelectedCities.text ="$currentText\n${city.cityName}, ${city.countryName}"
            }
            val currentText1 = currentSelectedCities.text.toString()
            Log.d("currentText1", "$currentText1")
            currentSelectedCities.text = "$currentText1\nAnd more..."
        }

        yesButtonDeleteDialog.setOnClickListener {
            removeSelectedItemsFromSharedPrefs(selectedItems)
            Log.d("SelectedItems", "$selectedItems")
            modifiedList.removeAll(selectedItems)
            Log.d("SelectedItemsModifiedList", "$modifiedList")
            if(allCitiesSearchView.query.isNullOrEmpty()) {
                allLocationsRVAdapter.setList(modifiedList)

            }
            else
            {
                val query = allCitiesSearchView.query
                allCitiesSearchView.setQuery(query,true)
            }
            allLocationsRVAdapter.clearSelections()
            alertDialog.dismiss()
        }

        cancelButtonDeleteDialog.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(true)



        alertDialog.dismiss()

        // Display the custom AlertDialog
        alertDialog.show()
    }

    private fun removeSelectedItemsFromSharedPrefs(selectedItems: Set<CityWeather>) {
        for(item in selectedItems)
        {
            val sharedPreferences = getSharedPreferences("CityCountryNames", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            //editor.clear()
            editor.remove("${item.cityName}, ${item.countryName}")
            editor.apply()
        }
    }

    private fun initUI() {
        allLocationsRV = findViewById(R.id.allLocationsRV)
        allLocationsRVAdapter = AllLocationsRVAdapter(this)
        allLocationsRV.adapter=allLocationsRVAdapter
        deleteButton = findViewById(R.id.addCityDelete)
        backButton = findViewById(R.id.backArrowDarkAL)
        backButton.setOnClickListener {
            finish()
        }
        allCitiesSearchView=findViewById(R.id.allCitiesSearchView)

    }

    private fun modifyListForRV(query:String?, modifiedList:MutableList<CityWeather>){
        Log.d("ContainsQuery","Modified List: $modifiedList \n Contains: $query")

        val filteredList = modifiedList.filter{cityWeather->
                    cityWeather.cityName.contains(query!!,ignoreCase = true) ||
                    cityWeather.countryName.contains(query!!,ignoreCase = true)
        }

        allLocationsRVAdapter.setList(filteredList)
        allLocationsRVAdapter.notifyDataSetChanged()
        Log.d("ContainsQueryFiltList","Filtered List: $filteredList \n Contains: $query")
//        for(el in modifiedList)
//        {
//            Log.d("ContainsQueryCityName","ModListEl: ${el.cityName} \n Contains: $query")
//
//            val stringContainingQuery = el.cityName.any{customObject->
//
//                query.lowercase()
//
//            }
//            if(el.cityName.contains(query.toString()))
//                Log.d("ContainsQueryFunction","Element: ${el.cityName} Contains: $query")
//        }

    }

    override fun onItemClick(city: CityWeather) {
//        Toast.makeText(this,"${city.cityName.substringAfter("=")}",Toast.LENGTH_SHORT).show()
        saveLastSearchedOrSelectedCityToSharedPrefs(city.cityName)
        finish()
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
}