package com.app.weatherstack.datausage

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initialize(this)
    }
}

