package pheonix.app.patient.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.data.model.MedicalCondition
import pheonix.app.patient.data.model.EmergencyContact
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PatientRepository {

    private val patientsCollection = firestore.collection("patients")

    override fun getAllPatients(): Flow<List<Patient>> = callbackFlow {
        val subscription = patientsCollection
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val patients = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Patient::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.updatedAt } ?: emptyList()
                
                trySend(patients)
            }
        
        awaitClose { subscription.remove() }
    }

    override fun getActivePatients(): Flow<List<Patient>> = getAllPatients().map { patients ->
        patients.filter { it.status == Patient.PatientStatus.ACTIVE }
    }

    override fun searchPatients(query: String): Flow<List<Patient>> = getAllPatients().map { patients ->
        val searchQuery = query.trim().lowercase()
        patients.filter { patient ->
            patient.name.lowercase().contains(searchQuery) ||
            patient.contactNumber?.contains(searchQuery) == true ||
            patient.email?.lowercase()?.contains(searchQuery) == true
        }
    }

    override fun getPatientById(id: String): Flow<Patient?> = callbackFlow {
        val subscription = patientsCollection
            .document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val patient = snapshot?.toObject(Patient::class.java)?.copy(id = snapshot.id)
                trySend(patient)
            }
        
        awaitClose { subscription.remove() }
    }

    override fun getPatientsWithUpcomingAppointments(): Flow<List<Patient>> = getAllPatients().map { patients ->
        val now = Date()
        patients.filter { it.nextAppointment?.after(now) == true }
            .sortedBy { it.nextAppointment }
    }

    override fun getPatientsWithAppointmentsInRange(
        startDate: Date,
        endDate: Date
    ): Flow<List<Patient>> = getAllPatients().map { patients ->
        patients.filter { patient ->
            patient.nextAppointment?.let { appointmentDate ->
                appointmentDate.after(startDate) && appointmentDate.before(endDate)
            } == true
        }.sortedBy { it.nextAppointment }
    }

    override suspend fun addPatient(patient: Patient): Result<String> = try {
        val docRef = patientsCollection.add(patient.copy(
            createdAt = Date(),
            updatedAt = Date()
        )).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updatePatient(patient: Patient): Result<Unit> = try {
        patientsCollection.document(patient.id)
            .set(patient.copy(updatedAt = Date()))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deletePatient(patientId: String): Result<Unit> = try {
        patientsCollection.document(patientId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updatePatientStatus(
        patientId: String,
        status: Patient.PatientStatus
    ): Result<Unit> = try {
        patientsCollection.document(patientId)
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

    override suspend fun updatePatientLastVisit(
        patientId: String,
        lastVisitDate: Date
    ): Result<Unit> = try {
        patientsCollection.document(patientId)
            .update(
                mapOf(
                    "lastVisit" to lastVisitDate,
                    "updatedAt" to Date()
                )
            )
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addMedicalCondition(
        patientId: String,
        condition: String,
        date: Date,
        notes: String?
    ): Result<Unit> = try {
        val newCondition = MedicalCondition(
            condition = condition,
            diagnosedDate = date,
            notes = notes
        )
        
        patientsCollection.document(patientId)
            .update(
                mapOf(
                    "medicalHistory" to com.google.firebase.firestore.FieldValue.arrayUnion(newCondition),
                    "updatedAt" to Date()
                )
            )
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateEmergencyContact(
        patientId: String,
        name: String,
        relationship: String,
        phone: String,
        address: String?
    ): Result<Unit> = try {
        val emergencyContact = EmergencyContact(
            name = name,
            relationship = relationship,
            phoneNumber = phone,
            address = address
        )
        
        patientsCollection.document(patientId)
            .update(
                mapOf(
                    "emergencyContact" to emergencyContact,
                    "updatedAt" to Date()
                )
            )
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 