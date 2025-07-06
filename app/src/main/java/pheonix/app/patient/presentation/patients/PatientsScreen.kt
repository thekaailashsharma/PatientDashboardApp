package pheonix.app.patient.presentation.patients

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.presentation.patients.components.PatientCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PatientsScreen(
    onNavigateToAddPatient: () -> Unit,
    onNavigateToEditPatient: (String) -> Unit,
    viewModel: PatientsViewModel = hiltViewModel()
) {
    val patients by viewModel.patients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showDeleteConfirmation by remember { mutableStateOf<Patient?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = "Patients",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSearchBar = !showSearchBar }) {
                            Icon(
                                if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (showSearchBar) "Hide search" else "Show search"
                            )
                        }
                        
                        IconButton(onClick = { showFilterSheet = true }) {
                            Badge(
                                modifier = Modifier.offset(x = 14.dp, y = (-8).dp),
                                containerColor = if (filterStatus != null)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            ) {
                                if (filterStatus != null) {
                                    Text("1")
                                }
                            }
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter patients"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )

                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        placeholder = { Text("Search patients...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true
                    )
                }

                AnimatedVisibility(
                    visible = filterStatus != null,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterStatus?.let { status ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.updateFilterStatus(null) },
                                label = {
                                    Text(
                                        status.name.lowercase()
                                            .replaceFirstChar { it.uppercase() }
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear filter",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddPatient,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Patient") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { viewModel.refresh() }
            ) {
                if (patients.isEmpty() && !isRefreshing) {
                    EmptyPatientsView(
                        hasFilters = filterStatus != null || searchQuery.isNotEmpty(),
                        onAddClick = onNavigateToAddPatient,
                        onClearFilters = {
                            viewModel.updateFilterStatus(null)
                            viewModel.updateSearchQuery("")
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = patients,
                            key = { it.id }
                        ) { patient ->
                            PatientCard(
                                patient = patient,
                                onClick = { onNavigateToEditPatient(patient.id) },
                                onStatusChange = { viewModel.updatePatientStatus(patient.id, it) },
                                onDeleteClick = { showDeleteConfirmation = patient },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }

        // Filter Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Filter Patients",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Patient.PatientStatus.values().forEach { status ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.updateFilterStatus(
                                            if (filterStatus == status) null else status
                                        )
                                        showFilterSheet = false
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when (status) {
                                            Patient.PatientStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                                            Patient.PatientStatus.INACTIVE -> MaterialTheme.colorScheme.surfaceVariant
                                            Patient.PatientStatus.BLOCKED -> MaterialTheme.colorScheme.errorContainer
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = when (status) {
                                                    Patient.PatientStatus.ACTIVE -> Icons.Default.CheckCircle
                                                    Patient.PatientStatus.INACTIVE -> Icons.Default.PauseCircle
                                                    Patient.PatientStatus.BLOCKED -> Icons.Default.Block
                                                },
                                                contentDescription = null,
                                                tint = when (status) {
                                                    Patient.PatientStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    Patient.PatientStatus.INACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
                                                    Patient.PatientStatus.BLOCKED -> MaterialTheme.colorScheme.onErrorContainer
                                                }
                                            )
                                        }
                                    }
                                    Text(
                                        text = status.name.lowercase()
                                            .replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                RadioButton(
                                    selected = filterStatus == status,
                                    onClick = {
                                        viewModel.updateFilterStatus(
                                            if (filterStatus == status) null else status
                                        )
                                        showFilterSheet = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Delete Confirmation Dialog
        showDeleteConfirmation?.let { patient ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = null },
                icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                title = { Text("Delete Patient") },
                text = {
                    Text(
                        "Are you sure you want to delete ${patient.name}? " +
                        "This action cannot be undone."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePatient(patient.id)
                            showDeleteConfirmation = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Error Snackbar
        error?.let { errorMessage ->
            Snackbar(
                modifier = Modifier
                    .padding(16.dp),
                action = {
                    TextButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(errorMessage)
            }
            LaunchedEffect(errorMessage) {
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun EmptyPatientsView(
    hasFilters: Boolean,
    onAddClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (hasFilters) Icons.Default.FilterAlt else Icons.Default.PeopleAlt,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (hasFilters) "No matching patients found" else "No patients yet",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (hasFilters) {
                "Try adjusting your filters or search terms"
            } else {
                "Add your first patient to get started"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (hasFilters) {
            OutlinedButton(
                onClick = onClearFilters,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.Default.FilterAltOff, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Filters")
            }
        } else {
            Button(
                onClick = onAddClick,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Patient")
            }
        }
    }
} 