package pheonix.app.patient.presentation.appointments

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pheonix.app.patient.data.model.Appointment
import pheonix.app.patient.presentation.appointments.components.AppointmentCard
import pheonix.app.patient.presentation.appointments.components.EmptyAppointments
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues

enum class AppointmentTab {
    TODAY, UPCOMING, PAST
}

enum class SortOption {
    DATE_ASC, DATE_DESC, DURATION_ASC, DURATION_DESC
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppointmentsScreen(
    viewModel: AppointmentsViewModel = hiltViewModel(),
    onNavigateToAppointmentDetails: (String) -> Unit,
    onNavigateToCreateAppointment: () -> Unit,
    onNavigateToEditAppointment: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }
    var selectedTab by remember { mutableStateOf(AppointmentTab.TODAY) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var selectedSortOption by remember { mutableStateOf(SortOption.DATE_ASC) }
    var selectedStatusFilter by remember { mutableStateOf<Appointment.AppointmentStatus?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<Appointment.AppointmentType?>(null) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Appointments",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        // Filter Button
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                        // Sort Button
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        // Add Button
                        IconButton(onClick = onNavigateToCreateAppointment) {
                            Icon(Icons.Default.Add, contentDescription = "Add appointment")
                        }
                    }
                )
                
                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppointmentTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = when (tab) {
                                        AppointmentTab.TODAY -> "Today"
                                        AppointmentTab.UPCOMING -> "Upcoming"
                                        AppointmentTab.PAST -> "Past"
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val appointments = when (selectedTab) {
                    AppointmentTab.TODAY -> uiState.todayAppointments
                    AppointmentTab.UPCOMING -> uiState.upcomingAppointments
                    AppointmentTab.PAST -> uiState.pastAppointments
                }.let { appointments ->
                    // Apply filters
                    appointments.filter { appointment ->
                        (selectedStatusFilter == null || appointment.status == selectedStatusFilter) &&
                        (selectedTypeFilter == null || appointment.type == selectedTypeFilter)
                    }
                }.let { filtered ->
                    // Apply sorting
                    when (selectedSortOption) {
                        SortOption.DATE_ASC -> filtered.sortedBy { it.scheduledFor }
                        SortOption.DATE_DESC -> filtered.sortedByDescending { it.scheduledFor }
                        SortOption.DURATION_ASC -> filtered.sortedBy { it.duration }
                        SortOption.DURATION_DESC -> filtered.sortedByDescending { it.duration }
                    }
                }

                if (appointments.isEmpty()) {
                    EmptyAppointments(
                        title = "No ${selectedTab.name.lowercase().replaceFirstChar { it.uppercase() }} Appointments",
                        message = when (selectedTab) {
                            AppointmentTab.TODAY -> "You have no appointments scheduled for today."
                            AppointmentTab.UPCOMING -> "You have no upcoming appointments scheduled."
                            AppointmentTab.PAST -> "You have no past appointments."
                        },
                        onAddClick = onNavigateToCreateAppointment,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(appointments) { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                onClick = { onNavigateToAppointmentDetails(appointment.id) },
                                onEditClick = { onNavigateToEditAppointment(appointment.id) },
                                onDeleteClick = {
                                    appointmentToDelete = appointment
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Filter Dialog
            if (showFilterDialog) {
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    title = { Text("Filter Appointments") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Status Filter
                            Text("Status", style = MaterialTheme.typography.titleMedium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedStatusFilter == null,
                                    onClick = { selectedStatusFilter = null },
                                    label = { Text("All") }
                                )
                                Appointment.AppointmentStatus.values().forEach { status ->
                                    FilterChip(
                                        selected = selectedStatusFilter == status,
                                        onClick = { selectedStatusFilter = status },
                                        label = { Text(status.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) }
                                    )
                                }
                            }

                            // Type Filter
                            Text("Type", style = MaterialTheme.typography.titleMedium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedTypeFilter == null,
                                    onClick = { selectedTypeFilter = null },
                                    label = { Text("All") }
                                )
                                Appointment.AppointmentType.values().forEach { type ->
                                    FilterChip(
                                        selected = selectedTypeFilter == type,
                                        onClick = { selectedTypeFilter = type },
                                        label = { Text(type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFilterDialog = false }) {
                            Text("Apply")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                selectedStatusFilter = null
                                selectedTypeFilter = null
                                showFilterDialog = false
                            }
                        ) {
                            Text("Reset")
                        }
                    }
                )
            }

            // Sort Dialog
            if (showSortDialog) {
                AlertDialog(
                    onDismissRequest = { showSortDialog = false },
                    title = { Text("Sort By") },
                    text = {
                        Column {
                            SortOption.values().forEach { option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            selectedSortOption = option
                                            showSortDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedSortOption == option,
                                        onClick = { 
                                            selectedSortOption = option
                                            showSortDialog = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (option) {
                                            SortOption.DATE_ASC -> "Date (Oldest First)"
                                            SortOption.DATE_DESC -> "Date (Newest First)"
                                            SortOption.DURATION_ASC -> "Duration (Shortest First)"
                                            SortOption.DURATION_DESC -> "Duration (Longest First)"
                                        }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {}
                )
            }

            // Delete Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Appointment") },
                    text = { Text("Are you sure you want to delete this appointment?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                appointmentToDelete?.let { appointment ->
                                    viewModel.deleteAppointment(appointment.id)
                                }
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Error Snackbar
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(uiState.error!!)
                }
            }

            // Success Snackbar
            if (uiState.successMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.clearSuccessMessage() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(uiState.successMessage!!)
                }
            }
        }
    }
} 