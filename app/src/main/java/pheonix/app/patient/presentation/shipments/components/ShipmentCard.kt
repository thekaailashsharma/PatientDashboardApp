package pheonix.app.patient.presentation.shipments.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pheonix.app.patient.data.model.Shipment
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentCard(
    shipment: Shipment,
    onStatusChange: (Shipment.ShipmentStatus) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Blinking animation for pending status
    val infiniteTransition = rememberInfiniteTransition(label = "blinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinking"
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status indicator with blinking effect for PENDING
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when (shipment.status) {
                                    Shipment.ShipmentStatus.PENDING -> MaterialTheme.colorScheme.primary.copy(
                                        alpha = if (shipment.status == Shipment.ShipmentStatus.PENDING) alpha else 1f
                                    )
                                    Shipment.ShipmentStatus.IN_TRANSIT -> MaterialTheme.colorScheme.tertiary
                                    Shipment.ShipmentStatus.DELIVERED -> MaterialTheme.colorScheme.secondary
                                    Shipment.ShipmentStatus.PROCESSING,
                                    Shipment.ShipmentStatus.SHIPPED,
                                    Shipment.ShipmentStatus.OUT_FOR_DELIVERY -> MaterialTheme.colorScheme.tertiary
                                    Shipment.ShipmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = shipment.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                        
                        if (shipment.status != Shipment.ShipmentStatus.DELIVERED) {
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Mark as ${getNextStatus(shipment.status)}") },
                                onClick = {
                                    expanded = false
                                    onStatusChange(getNextStatus(shipment.status))
                                },
                                leadingIcon = {
                                    Icon(
                                        if (shipment.status == Shipment.ShipmentStatus.PENDING)
                                            Icons.Default.LocalShipping
                                        else Icons.Default.Done,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Status: ${shipment.status.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = shipment.shippingAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!shipment.items.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Items:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                shipment.items.forEach { item ->
                    Text(
                        text = "• ${item.quantity}x ${item.name} (${item.type.name})",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }

            shipment.notes?.let { notes ->
                if (notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notes: $notes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Shipment") },
            text = { Text("Are you sure you want to delete this shipment?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
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
}

private fun getNextStatus(current: Shipment.ShipmentStatus): Shipment.ShipmentStatus {
    return when (current) {
        Shipment.ShipmentStatus.PENDING -> Shipment.ShipmentStatus.PROCESSING
        Shipment.ShipmentStatus.PROCESSING -> Shipment.ShipmentStatus.SHIPPED
        Shipment.ShipmentStatus.SHIPPED -> Shipment.ShipmentStatus.IN_TRANSIT
        Shipment.ShipmentStatus.IN_TRANSIT -> Shipment.ShipmentStatus.OUT_FOR_DELIVERY
        Shipment.ShipmentStatus.OUT_FOR_DELIVERY -> Shipment.ShipmentStatus.DELIVERED
        Shipment.ShipmentStatus.DELIVERED -> Shipment.ShipmentStatus.DELIVERED
        Shipment.ShipmentStatus.CANCELLED -> Shipment.ShipmentStatus.CANCELLED
    }
} 