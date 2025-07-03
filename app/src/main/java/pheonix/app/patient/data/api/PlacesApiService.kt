package pheonix.app.patient.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import pheonix.app.patient.data.model.PlacesResponse
import javax.inject.Inject

class PlacesApiService @Inject constructor(
    private val client: HttpClient
) {
    suspend fun getPlacePredictions(input: String): PlacesResponse {
        return client.get("https://maps.googleapis.com/maps/api/place/autocomplete/json") {
            parameter("input", input)
            parameter("key", "AIzaSyBKuSP7tnwlqkAAF-OJccMpJ0_o7xf5998")
        }.body()
    }
} 