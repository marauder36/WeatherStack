package com.app.weatherstack.settings

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.TrafficStats
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.weatherstack.R
import com.app.weatherstack.datausage.DataUsageViewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var backArrowDarkCurrent   : ImageView

    private lateinit var tempUnitSelect         : ConstraintLayout
    private lateinit var celsiusSelect          : TextView
    private lateinit var celsiusBackgroundLL    : LinearLayout
    private lateinit var fahrenheitSelect       : TextView
    private lateinit var fahrenheitBackgroundLL : LinearLayout

    private lateinit var windUnitSelect         : ConstraintLayout
    private lateinit var milesSelect            : TextView
    private lateinit var milesBackgroundLL      : LinearLayout
    private lateinit var kmSelect               : TextView
    private lateinit var kmBackgroundLL         : LinearLayout

    private lateinit var pressureUnitSelect     : ConstraintLayout
    private lateinit var mmHgSelect             : TextView
    private lateinit var mmHgBackgroundLL       : LinearLayout
    private lateinit var mBarSelect             : TextView
    private lateinit var mBarBackgroundLL       : LinearLayout

//    private lateinit var proCL                  : ConstraintLayout
//    private lateinit var aboutCL                : ConstraintLayout
    private lateinit var shareCL                : ConstraintLayout
    private lateinit var joinCL                 : ConstraintLayout
    private lateinit var mobileDataCL           : ConstraintLayout
    private lateinit var reviewCL               : ConstraintLayout
    private lateinit var feedbackCL             : ConstraintLayout

    private lateinit var viewModel: DataUsageViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initUI()
        checkSettings()
        setOnClickListeners()

        tempUnitSelect.setOnClickListener {
            tempUnitSelectChanger()
        }

        windUnitSelect.setOnClickListener {
            windUnitSelectChanger()
        }

        pressureUnitSelect.setOnClickListener {
            pressureUnitSelectChanger()
        }

        mobileDataCL.setOnClickListener {
            dataLimit()
        }

