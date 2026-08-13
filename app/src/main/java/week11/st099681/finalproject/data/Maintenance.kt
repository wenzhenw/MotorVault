package week11.st099681.finalproject.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class MaintenanceCategory(
    val name: String,
    val intervalMonths: Int,
    val intervalLabel: String,
    val isRenewal: Boolean = false
)

object Maintenance {

    val categories = listOf(
        MaintenanceCategory("Oil Change", 6, "Every 6 months or 8,000 km"),
        MaintenanceCategory("Tire Service", 12, "Every 12 months or rotation every 10,000 km"),
        MaintenanceCategory("Brake Service", 12, "Inspect every 12 months or 20,000 km"),
        MaintenanceCategory("Insurance", 12, "Renew every 12 months", isRenewal = true),
        MaintenanceCategory("Registration", 12, "Renew every 12 months", isRenewal = true)
    )

    fun category(name: String): MaintenanceCategory =
        categories.firstOrNull { it.name == name }
            ?: MaintenanceCategory(name, 12, "Every 12 months")

    fun lastService(records: List<ServiceRecord>, categoryName: String): ServiceRecord? =
        records.filter { it.category == categoryName && it.dateMillis > 0L }
            .maxByOrNull { it.dateMillis }

    fun nextDueMillis(last: ServiceRecord?, category: MaintenanceCategory): Long? {
        if (last == null) return null
        val cal = Calendar.getInstance()
        cal.timeInMillis = last.dateMillis
        cal.add(Calendar.MONTH, category.intervalMonths)
        return cal.timeInMillis
    }

    enum class Level { OK, WARN, OVERDUE, UNKNOWN }
    data class Status(val label: String, val level: Level)

    fun status(records: List<ServiceRecord>, category: MaintenanceCategory): Status {
        val next = nextDueMillis(lastService(records, category.name), category)
            ?: return Status(if (category.isRenewal) "Active" else "No records", Level.UNKNOWN)
        val days = TimeUnit.MILLISECONDS.toDays(next - System.currentTimeMillis())
        return when {
            days < 0 -> Status("Overdue", Level.OVERDUE)
            days <= 7 -> Status("Due soon", Level.WARN)
            days <= 30 -> Status("Due in $days days", Level.WARN)
            else -> Status(if (category.isRenewal) "Active" else "Up to date", Level.OK)
        }
    }

    /** Worst status across all categories, used on the My Vehicles card. */
    fun overallStatus(records: List<ServiceRecord>): Status {
        val statuses = categories.map { status(records, it) }
        return statuses.firstOrNull { it.level == Level.OVERDUE }
            ?: statuses.firstOrNull { it.level == Level.WARN }
            ?: Status("Maintenance up to date", Level.OK)
    }

    // ---------- date helpers ----------

    private val displayFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)

    fun formatDate(millis: Long): String =
        if (millis <= 0L) "—" else displayFormat.format(Date(millis))

    /** Parses common receipt/user date formats; returns null when nothing matches. */
    fun parseDate(text: String?): Long? {
        if (text.isNullOrBlank()) return null
        val patterns = listOf(
            "M/d/yyyy", "M-d-yyyy", "M/d/yy", "M-d-yy",
            "yyyy-M-d", "MMMM d, yyyy", "MMM d, yyyy", "MMM d yyyy", "d MMMM yyyy"
        )
        for (p in patterns) {
            try {
                val f = SimpleDateFormat(p, Locale.US)
                f.isLenient = false
                return f.parse(text.trim())?.time ?: continue
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun monthsSince(millis: Long): Long {
        if (millis <= 0L) return 0
        val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - millis)
        return days / 30
    }
}
