package week11.st099681.finalproject.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import week11.st099681.finalproject.data.Receipt
import week11.st099681.finalproject.data.ReceiptType
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/***
 * SERVICE receipt example:
 *
 * Joe's Auto Service Center       - vendor name
 * Date: 03/15/2026                - date
 * Full Synthetic Oil Change       - categories
 * Total:            $90.18        - amount
 *
 * FUEL receipt example:
 *
 * Shell Gas Station               - vendor / station
 * Date: 03/15/2026
 * Pump #4  Regular Unleaded       - fuel type
 * 12.403 gal  @ $3.459/gal        - volume, price/unit
 * Odometer: 45,231 mi             - odometer
 * Total:            $42.90        - amount
 *
 * The scanner classifies which kind of receipt it's looking at (next-level OCR:
 * one pipeline, two structured output shapes) and extracts the fields that
 * matter for each — service category vs. fuel volume/price/odometer/grade.
 */
class ReceiptScanner(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanImage(imageUri: Uri): Receipt {
        val image = InputImage.fromFilePath(context, imageUri)
        val rawText = recognizeText(image)
        return parseReceipt(rawText)
    }

    private suspend fun recognizeText(image: InputImage): String =
        suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { visionText -> cont.resume(visionText.text) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }

    // ---- shared patterns ----
    private val totalAmountPattern = Pattern.compile("""\$\s?(\d+\.\d{2})\b""")
    private val datePattern = Pattern.compile("""(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})""")

    // ---- fuel-specific patterns ----
    private val volumePattern =
        Pattern.compile("""(\d{1,3}\.\d{2,3})\s?(gal(?:lons)?|gals|l|liters?|litres?)\b""", Pattern.CASE_INSENSITIVE)
    private val pricePerUnitPattern =
        Pattern.compile("""\$\s?(\d\.\d{2,3})\s?/\s?(gal|l)\b""", Pattern.CASE_INSENSITIVE)
    private val odometerPattern =
        Pattern.compile("""(?:odometer|mileage)\s*[:#]?\s*([\d,]{3,7})""", Pattern.CASE_INSENSITIVE)

    private val serviceCategoryKeywords = mapOf(
        "oil" to "Oil Change",
        "tire" to "Tire Service",
        "brake" to "Brake Service",
        "insurance" to "Insurance",
        "registration" to "Registration"
    )

    private val fuelKeywords = listOf(
        "gallon", "gal ", "gal.", "liter", "litre", " fuel", "gas station",
        "unleaded", "diesel", "octane", "pump #", "pump#", "fuel type", "e85"
    )

    private val fuelTypeKeywords = listOf(
        "premium" to "Premium",
        "midgrade" to "Midgrade",
        "plus" to "Midgrade",
        "diesel" to "Diesel",
        "e85" to "E85",
        "regular" to "Regular",
        "unleaded" to "Regular"
    )

    private fun parseReceipt(text: String): Receipt {
        val lower = text.lowercase()
        val zeroToO = lower.replace(Regex("""\b0il\b"""), "oil") // common OCR misread

        // ---- classify: FUEL vs SERVICE ----
        val fuelScore = fuelKeywords.count { lower.contains(it) }
        val serviceScore = serviceCategoryKeywords.keys.count { zeroToO.contains(it) }
        val hasVolumeMatch = volumePattern.matcher(text).find()
        val type = when {
            (fuelScore > 0 || hasVolumeMatch) && fuelScore >= serviceScore -> ReceiptType.FUEL
            serviceScore > 0 -> ReceiptType.SERVICE
            else -> ReceiptType.OTHER
        }

        // ---- shared fields ----
        val amountMatcher = totalAmountPattern.matcher(text)
        var amount: Double? = null
        while (amountMatcher.find()) { // last $XX.XX on the receipt is usually the grand total
            amount = amountMatcher.group(1)?.toDoubleOrNull()
        }

        val dateMatcher = datePattern.matcher(text)
        val date = if (dateMatcher.find()) dateMatcher.group(1) else null

        val lines = text.lines().filter { it.isNotBlank() }
        val vendor = lines.firstOrNull()

        return if (type == ReceiptType.FUEL) {
            val volumeMatcher = volumePattern.matcher(text)
            var volume: Double? = null
            var volumeUnit: String? = null
            if (volumeMatcher.find()) {
                volume = volumeMatcher.group(1)?.toDoubleOrNull()
                volumeUnit = when (volumeMatcher.group(2)?.lowercase()?.firstOrNull()) {
                    'g' -> "gal"
                    else -> "L"
                }
            }

            val priceMatcher = pricePerUnitPattern.matcher(text)
            val pricePerUnit = if (priceMatcher.find()) priceMatcher.group(1)?.toDoubleOrNull() else null

            val odoMatcher = odometerPattern.matcher(text)
            val odometer = if (odoMatcher.find()) {
                odoMatcher.group(1)?.replace(",", "")?.toIntOrNull()
            } else null

            val fuelType = fuelTypeKeywords.firstOrNull { lower.contains(it.first) }?.second

            Receipt(
                rawText = text,
                type = ReceiptType.FUEL,
                vendor = vendor,
                amount = amount,
                date = date,
                volume = volume,
                volumeUnit = volumeUnit,
                pricePerUnit = pricePerUnit,
                odometer = odometer,
                fuelType = fuelType
            )
        } else {
            val categories = serviceCategoryKeywords
                .filterKeys { keyword -> zeroToO.contains(keyword, ignoreCase = true) }
                .values
                .toList()

            Receipt(
                rawText = text,
                type = type,
                vendor = vendor,
                amount = amount,
                date = date,
                categories = categories
            )
        }
    }
}
