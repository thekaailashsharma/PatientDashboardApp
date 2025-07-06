package pheonix.app.patient.data.repository

import kotlinx.coroutines.flow.Flow
import pheonix.app.patient.data.model.Shipment
import java.util.Date

interface ShipmentRepository {
    fun getShipmentsByPatient(patientId: String): Flow<List<Shipment>>
    fun getAllShipments(): Flow<List<Shipment>>
    fun getShipmentsByStatus(status: Shipment.ShipmentStatus): Flow<List<Shipment>>
    fun getShipmentsByDateRange(startDate: Date, endDate: Date): Flow<List<Shipment>>
    suspend fun createShipment(shipment: Shipment): Result<String>
    suspend fun updateShipment(shipment: Shipment): Result<Unit>
    suspend fun updateShipmentStatus(shipmentId: String, status: Shipment.ShipmentStatus): Result<Unit>
    suspend fun deleteShipment(shipmentId: String): Result<Unit>
    suspend fun getShipmentById(shipmentId: String): Result<Shipment>
    fun getShipments(): Flow<List<Shipment>>
} 