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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalGasStation
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st099681.finalproject.AppViewModel
import week11.st099681.finalproject.data.FuelLog
import week11.st099681.finalproject.data.ImageUtils
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.data.ReceiptType
import week11.st099681.finalproject.data.ServiceRecord
import week11.st099681.finalproject.ui.AppTextField
import week11.st099681.finalproject.ui.BackHeader
import week11.st099681.finalproject.ui.PrimaryButton
import week11.st099681.finalproject.ui.theme.Amber
import week11.st099681.finalproject.ui.theme.Green
import week11.st099681.finalproject.ui.theme.NavyCard
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.TextSecondary

private val fuelTypes = listOf("Regular", "Midgrade", "Premium", "Diesel", "E85", "Electric")

@Composable
fun OcrResultsScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onSaved: (ReceiptType) -> Unit
) {
    val receipt = vm.scannedReceipt
    val isFuel = receipt?.type == ReceiptType.FUEL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BackHeader(
            "OCR Results",
            if (isFuel) "Fuel receipt detected — review the extracted details" else "Review and correct the extracted information",
            onBack
        )
        Spacer(Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Green.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Icon(
                if (isFuel) Icons.Filled.LocalGasStation else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Green
            )
            Spacer(Modifier.width(10.dp))
            Text(
                if (isFuel) "Fuel receipt scanned — auto-detected from receipt text" else "Receipt scanned successfully",
                color = Green,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(14.dp))

        if (isFuel) {
            FuelResultsForm(vm, onSaved)
        } else {
            ServiceResultsForm(vm, onSaved)
        }
    }
}

