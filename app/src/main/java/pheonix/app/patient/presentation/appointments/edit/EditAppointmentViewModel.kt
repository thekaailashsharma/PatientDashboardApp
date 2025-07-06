package pheonix.app.patient.presentation.appointments.edit

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
class EditAppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditAppointmentState())
    val uiState: StateFlow<EditAppointmentState> = _uiState.asStateFlow()

    fun loadAppointment(appointmentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            appointmentRepository.getAppointmentById(appointmentId)
                .onSuccess { appointment ->
                    _uiState.update { 
                        it.copy(
                            appointment = appointment,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            error = error.message ?: "Failed to load appointment",
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            appointmentRepository.updateAppointment(appointment)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isAppointmentUpdated = true,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            error = error.message ?: "Failed to update appointment",
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

data class EditAppointmentState(
    val appointment: Appointment? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAppointmentUpdated: Boolean = false
) 