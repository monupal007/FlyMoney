package com.laundryapp.admin.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.laundryapp.admin.presentation.auth.AdminLoginScreen
import com.laundryapp.admin.presentation.customers.CustomerManagementScreen
import com.laundryapp.admin.presentation.dashboard.AdminDashboardScreen
import com.laundryapp.admin.presentation.delivery.DriverManagementScreen
import com.laundryapp.admin.presentation.orders.AdminOrderDetailScreen
import com.laundryapp.admin.presentation.orders.AdminOrdersScreen
import com.laundryapp.admin.presentation.services.AdminOfferApprovalScreen
import com.laundryapp.admin.presentation.services.ServiceManagementScreen
import com.laundryapp.admin.presentation.vendors.VendorDetailScreen
import com.laundryapp.admin.presentation.vendors.VendorManagementScreen
import com.laundryapp.core.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

sealed class AdminScreen(val route: String) {
    object Login : AdminScreen("login")
    object Dashboard : AdminScreen("dashboard")
    object Orders : AdminScreen("orders")
    object OrderDetail : AdminScreen("orders/{orderId}") {
        fun createRoute(orderId: String) = "orders/$orderId"
    }
    object Customers : AdminScreen("customers")
    object Delivery : AdminScreen("delivery")
    object Vendors : AdminScreen("vendors")
    object VendorDetail : AdminScreen("vendors/{vendorId}") {
        fun createRoute(vendorId: String) = "vendors/$vendorId"
    }
    object Services : AdminScreen("services")
    object OfferApproval : AdminScreen("offer_approval")
}

class AdminNavViewModel(
    val authRepository: AuthRepository
) : ViewModel()

@Composable
fun AdminNavGraph(
    navController: NavHostController,
    viewModel: AdminNavViewModel = koinViewModel()
) {
    val authState by viewModel.authRepository.authState.collectAsState()
    val scope = rememberCoroutineScope()

    // Global Logout observer
    LaunchedEffect(authState) {
        if (authState == null) {
            navController.navigate(AdminScreen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AdminScreen.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AdminScreen.Login.route) {
                AdminLoginScreen(
                    onLoginSuccess = {
                        navController.navigate(AdminScreen.Dashboard.route) {
                            popUpTo(AdminScreen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(AdminScreen.Dashboard.route) {
                AdminDashboardScreen(
                    onNavigateToOrders = { navController.navigate(AdminScreen.Orders.route) },
                    onNavigateToCustomers = { navController.navigate(AdminScreen.Customers.route) },
                    onNavigateToDelivery = { navController.navigate(AdminScreen.Delivery.route) },
                    onNavigateToVendors = { navController.navigate(AdminScreen.Vendors.route) },
                    onNavigateToServices = { navController.navigate(AdminScreen.Services.route) },
                    onNavigateToOfferApproval = { navController.navigate(AdminScreen.OfferApproval.route) },
                    onLogout = {
                        scope.launch {
                            viewModel.authRepository.signOut()
                        }
                    }
                )
            }

            composable(AdminScreen.Orders.route) {
                AdminOrdersScreen(
                    onOrderClick = { orderId -> 
                        navController.navigate(AdminScreen.OrderDetail.createRoute(orderId)) 
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = AdminScreen.OrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                AdminOrderDetailScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AdminScreen.Customers.route) {
                CustomerManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AdminScreen.Vendors.route) {
                VendorManagementScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onVendorClick = { vendorId ->
                        navController.navigate(AdminScreen.VendorDetail.createRoute(vendorId))
                    }
                )
            }

            composable(
                route = AdminScreen.VendorDetail.route,
                arguments = listOf(navArgument("vendorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val vendorId = backStackEntry.arguments?.getString("vendorId") ?: ""
                VendorDetailScreen(
                    vendorId = vendorId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AdminScreen.Delivery.route) {
                DriverManagementScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onDriverClick = { /* TODO: Driver Detail */ }
                )
            }

            composable(AdminScreen.Services.route) {
                ServiceManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AdminScreen.OfferApproval.route) {
                AdminOfferApprovalScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
