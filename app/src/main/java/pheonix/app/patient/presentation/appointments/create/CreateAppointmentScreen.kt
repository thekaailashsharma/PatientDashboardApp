package pheonix.app.patient.presentation.appointments.create

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import pheonix.app.patient.data.model.Appointment
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.presentation.patients.components.PatientCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentScreen(
    viewModel: CreateAppointmentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAppointmentCreated: () -> Unit,
    onNavigateToAddPatient: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scrollState = rememberScrollState()

    var showPatientSearch by remember { mutableStateOf(false) }
    var appointmentType by remember { mutableStateOf(Appointment.AppointmentType.IN_PERSON) }
    var selectedDate by remember { mutableStateOf<Date>(Date()) }
    var selectedTime by remember { mutableStateOf<Date>(Date()) }
    var duration by remember { mutableStateOf(30) } // Default 30 minutes
    var notes by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Get selected patient
    val selectedPatient = patients.find { it.id == uiState.selectedPatientId }

    LaunchedEffect(uiState.isAppointmentCreated) {
        if (uiState.isAppointmentCreated) {
            onAppointmentCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Appointment") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Patient Selection
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPatientSearch = true }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Patient",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (selectedPatient != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = selectedPatient.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Change patient",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Select a patient",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Select patient",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Appointment Type
                Column {
                    Text(
                        text = "Appointment Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Appointment.AppointmentType.values().forEach { type ->
                            FilterChip(
                                selected = appointmentType == type,
                                onClick = { appointmentType = type },
                                label = {
                                    Text(
                                        text = when (type) {
                                            Appointment.AppointmentType.VIDEO_CALL -> "Video Call"
                                            Appointment.AppointmentType.IN_PERSON -> "In-Person"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (type) {
                                            Appointment.AppointmentType.VIDEO_CALL -> Icons.Default.VideoCall
                                            Appointment.AppointmentType.IN_PERSON -> Icons.Default.Person
                                        },
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }

                // Date and Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedDate?.let { 
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Select date")
                            }
                        }
                    )

                    OutlinedTextField(
                        value = selectedTime?.let { 
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Select time")
                            }
                        }
                    )
                }

                // Duration
                OutlinedTextField(
                    value = duration.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { value ->
                            if (value > 0) duration = value
                        }
                    },
                    label = { Text("Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Timer, contentDescription = null)
                    }
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null)
                    }
                )

                // Symptoms
                OutlinedTextField(
                    value = symptoms,
                    onValueChange = { symptoms = it },
                    label = { Text("Symptoms (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    leadingIcon = {
                        Icon(Icons.Default.LocalHospital, contentDescription = null)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Create Button
                Button(
                    onClick = {
                        if (selectedDate != null && selectedTime != null && selectedPatient != null) {
                            val calendar = Calendar.getInstance().apply {
                                time = selectedDate!!
                                val timeCalendar = Calendar.getInstance().apply { time = selectedTime!! }
                                set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                            }

                            viewModel.createAppointment(
                                Appointment(
                                    patientId = selectedPatient.id,
                                    patientName = selectedPatient.name,
                                    type = appointmentType,
                                    scheduledFor = calendar.time,
                                    duration = duration,
                                    notes = notes,
                                    symptoms = symptoms.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedPatient != null && selectedDate != null && selectedTime != null
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Create Appointment")
                    }
                }
            }

            // Patient Search Dialog
            if (showPatientSearch) {
                Dialog(
                    onDismissRequest = { showPatientSearch = false }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Select Patient",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search patients...") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(patients) { patient ->
                                    PatientListItem(
                                        patient = patient,
                                        onClick = {
                                            viewModel.selectPatient(patient.id)
                                            showPatientSearch = false
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    showPatientSearch = false
                                    onNavigateToAddPatient()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add New Patient")
                            }
                        }
                    }
                }
            }

            // Error handling
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(uiState.error!!)
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Date(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                        }
                        selectedTime = calendar.time
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun PatientListItem(
    patient: Patient,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp),
                shape = CircleShape,
                color = when (patient.status) {
                    Patient.PatientStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                    Patient.PatientStatus.INACTIVE -> MaterialTheme.colorScheme.surfaceVariant
                    Patient.PatientStatus.BLOCKED -> MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = when (patient.status) {
                            Patient.PatientStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimaryContainer
                            Patient.PatientStatus.INACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
                            Patient.PatientStatus.BLOCKED -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }

            Column {
                Text(
                    text = patient.name,
                    style = MaterialTheme.typography.titleMedium
                )
                patient.contactNumber?.let { phone ->
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(onDismissRequest, confirmButton = confirmButton, dismissButton = dismissButton, title = content)
} 