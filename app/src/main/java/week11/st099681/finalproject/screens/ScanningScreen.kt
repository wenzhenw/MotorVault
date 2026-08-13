package week11.st099681.finalproject.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import week11.st099681.finalproject.AppViewModel
import week11.st099681.finalproject.ocr.ReceiptScanner
import week11.st099681.finalproject.ui.BackHeader
import week11.st099681.finalproject.ui.theme.Blue
import week11.st099681.finalproject.ui.theme.NavyCard
import week11.st099681.finalproject.ui.theme.SlateLight
import week11.st099681.finalproject.ui.theme.TextSecondary

@Composable
fun ScanningScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var progress by remember { mutableFloatStateOf(0f) }
    val animated by animateFloatAsState(progress, animationSpec = tween(400), label = "scan")

    LaunchedEffect(Unit) {
        val uri = vm.receiptUri
        if (uri == null) {
            onBack()
            return@LaunchedEffect
        }
        // Fake staged progress while ML Kit runs
        progress = 0.3f
        try {
            val scanner = ReceiptScanner(context)
            progress = 0.6f
            val receipt = scanner.scanImage(uri)
            progress = 0.9f
            vm.scannedReceipt = receipt
            delay(400)
            progress = 1f
            delay(250)
            onDone()
        } catch (e: Exception) {
            Toast.makeText(context, "Scan failed: ${e.message}", Toast.LENGTH_LONG).show()
            onBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        BackHeader("Scanning Receipt", "ML Kit OCR is reading your receipt", onBack)

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(88.dp).background(NavyCard, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text("Scanning your receipt…", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Extracting the service date and maintenance\ncategory",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            LinearProgressIndicator(
                progress = { animated },
                color = Blue,
                trackColor = SlateLight,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text("Processing… ${(animated * 100).toInt()}%", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            Text("This may take a few seconds", color = TextSecondary, fontSize = 11.sp)
        }
    }
}
