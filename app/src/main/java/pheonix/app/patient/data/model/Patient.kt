package pheonix.app.patient.data.model

import java.util.Date

data class Patient(
    val id: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val dateOfBirth: Date? = null,
    val gender: Gender = Gender.OTHER,
    val contactNumber: String = "",
    val email: String? = null,
    val address: String = "",
    val bloodGroup: String? = null,
    val medicalHistory: List<MedicalCondition> = emptyList(),
    val allergies: List<String> = emptyList(),
    val currentMedications: List<Medication> = emptyList(),
    val emergencyContact: EmergencyContact? = null,
    val lastVisit: Date? = null,
    val nextAppointment: Date? = null,
    val status: PatientStatus = PatientStatus.ACTIVE,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    constructor() : this(
        id = "",
        name = "",
        photoUrl = null,
        dateOfBirth = null,
        gender = Gender.OTHER,
        contactNumber = "",
        email = null,
        address = "",
        bloodGroup = null,
        medicalHistory = emptyList(),
        allergies = emptyList(),
        currentMedications = emptyList(),
        emergencyContact = null,
        lastVisit = null,
        nextAppointment = null,
        status = PatientStatus.ACTIVE,
        createdAt = Date(),
        updatedAt = Date()
    )

    enum class Gender {
        MALE, FEMALE, OTHER
    }

    enum class PatientStatus {
        ACTIVE, INACTIVE, BLOCKED
    }
}

data class MedicalCondition(
    val condition: String = "",
    val diagnosedDate: Date? = null,
    val notes: String? = null,
    val status: Status = Status.ONGOING
) {
    constructor() : this(
        condition = "",
        diagnosedDate = null,
        notes = null,
        status = Status.ONGOING
    )

    enum class Status {
        ONGOING, RECOVERED, MANAGED
    }
}

data class Medication(
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val startDate: Date? = null,
    val endDate: Date? = null,
    val prescribedBy: String? = null,
    val notes: String? = null
) {
    constructor() : this(
        name = "",
        dosage = "",
        frequency = "",
        startDate = null,
        endDate = null,
        prescribedBy = null,
        notes = null
    )
}

data class EmergencyContact(
    val name: String = "",
    val relationship: String = "",
    val phoneNumber: String = "",
    val address: String? = null
) {
    constructor() : this(
        name = "",
        relationship = "",
        phoneNumber = "",
        address = null
    )
} 