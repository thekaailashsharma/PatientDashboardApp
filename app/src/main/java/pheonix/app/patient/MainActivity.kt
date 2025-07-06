package pheonix.app.patient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pheonix.app.patient.presentation.navigation.BottomNavigation
import pheonix.app.patient.presentation.navigation.NavGraph
import pheonix.app.patient.presentation.navigation.Screen
import pheonix.app.patient.ui.theme.PatientDashboardAppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatientDashboardAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Control bottom bar visibility
                    val showBottomBar = when (currentRoute) {
                        Screen.Home.route, Screen.Appointments.route,
                        Screen.Chat.route, Screen.Profile.route -> true
                        else -> false
                    }

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavigation(navController = navController)
                            }
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            NavGraph(
                                navController = navController,
                                startDestination = Screen.Login.route
                            )
                        }
                    }
                }
            }
        }
    }
}