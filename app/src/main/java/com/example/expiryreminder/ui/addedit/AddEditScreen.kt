package com.example.expiryreminder.ui.addedit

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.expiryreminder.R
import com.example.expiryreminder.domain.ReminderMethod
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddEditScreen(
    viewModel: AddEditViewModel,
    onBack: () -> Unit,
    productId: Long?
) {
    val uiState by viewModel.uiState.collectAsState()

    val permissionsList = listOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )

    val permissionsState = rememberMultiplePermissionsState(permissions = permissionsList)
    val context = LocalContext.current

    var canScheduleExactAlarms by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                true
            } else {
                val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
                alarmManager?.canScheduleExactAlarms() ?: false
            }
        )
    }

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            canScheduleExactAlarms = alarmManager?.canScheduleExactAlarms() ?: false
        }
    }

    val alarmSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            canScheduleExactAlarms = alarmManager?.canScheduleExactAlarms() ?: false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (productId == null) R.string.add_product else R.string.edit_product)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!permissionsState.allPermissionsGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.permissions_required_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            stringResource(R.string.permissions_required_message),
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = { permissionsState.launchMultiplePermissionRequest() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(stringResource(R.string.grant_permissions))
                        }
                    }
                }
            }

            if (uiState.reminderMethod == ReminderMethod.ALARM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.exact_alarm_permission_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            stringResource(R.string.exact_alarm_permission_message),
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    alarmSettingsLauncher.launch(intent)
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(stringResource(R.string.open_settings))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = uiState.productName,
                onValueChange = { viewModel.updateProductName(it) },
                label = { Text(stringResource(R.string.product_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.date_input_method), style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.dateInputMethod == DateInputMethod.DIRECT,
                    onClick = { viewModel.updateDateInputMethod(DateInputMethod.DIRECT) },
                    label = { Text(stringResource(R.string.direct_date)) }
                )
                FilterChip(
                    selected = uiState.dateInputMethod == DateInputMethod.PRODUCTION_DATE,
                    onClick = { viewModel.updateDateInputMethod(DateInputMethod.PRODUCTION_DATE) },
                    label = { Text(stringResource(R.string.production_date_shelf_life)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState.dateInputMethod) {
                DateInputMethod.DIRECT -> {
                    DatePickerField(
                        label = stringResource(R.string.expiration_date_label),
                        selectedDate = uiState.expirationDate,
                        onDateSelected = { viewModel.updateExpirationDate(it) }
                    )
                }
                DateInputMethod.PRODUCTION_DATE -> {
                    DatePickerField(
                        label = stringResource(R.string.production_date_label),
                        selectedDate = uiState.productionDate,
                        onDateSelected = { viewModel.updateProductionDate(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.shelfLifeDays,
                        onValueChange = { viewModel.updateShelfLifeDays(it) },
                        label = { Text(stringResource(R.string.shelf_life_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    if (uiState.expirationDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val dateFormatter = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                        Text(
                            stringResource(
                                R.string.calculated_expiration,
                                dateFormatter.format(Date(uiState.expirationDate!!))
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.reminder_settings), style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.daysToRemindBefore.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { viewModel.updateDaysToRemindBefore(it) }
                },
                label = { Text(stringResource(R.string.days_before_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            TimePickerField(
                label = stringResource(R.string.reminder_time_label),
                selectedTime = uiState.reminderTime,
                onTimeSelected = { viewModel.updateReminderTime(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(R.string.reminder_method_label), style = MaterialTheme.typography.bodyLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.reminderMethod == ReminderMethod.NOTIFICATION,
                    onClick = { viewModel.updateReminderMethod(ReminderMethod.NOTIFICATION) },
                    label = { Text(stringResource(R.string.reminder_method_notification)) }
                )
                FilterChip(
                    selected = uiState.reminderMethod == ReminderMethod.ALARM,
                    onClick = { viewModel.updateReminderMethod(ReminderMethod.ALARM) },
                    label = { Text(stringResource(R.string.reminder_method_alarm)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = { viewModel.saveProduct(onBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && permissionsState.allPermissionsGranted
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(R.string.save_product))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)

    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        val displayText = selectedDate?.let { dateFormatter.format(Date(it)) }
            ?: stringResource(R.string.select_label, label)
        Text(displayText)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let(onDateSelected)
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showTimePicker = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.reminder_time_with_value, label, selectedTime))
    }

    if (showTimePicker) {
        val timeParts = selectedTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    onTimeSelected(formattedTime)
                    showTimePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}
