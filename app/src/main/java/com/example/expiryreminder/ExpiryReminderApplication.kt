package com.example.expiryreminder

import android.app.Application
import com.example.expiryreminder.core.AppGraph
import com.example.expiryreminder.notification.NotificationHelper

class ExpiryReminderApplication : Application() {
    val appGraph: AppGraph
        get() = AppGraph

    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
        NotificationHelper(this)
    }
}
