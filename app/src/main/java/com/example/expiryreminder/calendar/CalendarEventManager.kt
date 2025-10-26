package com.example.expiryreminder.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import java.util.TimeZone

class CalendarEventManager(private val context: Context) {

    data class EventUpsertResult(
        val eventId: Long,
        val usedAlarmMethod: Boolean
    )

    fun upsertEvent(
        eventId: Long?,
        title: String,
        description: String,
        startMillis: Long,
        endMillis: Long,
        reminderMinutesBefore: Int,
        useAlarmMethod: Boolean
    ): EventUpsertResult? {
        return runCatching {
            val calendarId = getDefaultCalendarId() ?: return null

            val eventValues = ContentValues().apply {
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, if (reminderMinutesBefore >= 0) 1 else 0)
                put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE)
            }

            val finalEventId = if (eventId != null) {
                val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
                val rows = context.contentResolver.update(updateUri, eventValues, null, null)
                if (rows > 0) {
                    eventId
                } else {
                    insertEvent(eventValues)
                }
            } else {
                insertEvent(eventValues)
            } ?: return null

            clearReminders(finalEventId)

            var alarmMethodWorked = false

            if (reminderMinutesBefore >= 0) {
                val reminderMethod = if (useAlarmMethod) {
                    CalendarContract.Reminders.METHOD_ALERT
                } else {
                    CalendarContract.Reminders.METHOD_ALERT
                }
                alarmMethodWorked = addReminder(
                    eventId = finalEventId,
                    minutesBefore = reminderMinutesBefore,
                    method = reminderMethod
                )
            }

            EventUpsertResult(
                eventId = finalEventId,
                usedAlarmMethod = alarmMethodWorked && useAlarmMethod
            )
        }.onFailure { throwable ->
            Log.e("CalendarEventManager", "Failed to upsert event", throwable)
        }.getOrNull()
    }

    fun deleteEvent(eventId: Long) {
        runCatching {
            val uri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            context.contentResolver.delete(uri, null, null)
        }.onFailure { throwable ->
            Log.e("CalendarEventManager", "Failed to delete event", throwable)
        }
    }

    private fun insertEvent(values: ContentValues): Long? {
        return context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            ?.lastPathSegment
            ?.toLongOrNull()
    }

    private fun addReminder(eventId: Long, minutesBefore: Int, method: Int): Boolean {
        return runCatching {
            val values = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, minutesBefore)
                put(CalendarContract.Reminders.METHOD, method)
            }
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values) != null
        }.onFailure { throwable ->
            Log.e("CalendarEventManager", "Failed to add reminder", throwable)
        }.getOrDefault(false)
    }

    private fun clearReminders(eventId: Long) {
        runCatching {
            context.contentResolver.delete(
                CalendarContract.Reminders.CONTENT_URI,
                "${CalendarContract.Reminders.EVENT_ID} = ?",
                arrayOf(eventId.toString())
            )
        }.onFailure { throwable ->
            Log.e("CalendarEventManager", "Failed to clear reminders", throwable)
        }
    }

    private fun getDefaultCalendarId(): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY
        )

        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            null
        )?.use { cursor ->
            var fallbackCalendarId: Long? = null
            while (cursor.moveToNext()) {
                val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                val primaryIndex = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                if (idIndex >= 0) {
                    val calendarId = cursor.getLong(idIndex)
                    if (primaryIndex >= 0 && cursor.getInt(primaryIndex) == 1) {
                        return calendarId
                    }
                    if (fallbackCalendarId == null) {
                        fallbackCalendarId = calendarId
                    }
                }
            }
            return fallbackCalendarId
        }
        return null
    }
}
