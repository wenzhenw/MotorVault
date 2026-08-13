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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.ui.ConfirmDialog
import week11.st099681.finalproject.ui.PrimaryButton
import week11.st099681.finalproject.ui.RowIconButton
import week11.st099681.finalproject.ui.theme.Amber
import week11.st099681.finalproject.ui.theme.Green
import week11.st099681.finalproject.ui.theme.NavyCard
import week11.st099681.finalproject.ui.theme.Red
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.TextSecondary

@Composable
fun FuelHistoryScreen(
    vm: AppViewModel,
    onAddFuel: () -> Unit,
    onEditFuel: (FuelLog) -> Unit
) {
    val context = LocalContext.current
    val vehicles by vm.vehicles.collectAsState()
    vm.fuelLogs.collectAsState() // subscribe so this screen recomposes on data changes

    var vehicleMenuOpen by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<FuelLog?>(null) }

    val vehicle = vehicles.firstOrNull { it.id == vm.selectedVehicleId }
    val logs = vm.fuelLogsForSelected()

    val totalSpent = logs.sumOf { it.totalCost ?: 0.0 }
    val totalVolume = logs.sumOf { it.volume ?: 0.0 }
    val avgPrice = logs.mapNotNull { it.pricePerUnit }.let { if (it.isEmpty()) 0.0 else it.average() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Fuel Tracking", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Log fill-ups and track fuel spend", color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))

        Text("Vehicle", color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate, RoundedCornerShape(10.dp))
                    .clickable { vehicleMenuOpen = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(vehicle?.displayName ?: "Select vehicle", modifier = Modifier.weight(1f), fontSize = 14.sp)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextSecondary)
            }
            DropdownMenu(expanded = vehicleMenuOpen, onDismissRequest = { vehicleMenuOpen = false }) {
                vehicles.forEach { v ->
                    DropdownMenuItem(
                        text = { Text(v.displayName) },
                        onClick = { vm.selectedVehicleId = v.id; vehicleMenuOpen = false }
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // ---- summary stat row ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyCard, RoundedCornerShape(12.dp))
                .padding(vertical = 14.dp)
        ) {
            StatBlock("Total Spent", "$" + "%.2f".format(totalSpent), Green, Modifier.weight(1f))
            StatBlock("Total Volume", "%.1f".format(totalVolume), Amber, Modifier.weight(1f))
            StatBlock("Avg Price/Unit", if (avgPrice > 0) "$" + "%.3f".format(avgPrice) else "—", TextSecondary, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))

        PrimaryButton("Log Fuel Fill-up") {
            if (vehicle == null) {
                Toast.makeText(context, "Select a vehicle first", Toast.LENGTH_SHORT).show()
            } else {
                onAddFuel()
            }
        }
        Spacer(Modifier.height(18.dp))

        Text("Fill-up History", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.LocalGasStation, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No fuel logs yet", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(logs, key = { it.id }) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(38.dp).background(Slate, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.LocalGasStation, contentDescription = null, tint = Amber, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    log.station.ifBlank { log.fuelType },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(Maintenance.formatDate(log.dateMillis), color = TextSecondary, fontSize = 11.sp)
                                Row {
                                    log.volume?.let {
                                        Text("%.2f ${log.unit}".format(it), color = TextSecondary, fontSize = 11.sp)
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    log.odometer?.let {
                                        Text("$it mi", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                                log.totalCost?.let {
                                    Text("$" + "%.2f".format(it), color = Green, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            RowIconButton(Icons.Filled.Edit, contentDescription = "Edit") { onEditFuel(log) }
                            Spacer(Modifier.width(6.dp))
                            RowIconButton(Icons.Filled.Delete, tint = Red, contentDescription = "Delete") { pendingDelete = log }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { log ->
        ConfirmDialog(
            title = "Delete fuel log?",
            message = "This removes the ${log.station.ifBlank { "fill-up" }} entry from ${Maintenance.formatDate(log.dateMillis)}.",
            onConfirm = {
                vm.deleteFuelLog(log.id) { ok ->
                    if (!ok) Toast.makeText(context, "Could not delete fuel log", Toast.LENGTH_SHORT).show()
                }
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun StatBlock(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}
