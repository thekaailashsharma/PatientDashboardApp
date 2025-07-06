package pheonix.app.patient.data.repository

import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import pheonix.app.patient.data.model.Appointment
import java.util.Date

interface AppointmentRepository {
    fun getTodayAppointments(): Flow<List<Appointment>>
    fun getUpcomingAppointments(): Flow<List<Appointment>>
    fun getPastAppointments(): Flow<List<Appointment>>
    
    suspend fun createAppointment(appointment: Appointment): Result<String>
    suspend fun updateAppointment(appointment: Appointment): Result<Unit>
    suspend fun deleteAppointment(appointmentId: String): Result<Unit>
    suspend fun getAppointmentById(appointmentId: String): Result<Appointment>
    
    fun getAppointmentsByDateRange(
        startDate: Date,
        endDate: Date
    ): Flow<List<Appointment>>
    
    fun getAppointmentsByStatus(status: Appointment.AppointmentStatus): Flow<List<Appointment>>
} 