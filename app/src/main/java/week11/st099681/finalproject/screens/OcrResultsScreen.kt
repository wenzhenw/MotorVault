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
import week11.st099681.finalproject.data.ImageUtils
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.data.ServiceRecord
import week11.st099681.finalproject.ui.AppTextField
import week11.st099681.finalproject.ui.BackHeader
import week11.st099681.finalproject.ui.PrimaryButton
import week11.st099681.finalproject.ui.SecondaryButton
import week11.st099681.finalproject.ui.theme.Green
import week11.st099681.finalproject.ui.theme.NavyCard
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.TextSecondary

@Composable
fun OcrResultsScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val receipt = vm.scannedReceipt
    val vehicle = vm.selectedVehicle()

    var dateText by remember {
        mutableStateOf(
            Maintenance.parseDate(receipt?.date)?.let { Maintenance.formatDate(it) }
                ?: receipt?.date.orEmpty()
        )
    }
    var category by remember {
        mutableStateOf(receipt?.categories?.firstOrNull() ?: "Oil Change")
    }
    var amountText by remember {
        mutableStateOf(receipt?.amount?.let { String.format("%.2f", it) } ?: "")
    }
    var menuOpen by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BackHeader("OCR Results", "Review and correct the extracted information", onBack)
        Spacer(Modifier.height(14.dp))

        // Success banner
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Green.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Green)
            Spacer(Modifier.width(10.dp))
            Text("Receipt scanned successfully", color = Green, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(14.dp))

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
                    DropdownMenuItem(
                        text = { Text(c.name) },
                        onClick = {
                            category = c.name
                            menuOpen = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Text("Total amount", color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        AppTextField(amountText, { amountText = it }, "e.g. 125.00")
        Spacer(Modifier.height(12.dp))

        LabeledBox(label = "Recognized receipt text") {
            Text(
                receipt?.rawText?.take(600) ?: "No text recognized",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("Edit Details", modifier = Modifier.weight(1f)) {
                Toast.makeText(context, "Edit the fields above, then Save Receipt", Toast.LENGTH_SHORT).show()
            }
            PrimaryButton(
                if (saving) "Saving…" else "Save Receipt",
                modifier = Modifier.weight(1f),
                enabled = !saving
            ) {
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
                        onSaved()
                    } else {
                        Toast.makeText(context, "Could not save receipt", Toast.LENGTH_SHORT).show()
                    }
                }
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