@Composable
private fun ServiceResultsForm(vm: AppViewModel, onSaved: (ReceiptType) -> Unit) {
    val context = LocalContext.current
    val receipt = vm.scannedReceipt
    val vehicle = vm.selectedVehicle()

    var dateText by remember {
        mutableStateOf(
            Maintenance.parseDate(receipt?.date)?.let { Maintenance.formatDate(it) }
                ?: receipt?.date.orEmpty()
        )
    }
    var category by remember { mutableStateOf(receipt?.categories?.firstOrNull() ?: "Oil Change") }
    var amountText by remember { mutableStateOf(receipt?.amount?.let { "%.2f".format(it) } ?: "") }
    var menuOpen by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    LabeledBox(label = "Vehicle") {
        Text(vehicle?.displayName ?: "No vehicle selected", fontSize = 14.sp)
    }
    Spacer(Modifier.height(12.dp))

    Text("Service date", color = TextSecondary, fontSize = 11.sp)
    Spacer(Modifier.height(4.dp))
    AppTextField(dateText, { dateText = it }, "e.g. July 15, 2026 or 07/15/2026")
    Spacer(Modifier.height(12.dp))

    Text("Service category", color = TextSecondary, fontSize = 11.sp)
    Spacer(Modifier.height(4.dp))
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate, RoundedCornerShape(10.dp))
                .clickable { menuOpen = true }
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(category, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextSecondary)
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            Maintenance.categories.forEach { c ->
                DropdownMenuItem(text = { Text(c.name) }, onClick = { category = c.name; menuOpen = false })
            }
        }
    }
    Spacer(Modifier.height(12.dp))

    Text("Total amount", color = TextSecondary, fontSize = 11.sp)
    Spacer(Modifier.height(4.dp))
    AppTextField(amountText, { amountText = it }, "e.g. 125.00")
    Spacer(Modifier.height(12.dp))

    LabeledBox(label = "Recognized receipt text") {
        Text(receipt?.rawText?.take(600) ?: "No text recognized", color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
    }
    Spacer(Modifier.height(20.dp))

    PrimaryButton(if (saving) "Saving…" else "Save Receipt", enabled = !saving) {
        val vehicleId = vm.selectedVehicleId
        if (vehicleId == null) {
            Toast.makeText(context, "Select a vehicle first", Toast.LENGTH_SHORT).show()
            return@PrimaryButton
        }
        val dateMillis = Maintenance.parseDate(dateText) ?: System.currentTimeMillis()
        saving = true
        val imageB64 = vm.receiptUri?.let { ImageUtils.uriToBase64(context, it) }
        vm.saveServiceRecord(
            ServiceRecord(
                vehicleId = vehicleId,
                vendor = receipt?.vendor.orEmpty(),
                category = category,
                dateMillis = dateMillis,
                amount = amountText.replace("$", "").trim().toDoubleOrNull(),
                rawText = receipt?.rawText.orEmpty(),
                imageBase64 = imageB64
            )
        ) { ok ->
            saving = false
            if (ok) {
                Toast.makeText(context, "Receipt saved", Toast.LENGTH_SHORT).show()
                vm.clearReceiptFlow()
                onSaved(ReceiptType.SERVICE)
            } else {
                Toast.makeText(context, "Could not save receipt", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun FuelResultsForm(vm: AppViewModel, onSaved: (ReceiptType) -> Unit) {
    val context = LocalContext.current
    val receipt = vm.scannedReceipt
    val vehicle = vm.selectedVehicle()

    var dateText by remember {
        mutableStateOf(
            Maintenance.parseDate(receipt?.date)?.let { Maintenance.formatDate(it) }
                ?: receipt?.date.orEmpty()
        )
    }
    var station by remember { mutableStateOf(receipt?.vendor.orEmpty()) }
    var volumeText by remember { mutableStateOf(receipt?.volume?.let { "%.3f".format(it) } ?: "") }
    var unit by remember { mutableStateOf(receipt?.volumeUnit ?: "L") }
    var priceText by remember { mutableStateOf(receipt?.pricePerUnit?.let { "%.3f".format(it) } ?: "") }
    var totalText by remember { mutableStateOf(receipt?.amount?.let { "%.2f".format(it) } ?: "") }
    var odometerText by remember { mutableStateOf(receipt?.odometer?.toString() ?: "") }
    var fuelType by remember { mutableStateOf(receipt?.fuelType ?: "Regular") }
    var typeMenuOpen by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    LabeledBox(label = "Vehicle") {
        Text(vehicle?.displayName ?: "No vehicle selected", fontSize = 14.sp)
    }
    Spacer(Modifier.height(12.dp))

    Text("Date", color = TextSecondary, fontSize = 11.sp)
    Spacer(Modifier.height(4.dp))
    AppTextField(dateText, { dateText = it }, "e.g. July 15, 2026")
    Spacer(Modifier.height(12.dp))

    AppTextField(station, { station = it }, "Gas station")
    Spacer(Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTextField(volumeText, { volumeText = it }, "Volume (${unit})", modifier = Modifier.weight(1f))
        AppTextField(priceText, { priceText = it }, "Price/unit", modifier = Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))

    Text("Total amount", color = TextSecondary, fontSize = 11.sp)
    Spacer(Modifier.height(4.dp))
    AppTextField(totalText, { totalText = it }, "e.g. 42.90")
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
    Spacer(Modifier.height(12.dp))

    LabeledBox(label = "Recognized receipt text") {
        Text(receipt?.rawText?.take(600) ?: "No text recognized", color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
    }
    Spacer(Modifier.height(20.dp))

    PrimaryButton(if (saving) "Saving…" else "Save Fuel Log", enabled = !saving) {
        val vehicleId = vm.selectedVehicleId
        if (vehicleId == null) {
            Toast.makeText(context, "Select a vehicle first", Toast.LENGTH_SHORT).show()
            return@PrimaryButton
        }
        saving = true
        val imageB64 = vm.receiptUri?.let { ImageUtils.uriToBase64(context, it) }
        vm.saveFuelLog(
            FuelLog(
                vehicleId = vehicleId,
                dateMillis = Maintenance.parseDate(dateText) ?: System.currentTimeMillis(),
                station = station.trim(),
                volume = volumeText.toDoubleOrNull(),
                unit = unit,
                pricePerUnit = priceText.toDoubleOrNull(),
                totalCost = totalText.replace("$", "").trim().toDoubleOrNull(),
                odometer = odometerText.trim().toIntOrNull(),
                fuelType = fuelType,
                rawText = receipt?.rawText.orEmpty(),
                imageBase64 = imageB64
            )
        ) { ok ->
            saving = false
            if (ok) {
                Toast.makeText(context, "Fuel log saved", Toast.LENGTH_SHORT).show()
                vm.clearReceiptFlow()
                onSaved(ReceiptType.FUEL)
            } else {
                Toast.makeText(context, "Could not save fuel log", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
private fun LabeledBox(label: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyCard, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(label, color = TextSecondary, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        content()
    }
}
