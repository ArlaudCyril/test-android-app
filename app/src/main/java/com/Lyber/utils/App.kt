package com.Lyber.utils

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferenceManager(applicationContext)
        accessToken = prefsManager.accessToken
    }

    companion object {
        lateinit var accessToken: String
        lateinit var prefsManager: PreferenceManager
    }
}