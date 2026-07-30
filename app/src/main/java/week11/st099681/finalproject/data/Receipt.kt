package week11.st099681.finalproject.data

data class Receipt (
    val rawText: String,
    val vendor: String? = null,
    val amount: Double? = null,
    val date: String? = null,
    val categories: List<String> = emptyList()
)