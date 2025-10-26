package com.example.expiryreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SHOW_NOTIFICATION) {
            val productId = intent.getLongExtra(EXTRA_PRODUCT_ID, -1L)
            val productName = intent.getStringExtra(EXTRA_PRODUCT_NAME) ?: ""
            val daysBefore = intent.getIntExtra(EXTRA_DAYS_BEFORE, 0)

            val notificationHelper = NotificationHelper(context)
            notificationHelper.showReminderNotification(productId, productName, daysBefore)
        }
    }

    companion object {
        const val ACTION_SHOW_NOTIFICATION = "com.example.expiryreminder.ACTION_SHOW_NOTIFICATION"
        const val EXTRA_PRODUCT_ID = "product_id"
        const val EXTRA_PRODUCT_NAME = "product_name"
        const val EXTRA_DAYS_BEFORE = "days_before"
    }
}
