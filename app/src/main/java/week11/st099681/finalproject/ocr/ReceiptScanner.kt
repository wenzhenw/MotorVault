package week11.st099681.finalproject.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import week11.st099681.finalproject.data.Receipt
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/***
 * Receipt should follow the below example:
 *
 * Joe's Auto Service Center       - vendor name
 * 1234 Main Street
 * Springfield, IL 62704
 *
 * Date: 03/15/2026                - date
 * Invoice #: 45892
 *
 * ------------------------------
 * Service Description
 * ------------------------------
 * Full Synthetic Oil Change       - categories
 * Oil Filter Replacement
 * Tire Rotation
 *
 * ------------------------------
 * Labor:            $45.00
 * Parts:            $38.50
 * Tax:              $6.68
 * ------------------------------
 * Total:            $90.18        - amount
 *
 * Thank you for your business!
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

    private fun parseReceipt(text: String): Receipt {
        val amountPattern = Pattern.compile("""\$\s?(\d+\.\d{2})""")
        val datePattern = Pattern.compile("""(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})""")

        val amountMatcher = amountPattern.matcher(text)
        var amount: Double? = null
        while (amountMatcher.find()) { // loops through lines with $ until the last one which should be total
            amount = amountMatcher.group(1)?.toDoubleOrNull()
        }

        val dateMatcher = datePattern.matcher(text)
        val date = if (dateMatcher.find()) dateMatcher.group(1) else null

        val lines = text.lines().filter { it.isNotBlank() }
        val vendor = lines.firstOrNull()

        val zeroToO = text
            .lowercase()
            .replace(Regex("""\b0il\b"""), "oil")

        val categoryKeywords = mapOf(
            "oil" to "Oil Change",
            "tire" to "Tire Service",
            "brake" to "Brake Service",
            "insurance" to "Insurance",
            "registration" to "Registration"
        )

        val categories = categoryKeywords
            .filterKeys { keyword -> zeroToO.contains(keyword, ignoreCase = true) }
            .values
            .toList()

        return Receipt(
            rawText = text,
            vendor = vendor,
            amount = amount,
            date = date,
            categories = categories
        )
    }
}