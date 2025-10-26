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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                title = { Text("Expiry Reminder") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
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
                Text("No products yet. Tap + to add one.")
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
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val expirationDate = Date(product.expirationDate)

    val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
    val expiryLocalDate = Instant.ofEpochMilli(product.expirationDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val rawDaysLeft = ChronoUnit.DAYS.between(now, expiryLocalDate).toInt()

    val statusText = when {
        rawDaysLeft > 0 -> "Expires in $rawDaysLeft days"
        rawDaysLeft == 0 -> "Expires today"
        else -> "Expired ${abs(rawDaysLeft)} days ago"
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
                Text("Expires on ${dateFormatter.format(expirationDate)}", style = MaterialTheme.typography.bodyMedium)
                Text(statusText, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
