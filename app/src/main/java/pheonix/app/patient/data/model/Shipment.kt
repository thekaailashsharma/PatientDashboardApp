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
    enum class ShipmentStatus(val shipmentValue: String) {
        PENDING("Pending"),
        PROCESSING("Processing"),
        SHIPPED("Shipped"),
        IN_TRANSIT("In Transit"),
        OUT_FOR_DELIVERY("Out for Delivery"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled")
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