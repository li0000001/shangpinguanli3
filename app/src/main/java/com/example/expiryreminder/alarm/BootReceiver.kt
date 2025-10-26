package com.example.expiryreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.expiryreminder.ExpiryReminderApplication
import com.example.expiryreminder.reminder.ReminderCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed, rescheduling reminders")
            
            val application = context.applicationContext as? ExpiryReminderApplication
            if (application != null) {
                val pendingResult = goAsync()
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                
                scope.launch {
                    try {
                        val repository = application.appGraph.productRepository
                        val reminderCoordinator = application.appGraph.reminderCoordinator
                        
                        val products = repository.getAllProducts().first()
                        
                        products.forEach { product ->
                            try {
                                reminderCoordinator.createOrUpdateReminder(product)
                                Log.d("BootReceiver", "Rescheduled reminder for ${product.productName}")
                            } catch (e: Exception) {
                                Log.e("BootReceiver", "Failed to reschedule reminder for ${product.productName}", e)
                            }
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
