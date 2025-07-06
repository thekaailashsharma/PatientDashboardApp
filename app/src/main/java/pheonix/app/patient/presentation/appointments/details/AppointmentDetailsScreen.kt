package pheonix.app.patient.presentation.appointments.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppointmentDetailsScreen(
    appointmentId: String,
    viewModel: AppointmentDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateFormatFull = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    LaunchedEffect(appointmentId) {
        viewModel.loadAppointment(appointmentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                uiState.appointment?.let { appointment ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Status and Type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Type
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = when (appointment.type) {
                                    Appointment.AppointmentType.VIDEO_CALL -> MaterialTheme.colorScheme.primaryContainer
                                    Appointment.AppointmentType.IN_PERSON -> MaterialTheme.colorScheme.tertiaryContainer
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (appointment.type == Appointment.AppointmentType.VIDEO_CALL)
                                            Icons.Default.VideoCall
                                        else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = when (appointment.type) {
                                            Appointment.AppointmentType.VIDEO_CALL -> MaterialTheme.colorScheme.primary
                                            Appointment.AppointmentType.IN_PERSON -> MaterialTheme.colorScheme.tertiary
                                        }
                                    )
                                    Text(
                                        text = if (appointment.type == Appointment.AppointmentType.VIDEO_CALL)
                                            "Video Call"
                                        else "In-Person",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }

                            // Duration
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "${appointment.duration} min",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        // Date and Time
                        appointment.scheduledFor?.let { date ->
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = dateFormatFull.format(date),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = dateFormat.format(date),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Patient/Doctor Info
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (appointment.doctorId.isNotEmpty())
                                            appointment.patientName
                                        else appointment.doctorName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Status
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = when (appointment.status) {
                                        Appointment.AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primaryContainer
                                        Appointment.AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.tertiaryContainer
                                        Appointment.AppointmentStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                                        Appointment.AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
                                        Appointment.AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.onSecondaryContainer
                                        Appointment.AppointmentStatus.NO_SHOW -> MaterialTheme.colorScheme.onTertiaryContainer
                                        Appointment.AppointmentStatus.RESCHEDULED -> MaterialTheme.colorScheme.onErrorContainer
                                    }
                                ) {
                                    Text(
                                        text = appointment.status.name.replace("_", " ")
                                            .lowercase()
                                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = when (appointment.status) {
                                            Appointment.AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primaryContainer
                                            Appointment.AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.tertiaryContainer
                                            Appointment.AppointmentStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                                            Appointment.AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
                                            Appointment.AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.onSecondaryContainer
                                            Appointment.AppointmentStatus.NO_SHOW -> MaterialTheme.colorScheme.onTertiaryContainer
                                            Appointment.AppointmentStatus.RESCHEDULED -> MaterialTheme.colorScheme.onErrorContainer
                                        }
                                    )
                                }
                            }
                        }

                        // Notes
                        if (appointment.notes.isNotEmpty()) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Notes",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = appointment.notes,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        // Symptoms
                        if (appointment.symptoms.isNotEmpty()) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Symptoms",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        appointment.symptoms.forEach { symptom ->
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.surface
                                            ) {
                                                Text(
                                                    text = symptom,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Prescription
                        if (appointment.prescription != null) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Prescription",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = appointment.prescription!!,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        // Follow-up Date
                        appointment.followUpDate?.let { followUpDate ->
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Follow-up Date",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = dateFormatFull.format(followUpDate),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 