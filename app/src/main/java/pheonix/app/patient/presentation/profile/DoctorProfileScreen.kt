package pheonix.app.patient.presentation.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.launch
import pheonix.app.patient.R
import pheonix.app.patient.data.model.User
import pheonix.app.patient.presentation.components.BottomSheetContent
import pheonix.app.patient.presentation.components.CustomTextField
import pheonix.app.patient.presentation.components.PrimaryButton
import pheonix.app.patient.presentation.components.SelectionList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    viewModel: DoctorProfileViewModel = hiltViewModel(),
    isEditMode: Boolean = false,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val addressPredictions by viewModel.addressPredictions.collectAsState()
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val addressBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )

    var isEditing by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf(uiState.user?.fullName ?: "") }
    var phone by remember { mutableStateOf(uiState.user?.phone ?: "") }
    var specialization by remember { mutableStateOf(uiState.user?.specialization ?: "") }
    var clinicName by remember { mutableStateOf(uiState.user?.clinicName ?: "") }
    var clinicAddress by remember { mutableStateOf(uiState.user?.clinicAddress ?: "") }
    var showSpecializationSheet by remember { mutableStateOf(false) }
    var showAddressSheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val successComposition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.doctor_animation)
    )

    // Profile image animation
    val imageScale by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Validation states
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var specializationError by remember { mutableStateOf<String?>(null) }
    var clinicNameError by remember { mutableStateOf<String?>(null) }
    var clinicAddressError by remember { mutableStateOf<String?>(null) }

    // Animation for form fields
    val formFieldsAlpha by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 0f else 1f,
        animationSpec = tween(durationMillis = 500)
    )

    // Update local state when user data changes
    LaunchedEffect(uiState.user) {
        uiState.user?.let { user ->
            fullName = user.fullName
            phone = user.phone
            specialization = user.specialization
            clinicName = user.clinicName
            clinicAddress = user.clinicAddress
        }
    }

    LaunchedEffect(uiState.isProfileSaved) {
        if (uiState.isProfileSaved) {
            onNavigateToHome()
        }
    }

    fun validateInputs(): Boolean {
        var isValid = true

        if (fullName.isBlank()) {
            fullNameError = "Full name is required"
            isValid = false
        } else {
            fullNameError = null
        }

        if (phone.isBlank()) {
            phoneError = "Phone number is required"
            isValid = false
        } else if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            phoneError = "Invalid phone number"
            isValid = false
        } else {
            phoneError = null
        }

        if (specialization.isBlank()) {
            specializationError = "Specialization is required"
            isValid = false
        } else {
            specializationError = null
        }

        if (clinicName.isBlank()) {
            clinicNameError = "Clinic name is required"
            isValid = false
        } else {
            clinicNameError = null
        }

        if (clinicAddress.isBlank()) {
            clinicAddressError = "Clinic address is required"
            isValid = false
        } else {
            clinicAddressError = null
        }

        return isValid
    }

    LaunchedEffect(showSuccessAnimation) {
        if (showSuccessAnimation) {
            kotlinx.coroutines.delay(2000)
            onNavigateToHome()
        }
    }

    LaunchedEffect(clinicAddress) {
        if (clinicAddress.length >= 3) {
            viewModel.updateAddressQuery(clinicAddress)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = imageScale
                        scaleY = imageScale
                    }
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.user?.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 4.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(formFieldsAlpha),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditMode) "Edit Profile" else "Complete Your Profile",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (isEditMode && !isEditing) {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (!isEditMode) {
                        Text(
                            text = "Please provide your professional details",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )
                    }

                    // Profile Fields
                    CustomTextField(
                        value = fullName,
                        onValueChange = { if (isEditing || !isEditMode) fullName = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person,
                        errorMessage = fullNameError,
                        modifier = Modifier.padding(bottom = 16.dp),
                        enabled = isEditing || !isEditMode
                    )

                    CustomTextField(
                        value = phone,
                        onValueChange = { if (isEditing || !isEditMode) phone = it },
                        label = "Phone Number",
                        leadingIcon = Icons.Default.Phone,
                        errorMessage = phoneError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.padding(bottom = 16.dp),
                        enabled = isEditing || !isEditMode
                    )

                    // Specialization Field
                    OutlinedTextField(
                        value = specialization,
                        onValueChange = {},
                        label = { Text("Specialization") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.LocalHospital,
                                contentDescription = null
                            )
                        },
                        trailingIcon = { 
                            Icon(Icons.Default.ArrowDropDown, "Select specialization")
                        },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isEditing || !isEditMode) { showSpecializationSheet = true }
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        isError = specializationError != null,
                        enabled = false
                    )

                    if (specializationError != null) {
                        Text(
                            text = specializationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }

                    CustomTextField(
                        value = clinicName,
                        onValueChange = { if (isEditing || !isEditMode) clinicName = it },
                        label = "Clinic Name",
                        leadingIcon = Icons.Default.Business,
                        errorMessage = clinicNameError,
                        modifier = Modifier.padding(bottom = 16.dp),
                        enabled = isEditing || !isEditMode
                    )

                    // Address Field
                    OutlinedTextField(
                        value = clinicAddress,
                        onValueChange = {},
                        label = { Text("Clinic Address") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null
                            )
                        },
                        trailingIcon = { 
                            Icon(Icons.Default.Search, "Search address")
                        },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isEditing || !isEditMode) { showAddressSheet = true }
                            .padding(bottom = if (clinicAddressError != null) 0.dp else 32.dp),
                        shape = RoundedCornerShape(16.dp),
                        isError = clinicAddressError != null,
                        maxLines = 3,
                        enabled = false
                    )

                    if (clinicAddressError != null) {
                        Text(
                            text = clinicAddressError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, bottom = 32.dp)
                        )
                    }

                    // Show save/update button only when editing or in initial profile setup
                    if (isEditing || !isEditMode) {
                        Button(
                            onClick = {
                                if (validateInputs()) {
                                    isLoading = true
                                    viewModel.updateProfile(
                                        uiState.user?.copy(
                                            fullName = fullName,
                                            phone = phone,
                                            specialization = specialization,
                                            clinicName = clinicName,
                                            clinicAddress = clinicAddress,
                                            isProfileComplete = true
                                        ) ?: User(
                                            fullName = fullName,
                                            phone = phone,
                                            specialization = specialization,
                                            clinicName = clinicName,
                                            clinicAddress = clinicAddress,
                                            isProfileComplete = true
                                        )
                                    )
                                    if (isEditMode) {
                                        isEditing = false
                                    } else {
                                        showSuccessAnimation = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ),
                            enabled = !isLoading && fullName.isNotBlank() &&
                                     phone.isNotBlank() && specialization.isNotBlank() &&
                                     clinicName.isNotBlank() && clinicAddress.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = if (isEditMode) "Update Profile" else "Save Profile",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Success Animation
        AnimatedVisibility(
            visible = showSuccessAnimation,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LottieAnimation(
                    composition = successComposition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isEditMode) "Profile Updated Successfully!" else "Profile Updated Successfully!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                if (!isEditMode) {
                    Text(
                        text = "Redirecting to dashboard...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    // Bottom Sheets (keep your existing bottom sheet code)
    if (showSpecializationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSpecializationSheet = false },
            sheetState = bottomSheetState,
            dragHandle = null,
            windowInsets = WindowInsets(0)
        ) {
            BottomSheetContent(
                title = "Select Specialization",
                onDismiss = { 
                    scope.launch { 
                        bottomSheetState.hide()
                        showSpecializationSheet = false
                    }
                }
            ) {
                SelectionList(
                    items = DoctorProfileViewModel.SPECIALIZATIONS,
                    onItemSelected = { selected ->
                        specialization = selected
                        scope.launch { 
                            bottomSheetState.hide()
                            showSpecializationSheet = false
                        }
                    }
                )
            }
            // Add extra padding at bottom for better UX
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAddressSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddressSheet = false },
            sheetState = addressBottomSheetState,
            dragHandle = null,
            windowInsets = WindowInsets(0)
        ) {
            BottomSheetContent(
                title = "Select Address",
                onDismiss = { 
                    scope.launch { 
                        addressBottomSheetState.hide()
                        showAddressSheet = false
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = clinicAddress,
                        onValueChange = { 
                            clinicAddress = it
                            viewModel.updateAddressQuery(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        label = { Text("Search Address") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (addressPredictions.isNotEmpty()) {
                        SelectionList(
                            items = addressPredictions.map { it.description },
                            onItemSelected = { selected ->
                                clinicAddress = selected
                                scope.launch { 
                                    addressBottomSheetState.hide()
                                    showAddressSheet = false
                                }
                            }
                        )
                    }
                }
            }
            // Add extra padding at bottom for better UX
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}