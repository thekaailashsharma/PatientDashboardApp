package pheonix.app.patient.presentation.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.data.repository.PatientRepository
import java.util.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class PatientsViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterStatus = MutableStateFlow<Patient.PatientStatus?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val searchQuery = _searchQuery.asStateFlow()
    val filterStatus = _filterStatus.asStateFlow()
    val isRefreshing = _isRefreshing.asStateFlow()
    val selectedPatient = _selectedPatient.asStateFlow()
    val error = _error.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val patients = combine(
        _searchQuery.debounce(300.milliseconds),
        _filterStatus
    ) { query, status ->
        Pair(query, status)
    }.flatMapLatest { (query, status) ->
        when {
            query.isNotBlank() -> patientRepository.searchPatients(query)
            status != null -> when (status) {
                Patient.PatientStatus.ACTIVE -> patientRepository.getActivePatients()
                else -> patientRepository.getAllPatients()
                    .map { patients -> patients.filter { it.status == status } }
            }
            else -> patientRepository.getAllPatients()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val upcomingAppointments = patientRepository.getPatientsWithUpcomingAppointments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilterStatus(status: Patient.PatientStatus?) {
        _filterStatus.value = status
    }

    fun selectPatient(patient: Patient) {
        _selectedPatient.value = patient
    }

    fun clearSelectedPatient() {
        _selectedPatient.value = null
    }

    fun updatePatientStatus(patientId: String, status: Patient.PatientStatus) {
        viewModelScope.launch {
            patientRepository.updatePatientStatus(patientId, status)
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to update patient status"
                }
        }
    }

    fun updatePatientLastVisit(patientId: String) {
        viewModelScope.launch {
            patientRepository.updatePatientLastVisit(patientId, Date())
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to update last visit"
                }
        }
    }

    fun deletePatient(patientId: String) {
        viewModelScope.launch {
            patientRepository.deletePatient(patientId)
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to delete patient"
                }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // The actual refresh will happen automatically through Flow collection
            _isRefreshing.value = false
        }
    }
} 