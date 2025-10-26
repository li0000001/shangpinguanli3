package com.example.expiryreminder.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expiryreminder.data.ProductRepository
import com.example.expiryreminder.domain.Product
import com.example.expiryreminder.domain.ReminderMethod
import com.example.expiryreminder.reminder.ReminderCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AddEditUiState(
    val productName: String = "",
    val expirationDate: Long? = null,
    val productionDate: Long? = null,
    val shelfLifeDays: String = "",
    val reminderTime: String = "09:00",
    val daysToRemindBefore: Int = 3,
    val reminderMethod: ReminderMethod = ReminderMethod.NOTIFICATION,
    val dateInputMethod: DateInputMethod = DateInputMethod.DIRECT,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class DateInputMethod {
    DIRECT,
    PRODUCTION_DATE
}

class AddEditViewModel(
    private val productRepository: ProductRepository,
    private val reminderCoordinator: ReminderCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState

    private var productId: Long? = null

    fun loadProduct(id: Long?) {
        productId = id
        if (id != null) {
            viewModelScope.launch {
                val product = withContext(Dispatchers.IO) {
                    productRepository.getProductById(id)
                }
                if (product != null) {
                    _uiState.value = _uiState.value.copy(
                        productName = product.productName,
                        expirationDate = product.expirationDate,
                        reminderTime = product.reminderTime,
                        daysToRemindBefore = product.daysToRemindBefore,
                        reminderMethod = product.reminderMethod
                    )
                }
            }
        }
    }

    fun updateProductName(name: String) {
        _uiState.value = _uiState.value.copy(productName = name)
    }

    fun updateExpirationDate(date: Long) {
        _uiState.value = _uiState.value.copy(expirationDate = date)
    }

    fun updateProductionDate(date: Long) {
        _uiState.value = _uiState.value.copy(productionDate = date)
        calculateExpirationFromProduction()
    }

    fun updateShelfLifeDays(days: String) {
        _uiState.value = _uiState.value.copy(shelfLifeDays = days)
        calculateExpirationFromProduction()
    }

    fun updateReminderTime(time: String) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
    }

    fun updateDaysToRemindBefore(days: Int) {
        _uiState.value = _uiState.value.copy(daysToRemindBefore = days)
    }

    fun updateReminderMethod(method: ReminderMethod) {
        _uiState.value = _uiState.value.copy(reminderMethod = method)
    }

    fun updateDateInputMethod(method: DateInputMethod) {
        _uiState.value = _uiState.value.copy(dateInputMethod = method)
    }

    private fun calculateExpirationFromProduction() {
        val state = _uiState.value
        val productionDate = state.productionDate
        val shelfLifeDays = state.shelfLifeDays.toIntOrNull()

        if (productionDate != null && shelfLifeDays != null && shelfLifeDays > 0) {
            val expirationDate = productionDate + (shelfLifeDays * 24 * 60 * 60 * 1000L)
            _uiState.value = _uiState.value.copy(expirationDate = expirationDate)
        }
    }

    fun saveProduct(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.productName.isBlank()) {
            _uiState.value = state.copy(error = "商品名称不能为空")
            return
        }

        val expirationDate = state.expirationDate
        if (expirationDate == null) {
            _uiState.value = state.copy(error = "到期日期不能为空")
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val operation = runCatching {
                withContext(Dispatchers.IO) {
                    if (productId != null) {
                        val existingProduct = productRepository.getProductById(productId!!)
                        val productToSave = Product(
                            id = productId!!,
                            productName = state.productName,
                            expirationDate = expirationDate,
                            reminderTime = state.reminderTime,
                            daysToRemindBefore = state.daysToRemindBefore,
                            reminderMethod = state.reminderMethod,
                            calendarEventId = existingProduct?.calendarEventId
                        )
                        val result = reminderCoordinator.createOrUpdateReminder(productToSave)
                        if (!result.success) {
                            error(result.errorMessage ?: "无法设置提醒")
                        }
                        val finalProduct = productToSave.copy(calendarEventId = result.calendarEventId)
                        productRepository.updateProduct(finalProduct)
                    } else {
                        val baseProduct = Product(
                            id = 0,
                            productName = state.productName,
                            expirationDate = expirationDate,
                            reminderTime = state.reminderTime,
                            daysToRemindBefore = state.daysToRemindBefore,
                            reminderMethod = state.reminderMethod,
                            calendarEventId = null
                        )
                        val newId = productRepository.insertProduct(baseProduct)
                        productId = newId
                        val productWithId = baseProduct.copy(id = newId)
                        val result = reminderCoordinator.createOrUpdateReminder(productWithId)
                        if (!result.success) {
                            error(result.errorMessage ?: "无法设置提醒")
                        }
                        val finalProduct = productWithId.copy(calendarEventId = result.calendarEventId)
                        productRepository.updateProduct(finalProduct)
                    }
                }
            }

            operation.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "保存商品失败"
                )
            }
        }
    }
}

class AddEditViewModelFactory(
    private val productRepository: ProductRepository,
    private val reminderCoordinator: ReminderCoordinator
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditViewModel::class.java)) {
            return AddEditViewModel(productRepository, reminderCoordinator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
