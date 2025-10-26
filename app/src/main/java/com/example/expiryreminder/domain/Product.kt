package com.example.expiryreminder.domain

data class Product(
    val id: Long = 0,
    val productName: String,
    val expirationDate: Long,
    val reminderTime: String,
    val daysToRemindBefore: Int,
    val reminderMethod: ReminderMethod,
    val calendarEventId: Long? = null
)

enum class ReminderMethod {
    NOTIFICATION,
    ALARM
}
