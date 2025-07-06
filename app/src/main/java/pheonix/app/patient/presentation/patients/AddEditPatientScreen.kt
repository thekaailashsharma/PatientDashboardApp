package pheonix.app.patient.presentation.patients

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.data.model.EmergencyContact
import pheonix.app.patient.data.model.MedicalCondition
import pheonix.app.patient.data.model.Medication
import pheonix.app.patient.presentation.components.CustomTextField
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AddEditPatientScreen(
    patientId: String? = null,
    onNavigateBack: () -> Unit,
    onPatientAdded: (() -> Unit)? = null,
    viewModel: AddEditPatientViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var currentStep by remember { mutableStateOf(0) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    var showGenderDialog by remember { mutableStateOf(false) }
    var showBloodGroupDialog by remember { mutableStateOf(false) }
    var showAddConditionDialog by remember { mutableStateOf(false) }
    var showAddMedicationDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Form validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var contactError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    // Handle successful save
    LaunchedEffect(uiState.isPatientSaved) {
        if (uiState.isPatientSaved) {
            onPatientAdded?.invoke()
            onNavigateBack()
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateOfBirth?.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateDateOfBirth(Date(millis))
                            dateError = null
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
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (patientId == null) "Add Patient" else "Edit Patient",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Progress Indicator
            LinearProgressIndicator(
                progress = (currentStep + 1) / 3f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // Step Title
            Text(
                text = when (currentStep) {
                    0 -> "Basic Information"
                    1 -> "Medical Information"
                    else -> "Emergency Contact"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Form Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() with
                            slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() with
                            slideOutHorizontally { width -> width } + fadeOut()
                        }
                    }
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (step) {
                            0 -> {
                                // Basic Information Form
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = "Patient photo",
                                                modifier = Modifier.size(60.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                CustomTextField(
                                    value = uiState.name,
                                    onValueChange = {
                                        viewModel.updateName(it)
                                        nameError = if (it.isBlank()) "Name is required" else null
                                    },
                                    label = "Full Name",
                                    leadingIcon = Icons.Default.Person,
                                    errorMessage = nameError
                                )

                                // Date of Birth
                                OutlinedTextField(
                                    value = uiState.dateOfBirth?.let { dateFormat.format(it) } ?: "",
                                    onValueChange = {},
                                    label = { Text("Date of Birth") },
                                    leadingIcon = {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                                    },
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDatePicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        focusedBorderColor = if (dateError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = if (dateError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { showDatePicker = true }) {
                                            Icon(Icons.Default.Event, contentDescription = "Select date")
                                        }
                                    },
                                    isError = dateError != null,
                                    supportingText = dateError?.let { { Text(it) } }
                                )

                                // Gender Selection
                                OutlinedTextField(
                                    value = uiState.gender.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    onValueChange = {},
                                    label = { Text("Gender") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null)
                                    },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { showGenderDialog = true }) {
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = "Select gender"
                                            )
                                        }
                                    }
                                )

                                CustomTextField(
                                    value = uiState.contactNumber,
                                    onValueChange = {
                                        viewModel.updateContactNumber(it)
                                        contactError = if (it.isBlank())
                                            "Contact number is required"
                                        else null
                                    },
                                    label = "Contact Number",
                                    leadingIcon = Icons.Default.Phone,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone
                                    ),
                                    errorMessage = contactError
                                )

                                CustomTextField(
                                    value = uiState.email ?: "",
                                    onValueChange = { viewModel.updateEmail(it.takeIf { it.isNotBlank() }) },
                                    label = "Email (Optional)",
                                    leadingIcon = Icons.Default.Email,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email
                                    )
                                )

                                CustomTextField(
                                    value = uiState.address,
                                    onValueChange = { viewModel.updateAddress(it) },
                                    label = "Address (Optional)",
                                    leadingIcon = Icons.Default.LocationOn
                                )
                            }
                            1 -> {
                                // Medical Information Form
                                OutlinedTextField(
                                    value = uiState.bloodGroup ?: "",
                                    onValueChange = {},
                                    label = { Text("Blood Group") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Bloodtype, contentDescription = null)
                                    },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { showBloodGroupDialog = true }) {
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = "Select blood group"
                                            )
                                        }
                                    }
                                )

                                // Medical History
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Medical History",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            IconButton(onClick = { showAddConditionDialog = true }) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "Add condition"
                                                )
                                            }
                                        }

                                        if (uiState.medicalHistory.isEmpty()) {
                                            Text(
                                                text = "No medical conditions added",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            uiState.medicalHistory.forEach { condition ->
                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(
                                                                text = condition.condition,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                            condition.diagnosedDate?.let { date ->
                                                                Text(
                                                                    text = "Diagnosed: ${dateFormat.format(date)}",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.removeMedicalCondition(condition)
                                                            }
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Remove condition"
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Allergies
                                CustomTextField(
                                    value = uiState.allergies.joinToString(", "),
                                    onValueChange = { text ->
                                        // Allow direct typing, split only when saving
                                        viewModel.updateAllergies(
                                            if (text.isBlank()) {
                                                emptyList()
                                            } else {
                                                text.split(",")
                                                    .map { it.trim() }
                                                    .filter { it.isNotBlank() }
                                            }
                                        )
                                    },
                                    label = "Allergies (Optional)",
                                    placeholder = "Enter allergies separated by commas",
                                    leadingIcon = Icons.Default.Warning,
                                    helperText = "Example: Penicillin, Peanuts, Latex"
                                )

                                // Current Medications
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Current Medications",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            IconButton(onClick = { showAddMedicationDialog = true }) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "Add medication"
                                                )
                                            }
                                        }

                                        if (uiState.currentMedications.isEmpty()) {
                                            Text(
                                                text = "No medications added",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            uiState.currentMedications.forEach { medication ->
                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(
                                                                text = medication.name,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                            Text(
                                                                text = "${medication.dosage} - ${medication.frequency}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.removeMedication(medication)
                                                            }
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Remove medication"
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // Emergency Contact Form
                                val contact = uiState.emergencyContact

                                CustomTextField(
                                    value = contact?.name ?: "",
                                    onValueChange = { name ->
                                        viewModel.updateEmergencyContact(
                                            (contact ?: EmergencyContact(
                                                name = "",
                                                relationship = "",
                                                phoneNumber = "",
                                                address = null
                                            )).copy(name = name)
                                        )
                                    },
                                    label = "Contact Name",
                                    leadingIcon = Icons.Default.Person
                                )

                                CustomTextField(
                                    value = contact?.relationship ?: "",
                                    onValueChange = { relationship ->
                                        viewModel.updateEmergencyContact(
                                            (contact ?: EmergencyContact(
                                                name = "",
                                                relationship = "",
                                                phoneNumber = "",
                                                address = null
                                            )).copy(relationship = relationship)
                                        )
                                    },
                                    label = "Relationship",
                                    leadingIcon = Icons.Default.People
                                )

                                CustomTextField(
                                    value = contact?.phoneNumber ?: "",
                                    onValueChange = { phone ->
                                        viewModel.updateEmergencyContact(
                                            (contact ?: EmergencyContact(
                                                name = "",
                                                relationship = "",
                                                phoneNumber = "",
                                                address = null
                                            )).copy(phoneNumber = phone)
                                        )
                                    },
                                    label = "Phone Number",
                                    leadingIcon = Icons.Default.Phone,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone
                                    )
                                )

                                CustomTextField(
                                    value = contact?.address ?: "",
                                    onValueChange = { address ->
                                        viewModel.updateEmergencyContact(
                                            (contact ?: EmergencyContact(
                                                name = "",
                                                relationship = "",
                                                phoneNumber = "",
                                                address = null
                                            )).copy(address = address.takeIf { it.isNotBlank() })
                                        )
                                    },
                                    label = "Address (Optional)",
                                    leadingIcon = Icons.Default.LocationOn
                                )
                            }
                        }
                    }
                }
            }

            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Previous")
                    }
                }

                Button(
                    onClick = {
                        when (currentStep) {
                            0 -> {
                                // Clear previous errors
                                nameError = null
                                contactError = null
                                dateError = null

                                // Validate each field
                                var isValid = true
                                if (uiState.name.isBlank()) {
                                    nameError = "Name is required"
                                    isValid = false
                                }
                                if (uiState.contactNumber.isBlank()) {
                                    contactError = "Contact number is required"
                                    isValid = false
                                }
                                if (uiState.dateOfBirth == null) {
                                    dateError = "Date of birth is required"
                                    isValid = false
                                }

                                if (isValid) {
                                    currentStep++
                                }
                            }
                            1 -> {
                                // Medical info is optional, always proceed
                                currentStep++
                            }
                            2 -> {
                                // Emergency contact validation
                                if (viewModel.validateEmergencyContact()) {
                                    viewModel.savePatient()
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (currentStep < 2) "Next" else "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            if (currentStep < 2)
                                Icons.Default.ArrowForward
                            else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Gender Selection Dialog
    if (showGenderDialog) {
        AlertDialog(
            onDismissRequest = { showGenderDialog = false },
            title = { Text("Select Gender") },
            text = {
                Column {
                    Patient.Gender.values().forEach { gender ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = gender == uiState.gender,
                                onClick = {
                                    viewModel.updateGender(gender)
                                    showGenderDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = gender.name.lowercase()
                                    .replaceFirstChar { it.uppercase() }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGenderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Blood Group Selection Dialog
    if (showBloodGroupDialog) {
        AlertDialog(
            onDismissRequest = { showBloodGroupDialog = false },
            title = { Text("Select Blood Group") },
            text = {
                Column {
                    listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = group == uiState.bloodGroup,
                                onClick = {
                                    viewModel.updateBloodGroup(group)
                                    showBloodGroupDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = group)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBloodGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Medical Condition Dialog
    if (showAddConditionDialog) {
        var condition by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddConditionDialog = false },
            title = { Text("Add Medical Condition") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = { condition = it },
                        label = { Text("Condition") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (condition.isNotBlank()) {
                            viewModel.addMedicalCondition(
                                MedicalCondition(
                                    condition = condition,
                                    diagnosedDate = Date(),
                                    notes = notes.takeIf { it.isNotBlank() }
                                )
                            )
                            showAddConditionDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddConditionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Medication Dialog
    if (showAddMedicationDialog) {
        var name by remember { mutableStateOf("") }
        var dosage by remember { mutableStateOf("") }
        var frequency by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMedicationDialog = false },
            title = { Text("Add Medication") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medication Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { frequency = it },
                        label = { Text("Frequency") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank() && dosage.isNotBlank() && frequency.isNotBlank()) {
                            viewModel.addMedication(
                                Medication(
                                    name = name,
                                    dosage = dosage,
                                    frequency = frequency,
                                    startDate = Date(),
                                    endDate = null,
                                    prescribedBy = null,
                                    notes = notes.takeIf { it.isNotBlank() }
                                )
                            )
                            showAddMedicationDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMedicationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
} 