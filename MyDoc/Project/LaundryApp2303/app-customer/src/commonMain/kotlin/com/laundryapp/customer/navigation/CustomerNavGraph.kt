package com.laundryapp.customer.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.customer.presentation.auth.LoginScreen
import org.koin.compose.viewmodel.koinViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object OrderHistory : Screen("orders")
    object Profile : Screen("profile")
    object VendorSelection : Screen("vendor_selection?category={category}") {
        fun createRoute(category: String?) = if (category != null) "vendor_selection?category=$category" else "vendor_selection"
    }
    object OrderPlacement : Screen("order_placement/{vendorId}") {
        fun createRoute(vendorId: String) = "order_placement/$vendorId"
    }
    object OrderConfirmation : Screen("order_confirmation/{orderId}") {
        fun createRoute(orderId: String) = "order_confirmation/$orderId"
    }
    object OrderTracking : Screen("order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }
    object OrderRating : Screen("order_rating/{orderId}") {
        fun createRoute(orderId: String) = "order_rating/$orderId"
    }
    object AddressManagement : Screen("address_management")
}

class NavViewModel(
    val authRepository: AuthRepository
) : ViewModel()

@Composable
fun CustomerNavGraph(
    navController: NavHostController,
    viewModel: NavViewModel = koinViewModel()
) {
    val authState by viewModel.authRepository.authState.collectAsState()

    // Global Logout observer
    LaunchedEffect(authState) {
        if (authState == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route, // Temporarily start at Login for testing
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { _ ->
                        // Navigate to Home
                    }
                )
            }
            
            // Add other screens as they are migrated to commonMain
        }
    }
}
