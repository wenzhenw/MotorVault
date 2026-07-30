package week11.st099681.finalproject.data

data class ServiceRecord(
    val id: String = "",
    val vehicleId: String = "",
    val vendor: String = "",
    val category: String = "Other",
    val dateMillis: Long = 0L,
    val amount: Double? = null,
    val rawText: String = "",
    val imageBase64: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
