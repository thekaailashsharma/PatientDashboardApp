package pheonix.app.patient.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pheonix.app.patient.presentation.navigation.BottomNavigation
import pheonix.app.patient.presentation.navigation.Screen
import pheonix.app.patient.presentation.appointments.AppointmentsScreen
import pheonix.app.patient.presentation.appointments.details.AppointmentDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    var showBottomBar by remember { mutableStateOf(true) }
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    // Hide bottom bar on specific screens
    LaunchedEffect(currentRoute) {
        showBottomBar = when (currentRoute) {
            Screen.Login.route, Screen.DoctorProfile.route -> false
            Screen.Appointments.route -> true
            else -> true
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigation(
                    navController = navController,
                    showBottomBar = true
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (showBottomBar) paddingValues.calculateBottomPadding() else 0.dp,
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(layoutDirection = LayoutDirection.Ltr)
                )
        ) {
            when (currentRoute) {
                Screen.Home.route -> {
                    // Home content will be implemented later
                }
                Screen.Appointments.route -> {
                    AppointmentsScreen(
                        onNavigateToAppointmentDetails = { appointmentId ->
                            navController.navigate(Screen.AppointmentDetails.createRoute(appointmentId))
                        },
                        onNavigateToCreateAppointment = {
                            navController.navigate(Screen.CreateAppointment.route)
                        },
                        onNavigateToEditAppointment = { appointmentId ->
                            navController.navigate(Screen.EditAppointment.createRoute(appointmentId))
                        }
                    )
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