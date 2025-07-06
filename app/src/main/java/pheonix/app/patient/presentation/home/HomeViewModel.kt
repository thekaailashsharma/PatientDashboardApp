package pheonix.app.patient.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pheonix.app.patient.data.model.Appointment
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.data.model.Shipment
import pheonix.app.patient.data.repository.AppointmentRepository
import pheonix.app.patient.data.repository.PatientRepository
import pheonix.app.patient.data.repository.ShipmentRepository
import javax.inject.Inject

data class HomeUiState(
    val appointments: List<Appointment> = emptyList(),
    val patients: List<Patient> = emptyList(),
    val shipments: List<Shipment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val shipmentRepository: ShipmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                combine(
                    appointmentRepository.getAppointments(),
                    patientRepository.getPatients(),
                    shipmentRepository.getShipments()
                ) { appointments, patients, shipments ->
                    HomeUiState(
                        appointments = appointments,
                        patients = patients,
                        shipments = shipments,
                        isLoading = false
                    )
                }.catch { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = e.message
                    ) }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                ) }
            }
        }
    }
} 