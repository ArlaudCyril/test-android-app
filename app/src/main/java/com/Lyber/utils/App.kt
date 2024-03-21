package com.Lyber.utils

import android.app.Application
import android.content.Context
import com.Lyber.R
import com.google.android.libraries.places.api.Places

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Places.initialize(this, getString(R.string.API_KEY_STAGING))
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