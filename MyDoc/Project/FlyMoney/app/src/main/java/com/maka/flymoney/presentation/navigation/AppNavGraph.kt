package com.maka.flymoney.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maka.flymoney.presentation.auth.LoginScreen
import com.maka.flymoney.presentation.auth.RegisterScreen
import com.maka.flymoney.presentation.auth.PhoneAuthScreen
import com.maka.flymoney.presentation.game.GameScreen
import com.maka.flymoney.presentation.leaderboard.LeaderboardScreen
import com.maka.flymoney.presentation.profile.ProfileScreen
import com.maka.flymoney.presentation.wallet.WalletScreen
import com.maka.flymoney.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    object PhoneAuth : Screen("phone_auth", "Phone Auth")
    object Game : Screen("game", "Game", Icons.Default.PlayArrow)
    object Wallet : Screen("wallet", "Wallet", Icons.Default.History)
    object Leaderboard : Screen("leaderboard", "Ranks", Icons.Default.EmojiEvents)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: AuthViewModelWrapper = hiltViewModel()
) {
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val user = viewModel.authRepository.currentUser.first()
        startDestination = if (user != null) Screen.Game.route else Screen.Login.route
    }

    val destination = startDestination
    if (destination == null) {
        SplashScreen()
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Game.route,
        Screen.Wallet.route,
        Screen.Leaderboard.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = destination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToPhoneAuth = { navController.navigate(Screen.PhoneAuth.route) },
                    onLoginSuccess = { 
                        navController.navigate(Screen.Game.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Game.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.PhoneAuth.route) {
                PhoneAuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Game.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Game.route) { GameScreen() }
            composable(Screen.Wallet.route) { WalletScreen() }
            composable(Screen.Leaderboard.route) { LeaderboardScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class AuthViewModelWrapper @javax.inject.Inject constructor(
    val authRepository: AuthRepository
) : androidx.lifecycle.ViewModel()

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF00FF88))
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        Screen.Game,
        Screen.Wallet,
        Screen.Leaderboard,
        Screen.Profile
    )
    NavigationBar(
        containerColor = Color(0xFF13132A),
        contentColor = Color(0xFF00FF88)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF00FF88),
                    selectedTextColor = Color(0xFF00FF88),
                    unselectedIconColor = Color(0xFF8888AA),
                    unselectedTextColor = Color(0xFF8888AA),
                    indicatorColor = Color(0xFF1C1C3A)
                )
            )
        }
    }
}
