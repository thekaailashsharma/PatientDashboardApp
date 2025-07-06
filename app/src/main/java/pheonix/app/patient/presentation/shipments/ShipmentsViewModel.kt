package pheonix.app.patient.presentation.shipments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pheonix.app.patient.data.api.PlacesApiService
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.data.model.PlacesResponse
import pheonix.app.patient.data.model.Shipment
import pheonix.app.patient.data.model.ShipmentItem
import pheonix.app.patient.data.repository.PatientRepository
import pheonix.app.patient.data.repository.ShipmentRepository
import java.util.*
import javax.inject.Inject

data class ShipmentsUiState(
    val shipments: List<Shipment> = emptyList(),
    val patients: List<Patient> = emptyList(),
    val selectedPatient: Patient? = null,
    val selectedStatus: Shipment.ShipmentStatus? = null,
    val addressPredictions: List<PlacesResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ShipmentsViewModel @Inject constructor(
    private val shipmentRepository: ShipmentRepository,
    private val patientRepository: PatientRepository,
    private val placesApiService: PlacesApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipmentsUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedPatientId = MutableStateFlow<String?>(null)
    private val _selectedStatus = MutableStateFlow<Shipment.ShipmentStatus?>(null)
    private val _addressQuery = MutableStateFlow("")
    val addressPredictions = _addressQuery
        .debounce(300)
        .filter { it.length >= 3 }
        .flatMapLatest { query ->
            flow {
                try {
                    val predictions = placesApiService.getPlacePredictions(query)
                    emit(predictions.predictions)
                } catch (e: Exception) {
                    emit(emptyList())
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadPatients()
        observeShipments()
    }

    private fun loadPatients() {
        viewModelScope.launch {
            patientRepository.getAllPatients()
                .collect { patients ->
                    _uiState.update { it.copy(patients = patients) }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeShipments() {
        combine(
            _selectedPatientId,
            _selectedStatus
        ) { patientId, status ->
            _uiState.update { it.copy(isLoading = true) }
            
            val shipments = when {
                patientId != null -> shipmentRepository.getShipmentsByPatient(patientId)
                status != null -> shipmentRepository.getShipmentsByStatus(status)
                else -> shipmentRepository.getAllShipments()
            }

            shipments.collect { shipmentList ->
                _uiState.update {
                    it.copy(
                        shipments = shipmentList,
                        isLoading = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun selectPatient(patient: Patient?) {
        _selectedPatientId.value = patient?.id
        _uiState.update { it.copy(selectedPatient = patient) }
    }

    fun selectStatus(status: Shipment.ShipmentStatus?) {
        _selectedStatus.value = status
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun updateAddressQuery(query: String) {
        _addressQuery.value = query
    }

    fun createShipment(
        patient: Patient,
        items: List<ShipmentItem>,
        shippingAddress: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val shipment = Shipment(
                patientId = patient.id,
                patientName = patient.name,
                items = items,
                shippingAddress = shippingAddress,
                notes = notes
            )

            shipmentRepository.createShipment(shipment)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun updateShipmentStatus(shipmentId: String, status: Shipment.ShipmentStatus) {
        viewModelScope.launch {
            shipmentRepository.updateShipmentStatus(shipmentId, status)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteShipment(shipmentId: String) {
        viewModelScope.launch {
            shipmentRepository.deleteShipment(shipmentId)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 