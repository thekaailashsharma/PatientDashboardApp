package pheonix.app.patient.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pheonix.app.patient.data.api.PlacesApiService
import pheonix.app.patient.data.model.PlacesResponse
import pheonix.app.patient.data.model.Prediction
import pheonix.app.patient.data.model.User
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class DoctorProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val placesApiService: PlacesApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorProfileState())
    val uiState: StateFlow<DoctorProfileState> = _uiState.asStateFlow()

    private val _addressQuery = MutableStateFlow("")
    
    @OptIn(FlowPreview::class)
    val addressPredictions: StateFlow<List<Prediction>> = _addressQuery
        .debounce(300.milliseconds)
        .flatMapLatest { query ->
            if (query.length >= 3) {
                flow {
                    try {
                        val response = placesApiService.getPlacePredictions(query)
                        emit(response.predictions)
                    } catch (e: Exception) {
                        emit(emptyList())
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            firebaseAuth.currentUser?.let { user ->
                // Only load profile if we have an authenticated user
                loadUserProfile(user.uid, user.displayName ?: "", user.email ?: "", user.photoUrl?.toString())
            }
        }
    }

    private fun loadUserProfile(userId: String, displayName: String, email: String, photoUrl: String?) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // First create a base user with auth data
                val baseUser = User(
                    id = userId,
                    fullName = displayName,
                    email = email,
                    photoUrl = photoUrl,
                    isEmailVerified = auth.currentUser?.isEmailVerified ?: false
                )

                try {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    if (userDoc.exists()) {
                        // If document exists, merge with base user data
                        val firestoreUser = userDoc.toObject(User::class.java)
                        val mergedUser = firestoreUser?.copy(
                            id = userId,
                            fullName = displayName.ifBlank { firestoreUser.fullName },
                            email = email,
                            photoUrl = photoUrl ?: firestoreUser.photoUrl
                        )
                        _uiState.update { it.copy(user = mergedUser, isLoading = false) }
                    } else {
                        // If document doesn't exist, create it with base user data
                        firestore.collection("users").document(userId).set(baseUser).await()
                        _uiState.update { it.copy(user = baseUser, isLoading = false) }
                    }
                } catch (e: Exception) {
                    // If Firestore operation fails, still update UI with base user data
                    _uiState.update { it.copy(user = baseUser, isLoading = false) }
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    fun updateAddressQuery(query: String) {
        _addressQuery.value = query
    }

    fun updateProfile(updatedUser: User) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "User not authenticated"
                        )
                    }
                    return@launch
                }

                // Preserve authentication data
                val userToUpdate = updatedUser.copy(
                    id = currentUser.uid,
                    email = currentUser.email ?: updatedUser.email,
                    photoUrl = currentUser.photoUrl?.toString() ?: updatedUser.photoUrl,
                    isEmailVerified = currentUser.isEmailVerified
                )

                firestore.collection("users")
                    .document(currentUser.uid)
                    .set(userToUpdate)
                    .await()

                _uiState.update { 
                    it.copy(
                        user = userToUpdate,
                        isLoading = false,
                        isProfileUpdateSuccessful = true,
                        isProfileSaved = true
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update profile"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetUpdateStatus() {
        _uiState.update { it.copy(isProfileUpdateSuccessful = false, isProfileSaved = false) }
    }

    data class DoctorProfileState(
        val user: User? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isProfileUpdateSuccessful: Boolean = false,
        val isProfileSaved: Boolean = false
    )

    companion object {
        val SPECIALIZATIONS = listOf(
            "Cardiologist",
            "Dermatologist",
            "Endocrinologist",
            "Family Medicine",
            "Gastroenterologist",
            "Gynecologist",
            "Neurologist",
            "Oncologist",
            "Ophthalmologist",
            "Orthopedist",
            "Pediatrician",
            "Psychiatrist",
            "Pulmonologist",
            "Radiologist",
            "Surgeon",
            "Urologist"
        )
    }
} 