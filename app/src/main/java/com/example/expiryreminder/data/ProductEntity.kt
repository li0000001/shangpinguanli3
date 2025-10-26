package com.example.expiryreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val expirationDate: Long,
    val reminderTime: String,
    val daysToRemindBefore: Int,
    val reminderMethod: String,
    val calendarEventId: Long? = null
)
