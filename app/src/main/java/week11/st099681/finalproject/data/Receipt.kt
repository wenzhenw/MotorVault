package week11.st099681.finalproject.data

enum class ReceiptType { SERVICE, FUEL, OTHER }

data class Receipt(
    val rawText: String,
    val type: ReceiptType = ReceiptType.SERVICE,
    val vendor: String? = null,
    val amount: Double? = null,
    val date: String? = null,
    val categories: List<String> = emptyList(),
    // ---- fuel-specific fields, populated only when type == FUEL ----
    val volume: Double? = null,
    val volumeUnit: String? = null,
    val pricePerUnit: Double? = null,
    val odometer: Int? = null,
    val fuelType: String? = null
)
