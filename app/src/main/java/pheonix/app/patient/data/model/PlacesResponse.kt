package pheonix.app.patient.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlacesResponse(
    val predictions: List<Prediction>,
    val status: String
)

@Serializable
data class Prediction(
    val description: String,
    @SerialName("place_id")
    val placeId: String,
    @SerialName("structured_formatting")
    val structuredFormatting: StructuredFormatting,
    val types: List<String>
)

@Serializable
data class StructuredFormatting(
    @SerialName("main_text")
    val mainText: String,
    @SerialName("secondary_text")
    val secondaryText: String
) 