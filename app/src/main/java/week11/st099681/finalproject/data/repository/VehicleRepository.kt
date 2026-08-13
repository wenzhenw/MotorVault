package week11.st099681.finalproject.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import week11.st099681.finalproject.data.Vehicle

/**
 * Owns every Firestore read/write for the "vehicles" sub-collection.
 * Screens and the ViewModel never talk to Firestore directly for vehicles —
 * they go through this class, per the MVVM + Repository pattern.
 */
class VehicleRepository(private val db: FirebaseFirestore) {

    private fun collection(uid: String) =
        db.collection("users").document(uid).collection("vehicles")

    /** Real-time listener — Read. */
    fun listen(uid: String, onChange: (List<Vehicle>) -> Unit): ListenerRegistration =
        collection(uid).addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { d ->
                d.toObject(Vehicle::class.java)?.copy(id = d.id)
            } ?: emptyList()
            onChange(list)
        }

    /** Create. */
    fun add(uid: String, vehicle: Vehicle, onResult: (Boolean) -> Unit) {
        collection(uid).add(vehicle)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    /** Update. */
    fun update(uid: String, vehicleId: String, vehicle: Vehicle, onResult: (Boolean) -> Unit) {
        collection(uid).document(vehicleId).set(vehicle, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    /** Delete. */
    fun delete(uid: String, vehicleId: String, onResult: (Boolean) -> Unit) {
        collection(uid).document(vehicleId).delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
