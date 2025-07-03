package pheonix.app.patient.data.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    // Doctor specific fields
    val specialization: String = "",
    val clinicName: String = "",
    val clinicAddress: String = "",
    val phone: String = "",
    val isProfileComplete: Boolean = false
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        fullName = "",
        email = "",
        photoUrl = null,
        isEmailVerified = false,
        specialization = "",
        clinicName = "",
        clinicAddress = "",
        phone = "",
        isProfileComplete = false
    )
} 