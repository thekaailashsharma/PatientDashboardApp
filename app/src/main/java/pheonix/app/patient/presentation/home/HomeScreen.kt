package pheonix.app.patient.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import pheonix.app.patient.presentation.navigation.BottomNavigation
import pheonix.app.patient.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    var showBottomBar by remember { mutableStateOf(true) }
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    // Hide bottom bar on login and profile setup screens
    LaunchedEffect(currentRoute) {
        showBottomBar = when (currentRoute) {
            Screen.Login.route, Screen.DoctorProfile.route -> false
            else -> true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                showBottomBar = showBottomBar
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentRoute) {
                Screen.Home.route -> {
                    // Home content will be implemented later
                }
                Screen.Appointments.route -> {
                    // Appointments content will be implemented later
                }
                Screen.Chat.route -> {
                    // Chat content will be implemented later
                }
                Screen.Profile.route -> {
                    // Profile is handled in NavGraph
                }
            }
        }
    }
} 