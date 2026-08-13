package week11.st099681.finalproject.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import week11.st099681.finalproject.data.ServiceRecord

/** Owns every Firestore read/write for the "services" sub-collection. */
class ServiceRepository(private val db: FirebaseFirestore) {

    private fun collection(uid: String) =
        db.collection("users").document(uid).collection("services")

    fun listen(uid: String, onChange: (List<ServiceRecord>) -> Unit): ListenerRegistration =
        collection(uid).addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { d ->
                d.toObject(ServiceRecord::class.java)?.copy(id = d.id)
            } ?: emptyList()
            onChange(list)
        }

    fun add(uid: String, record: ServiceRecord, onResult: (Boolean) -> Unit) {
        collection(uid).add(record)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun update(uid: String, recordId: String, record: ServiceRecord, onResult: (Boolean) -> Unit) {
        collection(uid).document(recordId).set(record, SetOptions.merge())
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun delete(uid: String, recordId: String, onResult: (Boolean) -> Unit) {
        collection(uid).document(recordId).delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
