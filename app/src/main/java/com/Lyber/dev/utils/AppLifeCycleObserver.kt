package com.Lyber.dev.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

class AppLifeCycleObserver: Application.ActivityLifecycleCallbacks {
val TAG="AppLifeCycleObserver"
    companion object {
        var fromBack = false
        var progressDialogVisible = false
        var documentDialogVisible = false

    }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Unused
        Log.d(TAG,"onCreated")
    }

    override fun onActivityStarted(activity: Activity) {
        // Unused
        Log.d(TAG,"onStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        // Unused
        Log.d(TAG,"onResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        // Unused
        Log.d(TAG,"onPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        // App is going into the background
        // Add your logic here
        Log.d(TAG,"onStopped")
        if(progressDialogVisible) {
            fromBack = true
            Log.d(TAG,"progressVisible")
            CommonMethods.dismissProgressDialog()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // Unused
        Log.d(TAG,"onSaveINstance")
    }

    override fun onActivityDestroyed(activity: Activity) {
        // Unused
        fromBack=false
        Log.d(TAG,"onDestroy")
    }
}