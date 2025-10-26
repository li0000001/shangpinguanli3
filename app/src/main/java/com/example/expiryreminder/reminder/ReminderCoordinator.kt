package com.example.expiryreminder.reminder

import android.util.Log
import com.example.expiryreminder.alarm.ReminderScheduler
import com.example.expiryreminder.calendar.CalendarEventManager
import com.example.expiryreminder.domain.Product
import com.example.expiryreminder.domain.ReminderMethod
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderCoordinator(
    private val calendarEventManager: CalendarEventManager,
    private val reminderScheduler: ReminderScheduler
) {

    data class ReminderResult(
        val success: Boolean,
        val calendarEventId: Long?,
        val scheduledAppReminder: Boolean,
        val errorMessage: String? = null
    )

    fun createOrUpdateReminder(product: Product): ReminderResult {
        val reminderDateTime = calculateReminderDateTime(product)
        val title = "${product.productName} 将在 ${product.daysToRemindBefore} 天后过期"
        val description = "${product.productName}的过期提醒"

        val startMillis = reminderDateTime.toInstant().toEpochMilli()
        val endMillis = startMillis + (60 * 60 * 1000)

        val useAlarm = product.reminderMethod == ReminderMethod.ALARM

        val reminderMinutes = if (product.reminderMethod == ReminderMethod.NOTIFICATION) {
            -1
        } else {
            0
        }

        val result = calendarEventManager.upsertEvent(
            eventId = product.calendarEventId,
            title = title,
            description = description,
            startMillis = startMillis,
            endMillis = endMillis,
            reminderMinutesBefore = reminderMinutes,
            useAlarmMethod = useAlarm
        )

        if (result == null) {
            return ReminderResult(
                success = false,
                calendarEventId = product.calendarEventId,
                scheduledAppReminder = false,
                errorMessage = "无法创建或更新日历事件"
            )
        }

        reminderScheduler.cancelReminder(product)

        var usedAppReminder = false
        var reminderSuccess = true
        var reminderError: String? = null

        try {
            reminderScheduler.scheduleReminder(product, reminderDateTime)
            usedAppReminder = true
        } catch (e: Exception) {
            Log.e("ReminderCoordinator", "Error scheduling app-level reminder", e)
            reminderSuccess = false
            reminderError = e.message ?: "无法设置提醒"
        }

        return ReminderResult(
            success = reminderSuccess,
            calendarEventId = result.eventId,
            scheduledAppReminder = usedAppReminder,
            errorMessage = reminderError
        )
    }

    fun deleteReminder(product: Product) {
        product.calendarEventId?.let { eventId ->
            calendarEventManager.deleteEvent(eventId)
        }
        reminderScheduler.cancelReminder(product)
    }

    private fun calculateReminderDateTime(product: Product): ZonedDateTime {
        val expirationInstant = Instant.ofEpochMilli(product.expirationDate)
        val expirationDate = expirationInstant.atZone(ZoneId.systemDefault())

        val reminderDate = expirationDate.minusDays(product.daysToRemindBefore.toLong())

        val timeParts = product.reminderTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        return reminderDate.with(LocalTime.of(hour, minute))
    }
}
