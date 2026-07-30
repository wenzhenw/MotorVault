package week11.st099681.finalproject.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st099681.finalproject.AppViewModel
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.ui.StatusChip
import week11.st099681.finalproject.ui.categoryIcon
import week11.st099681.finalproject.ui.theme.Amber
import week11.st099681.finalproject.ui.theme.Blue
import week11.st099681.finalproject.ui.theme.NavyCard
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.SlateLight
import week11.st099681.finalproject.ui.theme.TextSecondary

@Composable
fun RemindersScreen(vm: AppViewModel) {
    val settings by vm.reminderSettings.collectAsState()
    val allServices by vm.services.collectAsState()

    val services = allServices.filter { it.vehicleId == vm.selectedVehicleId }
    val dueSoon = Maintenance.categories
        .map { it to Maintenance.status(services, it) }
        .filter { it.second.level == Maintenance.Level.WARN || it.second.level == Maintenance.Level.OVERDUE }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Maintenance Reminders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Manage upcoming service notifications", color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(16.dp))

        if (dueSoon.isNotEmpty()) {
            Text(
                "${dueSoon.size} service${if (dueSoon.size == 1) "" else "s"} due soon",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dueSoon.forEach { (category, status) ->
                    Card(colors = CardDefaults.cardColors(containerColor = NavyCard), shape = RoundedCornerShape(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier.size(34.dp).background(Amber.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(categoryIcon(category.name), contentDescription = null, tint = Amber, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(
                                    if (settings[category.name] == true) "Reminder scheduled" else "Reminders off",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            StatusChip(status)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        Text("Notification Settings", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(Maintenance.categories, key = { it.name }) { category ->
                val enabled = settings[category.name] ?: true
                val last = Maintenance.lastService(services, category.name)
                val next = Maintenance.nextDueMillis(last, category)
                Card(colors = CardDefaults.cardColors(containerColor = NavyCard), shape = RoundedCornerShape(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Box(
                            modifier = Modifier.size(34.dp).background(Slate, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(categoryIcon(category.name), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(category.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(
                                if (next != null) "Reminder: ${Maintenance.formatDate(next)}" else "No reminder scheduled",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { vm.setReminderEnabled(category.name, it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Blue,
                                checkedThumbColor = Color.White,
                                uncheckedTrackColor = SlateLight,
                                uncheckedThumbColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}
