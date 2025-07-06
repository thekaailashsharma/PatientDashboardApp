package pheonix.app.patient.presentation.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import pheonix.app.patient.data.model.Appointment
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.data.model.Shipment
import kotlin.math.min

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        WelcomeSection()
        Spacer(modifier = Modifier.height(24.dp))
        StatisticsSection(
            appointments = uiState.appointments,
            patients = uiState.patients,
            shipments = uiState.shipments
        )
        Spacer(modifier = Modifier.height(24.dp))
        AppointmentsGraph(appointments = uiState.appointments)
        Spacer(modifier = Modifier.height(24.dp))
        ShipmentsStatusChart(shipments = uiState.shipments)
        Spacer(modifier = Modifier.height(24.dp))
        RecentActivities(
            appointments = uiState.appointments,
            shipments = uiState.shipments,
            navController = navController
        )
    }
}

@Composable
private fun WelcomeSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Here's your dashboard overview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                Icons.Default.Dashboard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun StatisticsSection(
    appointments: List<Appointment>,
    patients: List<Patient>,
    shipments: List<Shipment>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            title = "Patients",
            value = patients.size.toString(),
            icon = Icons.Default.Person,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Appointments",
            value = appointments.size.toString(),
            icon = Icons.Default.Event,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Shipments",
            value = shipments.size.toString(),
            icon = Icons.Default.LocalShipping,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AppointmentsGraph(appointments: List<Appointment>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Appointments Timeline",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            val color1 = MaterialTheme.colorScheme.primary
            // Simple line graph
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val width = size.width
                val height = size.height
                val pointsCount = min(7, appointments.size)

                
                // Draw horizontal lines
                for (i in 0..3) {
                    drawLine(
                        color = color,
                        start = Offset(0f, height * i / 3),
                        end = Offset(width, height * i / 3),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                
                if (appointments.isNotEmpty()) {
                    // Draw line graph
                    val points = appointments.take(pointsCount).reversed()
                    val maxAppointments = points.size
                    val xStep = width / (pointsCount - 1)
                    
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = color1,
                            start = Offset(
                                x = i * xStep,
                                y = height * (1 - (i + 1).toFloat() / maxAppointments)
                            ),
                            end = Offset(
                                x = (i + 1) * xStep,
                                y = height * (1 - (i + 2).toFloat() / maxAppointments)
                            ),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShipmentsStatusChart(shipments: List<Shipment>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Shipments Status",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val pending = shipments.count { it.status == Shipment.ShipmentStatus.PENDING }
            val inTransit = shipments.count { it.status == Shipment.ShipmentStatus.IN_TRANSIT }
            val delivered = shipments.count { it.status == Shipment.ShipmentStatus.DELIVERED }
            val total = shipments.size.toFloat().coerceAtLeast(1f)
            val color = MaterialTheme.colorScheme.surfaceVariant
            val color1 = MaterialTheme.colorScheme.primary
            val color2 = MaterialTheme.colorScheme.tertiary
            val color3 = MaterialTheme.colorScheme.secondary

            // Donut chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                ) {
                    val strokeWidth = 20.dp.toPx()
                    val diameter = size.width - strokeWidth
                    
                    // Draw background circle
                    drawCircle(
                        color = color,
                        style = Stroke(strokeWidth)
                    )
                    
                    var startAngle = 0f
                    
                    // Draw pending
                    val pendingAngle = 360f * (pending / total)
                    drawArc(
                        color = color1,
                        startAngle = startAngle,
                        sweepAngle = pendingAngle,
                        useCenter = false,
                        style = Stroke(strokeWidth)
                    )
                    startAngle += pendingAngle
                    
                    // Draw in transit
                    val transitAngle = 360f * (inTransit / total)
                    drawArc(
                        color = color2,
                        startAngle = startAngle,
                        sweepAngle = transitAngle,
                        useCenter = false,
                        style = Stroke(strokeWidth)
                    )
                    startAngle += transitAngle
                    
                    // Draw delivered
                    val deliveredAngle = 360f * (delivered / total)
                    drawArc(
                        color = color3,
                        startAngle = startAngle,
                        sweepAngle = deliveredAngle,
                        useCenter = false,
                        style = Stroke(strokeWidth)
                    )
                }
                
                // Center text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = shipments.size.toString(),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusLegendItem("Pending", pending, MaterialTheme.colorScheme.primary)
                StatusLegendItem("In Transit", inTransit, MaterialTheme.colorScheme.tertiary)
                StatusLegendItem("Delivered", delivered, MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun StatusLegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label ($count)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentActivities(
    appointments: List<Appointment>,
    shipments: List<Shipment>,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Activities",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Combine and sort recent activities
            val recentAppointments = appointments.take(3)
            val recentShipments = shipments.take(3)
            
            if (recentAppointments.isEmpty() && recentShipments.isEmpty()) {
                Text(
                    text = "No recent activities",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                recentAppointments.forEach { appointment ->
                    ActivityItem(
                        icon = Icons.Default.Event,
                        title = "Appointment with ${appointment.patientName}",
                        subtitle = "Status: ${appointment.status}",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                recentShipments.forEach { shipment ->
                    ActivityItem(
                        icon = Icons.Default.LocalShipping,
                        title = "Shipment for ${shipment.patientName}",
                        subtitle = "Status: ${shipment.status}",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 