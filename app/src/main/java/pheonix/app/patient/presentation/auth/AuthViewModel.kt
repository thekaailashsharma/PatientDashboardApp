package pheonix.app.patient.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pheonix.app.patient.data.model.AuthResult
import pheonix.app.patient.data.model.User
import pheonix.app.patient.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState(googleSignInClient = repository.getGoogleSignInClient()))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _authState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.signInWithGoogle(credential)) {
                is AuthResult.Success -> {
                    _authState.update { it.copy(isLoading = false, currentUser = result.data) }
                }
                is AuthResult.Error -> {
                    _authState.update { it.copy(isLoading = false, error = result.message) }
                }
                is AuthResult.Loading -> {
                    _authState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun signUpWithEmail(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.signUpWithEmail(fullName, email, password)) {
                is AuthResult.Success -> {
                    _authState.update { it.copy(isLoading = false, currentUser = result.data) }
                }
                is AuthResult.Error -> {
                    _authState.update { it.copy(isLoading = false, error = result.message) }
                }
                is AuthResult.Loading -> {
                    _authState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    _authState.update { it.copy(isLoading = false, currentUser = result.data) }
                }
                is AuthResult.Error -> {
                    _authState.update { it.copy(isLoading = false, error = result.message) }
                }
                is AuthResult.Loading -> {
                    _authState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _authState.update { it.copy(currentUser = null) }
        }
    }

    data class AuthState(
        val currentUser: User? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val googleSignInClient: GoogleSignInClient
    )
} 