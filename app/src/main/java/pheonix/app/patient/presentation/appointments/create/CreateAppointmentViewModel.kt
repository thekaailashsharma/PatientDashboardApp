package pheonix.app.patient.presentation.appointments.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pheonix.app.patient.data.model.Appointment
import pheonix.app.patient.data.repository.AppointmentRepository
import javax.inject.Inject

@HiltViewModel
class CreateAppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAppointmentState())
    val uiState: StateFlow<CreateAppointmentState> = _uiState.asStateFlow()

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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CreateAppointmentState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAppointmentCreated: Boolean = false
) 