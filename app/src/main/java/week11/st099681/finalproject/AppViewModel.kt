package week11.st099681.finalproject

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.data.Receipt
import week11.st099681.finalproject.data.ServiceRecord
import week11.st099681.finalproject.data.Vehicle

class AppViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles

    private val _services = MutableStateFlow<List<ServiceRecord>>(emptyList())
    val services: StateFlow<List<ServiceRecord>> = _services

    private val _reminderSettings = MutableStateFlow(
        Maintenance.categories.associate { it.name to true }
    )
    val reminderSettings: StateFlow<Map<String, Boolean>> = _reminderSettings

    var selectedVehicleId by mutableStateOf<String?>(null)

    // ---- receipt flow state (Upload -> Preview -> Scanning -> Results) ----
    var receiptUri by mutableStateOf<Uri?>(null)
    var scannedReceipt by mutableStateOf<Receipt?>(null)

    private var vehicleReg: ListenerRegistration? = null
    private var serviceReg: ListenerRegistration? = null
    private var settingsReg: ListenerRegistration? = null

    val isLoggedIn: Boolean get() = auth.currentUser != null

    private fun userDoc() = auth.currentUser?.uid?.let { db.collection("users").document(it) }

    fun startListening() {
        val user = userDoc() ?: return
        stopListening()

        vehicleReg = user.collection("vehicles").addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { d ->
                d.toObject(Vehicle::class.java)?.copy(id = d.id)
            } ?: emptyList()
            _vehicles.value = list
            if (selectedVehicleId == null || list.none { it.id == selectedVehicleId }) {
                selectedVehicleId = list.firstOrNull()?.id
            }
        }

        serviceReg = user.collection("services").addSnapshotListener { snap, _ ->
            _services.value = snap?.documents?.mapNotNull { d ->
                d.toObject(ServiceRecord::class.java)?.copy(id = d.id)
            }?.sortedByDescending { it.dateMillis } ?: emptyList()
        }

        settingsReg = user.collection("settings").document("reminders")
            .addSnapshotListener { snap, _ ->
                val stored = snap?.data ?: emptyMap<String, Any>()
                _reminderSettings.value = Maintenance.categories.associate { c ->
                    c.name to ((stored[c.name] as? Boolean) ?: true)
                }
            }
    }

    fun stopListening() {
        vehicleReg?.remove(); vehicleReg = null
        serviceReg?.remove(); serviceReg = null
        settingsReg?.remove(); settingsReg = null
    }

    fun selectedVehicle(): Vehicle? = _vehicles.value.firstOrNull { it.id == selectedVehicleId }

    fun servicesForSelected(): List<ServiceRecord> =
        _services.value.filter { it.vehicleId == selectedVehicleId }

    fun servicesFor(vehicleId: String): List<ServiceRecord> =
        _services.value.filter { it.vehicleId == vehicleId }

    fun addVehicle(vehicle: Vehicle, onResult: (Boolean) -> Unit) {
        val user = userDoc() ?: return onResult(false)
        user.collection("vehicles").add(vehicle)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun saveServiceRecord(record: ServiceRecord, onResult: (Boolean) -> Unit) {
        val user = userDoc() ?: return onResult(false)
        user.collection("services").add(record)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun setReminderEnabled(categoryName: String, enabled: Boolean) {
        val user = userDoc() ?: return
        user.collection("settings").document("reminders")
            .set(mapOf(categoryName to enabled), SetOptions.merge())
    }

    fun clearReceiptFlow() {
        receiptUri = null
        scannedReceipt = null
    }

    fun signOut() {
        stopListening()
        auth.signOut()
        _vehicles.value = emptyList()
        _services.value = emptyList()
        selectedVehicleId = null
        clearReceiptFlow()
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
