package pheonix.app.patient.presentation.shipments

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.data.model.Shipment
import pheonix.app.patient.data.model.ShipmentItem
import pheonix.app.patient.presentation.shipments.components.ShipmentCard
import pheonix.app.patient.presentation.components.BottomSheetContent
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentsScreen(
    viewModel: ShipmentsViewModel = hiltViewModel(),
    onNavigateToPatient: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shipments") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Shipment")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.shipments.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No shipments found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (uiState.selectedPatient != null || uiState.selectedStatus != null) {
                        TextButton(onClick = {
                            viewModel.selectPatient(null)
                            viewModel.selectStatus(null)
                        }) {
                            Text("Clear filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.shipments) { shipment ->
                        ShipmentCard(
                            shipment = shipment,
                            onStatusChange = { status ->
                                viewModel.updateShipmentStatus(shipment.id, status)
                            },
                            onDelete = {
                                viewModel.deleteShipment(shipment.id)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }

        if (showAddSheet) {
            CreateShipmentBottomSheet(
                patients = uiState.patients,
                onDismiss = { showAddSheet = false },
                onSubmit = { patient, items, address, notes ->
                    viewModel.createShipment(patient, items, address, null)
                    showAddSheet = false
                }
            )
        }

        if (showFilterSheet) {
            FilterBottomSheet(
                selectedPatient = uiState.selectedPatient,
                selectedStatus = uiState.selectedStatus,
                patients = uiState.patients,
                onPatientSelected = { viewModel.selectPatient(it) },
                onStatusSelected = { viewModel.selectStatus(it) },
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateShipmentBottomSheet(
    patients: List<Patient>,
    onDismiss: () -> Unit,
    onSubmit: (Patient, List<ShipmentItem>, String, String?) -> Unit
) {
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var items by remember { mutableStateOf(listOf<ShipmentItem>()) }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showItemDialog by remember { mutableStateOf(false) }
    var showAddressSheet by remember { mutableStateOf(false) }
    var patientExpanded by remember { mutableStateOf(false) }
    val viewModel: ShipmentsViewModel = hiltViewModel()
    val addressPredictions by viewModel.addressPredictions.collectAsState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create Shipment",
                style = MaterialTheme.typography.headlineSmall
            )

            // Patient selection
            ExposedDropdownMenuBox(
                expanded = patientExpanded,
                onExpandedChange = { patientExpanded = !patientExpanded }
            ) {
                OutlinedTextField(
                    value = selectedPatient?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Patient") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = patientExpanded,
                    onDismissRequest = { patientExpanded = false }
                ) {
                    patients.forEach { patient ->
                        DropdownMenuItem(
                            text = { Text(patient.name) },
                            onClick = {
                                selectedPatient = patient
                                patientExpanded = false
                            }
                        )
                    }
                }
            }

            // Items section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Items",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FilledTonalButton(
                        onClick = { showItemDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Item")
                    }
                }

                if (items.isEmpty()) {
                    Text(
                        text = "No items added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${item.quantity}x - ${item.type.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    items = items.toMutableList().apply { removeAt(index) }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove item",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Address Field
            OutlinedTextField(
                value = address,
                onValueChange = {},
                readOnly = true,
                label = { Text("Shipping Address") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showAddressSheet = true }) {
                        Icon(Icons.Default.Search, "Search address")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAddressSheet = true },
                maxLines = 3
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Submit button
            Button(
                onClick = {
                    selectedPatient?.let { patient ->
                        onSubmit(patient, items, address, notes.ifEmpty { null })
                    }
                },
                enabled = selectedPatient != null && items.isNotEmpty() && address.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Shipment")
            }
        }
    }

    if (showItemDialog) {
        AddItemDialog(
            onDismiss = { showItemDialog = false },
            onSubmit = { newItem ->
                items = items + newItem
                showItemDialog = false
            }
        )
    }

    if (showAddressSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddressSheet = false }
        ) {
            BottomSheetContent(
                title = "Select Address",
                onDismiss = { showAddressSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { 
                            address = it
                            viewModel.updateAddressQuery(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        label = { Text("Search Address") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(addressPredictions) { prediction ->
                            ListItem(
                                headlineContent = { Text(prediction.description) },
                                modifier = Modifier.clickable {
                                    address = prediction.description
                                    showAddressSheet = false
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onSubmit: (ShipmentItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var type by remember { mutableStateOf(ShipmentItem.ItemType.MEDICATION) }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { if (it.toIntOrNull() != null) quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = type.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Item Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ShipmentItem.ItemType.values().forEach { itemType ->
                            DropdownMenuItem(
                                text = { Text(itemType.name) },
                                onClick = {
                                    type = itemType
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        ShipmentItem(
                            name = name,
                            quantity = quantity.toIntOrNull() ?: 1,
                            type = type,
                            notes = notes.ifEmpty { null }
                        )
                    )
                },
                enabled = name.isNotEmpty() && quantity.toIntOrNull() != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    selectedPatient: Patient?,
    selectedStatus: Shipment.ShipmentStatus?,
    patients: List<Patient>,
    onPatientSelected: (Patient?) -> Unit,
    onStatusSelected: (Shipment.ShipmentStatus?) -> Unit,
    onDismiss: () -> Unit
) {
    var patientExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filter Shipments",
                style = MaterialTheme.typography.headlineSmall
            )

            // Patient filter
            ExposedDropdownMenuBox(
                expanded = patientExpanded,
                onExpandedChange = { patientExpanded = !patientExpanded }
            ) {
                OutlinedTextField(
                    value = selectedPatient?.name ?: "All Patients",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter by Patient") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = patientExpanded,
                    onDismissRequest = { patientExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Patients") },
                        onClick = {
                            onPatientSelected(null)
                            patientExpanded = false
                        }
                    )
                    patients.forEach { patient ->
                        DropdownMenuItem(
                            text = { Text(patient.name) },
                            onClick = {
                                onPatientSelected(patient)
                                patientExpanded = false
                            }
                        )
                    }
                }
            }

            // Status filter
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded }
            ) {
                OutlinedTextField(
                    value = selectedStatus?.name ?: "All Statuses",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter by Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Statuses") },
                        onClick = {
                            onStatusSelected(null)
                            statusExpanded = false
                        }
                    )
                    Shipment.ShipmentStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = {
                                onStatusSelected(status)
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            // Clear filters button
            if (selectedPatient != null || selectedStatus != null) {
                Button(
                    onClick = {
                        onPatientSelected(null)
                        onStatusSelected(null)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Clear Filters")
                }
            }
        }
    }
} 