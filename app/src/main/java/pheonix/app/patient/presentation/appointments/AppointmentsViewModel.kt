package pheonix.app.patient.presentation.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pheonix.app.patient.data.model.Appointment
import pheonix.app.patient.data.repository.AppointmentRepository
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentsUiState())
    val uiState: StateFlow<AppointmentsUiState> = _uiState.asStateFlow()

    private val _selectedAppointment = MutableStateFlow<Appointment?>(null)
    val selectedAppointment: StateFlow<Appointment?> = _selectedAppointment.asStateFlow()

    init {
        loadAllAppointments()
    }

    private fun loadAllAppointments() {
        viewModelScope.launch {
            // Combine today's, upcoming, and past appointments
            combine(
                appointmentRepository.getTodayAppointments(),
                appointmentRepository.getUpcomingAppointments(),
                appointmentRepository.getPastAppointments()
            ) { today, upcoming, past ->
                AppointmentsUiState(
                    todayAppointments = today,
                    upcomingAppointments = upcoming,
                    pastAppointments = past,
                    isLoading = false
                )
            }.catch { error ->
                _uiState.update { it.copy(
                    error = error.message ?: "Failed to load appointments",
                    isLoading = false
                ) }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun createAppointment(appointment: Appointment) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            appointmentRepository.createAppointment(appointment)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Appointment created successfully"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to create appointment"
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
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Appointment updated successfully"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to update appointment"
                        )
                    }
                }
        }
    }

    fun deleteAppointment(appointmentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            appointmentRepository.deleteAppointment(appointmentId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Appointment deleted successfully"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to delete appointment"
                        )
                    }
                }
        }
    }

    fun selectAppointment(appointment: Appointment) {
        _selectedAppointment.value = appointment
    }

    fun clearSelectedAppointment() {
        _selectedAppointment.value = null
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

data class AppointmentsUiState(
    val todayAppointments: List<Appointment> = emptyList(),
    val upcomingAppointments: List<Appointment> = emptyList(),
    val pastAppointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null
) 