package pheonix.app.patient.data.repository

import kotlinx.coroutines.flow.Flow
import pheonix.app.patient.data.model.Patient
import java.util.Date

interface PatientRepository {
    fun getAllPatients(): Flow<List<Patient>>
    fun getActivePatients(): Flow<List<Patient>>
    fun searchPatients(query: String): Flow<List<Patient>>
    fun getPatientById(id: String): Flow<Patient?>
    fun getPatientsWithUpcomingAppointments(): Flow<List<Patient>>
    fun getPatientsWithAppointmentsInRange(startDate: Date, endDate: Date): Flow<List<Patient>>
    
    suspend fun addPatient(patient: Patient): Result<String>
    suspend fun updatePatient(patient: Patient): Result<Unit>
    suspend fun deletePatient(patientId: String): Result<Unit>
    suspend fun updatePatientStatus(patientId: String, status: Patient.PatientStatus): Result<Unit>
    suspend fun updatePatientLastVisit(patientId: String, lastVisitDate: Date): Result<Unit>
    suspend fun addMedicalCondition(patientId: String, condition: String, date: Date, notes: String?): Result<Unit>
    suspend fun updateEmergencyContact(patientId: String, name: String, relationship: String, phone: String, address: String?): Result<Unit>

    suspend fun createPatient(patient: Patient): Result<Unit>
    fun getPatients(): Flow<List<Patient>>
} 