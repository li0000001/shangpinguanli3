package com.example.expiryreminder.data

import com.example.expiryreminder.domain.Product
import com.example.expiryreminder.domain.ReminderMethod

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    productName = productName,
    expirationDate = expirationDate,
    reminderTime = reminderTime,
    daysToRemindBefore = daysToRemindBefore,
    reminderMethod = ReminderMethod.valueOf(reminderMethod),
    calendarEventId = calendarEventId
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    productName = productName,
    expirationDate = expirationDate,
    reminderTime = reminderTime,
    daysToRemindBefore = daysToRemindBefore,
    reminderMethod = reminderMethod.name,
    calendarEventId = calendarEventId
)
