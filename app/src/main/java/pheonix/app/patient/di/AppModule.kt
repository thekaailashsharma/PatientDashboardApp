package pheonix.app.patient.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import pheonix.app.patient.data.api.PlacesApiService
import pheonix.app.patient.data.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(context, auth, firestore)

    @Provides
    @Singleton
    fun provideAppointmentRepository(
        firestore: FirebaseFirestore
    ): AppointmentRepository = AppointmentRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun providePatientRepository(
        firestore: FirebaseFirestore,
        appointmentRepository: AppointmentRepository
    ): PatientRepository = PatientRepositoryImpl(firestore, appointmentRepository)

    @Provides
    @Singleton
    fun provideShipmentRepository(
        firestore: FirebaseFirestore
    ): ShipmentRepository = ShipmentRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                level = LogLevel.BODY
            }
        }
    }

    @Provides
    @Singleton
    fun providePlacesApiService(client: HttpClient): PlacesApiService {
        return PlacesApiService(client)
    }
} 