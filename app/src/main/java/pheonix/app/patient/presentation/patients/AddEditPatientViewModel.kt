package pheonix.app.patient.presentation.patients

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pheonix.app.patient.data.model.Patient
import pheonix.app.patient.data.model.EmergencyContact
import pheonix.app.patient.data.model.MedicalCondition
import pheonix.app.patient.data.model.Medication
import pheonix.app.patient.data.repository.PatientRepository
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditPatientViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patientId: String? = savedStateHandle["patientId"]
    private val isEditMode = patientId != null

    private val _uiState = MutableStateFlow(AddEditPatientState())
    val uiState: StateFlow<AddEditPatientState> = _uiState.asStateFlow()

    init {
        if (isEditMode) {
            loadPatient()
        }
    }

    private fun loadPatient() {
        viewModelScope.launch {
            patientId?.let { id ->
                patientRepository.getPatientById(id)
                    .collect { patient ->
                        patient?.let { updateUiState(it) }
                    }
            }
        }
    }

    private fun updateUiState(patient: Patient) {
        _uiState.update { state ->
            state.copy(
                name = patient.name,
                photoUrl = patient.photoUrl,
                dateOfBirth = patient.dateOfBirth,
                gender = patient.gender,
                contactNumber = patient.contactNumber,
                email = patient.email,
                address = patient.address,
                bloodGroup = patient.bloodGroup,
                medicalHistory = patient.medicalHistory,
                allergies = patient.allergies,
                currentMedications = patient.currentMedications,
                emergencyContact = patient.emergencyContact,
                status = patient.status
            )
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updatePhotoUrl(url: String?) {
        _uiState.update { it.copy(photoUrl = url) }
    }

    fun updateDateOfBirth(date: Date?) {
        _uiState.update { it.copy(dateOfBirth = date) }
    }

    fun updateGender(gender: Patient.Gender) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun updateContactNumber(number: String) {
        _uiState.update { it.copy(contactNumber = number) }
    }

    fun updateEmail(email: String?) {
        _uiState.update { it.copy(email = email) }
    }

    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun updateBloodGroup(bloodGroup: String?) {
        _uiState.update { it.copy(bloodGroup = bloodGroup) }
    }

    fun addMedicalCondition(condition: MedicalCondition) {
        _uiState.update { state ->
            state.copy(
                medicalHistory = state.medicalHistory + condition
            )
        }
    }

    fun removeMedicalCondition(condition: MedicalCondition) {
        _uiState.update { state ->
            state.copy(
                medicalHistory = state.medicalHistory - condition
            )
        }
    }

    fun updateAllergies(allergies: List<String>) {
        _uiState.update { it.copy(allergies = allergies) }
    }

    fun addMedication(medication: Medication) {
        _uiState.update { state ->
            state.copy(
                currentMedications = state.currentMedications + medication
            )
        }
    }

    fun removeMedication(medication: Medication) {
        _uiState.update { state ->
            state.copy(
                currentMedications = state.currentMedications - medication
            )
        }
    }

    fun updateEmergencyContact(contact: EmergencyContact?) {
        _uiState.update { it.copy(emergencyContact = contact) }
    }

    fun updateStatus(status: Patient.PatientStatus) {
        _uiState.update { it.copy(status = status) }
    }

    fun savePatient() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val patient = Patient(
                id = patientId ?: "",
                name = uiState.value.name,
                photoUrl = uiState.value.photoUrl,
                dateOfBirth = uiState.value.dateOfBirth,
                gender = uiState.value.gender,
                contactNumber = uiState.value.contactNumber,
                email = uiState.value.email,
                address = uiState.value.address,
                bloodGroup = uiState.value.bloodGroup,
                medicalHistory = uiState.value.medicalHistory,
                allergies = uiState.value.allergies,
                currentMedications = uiState.value.currentMedications,
                emergencyContact = uiState.value.emergencyContact,
                status = uiState.value.status
            )

            val result = if (isEditMode) {
                patientRepository.updatePatient(patient)
            } else {
                patientRepository.addPatient(patient)
            }

            result.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isPatientSaved = true
                    )
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to save patient"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun validateBasicInfo(): Boolean {
        return with(uiState.value) {
            name.isNotBlank() &&
            dateOfBirth != null &&
            contactNumber.isNotBlank()
        }
    }

    fun validateMedicalInfo(): Boolean {
        // Make medical info validation always pass since it's optional
        return true
    }

    fun validateEmergencyContact(): Boolean {
        return with(uiState.value.emergencyContact) {
            // Make emergency contact optional but if provided, validate required fields
            this == null || (name.isNotBlank() && relationship.isNotBlank() && phoneNumber.isNotBlank())
        }
    }
}

data class AddEditPatientState(
    val name: String = "",
    val photoUrl: String? = null,
    val dateOfBirth: Date? = null,
    val gender: Patient.Gender = Patient.Gender.OTHER,
    val contactNumber: String = "",
    val email: String? = null,
    val address: String = "",
    val bloodGroup: String? = null,
    val medicalHistory: List<MedicalCondition> = emptyList(),
    val allergies: List<String> = emptyList(),
    val currentMedications: List<Medication> = emptyList(),
    val emergencyContact: EmergencyContact? = null,
    val status: Patient.PatientStatus = Patient.PatientStatus.ACTIVE,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPatientSaved: Boolean = false
) 