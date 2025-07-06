package pheonix.app.patient.presentation.patients

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pheonix.app.patient.data.model.MedicalCondition
import pheonix.app.patient.data.model.Patient
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PatientProfileScreen(
    patientId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: PatientsViewModel = hiltViewModel()
) {
    val patients by viewModel.patients.collectAsState()
    val patient = patients.find { it.id == patientId }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Patient Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(patientId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (patient == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section with Avatar and Basic Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Patient Avatar
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = patient.name.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Patient Name and Status
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Chip
                    Surface(
                        color = when (patient.status) {
                            Patient.PatientStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                            Patient.PatientStatus.INACTIVE -> MaterialTheme.colorScheme.surfaceVariant
                            Patient.PatientStatus.BLOCKED -> MaterialTheme.colorScheme.errorContainer
                        },
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = when (patient.status) {
                                    Patient.PatientStatus.ACTIVE -> Icons.Default.CheckCircle
                                    Patient.PatientStatus.INACTIVE -> Icons.Default.PauseCircle
                                    Patient.PatientStatus.BLOCKED -> Icons.Default.Cancel
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = when (patient.status) {
                                    Patient.PatientStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                                    Patient.PatientStatus.INACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
                                    Patient.PatientStatus.BLOCKED -> MaterialTheme.colorScheme.error
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = patient.status.name,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // Content Sections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Info Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(
                        icon = Icons.Outlined.Cake,
                        label = "Age",
                        value = patient.dateOfBirth?.let {
                            "${calculateAge(it)} years"
                        } ?: "N/A",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        icon = Icons.Outlined.Bloodtype,
                        label = "Blood Group",
                        value = patient.bloodGroup ?: "N/A",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Contact Information
                ElevatedInfoSection(
                    title = "Contact Information",
                    icon = Icons.Outlined.ContactPhone
                ) {
                    InfoRow(
                        icon = Icons.Outlined.Phone,
                        label = "Phone",
                        value = patient.contactNumber,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    patient.email?.let {
                        InfoRow(
                            icon = Icons.Outlined.Email,
                            label = "Email",
                            value = it,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    InfoRow(
                        icon = Icons.Outlined.LocationOn,
                        label = "Address",
                        value = patient.address,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Medical History
                if (patient.medicalHistory.isNotEmpty()) {
                    ElevatedInfoSection(
                        title = "Medical History",
                        icon = Icons.Outlined.MedicalServices
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            patient.medicalHistory.forEach { condition ->
                                MedicalConditionCard(condition = condition)
                            }
                        }
                    }
                }

                // Allergies
                if (patient.allergies.isNotEmpty()) {
                    ElevatedInfoSection(
                        title = "Allergies",
                        icon = Icons.Outlined.ErrorOutline
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            patient.allergies.forEach { allergy ->
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = allergy,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Emergency Contact
                patient.emergencyContact?.let { contact ->
                    ElevatedInfoSection(
                        title = "Emergency Contact",
                        icon = Icons.Outlined.ContactEmergency
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = contact.relationship,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = contact.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            contact.address?.let { address ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = address,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Visit Information
                ElevatedInfoSection(
                    title = "Visit Information",
                    icon = Icons.Outlined.Event
                ) {
                    patient.lastVisit?.let {
                        InfoRow(
                            icon = Icons.Outlined.History,
                            label = "Last Visit",
                            value = dateFormat.format(it),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    patient.nextAppointment?.let {
                        InfoRow(
                            icon = Icons.Outlined.EventAvailable,
                            label = "Next Appointment",
                            value = dateFormat.format(it),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ElevatedInfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun MedicalConditionCard(condition: MedicalCondition) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = condition.condition,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = when (condition.status) {
                        MedicalCondition.Status.ONGOING -> MaterialTheme.colorScheme.errorContainer
                        MedicalCondition.Status.RECOVERED -> MaterialTheme.colorScheme.primaryContainer
                        MedicalCondition.Status.MANAGED -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = condition.status.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            condition.notes?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun calculateAge(dateOfBirth: Date): Int {
    val today = Calendar.getInstance()
    val birthDate = Calendar.getInstance()
    birthDate.time = dateOfBirth
    var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}