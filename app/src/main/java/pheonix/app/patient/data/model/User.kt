package pheonix.app.patient.data.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false
) 