package pheonix.app.patient.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pheonix.app.patient.data.model.Shipment
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShipmentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ShipmentRepository {

    private val shipmentsCollection = firestore.collection("shipments")

    override fun getShipmentsByPatient(patientId: String): Flow<List<Shipment>> = callbackFlow {
        val registration = shipmentsCollection
            .whereEqualTo("patientId", patientId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val shipments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Shipment::class.java)
                } ?: emptyList()
                trySend(shipments)
            }

        awaitClose { registration.remove() }
    }

    override fun getAllShipments(): Flow<List<Shipment>> = callbackFlow {
        val registration = shipmentsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val shipments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Shipment::class.java)
                } ?: emptyList()
                trySend(shipments)
            }

        awaitClose { registration.remove() }
    }

    override fun getShipmentsByStatus(status: Shipment.ShipmentStatus): Flow<List<Shipment>> = callbackFlow {
        val registration = shipmentsCollection
            .whereEqualTo("status", status)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val shipments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Shipment::class.java)
                } ?: emptyList()
                trySend(shipments)
            }

        awaitClose { registration.remove() }
    }

    override fun getShipmentsByDateRange(startDate: Date, endDate: Date): Flow<List<Shipment>> = callbackFlow {
        val registration = shipmentsCollection
            .whereGreaterThanOrEqualTo("createdAt", startDate)
            .whereLessThanOrEqualTo("createdAt", endDate)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val shipments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Shipment::class.java)
                } ?: emptyList()
                trySend(shipments)
            }

        awaitClose { registration.remove() }
    }

    override fun getShipments(): Flow<List<Shipment>> = callbackFlow {
        val listenerRegistration = shipmentsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val shipments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Shipment::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(shipments)
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun createShipment(shipment: Shipment): Result<String> {
        return try {
            val docRef = shipmentsCollection.document()
            val newShipment = shipment.copy(
                id = docRef.id,
                createdAt = Date(),
                updatedAt = Date()
            )
            docRef.set(newShipment).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateShipment(shipment: Shipment): Result<Unit> {
        return try {
            val updatedShipment = shipment.copy(updatedAt = Date())
            shipmentsCollection.document(shipment.id)
                .set(updatedShipment)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateShipmentStatus(
        shipmentId: String,
        status: Shipment.ShipmentStatus
    ): Result<Unit> {
        return try {
            shipmentsCollection.document(shipmentId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to Date()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteShipment(shipmentId: String): Result<Unit> {
        return try {
            shipmentsCollection.document(shipmentId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getShipmentById(shipmentId: String): Result<Shipment> {
        return try {
            val doc = shipmentsCollection.document(shipmentId)
                .get()
                .await()
            val shipment = doc.toObject(Shipment::class.java)
            if (shipment != null) {
                Result.success(shipment)
            } else {
                Result.failure(NoSuchElementException("Shipment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 