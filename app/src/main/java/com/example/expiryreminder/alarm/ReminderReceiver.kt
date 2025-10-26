package com.example.expiryreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TRIGGER_ALARM) {
            val productId = intent.getLongExtra(EXTRA_PRODUCT_ID, -1L)
            val productName = intent.getStringExtra(EXTRA_PRODUCT_NAME) ?: ""
            val expirationDate = intent.getLongExtra(EXTRA_EXPIRATION_DATE, 0L)
            val reminderTime = intent.getStringExtra(EXTRA_REMINDER_TIME) ?: ""
            val daysBefore = intent.getIntExtra(EXTRA_DAYS_BEFORE, 0)

            val fullScreenIntent = Intent(context, FullScreenAlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_PRODUCT_ID, productId)
                putExtra(EXTRA_PRODUCT_NAME, productName)
                putExtra(EXTRA_EXPIRATION_DATE, expirationDate)
                putExtra(EXTRA_REMINDER_TIME, reminderTime)
                putExtra(EXTRA_DAYS_BEFORE, daysBefore)
            }

            context.startActivity(fullScreenIntent)
        }
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.example.expiryreminder.ACTION_TRIGGER_ALARM"
        const val EXTRA_PRODUCT_ID = "product_id"
        const val EXTRA_PRODUCT_NAME = "product_name"
        const val EXTRA_EXPIRATION_DATE = "expiration_date"
        const val EXTRA_REMINDER_TIME = "reminder_time"
        const val EXTRA_DAYS_BEFORE = "days_before"
    }
}
