package com.example.expiryreminder.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expiryreminder.R
import com.example.expiryreminder.domain.Product
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit
) {
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.home_title)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_product))
            }
        }
    ) { paddingValues ->
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.empty_list_message))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(products) { product ->
                    ProductItem(
                        product = product,
                        onClick = { onEditProduct(product) },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
    val expirationDateText = dateFormatter.format(Date(product.expirationDate))

    val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
    val expiryLocalDate = Instant.ofEpochMilli(product.expirationDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val rawDaysLeft = ChronoUnit.DAYS.between(now, expiryLocalDate).toInt()

    val statusText = when {
        rawDaysLeft > 0 -> stringResource(id = R.string.status_expires_in, rawDaysLeft)
        rawDaysLeft == 0 -> stringResource(id = R.string.status_expires_today)
        else -> stringResource(id = R.string.status_expired_days, abs(rawDaysLeft))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(product.productName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(id = R.string.expires_on, expirationDateText), style = MaterialTheme.typography.bodyMedium)
                Text(statusText, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete_product))
            }
        }
    }
}
