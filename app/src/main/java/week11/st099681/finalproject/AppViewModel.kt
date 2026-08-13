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
import week11.st099681.finalproject.data.FuelLog
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.data.Receipt
import week11.st099681.finalproject.data.ServiceRecord
import week11.st099681.finalproject.data.Vehicle
import week11.st099681.finalproject.data.repository.FuelRepository
import week11.st099681.finalproject.data.repository.ServiceRepository
import week11.st099681.finalproject.data.repository.VehicleRepository

class AppViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ---- Repository layer (MVVM + Repository pattern) ----
    private val vehicleRepo = VehicleRepository(db)
    private val serviceRepo = ServiceRepository(db)
    private val fuelRepo = FuelRepository(db)

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles

    private val _services = MutableStateFlow<List<ServiceRecord>>(emptyList())
    val services: StateFlow<List<ServiceRecord>> = _services

    private val _fuelLogs = MutableStateFlow<List<FuelLog>>(emptyList())
    val fuelLogs: StateFlow<List<FuelLog>> = _fuelLogs

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
    private var fuelReg: ListenerRegistration? = null
    private var settingsReg: ListenerRegistration? = null

    val isLoggedIn: Boolean get() = auth.currentUser != null

    /** Display name captured at registration, falling back to the email's local part. */
    val userName: String
        get() {
            val displayName = auth.currentUser?.displayName?.trim()
            if (!displayName.isNullOrBlank()) return displayName
            val email = auth.currentUser?.email
            return email?.substringBefore("@")?.takeIf { it.isNotBlank() } ?: "Driver"
        }

    private fun uid(): String? = auth.currentUser?.uid
    private fun userDoc() = auth.currentUser?.uid?.let { db.collection("users").document(it) }

    fun startListening() {
        val id = uid() ?: return
        stopListening()

        vehicleReg = vehicleRepo.listen(id) { list ->
            _vehicles.value = list
            if (selectedVehicleId == null || list.none { it.id == selectedVehicleId }) {
                selectedVehicleId = list.firstOrNull()?.id
            }
        }

        serviceReg = serviceRepo.listen(id) { list ->
            _services.value = list.sortedByDescending { it.dateMillis }
        }

        fuelReg = fuelRepo.listen(id) { list ->
            _fuelLogs.value = list.sortedByDescending { it.dateMillis }
        }

        settingsReg = userDoc()?.collection("settings")?.document("reminders")
            ?.addSnapshotListener { snap, _ ->
                val stored = snap?.data ?: emptyMap<String, Any>()
                _reminderSettings.value = Maintenance.categories.associate { c ->
                    c.name to ((stored[c.name] as? Boolean) ?: true)
                }
            }
    }

    fun stopListening() {
        vehicleReg?.remove(); vehicleReg = null
        serviceReg?.remove(); serviceReg = null
        fuelReg?.remove(); fuelReg = null
        settingsReg?.remove(); settingsReg = null
    }

    fun selectedVehicle(): Vehicle? = _vehicles.value.firstOrNull { it.id == selectedVehicleId }

    fun servicesForSelected(): List<ServiceRecord> =
        _services.value.filter { it.vehicleId == selectedVehicleId }

    fun servicesFor(vehicleId: String): List<ServiceRecord> =
        _services.value.filter { it.vehicleId == vehicleId }

    fun fuelLogsForSelected(): List<FuelLog> =
        _fuelLogs.value.filter { it.vehicleId == selectedVehicleId }

    fun fuelLogsFor(vehicleId: String): List<FuelLog> =
        _fuelLogs.value.filter { it.vehicleId == vehicleId }

    // ---------------- Vehicles: full CRUD ----------------

    fun addVehicle(vehicle: Vehicle, onResult: (Boolean) -> Unit) {
        val id = uid() ?: return onResult(false)
        vehicleRepo.add(id, vehicle, onResult)
    }

    fun updateVehicle(vehicleId: String, vehicle: Vehicle, onResult: (Boolean) -> Unit) {
        val id = uid() ?: return onResult(false)
        vehicleRepo.update(id, vehicleId, vehicle, onResult)
    }

    fun deleteVehicle(vehicleId: String, onResult: (Boolean) -> Unit) {
        val id = uid() ?: return onResult(false)
        vehicleRepo.delete(id, vehicleId) { ok ->
            if (ok && selectedVehicleId == vehicleId) {
                selectedVehicleId = _vehicles.value.firstOrNull { it.id != vehicleId }?.id
            }
            onResult(ok)
        }
    }

    // ---------------- Service records: full CRUD ----------------

    fun saveServiceRecord(record: ServiceRecord, onResult: (Boolean) -> Unit) {
        val id = uid() ?: return onResult(false)
        serviceRepo.add(id, record, onResult)
    }

    fun updateServiceRecord(recordId: String, record: ServiceRecord, onResult: (Boolean) -> Unit) {
        val id = uid() ?: return onResult(false)
        serviceRepo.update(id, recordId, record, onResult)
    }

    fun deleteServiceRecord(recordId: String, onResult: (Boolean) -> Unit) {
        val id = uid() ?: return onResult(false)
        serviceRepo.delete(id, recordId, onResult)
    }

    // ---------------- Fuel logs: full CRUD ----------------

    fun saveFuelLog(log: FuelLog, onResult: (Boolean) -> Unit) {
        val id = uid() ?: return onResult(false)
        fuelRepo.add(id, log, onResult)
    }

    fun updateFuelLog(fuelId: String, log: FuelLog, onResult: (Boolean) -> Unit) {
        val id = uid() ?: return onResult(false)
        fuelRepo.update(id, fuelId, log, onResult)
    }

    fun deleteFuelLog(fuelId: String, onResult: (Boolean) -> Unit) {
        val id = uid() ?: return onResult(false)
        fuelRepo.delete(id, fuelId, onResult)
    }

    // ---------------- Settings ----------------

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
        _fuelLogs.value = emptyList()
        selectedVehicleId = null
        clearReceiptFlow()
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
