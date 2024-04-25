package com.Lyber.services

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.Lyber.ui.activities.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random
import com.Lyber.R


class MyFirebaseMessagingService : FirebaseMessagingService() {
    lateinit var intent: Intent
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i("PUSHNOTI", "in newIntent $p0")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("remoteMsg", "$remoteMessage")
        val randomInt = Random().nextInt(1000)

        if (isAppRunningInForeground()) {
            // If running in the foreground, broadcast intent to handle notification action
            intent = Intent("Constants.INTENT_FILTER_NOTIFICATION")
//            intent.putExtra("notificationData", remoteMessage.data) // Pass notification data if needed
//            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } else intent = Intent(this, SplashActivity::class.java)
            .apply {
                // extra
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

//        val intentFilter = Intent(Constants.INTENT_FILTER_NOTIFICATION)
//        intent.putExtra("notificationRec",true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        val pendingIntent = PendingIntent.getActivity(
            this, randomInt, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // set notification to display
        val notification = NotificationCompat
            .Builder(this, getString(R.string.app_name))
            .setSmallIcon(R.drawable.ic_logo)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_logo
                )
            )
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                getString(R.string.app_name),
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = remoteMessage.notification?.body
                it.enableLights(true)
                it.lightColor = ContextCompat.getColor(applicationContext, R.color.purple_gray_500)
                notificationManager.createNotificationChannel(it)
            }
        }
        notificationManager.notify(randomInt, notification)
    }

    private fun isAppRunningInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        for (processInfo in runningAppProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                processInfo.processName == applicationContext.packageName
            ) {
                return true
            }
        }
        return false
    }
}