package com.app.weatherstack.context

import android.app.Application

class MyApplication : Application() {

    companion object{

        lateinit var instance : MyApplication

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}