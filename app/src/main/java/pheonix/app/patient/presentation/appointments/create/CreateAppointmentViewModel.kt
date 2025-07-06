package pheonix.app.patient.presentation.appointments.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pheonix.app.patient.data.model.Appointment
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.data.repository.AppointmentRepository
import pheonix.app.patient.data.repository.PatientRepository
import javax.inject.Inject

@HiltViewModel
class CreateAppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAppointmentState())
    val uiState: StateFlow<CreateAppointmentState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val patients = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                patientRepository.getActivePatients()
            } else {
                patientRepository.searchPatients(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createAppointment(appointment: Appointment) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            appointmentRepository.createAppointment(appointment)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isAppointmentCreated = true,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            error = error.message ?: "Failed to create appointment",
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun addNewPatient(patient: Patient) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            patientRepository.addPatient(patient)
                .onSuccess { patientId ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            selectedPatientId = patientId
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            error = error.message ?: "Failed to add patient",
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun selectPatient(patientId: String) {
        _uiState.update { it.copy(selectedPatientId = patientId) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CreateAppointmentState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAppointmentCreated: Boolean = false,
    val selectedPatientId: String? = null
) 