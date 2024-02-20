package com.example.example

import com.google.gson.annotations.SerializedName


data class GeocodingResponse (

  @SerializedName("results" ) var results : ArrayList<Results> = arrayListOf(),
  @SerializedName("status"  ) var status  : String?            = null

)