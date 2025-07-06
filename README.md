<h1 align="center">🏥 Patient Dashboard Pro 👨‍⚕️</h1>
<br>
<p align="center">
  <img src="app/src/main/res/drawable/doctor.png" alt="Patient Dashboard Pro Banner" width="200">
</p>

# Patient Dashboard Pro - Smart Healthcare Management

![Static Badge](https://img.shields.io/badge/Kotlin-black?style=for-the-badge&logo=kotlin&logoColor=%237F52FF&labelColor=black)
![Static Badge](https://img.shields.io/badge/Jetpack_Compose-black?style=for-the-badge&logo=Jetpack%20Compose&logoColor=%234285F4&labelColor=black)
![Static Badge](https://img.shields.io/badge/Firebase-black?style=for-the-badge&logo=firebase&logoColor=%23FFCA28&labelColor=black)
![Static Badge](https://img.shields.io/badge/Material_Design_3-black?style=for-the-badge&logo=material-design&logoColor=%23757575&labelColor=black)
![Static Badge](https://img.shields.io/badge/Android-black?style=for-the-badge&logo=android&logoColor=%233DDC84&labelColor=black)

## Important Links
- [Complete Deck](https://patienty-your-complete-d-3rz6d7i.gamma.site/)
- [Figma Designs](https://www.figma.com/design/cF49xT4CvNtOpBk52IcNZi/Patient-Dashboard-App?node-id=0-1&t=EJIzd8ZPuniQlfzk-1)
- [Video](https://www.veed.io/view/375510c5-980a-4539-9597-361109ead027?panel=share)
- [APK](https://drive.google.com/drive/folders/1Kd8VVN8T_QFfPqZTMyHyHy1r-4bsAobm?usp=drive_link)

**Available for Android devices.**

### A comprehensive healthcare management solution for medical professionals.
Streamline patient care, manage appointments efficiently, and track medical shipments all in one place with Patient Dashboard Pro.

- 👨‍⚕️ **Smart Patient Management**: Complete patient profiles with medical history and treatment tracking
- 📅 **Intelligent Scheduling**: Advanced appointment management with real-time updates
- 📦 **Shipment Tracking**: Integrated medical supply and equipment tracking system
- 🔐 **Secure Authentication**: Protected access with email and Google Sign-In options

## App Preview
__________________

| Appointments | Patient Management | Profile |
|-------------|-------------------|----------|
|![1000000601](https://github.com/user-attachments/assets/35a8aa04-53c7-47ac-bdc4-12f29050dd7c)) | ![1000000600](https://github.com/user-attachments/assets/65f584c3-6efe-4d5c-9784-6b6871b04bbe) | ![1000000606](https://github.com/user-attachments/assets/237982dc-3ff2-458d-9133-a843ad6719dd) |


| Dashboard | Shipments | Empty States |
|-------------|-------------------|----------|
|![1000000596](https://github.com/user-attachments/assets/4297234f-61cb-464a-bdd8-76d0552171e8) | ![1000000605](https://github.com/user-attachments/assets/93639c55-05d3-4ff1-ba33-3e4698dd4df2) | ![1000000598](https://github.com/user-attachments/assets/63a22c05-0b26-49ff-8cf0-178dd88789bd)

## Core Features ⚕️

### 1. Patient Management
- Complete patient profiles
- Medical history tracking
- Quick search and filters
- Patient appointment history


### 2. Appointment System
- Smart scheduling
- Real-time status updates
- Appointment details view
- Calendar integration

### 3. Shipment Tracking
- Medical supply management
- Real-time tracking
- Patient-linked shipments
- Delivery status updates

### 4. Doctor Profile
- Professional information
- Credentials management
- Settings customization
- Profile analytics
- Google Maps SDK

## Technical Architecture 🔧

### Data Management
```kotlin
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Appointments : BottomNavItem("appointments", "Appts", Icons.Default.CalendarToday)
    // ... other navigation items
}
```

### Tech Stack
| Technology | Purpose |
|------------|---------|
| Kotlin | Primary programming language |
| Jetpack Compose | Modern UI toolkit |
| Material Design 3 | UI/UX framework |
| Firebase | Backend & Authentication |
| MVVM | Architecture pattern |
| Hilt | Dependency injection |
| Room | Local database |
| Coroutines | Asynchronous programming |

## Implementation Highlights

### Clean Architecture
```kotlin
interface AppointmentRepository {
    suspend fun getAppointments(): Flow<List<Appointment>>
    suspend fun createAppointment(appointment: Appointment)
    suspend fun updateAppointment(appointment: Appointment)
    // ... other repository methods
}
```

### Modern UI Components
- Custom TextField
- Primary Button
- Social Sign-In Button
- Appointment Cards
- Patient Profile Cards
- Shipment Tracking Cards

## Security Features 🔒

1. **Authentication**
   - Email/Password login
   - Google Sign-In integration
   - Session management
   - Secure token handling

2. **Data Protection**
   - Encrypted storage
   - Secure API communication
   - Role-based access control

## System Requirements

- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin Version: 1.9.0
- Compose Version: 1.5.0

## Getting Started

1. Clone the repository
```bash
git clone https://github.com/yourusername/patient-dashboard-pro.git
```

2. Add your Firebase configuration file
```bash
app/google-services.json
```

3. Build and run
```bash
./gradlew build
```

## Dependencies

```kotlin
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("com.google.dagger:hilt-android:2.48")
    // ... other dependencies
}
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Authors

[@thekaailashsharma](https://github.com/thekaailashsharma)

## Acknowledgments

- Material Design 3 Guidelines
- Android Jetpack Libraries
- Firebase Documentation
