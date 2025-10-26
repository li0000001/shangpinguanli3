package com.example.expiryreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.expiryreminder.core.AppGraph
import com.example.expiryreminder.domain.Product
import com.example.expiryreminder.ui.addedit.AddEditScreen
import com.example.expiryreminder.ui.addedit.AddEditViewModel
import com.example.expiryreminder.ui.addedit.AddEditViewModelFactory
import com.example.expiryreminder.ui.home.HomeScreen
import com.example.expiryreminder.ui.home.HomeViewModel
import com.example.expiryreminder.ui.home.HomeViewModelFactory
import com.example.expiryreminder.ui.theme.ExpiryReminderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpiryReminderTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        val homeViewModel: HomeViewModel = viewModel(
                            factory = HomeViewModelFactory(AppGraph.productRepository, AppGraph.reminderCoordinator)
                        )
                        HomeScreen(
                            viewModel = homeViewModel,
                            onAddProduct = { navController.navigate("addEdit") },
                            onEditProduct = { product -> navController.navigate("addEdit/${product.id}") }
                        )
                    }
                    composable("addEdit") {
                        val addEditViewModel: AddEditViewModel = viewModel(
                            factory = AddEditViewModelFactory(AppGraph.productRepository, AppGraph.reminderCoordinator)
                        )
                        AddEditScreen(
                            viewModel = addEditViewModel,
                            onBack = { navController.popBackStack() },
                            productId = null
                        )
                    }
                    composable("addEdit/{productId}") { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull()
                        val addEditViewModel: AddEditViewModel = viewModel(
                            factory = AddEditViewModelFactory(AppGraph.productRepository, AppGraph.reminderCoordinator)
                        )
                        AddEditScreen(
                            viewModel = addEditViewModel,
                            onBack = { navController.popBackStack() },
                            productId = productId
                        )
                    }
                }
            }
        }
    }
}
