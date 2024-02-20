package com.app.weatherstack

import com.google.gson.annotations.SerializedName


data class Rain (

  @SerializedName("f3h" ) var f3h : Double? = null

)