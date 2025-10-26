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
        val usedFallbackAlarm: Boolean,
        val errorMessage: String? = null
    )

    fun createOrUpdateReminder(product: Product): ReminderResult {
        val reminderDateTime = calculateReminderDateTime(product)
        val title = "${product.productName} 将在 ${product.daysToRemindBefore} 天后过期"
        val description = "${product.productName}的过期提醒"

        val startMillis = reminderDateTime.toInstant().toEpochMilli()
        val endMillis = startMillis + (60 * 60 * 1000)

        val useAlarm = product.reminderMethod == ReminderMethod.ALARM

        val result = calendarEventManager.upsertEvent(
            eventId = product.calendarEventId,
            title = title,
            description = description,
            startMillis = startMillis,
            endMillis = endMillis,
            reminderMinutesBefore = 0,
            useAlarmMethod = useAlarm
        )

        if (result == null) {
            return ReminderResult(
                success = false,
                calendarEventId = product.calendarEventId,
                usedFallbackAlarm = false,
                errorMessage = "无法创建或更新日历事件"
            )
        }

        reminderScheduler.cancelAlarm(product)

        var usedFallback = false
        var fallbackSuccess = true
        var fallbackError: String? = null

        if (useAlarm) {
            if (!result.usedAlarmMethod) {
                usedFallback = true
                try {
                    reminderScheduler.scheduleAlarm(product, reminderDateTime)
                } catch (e: Exception) {
                    Log.e("ReminderCoordinator", "Error scheduling fallback alarm", e)
                    fallbackSuccess = false
                    fallbackError = e.message ?: "无法设置后备闹钟"
                }
            }
        }

        if (!useAlarm) {
            reminderScheduler.cancelAlarm(product)
        }

        return ReminderResult(
            success = fallbackSuccess,
            calendarEventId = result.eventId,
            usedFallbackAlarm = usedFallback,
            errorMessage = fallbackError
        )
    }

    fun deleteReminder(product: Product) {
        product.calendarEventId?.let { eventId ->
            calendarEventManager.deleteEvent(eventId)
        }
        reminderScheduler.cancelAlarm(product)
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
