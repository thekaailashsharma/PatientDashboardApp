package pheonix.app.patient.presentation.appointments.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pheonix.app.patient.data.model.Appointment
import pheonix.app.patient.data.repository.AppointmentRepository
import javax.inject.Inject

data class AppointmentDetailsUiState(
    val isLoading: Boolean = false,
    val appointment: Appointment? = null,
    val error: String? = null
)

@HiltViewModel
class AppointmentDetailsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentDetailsUiState())
    val uiState: StateFlow<AppointmentDetailsUiState> = _uiState

    fun loadAppointment(appointmentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            appointmentRepository.getAppointmentById(appointmentId)
                .onSuccess { appointment ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            appointment = appointment,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load appointment"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 