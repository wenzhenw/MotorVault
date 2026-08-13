package week11.st099681.finalproject.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.height
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st099681.finalproject.AppViewModel
import week11.st099681.finalproject.data.ImageUtils
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.notifications.scheduleReminder
import week11.st099681.finalproject.ui.BackHeader
import week11.st099681.finalproject.ui.PrimaryButton
import week11.st099681.finalproject.ui.StatusChip
import week11.st099681.finalproject.ui.categoryIcon
import week11.st099681.finalproject.ui.theme.Green
import week11.st099681.finalproject.ui.theme.NavyCard
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.TextSecondary

@Composable
fun ServiceDetailsScreen(
    vm: AppViewModel,
    categoryName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allServices by vm.services.collectAsState()

    val category = Maintenance.category(categoryName)
    val services = allServices.filter { it.vehicleId == vm.selectedVehicleId }
    val last = Maintenance.lastService(services, categoryName)
    val status = Maintenance.status(services, category)
    val nextDue = Maintenance.nextDueMillis(last, category)
    val vehicle = vm.selectedVehicle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            setReminder(context, vm, categoryName, nextDue)
        } else {
            Toast.makeText(context, "Notification permission is needed for reminders", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BackHeader("$categoryName Details", "Maintenance status and previous records", onBack)
        Spacer(Modifier.height(16.dp))

        // Summary card
        Card(colors = CardDefaults.cardColors(containerColor = NavyCard), shape = RoundedCornerShape(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier.size(42.dp).background(Slate, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(categoryIcon(categoryName), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(categoryName, fontWeight = FontWeight.Bold)
                    Text(
                        if (last != null) "Last serviced: ${Maintenance.formatDate(last.dateMillis)}"
                        else "No service recorded yet",
                        color = TextSecondary, fontSize = 11.sp
                    )
                    last?.amount?.let {
                        Text("$" + String.format("%.2f", it), color = Green, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (last != null) {
                        val months = Maintenance.monthsSince(last.dateMillis)
                        Text(
                            if (months <= 0) "Serviced this month" else "$months month${if (months == 1L) "" else "s"} since last service",
                            color = Green, fontSize = 11.sp
                        )
                    }
                }
                StatusChip(status)
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("Service Schedule", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = NavyCard), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text("Recommended interval", color = TextSecondary, fontSize = 10.sp)
                Text(category.intervalLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (nextDue != null) "Next service: ${Maintenance.formatDate(nextDue)}"
                    else "Save a receipt to start tracking this service",
                    color = if (nextDue != null) week11.st099681.finalproject.ui.statusColor(status.level) else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        val previousImage = ImageUtils.base64ToBitmap(last?.imageBase64)
        if (last != null) {
            Text("Previous Receipt", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = NavyCard), shape = RoundedCornerShape(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier.size(56.dp).background(Color.White, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (previousImage != null) {
                            Image(
                                bitmap = previousImage.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(last.vendor.ifBlank { "Receipt" }, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(Maintenance.formatDate(last.dateMillis), color = TextSecondary, fontSize = 11.sp)
                        last.amount?.let {
                            Text("Total: $" + String.format("%.2f", it), color = Green, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        PrimaryButton("Set Maintenance Reminder") {
            if (vehicle == null) {
                Toast.makeText(context, "Select a vehicle first", Toast.LENGTH_SHORT).show()
                return@PrimaryButton
            }
            if (Build.VERSION.SDK_INT >= 33) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                setReminder(context, vm, categoryName, nextDue)
            }
        }
    }
}

private fun setReminder(
    context: android.content.Context,
    vm: AppViewModel,
    categoryName: String,
    nextDue: Long?
) {
    val vehicleName = vm.selectedVehicle()?.displayName ?: "your vehicle"
    val trigger = nextDue ?: (System.currentTimeMillis() + 10_000L)
    scheduleReminder(
        context,
        title = "$categoryName due",
        text = "$categoryName is due for $vehicleName.",
        triggerAtMillis = trigger
    )
    val whenText = if (nextDue != null) "for ${Maintenance.formatDate(trigger)}" else "now (no schedule yet)"
    Toast.makeText(context, "Reminder set $whenText", Toast.LENGTH_LONG).show()
}