//        val dataUsageWorkerRequest = PeriodicWorkRequestBuilder<DataUsageWorker>(1, TimeUnit.MINUTES)
//            .build()
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//            "dataUsageWorker",
//            ExistingPeriodicWorkPolicy.KEEP,
//            dataUsageWorkerRequest
//        )
//        viewModel = ViewModelProvider(this).get(DataUsageViewModel::class.java)
//        viewModel.dataUsage.observe(this, Observer { usage ->
//            // Update UI with the current data usage
//        })


    }

    private fun dataLimit() {
        val uid = android.os.Process.myUid()
        val rxBytes = TrafficStats.getUidRxBytes(uid)
        val txBytes = TrafficStats.getUidTxBytes(uid)
        val totalBytes = rxBytes + txBytes

        showDataLimitDialog(totalBytes,getLastDataLimit())
    }

    private fun showDataLimitDialog(currentUsage: Long, dataLimit: String) {

        // Inflate the custom layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_data_limit, null)
        val dialogBuilder = AlertDialog.Builder(this)

        // Set the custom layout as dialog view
        dialogBuilder.setView(dialogView)

        // Initialize the elements of your custom layout
        val currentLimitTV   = dialogView.findViewById<TextView>(R.id.tvCurrentLimit)
        val currentUsageTV   = dialogView.findViewById<TextView>(R.id.tvCurrentUsage)
        val newLimitET       = dialogView.findViewById<EditText>(R.id.etDataLimit)
        val setNewLimitButton= dialogView.findViewById<AppCompatButton>(R.id.setNewLimitButton)

        // Create the AlertDialog object
        val alertDialog = dialogBuilder.create()

        if(dataLimit=="Unlimited")
            currentLimitTV.text= dataLimit
        else
            currentLimitTV.text= dataLimit+" MB"

        currentUsageTV.text= "${currentUsage / (1024 * 1024)} MB"


        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(true)

        setNewLimitButton.setOnClickListener {


            if(newLimitET.text.isNotEmpty())
                saveLastDataLimit(newLimitET.text.toString())
            else
                saveLastDataLimit("Unlimited")

            alertDialog.dismiss()
        }

        // Display the custom AlertDialog
        alertDialog.show()
    }

    private fun getLastDataLimit():String {
        val sharedPreferences = getSharedPreferences("DataLimitSelected", Context.MODE_PRIVATE)
        val unit = sharedPreferences.getString("DataLimit", null)
        return unit ?: "Unlimited"
    }

    private fun saveLastDataLimit(unit:String) {
        val sharedPreferences = getSharedPreferences("DataLimitSelected", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("DataLimit", "$unit")
        editor.apply()
    }

    private fun pressureUnitSelectChanger() {
        if(getLastPressureSelectedUnit()=="mmHg") {
            saveLastPressureSelectedUnit("mBar")
            changePressureUnitsAppereance()
        }
        else {
            saveLastPressureSelectedUnit("mmHg")
            changePressureUnitsAppereance()
        }
    }

    private fun windUnitSelectChanger() {
        if(getLastWindSelectedUnit()=="Km") {
            saveLastWindSelectedUnit("Miles")
            changeWindUnitsAppereance()
        }
        else {
            saveLastWindSelectedUnit("Km")
            changeWindUnitsAppereance()
        }
    }

    private fun checkSettings() {
        if(getLastTempSelectedUnit()=="C")
        {
            fahrenheitBackgroundLL.visibility = View.INVISIBLE
        }
        else
        {
            celsiusBackgroundLL.visibility = View.INVISIBLE
        }

        if(getLastWindSelectedUnit()=="Miles")
        {
            kmBackgroundLL.visibility = View.INVISIBLE
        }
        else
        {
            milesBackgroundLL.visibility=View.INVISIBLE
        }

        if(getLastPressureSelectedUnit()=="mBar")
        {
            mmHgBackgroundLL.visibility= View.INVISIBLE
        }
        else
        {
            mBarBackgroundLL.visibility= View.INVISIBLE
        }
    }

    private fun tempUnitSelectChanger() {
        if(getLastTempSelectedUnit()=="C") {
            saveLastTempSelectedUnit("F")
            changeTempUnitsAppereance()
        }
        else {
            saveLastTempSelectedUnit("C")
            changeTempUnitsAppereance()
        }
    }

    private fun changePressureUnitsAppereance() {
        if(getLastPressureSelectedUnit()=="mmHg"){
            slidePressureToLeft()
        }
        else if(getLastPressureSelectedUnit()=="mBar"){
            slidePressureToRight()
        }
    }
    
    private fun changeWindUnitsAppereance() {
        if(getLastWindSelectedUnit()=="Km"){
            slideWindToLeft()
        }
        else if(getLastWindSelectedUnit()=="Miles"){
            slideWindToRight()
        }
    }

    private fun changeTempUnitsAppereance() {
        if(getLastTempSelectedUnit()=="C"){
//            celsiusSelect.setTextColor(getColor(R.color.white))
//            celsiusBackgroundLL.setBackgroundResource(R.drawable.blue_gradient_top_to_bottom_drawer)
//            fahrenheitSelect.setTextColor(getColor(R.color.secondaru))
//            fahrenheitBackgroundLL.setBackgroundResource(R.color.gray_bkg)
            slideTempToRight()
        }
        else if(getLastTempSelectedUnit()=="F"){
//            celsiusSelect.setTextColor(getColor(R.color.secondaru))
//            celsiusBackgroundLL.setBackgroundResource(R.color.gray_bkg)
//            fahrenheitSelect.setTextColor(getColor(R.color.white))
//            fahrenheitBackgroundLL.setBackgroundResource(R.drawable.blue_gradient_right_to_left)
        slideTempToLeft()
        }
    }

    private fun slideTempToLeft() {
        // Slide the Celsius to the left out of view
        celsiusBackgroundLL.animate()
            .translationX(celsiusBackgroundLL.width.toFloat())
            .alpha(0.0f)
            .setDuration(500)
            .withEndAction {
                // Set to invisible at the end of animation
                celsiusBackgroundLL.visibility  = View.INVISIBLE
            }
            .start()

        // Bring the Fahrenheit in from the right
        fahrenheitBackgroundLL.translationX = -fahrenheitBackgroundLL.width.toFloat()
        fahrenheitBackgroundLL.visibility = View.VISIBLE
        fahrenheitBackgroundLL.animate()
            .translationX(0f)
            .alpha(1.0f)
            .setDuration(500)
            .start()
    }

    private fun slideWindToLeft() {
        // Slide the Celsius to the left out of view
        milesBackgroundLL.animate()
            .translationX(milesBackgroundLL.width.toFloat())
            .alpha(0.0f)
            .setDuration(500)
            .withEndAction {
                // Set to invisible at the end of animation
                milesBackgroundLL.visibility  = View.INVISIBLE
            }
            .start()

        // Bring the Fahrenheit in from the right
        kmBackgroundLL.translationX = -kmBackgroundLL.width.toFloat()
        kmBackgroundLL.visibility = View.VISIBLE
        kmBackgroundLL.animate()
            .translationX(0f)
            .alpha(1.0f)
            .setDuration(500)
            .start()
    }

    private fun slidePressureToLeft() {
        // Slide the Celsius to the left out of view
        mBarBackgroundLL.animate()
            .translationX(-mBarBackgroundLL.width.toFloat())
            .alpha(0.0f)
            .setDuration(500)
            .withEndAction {
                // Set to invisible at the end of animation
                mBarBackgroundLL.visibility  = View.INVISIBLE
            }
            .start()

        // Bring the Fahrenheit in from the right
        mmHgBackgroundLL.translationX = mmHgBackgroundLL.width.toFloat()
        mmHgBackgroundLL.visibility = View.VISIBLE
        mmHgBackgroundLL.animate()
            .translationX(0f)
            .alpha(1.0f)
            .setDuration(500)
            .start()
    }

    private fun slideTempToRight() {
        // Slide the Fahrenheit to the right out of view
        fahrenheitBackgroundLL.animate()
            .translationX(-fahrenheitBackgroundLL.width.toFloat())
            .alpha(0.0f)
            .setDuration(500)
            .withEndAction {
                // Set to invisible at the end of animation
                fahrenheitBackgroundLL.visibility = View.INVISIBLE
            }
            .start()

        // Bring the Celsius in from the left
        celsiusBackgroundLL.translationX = celsiusBackgroundLL.width.toFloat()
        celsiusBackgroundLL.visibility = View.VISIBLE
        celsiusBackgroundLL.animate()
            .translationX(0f)
            .alpha(1.0f)
            .setDuration(500)
            .start()
    }

    private fun slideWindToRight() {
        // Slide the Fahrenheit to the right out of view
        kmBackgroundLL.animate()
            .translationX(-kmBackgroundLL.width.toFloat())
            .alpha(0.0f)
            .setDuration(500)
            .withEndAction {
                // Set to invisible at the end of animation
                kmBackgroundLL.visibility = View.INVISIBLE
            }
            .start()

        // Bring the Celsius in from the left
        milesBackgroundLL.translationX = milesBackgroundLL.width.toFloat()
        milesBackgroundLL.visibility = View.VISIBLE
        milesBackgroundLL.animate()
            .translationX(0f)
            .alpha(1.0f)
            .setDuration(500)
            .start()
    }

    private fun slidePressureToRight() {
        // Slide the Fahrenheit to the right out of view
        mmHgBackgroundLL.animate()
            .translationX(mmHgBackgroundLL.width.toFloat())
            .alpha(0.0f)
            .setDuration(500)
            .withEndAction {
                // Set to invisible at the end of animation
                mmHgBackgroundLL.visibility = View.INVISIBLE
            }
            .start()

        // Bring the Celsius in from the left
        mBarBackgroundLL.translationX = -mBarBackgroundLL.width.toFloat()
        mBarBackgroundLL.visibility = View.VISIBLE
        mBarBackgroundLL.animate()
            .translationX(0f)
            .alpha(1.0f)
            .setDuration(500)
            .start()
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

    private fun saveLastTempSelectedUnit(unit:String) {
        val sharedPreferences = getSharedPreferences("LastSelectedTempUnit", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("LastTempUnit", "$unit")
        editor.apply()
    }



    private fun saveLastWindSelectedUnit(unit:String) {
        val sharedPreferences = getSharedPreferences("LastSelectedWindUnit", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("LastWindUnit", "$unit")
        editor.apply()
    }



    private fun saveLastPressureSelectedUnit(unit:String) {
        val sharedPreferences = getSharedPreferences("LastSelectedPressureUnit", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //editor.clear()
        editor.putString("LastPressureUnit", "$unit")
        editor.apply()
    }

    private fun initUI() {

        backArrowDarkCurrent    =findViewById(R.id.backArrowDarkCurrent)
        backArrowDarkCurrent.setOnClickListener {
            finish()
        }

        tempUnitSelect          =findViewById(R.id.tempUnitSelect)
        celsiusSelect           =findViewById(R.id.celsiusSelect)
        celsiusBackgroundLL     =findViewById(R.id.celsiusBackgroundLL)
        fahrenheitSelect        =findViewById(R.id.fahrenheitSelect)
        fahrenheitBackgroundLL  =findViewById(R.id.fahrenheitBackgroundLL)

        windUnitSelect          =findViewById(R.id.windUnitSelect)
        milesSelect             =findViewById(R.id.milesSelect)
        milesBackgroundLL       =findViewById(R.id.milesBackgroundLL)
        kmSelect                =findViewById(R.id.kilometersSelect)
        kmBackgroundLL          =findViewById(R.id.kilometersBackgroundLL)

        pressureUnitSelect      =findViewById(R.id.pressureUnitSelect)
        mmHgSelect              =findViewById(R.id.mmHgSelect)
        mmHgBackgroundLL        =findViewById(R.id.mmHgBackgroundLL)
        mBarSelect              =findViewById(R.id.mBarSelect)
        mBarBackgroundLL        =findViewById(R.id.mBarBackgroundLL)

//        proCL                   =findViewById(R.id.proCL)
//        aboutCL                 =findViewById(R.id.aboutCL)
        shareCL                 =findViewById(R.id.shareCL)
        joinCL                  =findViewById(R.id.joinCL)
        mobileDataCL            =findViewById(R.id.mobileDataCL)
        reviewCL                =findViewById(R.id.reviewCL)
        feedbackCL              =findViewById(R.id.feedbackCL)
    }

    private fun setOnClickListeners()
    {
//        proCL.setOnClickListener {}
//        aboutCL.setOnClickListener {}
        shareCL.setOnClickListener {}
        joinCL.setOnClickListener {}
        mobileDataCL.setOnClickListener {}
        reviewCL.setOnClickListener {}
        feedbackCL.setOnClickListener {}
    }
    object DataUsageManager {
        var isDataLimitReached = false
    }

}