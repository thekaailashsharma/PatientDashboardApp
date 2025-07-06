package pheonix.app.patient.data.model

import java.util.Date

data class Shipment(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val items: List<ShipmentItem> = emptyList(),
    val status: ShipmentStatus = ShipmentStatus.PENDING,
    val trackingNumber: String? = null,
    val shippingAddress: String = "",
    val actualDeliveryDate: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val notes: String? = null
) {
    enum class ShipmentStatus {
        PENDING,
        PROCESSING,
        SHIPPED,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED
    }
}

data class ShipmentItem(
    val name: String = "",
    val quantity: Int = 0,
    val type: ItemType = ItemType.MEDICATION,
    val notes: String? = null
) {
    enum class ItemType {
        MEDICATION,
        MEDICAL_SUPPLIES,
        EQUIPMENT,
        OTHER
    }
} 