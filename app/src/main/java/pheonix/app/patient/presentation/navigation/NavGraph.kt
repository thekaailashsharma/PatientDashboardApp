package pheonix.app.patient.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pheonix.app.patient.presentation.appointments.AppointmentsScreen
import pheonix.app.patient.presentation.appointments.create.CreateAppointmentScreen
import pheonix.app.patient.presentation.appointments.details.AppointmentDetailsScreen
import pheonix.app.patient.presentation.appointments.edit.EditAppointmentScreen
import pheonix.app.patient.presentation.auth.AuthViewModel
import pheonix.app.patient.presentation.auth.LoginScreen
import pheonix.app.patient.presentation.auth.SignUpScreen
import pheonix.app.patient.presentation.home.HomeScreen
import pheonix.app.patient.presentation.profile.DoctorProfileScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object DoctorProfile : Screen("doctor_profile")
    object Appointments : Screen("appointments")
    object AppointmentDetails : Screen("appointment_details/{appointmentId}") {
        fun createRoute(appointmentId: String) = "appointment_details/$appointmentId"
    }

    object CreateAppointment : Screen("create_appointment")
    object EditAppointment : Screen("edit_appointment/{appointmentId}") {
        fun createRoute(appointmentId: String) = "edit_appointment/$appointmentId"
    }

    object Chat : Screen("chat")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                state = authState,
                onGoogleSignInClick = { credential ->
                    authViewModel.signInWithGoogle(credential)
                },
                onNavigateToHome = {
                    // Check if profile is complete before navigating
                    if (authState.currentUser?.isProfileComplete == true) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.DoctorProfile.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.DoctorProfile.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            }
        ) {
            DoctorProfileScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.DoctorProfile.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.EditAppointment.route,
            arguments = listOf(
                navArgument("appointmentId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val appointmentId =
                backStackEntry.arguments?.getString("appointmentId") ?: return@composable
            EditAppointmentScreen(
                appointmentId = appointmentId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAppointmentUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Chat.route) {
            // ChatScreen will be implemented later
        }

        composable(route = Screen.Profile.route) {
            DoctorProfileScreen(
                isEditMode = true,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Profile.route)
                    }
                }
            )
        }

        composable(
            route = Screen.Home.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            }
        ) {
            HomeScreen(navController = navController)
        }

        composable(
            route = Screen.Appointments.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            }
        ) {
            AppointmentsScreen(
                viewModel = hiltViewModel(),
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

        composable(
            route = Screen.CreateAppointment.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            }
        ) {
            CreateAppointmentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAppointmentCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.AppointmentDetails.route,
            arguments = listOf(
                navArgument("appointmentId") {
                    type = NavType.StringType
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: return@composable
            AppointmentDetailsScreen(
                appointmentId = appointmentId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}