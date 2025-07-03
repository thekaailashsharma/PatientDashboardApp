package pheonix.app.patient.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow
import pheonix.app.patient.data.model.AuthResult
import pheonix.app.patient.data.model.User

interface AuthRepository {
    val currentUser: Flow<User?>
    
    suspend fun signInWithGoogle(credential: AuthCredential): AuthResult<User>
    suspend fun signUpWithEmail(fullName: String, email: String, password: String): AuthResult<User>
    suspend fun signInWithEmail(email: String, password: String): AuthResult<User>
    suspend fun signOut()
    
    fun getGoogleSignInClient(): GoogleSignInClient
} 