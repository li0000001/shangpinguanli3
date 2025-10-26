package com.example.expiryreminder.core

import android.content.Context
import com.example.expiryreminder.alarm.ReminderScheduler
import com.example.expiryreminder.calendar.CalendarEventManager
import com.example.expiryreminder.data.AppDatabase
import com.example.expiryreminder.data.ProductRepository
import com.example.expiryreminder.data.ProductRepositoryImpl
import com.example.expiryreminder.reminder.ReminderCoordinator

object AppGraph {
    lateinit var database: AppDatabase
        private set

    lateinit var productRepository: ProductRepository
        private set

    lateinit var calendarEventManager: CalendarEventManager
        private set

    lateinit var reminderScheduler: ReminderScheduler
        private set

    lateinit var reminderCoordinator: ReminderCoordinator
        private set

    fun init(context: Context) {
        val appContext = context.applicationContext
        database = AppDatabase.getDatabase(appContext)
        val productDao = database.productDao()
        productRepository = ProductRepositoryImpl(productDao)
        calendarEventManager = CalendarEventManager(appContext)
        reminderScheduler = ReminderScheduler(appContext)
        reminderCoordinator = ReminderCoordinator(
            calendarEventManager = calendarEventManager,
            reminderScheduler = reminderScheduler
        )
    }
}
