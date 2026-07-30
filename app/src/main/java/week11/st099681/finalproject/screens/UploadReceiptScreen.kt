package week11.st099681.finalproject.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st099681.finalproject.AppViewModel
import week11.st099681.finalproject.data.ImageUtils
import week11.st099681.finalproject.ui.BackHeader
import week11.st099681.finalproject.ui.PrimaryButton
import week11.st099681.finalproject.ui.SecondaryButton
import week11.st099681.finalproject.ui.theme.Blue
import week11.st099681.finalproject.ui.theme.NavyCard
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.TextSecondary

@Composable
fun UploadReceiptScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onImageReady: () -> Unit
) {
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            vm.receiptUri = uri
            onImageReady()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok && cameraUri != null) {
            vm.receiptUri = cameraUri
            onImageReady()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        BackHeader("Upload Receipt", "Add a maintenance receipt for OCR scanning", onBack)
        Spacer(Modifier.height(16.dp))

        SelectedVehicleChip(vm)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(NavyCard, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(14.dp))
                Text("Upload a receipt image", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    "Take a photo or choose one from your gallery",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("Take Photo", modifier = Modifier.weight(1f)) {
                val uri = ImageUtils.newCameraUri(context)
                cameraUri = uri
                cameraLauncher.launch(uri)
            }
            SecondaryButton("Choose Image", modifier = Modifier.weight(1f)) {
                galleryLauncher.launch("image/*")
            }
        }
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyCard, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text("For better OCR results:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text("• Keep the receipt flat", color = TextSecondary, fontSize = 12.sp)
            Text("• Use clear lighting", color = TextSecondary, fontSize = 12.sp)
            Text("• Make sure all text is visible", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun SelectedVehicleChip(vm: AppViewModel) {
    val vehicle = vm.selectedVehicle()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Selected vehicle", color = TextSecondary, fontSize = 10.sp)
            Text(vehicle?.displayName ?: "None", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = Blue)
    }
}
