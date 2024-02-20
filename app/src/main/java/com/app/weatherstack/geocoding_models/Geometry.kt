package com.example.example

import com.google.gson.annotations.SerializedName


data class Geometry (

  @SerializedName("location"      ) var location     : Location? = Location(),
  @SerializedName("location_type" ) var locationType : String?   = null,
  @SerializedName("viewport"      ) var viewport     : Viewport? = Viewport()

)