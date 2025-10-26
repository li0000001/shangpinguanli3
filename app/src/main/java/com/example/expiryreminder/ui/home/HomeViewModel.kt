package com.example.expiryreminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expiryreminder.data.ProductRepository
import com.example.expiryreminder.domain.Product
import com.example.expiryreminder.reminder.ReminderCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val productRepository: ProductRepository,
    private val reminderCoordinator: ReminderCoordinator
) : ViewModel() {

    val products: StateFlow<List<Product>> = productRepository.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                reminderCoordinator.deleteReminder(product)
                productRepository.deleteProduct(product)
            }
        }
    }
}

class HomeViewModelFactory(
    private val productRepository: ProductRepository,
    private val reminderCoordinator: ReminderCoordinator
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(productRepository, reminderCoordinator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
