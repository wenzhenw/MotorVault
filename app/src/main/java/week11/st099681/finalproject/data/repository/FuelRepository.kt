package week11.st099681.finalproject.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import week11.st099681.finalproject.data.FuelLog

/** Owns every Firestore read/write for the "fuel" sub-collection. */
class FuelRepository(private val db: FirebaseFirestore) {

    private fun collection(uid: String) =
        db.collection("users").document(uid).collection("fuel")

    fun listen(uid: String, onChange: (List<FuelLog>) -> Unit): ListenerRegistration =
        collection(uid).addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { d ->
                d.toObject(FuelLog::class.java)?.copy(id = d.id)
            } ?: emptyList()
            onChange(list)
        }

    fun add(uid: String, log: FuelLog, onResult: (Boolean) -> Unit) {
        collection(uid).add(log)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun update(uid: String, fuelId: String, log: FuelLog, onResult: (Boolean) -> Unit) {
        collection(uid).document(fuelId).set(log, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun delete(uid: String, fuelId: String, onResult: (Boolean) -> Unit) {
        collection(uid).document(fuelId).delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
