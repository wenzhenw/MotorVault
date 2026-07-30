package week11.st099681.finalproject.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st099681.finalproject.AppViewModel
import week11.st099681.finalproject.data.ImageUtils
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.ui.FilterPill
import week11.st099681.finalproject.ui.theme.Green
import week11.st099681.finalproject.ui.theme.NavyCard
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.SlateLight
import week11.st099681.finalproject.ui.theme.TextSecondary

private val filters = listOf(
    "All" to null,
    "Oil" to "Oil Change",
    "Tires" to "Tire Service",
    "Brakes" to "Brake Service",
    "Insurance" to "Insurance",
    "Registration" to "Registration"
)

@Composable
fun HistoryScreen(
    vm: AppViewModel,
    onViewDetails: (String) -> Unit
) {
    val vehicles by vm.vehicles.collectAsState()
    val allServices by vm.services.collectAsState()

    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var vehicleMenuOpen by remember { mutableStateOf(false) }

    val vehicle = vehicles.firstOrNull { it.id == vm.selectedVehicleId }
    val records = allServices
        .filter { it.vehicleId == vm.selectedVehicleId }
        .filter { selectedFilter == null || it.category == selectedFilter }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Maintenance History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("View saved service records and receipts", color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))

        // Vehicle dropdown
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
                        onClick = {
                            vm.selectedVehicleId = v.id
                            vehicleMenuOpen = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            filters.forEach { (label, value) ->
                FilterPill(label, selected = selectedFilter == value) { selectedFilter = value }
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("Recent Records", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))

        if (records.isEmpty()) {
            Text("No service records yet", color = TextSecondary, fontSize = 13.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(records, key = { it.id }) { record ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            val thumb = ImageUtils.base64ToBitmap(record.imageBase64)
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color.White, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (thumb != null) {
                                    Image(
                                        bitmap = thumb.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Filled.Receipt, contentDescription = null, tint = SlateLight)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(record.category, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(Maintenance.formatDate(record.dateMillis), color = TextSecondary, fontSize = 11.sp)
                                record.amount?.let {
                                    Text("$" + String.format("%.2f", it), color = Green, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Text(
                                "View Details",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(SlateLight, RoundedCornerShape(6.dp))
                                    .clickable { onViewDetails(record.category) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
