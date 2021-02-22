package com.apolis.bltindoor.ui

import android.app.Application
import android.content.Context
import android.util.Log
import com.apolis.bltindoor.helper.AppComponent

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
//        appComponent = DaggerAppComponent.create()

    }

    companion object{
        private lateinit var instance: MyApplication
//        lateinit var appComponent: AppComponent

        fun getAppContext(): Context {
            Log.d("abc","getContext")
            return instance.applicationContext
        }

        fun getInstance(): MyApplication{
            return instance
        }
    }
}