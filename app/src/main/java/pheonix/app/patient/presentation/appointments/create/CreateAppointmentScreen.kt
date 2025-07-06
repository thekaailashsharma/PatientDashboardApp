package pheonix.app.patient.presentation.appointments.create

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
import com.google.firebase.Timestamp
import pheonix.app.patient.data.model.Appointment
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentScreen(
    viewModel: CreateAppointmentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAppointmentCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var patientName by remember { mutableStateOf("") }
    var appointmentType by remember { mutableStateOf(Appointment.AppointmentType.IN_PERSON) }
    var selectedDate by remember { mutableStateOf<Date>(Date()) }
    var selectedTime by remember { mutableStateOf<Date>(Date()) }
    var duration by remember { mutableStateOf(30) } // Default 30 minutes
    var notes by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

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
                // Patient Name
                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text("Patient Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                )

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
                        if (selectedDate != null && selectedTime != null) {
                            val calendar = Calendar.getInstance().apply {
                                time = selectedDate!!
                                val timeCalendar = Calendar.getInstance().apply { time = selectedTime!! }
                                set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                            }

                            viewModel.createAppointment(
                                Appointment(
                                    patientName = patientName,
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
                    enabled = patientName.isNotBlank() && selectedDate != null && selectedTime != null
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
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(onDismissRequest, confirmButton = confirmButton, dismissButton = dismissButton, title = content)
} 