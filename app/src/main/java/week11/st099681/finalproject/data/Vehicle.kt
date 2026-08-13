package week11.st099681.finalproject.data

import com.google.firebase.firestore.Exclude

data class Vehicle(
    val id: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val licensePlate: String = "",
    val mileage: String = "",
    val photoBase64: String? = null
) {
    @get:Exclude
    val displayName: String
        get() = listOf(year, make, model).filter { it.isNotBlank() }.joinToString(" ")
}
