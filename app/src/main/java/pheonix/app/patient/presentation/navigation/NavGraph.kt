package pheonix.app.patient.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pheonix.app.patient.presentation.auth.AuthViewModel
import pheonix.app.patient.presentation.auth.LoginScreen
import pheonix.app.patient.presentation.home.HomeScreen
import pheonix.app.patient.presentation.profile.DoctorProfileScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object DoctorProfile : Screen("doctor_profile")
    object Home : Screen("home")
    object Appointments : Screen("appointments")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
    ) {
        composable(
            route = Screen.Login.route,
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
            HomeScreen(
                navController = navController
            )
        }

        composable(route = Screen.Appointments.route) {
            // AppointmentsScreen will be implemented later
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
    }
} 