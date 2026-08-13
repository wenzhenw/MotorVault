package week11.st099681.finalproject.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st099681.finalproject.AppViewModel
import week11.st099681.finalproject.data.FuelLog
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.ui.AppTextField
import week11.st099681.finalproject.ui.BackHeader
import week11.st099681.finalproject.ui.PrimaryButton
import week11.st099681.finalproject.ui.theme.Blue
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.TextSecondary

private val fuelTypes = listOf("Regular", "Midgrade", "Premium", "Diesel", "E85", "Electric")
private val units = listOf("L", "gal")

@Composable
fun AddFuelScreen(
    vm: AppViewModel,
    editFuel: FuelLog?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val isEdit = editFuel != null

    var dateText by remember {
        mutableStateOf(editFuel?.dateMillis?.let { Maintenance.formatDate(it) } ?: Maintenance.formatDate(System.currentTimeMillis()))
    }
    var station by remember { mutableStateOf(editFuel?.station ?: "") }
    var volumeText by remember { mutableStateOf(editFuel?.volume?.let { "%.3f".format(it) } ?: "") }
    var unit by remember { mutableStateOf(editFuel?.unit ?: "L") }
    var priceText by remember { mutableStateOf(editFuel?.pricePerUnit?.let { "%.3f".format(it) } ?: "") }
    var totalText by remember { mutableStateOf(editFuel?.totalCost?.let { "%.2f".format(it) } ?: "") }
    var odometerText by remember { mutableStateOf(editFuel?.odometer?.toString() ?: "") }
    var fuelType by remember { mutableStateOf(editFuel?.fuelType ?: "Regular") }
    var unitMenuOpen by remember { mutableStateOf(false) }
    var typeMenuOpen by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BackHeader(
            if (isEdit) "Edit Fuel Log" else "Log Fuel Fill-up",
            SelectedVehicleLabel(vm),
            onBack
        )
        Spacer(Modifier.height(20.dp))

        Text("Date", color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        AppTextField(dateText, { dateText = it }, "e.g. July 15, 2026")
        Spacer(Modifier.height(12.dp))

        AppTextField(station, { station = it }, "Gas station (e.g. Shell, Petro-Canada)")
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(volumeText, { volumeText = it }, "Volume", modifier = Modifier.weight(1f))
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(90.dp)
                        .background(Slate, RoundedCornerShape(10.dp))
                        .clickable { unitMenuOpen = true }
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Text(unit, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                }
                DropdownMenu(expanded = unitMenuOpen, onDismissRequest = { unitMenuOpen = false }) {
                    units.forEach { u ->
                        DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitMenuOpen = false })
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(priceText, { txt ->
                priceText = txt
                autoFillTotal(txt, volumeText) { totalText = it }
            }, "Price per unit", modifier = Modifier.weight(1f))
            AppTextField(totalText, { totalText = it }, "Total cost", modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))

        Text("Fuel type", color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate, RoundedCornerShape(10.dp))
                    .clickable { typeMenuOpen = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(fuelType, modifier = Modifier.weight(1f), fontSize = 14.sp)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextSecondary)
            }
            DropdownMenu(expanded = typeMenuOpen, onDismissRequest = { typeMenuOpen = false }) {
                fuelTypes.forEach { t ->
                    DropdownMenuItem(text = { Text(t) }, onClick = { fuelType = t; typeMenuOpen = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        AppTextField(odometerText, { odometerText = it }, "Odometer reading (optional)")
        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            if (saving) "Saving…" else if (isEdit) "Save Changes" else "Save Fuel Log",
            enabled = !saving
        ) {
            val vehicleId = vm.selectedVehicleId
            if (vehicleId == null) {
                Toast.makeText(context, "Select a vehicle first", Toast.LENGTH_SHORT).show()
                return@PrimaryButton
            }
            val volume = volumeText.toDoubleOrNull()
            val total = totalText.toDoubleOrNull()
            if (volume == null && total == null) {
                Toast.makeText(context, "Enter at least a volume or total cost", Toast.LENGTH_SHORT).show()
                return@PrimaryButton
            }
            saving = true
            val log = FuelLog(
                vehicleId = vehicleId,
                dateMillis = Maintenance.parseDate(dateText) ?: System.currentTimeMillis(),
                station = station.trim(),
                volume = volume,
                unit = unit,
                pricePerUnit = priceText.toDoubleOrNull(),
                totalCost = total,
                odometer = odometerText.trim().toIntOrNull(),
                fuelType = fuelType,
                imageBase64 = editFuel?.imageBase64,
                rawText = editFuel?.rawText.orEmpty()
            )
            val onResult: (Boolean) -> Unit = { ok ->
                saving = false
                if (ok) {
                    Toast.makeText(context, if (isEdit) "Fuel log updated" else "Fuel log saved", Toast.LENGTH_SHORT).show()
                    onSaved()
                } else {
                    Toast.makeText(context, "Could not save fuel log", Toast.LENGTH_SHORT).show()
                }
            }
            if (isEdit) vm.updateFuelLog(editFuel!!.id, log, onResult) else vm.saveFuelLog(log, onResult)
        }
    }
}

private fun autoFillTotal(priceText: String, volumeText: String, setTotal: (String) -> Unit) {
    val price = priceText.toDoubleOrNull()
    val volume = volumeText.toDoubleOrNull()
    if (price != null && volume != null) {
        setTotal("%.2f".format(price * volume))
    }
}

@Composable
private fun SelectedVehicleLabel(vm: AppViewModel): String =
    vm.selectedVehicle()?.displayName?.let { "For $it" } ?: "Select a vehicle first"
