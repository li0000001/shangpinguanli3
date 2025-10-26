package com.example.expiryreminder

import android.app.Application
import com.example.expiryreminder.core.AppGraph

class ExpiryReminderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}
