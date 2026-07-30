package week11.st099681.finalproject.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st099681.finalproject.AppViewModel
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.ui.PrimaryButton
import week11.st099681.finalproject.ui.StatusChip
import week11.st099681.finalproject.ui.categoryIcon
import week11.st099681.finalproject.ui.theme.NavyCard
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    vm: AppViewModel,
    onUploadReceipt: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    // subscribe so the screen recomposes on data changes
    val allServices by vm.services.collectAsState()
    val vehicles by vm.vehicles.collectAsState()

    val vehicle = vehicles.firstOrNull { it.id == vm.selectedVehicleId }
    val services = allServices.filter { it.vehicleId == vm.selectedVehicleId }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                vehicle?.displayName ?: "No vehicle selected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (vehicle != null) "Licence plate: ${vehicle.licensePlate}" else "Add a vehicle to get started",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(16.dp))

        if (vehicle != null) {
            PrimaryButton("Upload Maintenance Receipt") { onUploadReceipt() }
            Spacer(Modifier.height(20.dp))

            Text("Maintenance Status", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(Maintenance.categories, key = { it.name }) { category ->
                    val last = Maintenance.lastService(services, category.name)
                    val status = Maintenance.status(services, category)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategoryClick(category.name) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(38.dp).background(Slate, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    categoryIcon(category.name),
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    if (last != null) {
                                        val verb = if (category.isRenewal) "Renewal" else "Last serviced"
                                        "$verb: ${Maintenance.formatDate(last.dateMillis)}"
                                    } else "No service recorded",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            StatusChip(status)
                        }
                    }
                }
            }
        }
    }
}
