package pheonix.app.patient.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import pheonix.app.patient.presentation.patients.AddEditPatientScreen
import pheonix.app.patient.presentation.patients.PatientsScreen
import pheonix.app.patient.presentation.patients.PatientProfileScreen
import pheonix.app.patient.presentation.profile.DoctorProfileScreen
import pheonix.app.patient.presentation.shipments.ShipmentsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Appointments : Screen("appointments")
    object CreateAppointment : Screen("create_appointment")
    object AppointmentDetails : Screen("appointment_details/{appointmentId}") {
        fun createRoute(appointmentId: String) = "appointment_details/$appointmentId"
    }

    object EditAppointment : Screen("edit_appointment/{appointmentId}") {
        fun createRoute(appointmentId: String) = "edit_appointment/$appointmentId"
    }

    object Patients : Screen("patients")
    object AddEditPatient :
        Screen("add_edit_patient?patientId={patientId}&fromAppointment={fromAppointment}") {
        fun createRoute(patientId: String? = null, fromAppointment: Boolean = false) =
            "add_edit_patient?patientId=$patientId&fromAppointment=$fromAppointment"
    }

    object PatientProfile : Screen("patient_profile/{patientId}") {
        fun createRoute(patientId: String) = "patient_profile/$patientId"
    }

    object DoctorProfile : Screen("doctor_profile")
    object Shipments : Screen("shipments")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                state = authState,
                onGoogleSignInClick = { credential ->
                    authViewModel.signInWithGoogle(credential)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                state = authState,
                onSignUpClick = { email, password, name ->
                    authViewModel.signUpWithEmail(email, password, name)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController
            )
        }

        composable(Screen.Appointments.route) {
            AppointmentsScreen(
                navController = navController,
                onNavigateToCreateAppointment = {
                    navController.navigate(Screen.CreateAppointment.route)
                },
                onNavigateToAppointmentDetails = { appointmentId ->
                    navController.navigate(Screen.AppointmentDetails.createRoute(appointmentId))
                },
                onNavigateToEditAppointment = { appointmentId ->
                    navController.navigate(Screen.EditAppointment.createRoute(appointmentId))
                },
                onNavigateToPatientProfile = { patientId ->
                    navController.navigate(Screen.PatientProfile.createRoute(patientId))
                }
            )
        }

        composable(Screen.CreateAppointment.route) {
            CreateAppointmentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAppointmentCreated = {
                    navController.popBackStack()
                },
                onNavigateToAddPatient = {
                    navController.navigate(Screen.AddEditPatient.createRoute(fromAppointment = true))
                }
            )
        }

        composable(
            route = Screen.AppointmentDetails.route,
            arguments = listOf(
                navArgument("appointmentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val appointmentId =
                backStackEntry.arguments?.getString("appointmentId") ?: return@composable
            AppointmentDetailsScreen(
                appointmentId = appointmentId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditAppointment.route,
            arguments = listOf(
                navArgument("appointmentId") { type = NavType.StringType }
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

        // Add Patients routes
        composable(Screen.Patients.route) {
            PatientsScreen(
                onNavigateToAddPatient = {
                    navController.navigate(Screen.AddEditPatient.createRoute())
                },
                onNavigateToEditPatient = { patientId ->
                    navController.navigate(Screen.AddEditPatient.createRoute(patientId))
                }
            )
        }

        composable(
            route = Screen.AddEditPatient.route,
            arguments = listOf(
                navArgument("patientId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("fromAppointment") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val fromAppointment = backStackEntry.arguments?.getBoolean("fromAppointment") == true
            AddEditPatientScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.PatientProfile.route,
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: return@composable
            PatientProfileScreen(
                patientId = patientId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(Screen.AddEditPatient.createRoute(patientId))
                }
            )
        }

        composable(Screen.DoctorProfile.route) {
            DoctorProfileScreen(
                onNavigateToHome = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Shipments.route) {
            ShipmentsScreen(
                onNavigateToPatient = { patientId ->
                    navController.navigate(Screen.PatientProfile.createRoute(patientId))
                }
            )
        }
    }
}