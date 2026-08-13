package week11.st099681.finalproject.data

import com.google.firebase.firestore.Exclude

data class FuelLog(
    val id: String = "",
    val vehicleId: String = "",
    val dateMillis: Long = 0L,
    val station: String = "",
    val volume: Double? = null,       // amount of fuel purchased
    val unit: String = "L",           // "L" or "gal"
    val pricePerUnit: Double? = null,
    val totalCost: Double? = null,
    val odometer: Int? = null,
    val fuelType: String = "Regular", // Regular, Midgrade, Premium, Diesel, Electric
    val rawText: String = "",
    val imageBase64: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Distance-based economy needs a prior fill-up's odometer reading — computed in the ViewModel/UI. */
    @get:Exclude
    val costPerUnitLabel: String
        get() = if (unit == "gal") "$/gal" else "$/L"
}
