package week11.st099681.finalproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st099681.finalproject.data.Maintenance
import week11.st099681.finalproject.ui.theme.Amber
import week11.st099681.finalproject.ui.theme.Blue
import week11.st099681.finalproject.ui.theme.Green
import week11.st099681.finalproject.ui.theme.Red
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.SlateLight
import week11.st099681.finalproject.ui.theme.TextSecondary

/** Blue circular badge with the car icon, used on splash + auth screens. */
@Composable
fun CarBadge(size: Int = 64) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(Blue, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.DirectionsCar,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

/** Rounded dark text field used across the app. */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextSecondary, fontSize = 14.sp) },
        singleLine = true,
        visualTransformation =
            if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Slate,
            unfocusedContainerColor = Slate,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Blue,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SlateLight, contentColor = Color.White),
        modifier = modifier.height(48.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

/** Screen title row with a back arrow, matching frames 06-13. */
@Composable
fun BackHeader(title: String, subtitle: String? = null, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f).padding(end = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (subtitle != null) {
                Text(subtitle, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

/** Colored status text ("Due in 18 days", "Up to date", ...). */
@Composable
fun StatusText(status: Maintenance.Status, fontSize: Int = 12) {
    Text(
        status.label,
        color = statusColor(status.level),
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Medium
    )
}

fun statusColor(level: Maintenance.Level): Color = when (level) {
    Maintenance.Level.OK -> Green
    Maintenance.Level.WARN -> Amber
    Maintenance.Level.OVERDUE -> Red
    Maintenance.Level.UNKNOWN -> TextSecondary
}

/** Small rounded status pill used on the dashboard rows. */
@Composable
fun StatusChip(status: Maintenance.Status) {
    val color = statusColor(status.level)
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status.label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ---------------- bottom navigation ----------------

data class BottomTab(val route: String, val label: String, val icon: ImageVector)

val bottomTabs = listOf(
    BottomTab("vehicles", "Vehicles", Icons.Filled.DirectionsCar),
    BottomTab("dashboard", "Dashboard", Icons.Filled.Dashboard),
    BottomTab("history", "History", Icons.Filled.History),
    BottomTab("reminders", "Reminders", Icons.Filled.Notifications)
)

@Composable
fun AppBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = week11.st099681.finalproject.ui.theme.NavyCard) {
        bottomTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onNavigate(tab.route) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Blue,
                    selectedTextColor = Blue,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

/** Icon per maintenance category. */
fun categoryIcon(name: String): ImageVector = when (name) {
    "Oil Change" -> Icons.Filled.WaterDrop
    "Tire Service" -> Icons.Filled.TripOrigin
    "Brake Service" -> Icons.Filled.Build
    "Insurance" -> Icons.Filled.Shield
    "Registration" -> Icons.Filled.Description
    else -> Icons.Filled.Build
}

/** Simple selectable filter chip row item (History screen). */
@Composable
fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (selected) Blue else Slate, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text, color = if (selected) Color.White else TextSecondary, fontSize = 12.sp)
    }
}
