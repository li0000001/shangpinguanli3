package com.example.expiryreminder.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.expiryreminder.domain.Product
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderScheduler(private val context: Context) {

    private val alarmManager: AlarmManager? = ContextCompat.getSystemService(context, AlarmManager::class.java)

    fun scheduleAlarm(product: Product, triggerTime: ZonedDateTime) {
        val alarmManager = this.alarmManager ?: return

        val pendingIntent = createPendingIntent(product)

        val triggerAtMillis = triggerTime.withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerAtMillis <= System.currentTimeMillis()) {
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancelAlarm(product: Product) {
        val alarmManager = this.alarmManager ?: return
        val pendingIntent = createPendingIntent(product)
        alarmManager.cancel(pendingIntent)
    }

    private fun createPendingIntent(product: Product): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_ALARM
            putExtra(ReminderReceiver.EXTRA_PRODUCT_ID, product.id)
            putExtra(ReminderReceiver.EXTRA_PRODUCT_NAME, product.productName)
            putExtra(ReminderReceiver.EXTRA_EXPIRATION_DATE, product.expirationDate)
            putExtra(ReminderReceiver.EXTRA_REMINDER_TIME, product.reminderTime)
            putExtra(ReminderReceiver.EXTRA_DAYS_BEFORE, product.daysToRemindBefore)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getBroadcast(
            context,
            product.id.toInt(),
            intent,
            flags
        )
    }
}
