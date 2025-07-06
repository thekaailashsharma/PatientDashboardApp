package pheonix.app.patient.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pheonix.app.patient.data.model.Appointment
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AppointmentRepository {
    
    private val appointmentsCollection = firestore.collection("appointments")

    override fun getTodayAppointments(): Flow<List<Appointment>> = callbackFlow {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.time

        val registration = appointmentsCollection
            .whereGreaterThanOrEqualTo("scheduledFor", startOfDay)
            .whereLessThanOrEqualTo("scheduledFor", endOfDay)
            .orderBy("scheduledFor", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)
                } ?: emptyList()
                trySend(appointments)
            }

        awaitClose { registration.remove() }
    }

    override fun getUpcomingAppointments(): Flow<List<Appointment>> = callbackFlow {
        val now = Date()
        val registration = appointmentsCollection
            .whereGreaterThan("scheduledFor", now)
            .orderBy("scheduledFor", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)
                } ?: emptyList()
                trySend(appointments)
            }

        awaitClose { registration.remove() }
    }

    override fun getPastAppointments(): Flow<List<Appointment>> = callbackFlow {
        val now = Date()
        val registration = appointmentsCollection
            .whereLessThan("scheduledFor", now)
            .orderBy("scheduledFor", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)
                } ?: emptyList()
                trySend(appointments)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun createAppointment(appointment: Appointment): Result<String> {
        return try {
            val docRef = appointmentsCollection.document()
            val newAppointment = appointment.copy(
                id = docRef.id,
                createdAt = Date()
            )
            docRef.set(newAppointment).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAppointment(appointment: Appointment): Result<Unit> {
        return try {
            appointmentsCollection.document(appointment.id)
                .set(appointment)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAppointment(appointmentId: String): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> {
        return try {
            val doc = appointmentsCollection.document(appointmentId)
                .get()
                .await()
            val appointment = doc.toObject(Appointment::class.java)
            if (appointment != null) {
                Result.success(appointment)
            } else {
                Result.failure(NoSuchElementException("Appointment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAppointmentsByDateRange(
        startDate: Date,
        endDate: Date
    ): Flow<List<Appointment>> = callbackFlow {
        val registration = appointmentsCollection
            .whereGreaterThanOrEqualTo("scheduledFor", startDate)
            .whereLessThanOrEqualTo("scheduledFor", endDate)
            .orderBy("scheduledFor", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)
                } ?: emptyList()
                trySend(appointments)
            }

        awaitClose { registration.remove() }
    }

    override fun getAppointmentsByStatus(
        status: Appointment.AppointmentStatus
    ): Flow<List<Appointment>> = callbackFlow {
        val registration = appointmentsCollection
            .whereEqualTo("status", status)
            .orderBy("scheduledFor", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)
                } ?: emptyList()
                trySend(appointments)
            }

        awaitClose { registration.remove() }
    }
} 