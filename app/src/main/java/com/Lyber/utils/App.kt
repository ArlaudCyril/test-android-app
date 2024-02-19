package com.Lyber.utils

import android.app.Application
import android.content.Context

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext=this
        prefsManager = PreferenceManager(applicationContext)
        accessToken = prefsManager.accessToken
    }

    companion object {
        lateinit var accessToken: String
        lateinit var prefsManager: PreferenceManager
        lateinit var appContext: Context
    }
}