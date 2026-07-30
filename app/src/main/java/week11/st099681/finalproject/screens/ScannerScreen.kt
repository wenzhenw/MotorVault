package week11.st099681.finalproject.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import week11.st099681.finalproject.data.Receipt
import week11.st099681.finalproject.ocr.ReceiptScanner

@Composable
fun ScannerScreen(
    receipt: (Receipt) -> Unit = {}
) {
    val context = LocalContext.current
    val analyzer = remember { ReceiptScanner(context) }
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var result by remember { mutableStateOf<Receipt?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            isLoading = true
            error = null
            scope.launch {
                try {
                    val scanned = analyzer.scanImage(it)
                    result = scanned
                    receipt(scanned)
                } catch (e: Exception) {
                    error = e.message ?: "Failed to scan receipt"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Receipt Scanner", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Pick Receipt Image")
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        error?.let {
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }

        result?.let { r ->
            Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Vendor: ${r.vendor ?: "Unknown"}")
                    Text("Amount: ${r.amount?.let { "$$it" } ?: "Not found"}")
                    Text("Date: ${r.date ?: "Not found"}")
                    Text(
                        "Categories: ${
                            if (r.categories.isNotEmpty()) r.categories.joinToString(", ")
                            else "Uncategorized"
                        }"
                    )
                    // rawtext for debug Text(r.rawText)
                }
            }
        }
    }
}