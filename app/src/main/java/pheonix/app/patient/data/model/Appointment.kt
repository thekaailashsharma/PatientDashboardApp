package pheonix.app.patient.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Appointment(
    @DocumentId
    val id: String = "",
    
    @PropertyName("doctorId")
    val doctorId: String = "",
    
    @PropertyName("patientId")
    val patientId: String = "",
    
    @PropertyName("doctorName")
    val doctorName: String = "",
    
    @PropertyName("patientName")
    val patientName: String = "",
    
    @PropertyName("doctorPhotoUrl")
    val doctorPhotoUrl: String? = null,
    
    @PropertyName("patientPhotoUrl")
    val patientPhotoUrl: String? = null,
    
    @PropertyName("type")
    val type: AppointmentType = AppointmentType.IN_PERSON,
    
    @PropertyName("status")
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    
    @ServerTimestamp
    @PropertyName("createdAt")
    val createdAt: Date = Date(),
    
    @PropertyName("scheduledFor")
    val scheduledFor: Date? = null,
    
    @PropertyName("duration")
    val duration: Int = 30, // in minutes
    
    @PropertyName("notes")
    val notes: String = "",
    
    @PropertyName("symptoms")
    val symptoms: List<String> = emptyList(),
    
    @PropertyName("prescription")
    val prescription: String? = null,
    
    @PropertyName("followUpDate")
    val followUpDate: Date? = null
) {
    enum class AppointmentType {
        VIDEO_CALL,
        IN_PERSON
    }

    enum class AppointmentStatus {
        SCHEDULED,
        CONFIRMED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_SHOW,
        RESCHEDULED
    }
} 