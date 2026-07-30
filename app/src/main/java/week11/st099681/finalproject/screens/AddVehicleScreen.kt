package week11.st099681.finalproject.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st099681.finalproject.AppViewModel
import week11.st099681.finalproject.data.ImageUtils
import week11.st099681.finalproject.data.Vehicle
import week11.st099681.finalproject.ui.AppTextField
import week11.st099681.finalproject.ui.BackHeader
import week11.st099681.finalproject.ui.PrimaryButton
import week11.st099681.finalproject.ui.theme.Slate
import week11.st099681.finalproject.ui.theme.TextSecondary

@Composable
fun AddVehicleScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current

    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var photoBase64 by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { photoBase64 = ImageUtils.uriToBase64(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BackHeader("Add Vehicle", "Enter your vehicle information", onBack)
        Spacer(Modifier.height(20.dp))

        // Photo picker card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(Slate, RoundedCornerShape(12.dp))
                .clickable { photoPicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            val bmp = ImageUtils.base64ToBitmap(photoBase64)
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    Text("Add vehicle photo", fontSize = 13.sp)
                    Text("Optional", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(make, { make = it }, "Vehicle make", modifier = Modifier.weight(1f))
            AppTextField(model, { model = it }, "Vehicle model", modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(year, { year = it }, "Year", modifier = Modifier.weight(1f))
            AppTextField(plate, { plate = it }, "License plate", modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        AppTextField(mileage, { mileage = it }, "Current mileage")
        Spacer(Modifier.height(24.dp))

        PrimaryButton(if (saving) "Saving…" else "Save Vehicle", enabled = !saving) {
            if (make.isBlank() || model.isBlank() || year.isBlank() || plate.isBlank()) {
                Toast.makeText(context, "Make, model, year, and plate are required", Toast.LENGTH_SHORT).show()
                return@PrimaryButton
            }
            saving = true
            vm.addVehicle(
                Vehicle(
                    make = make.trim(),
                    model = model.trim(),
                    year = year.trim(),
                    licensePlate = plate.trim().uppercase(),
                    mileage = mileage.trim(),
                    photoBase64 = photoBase64
                )
            ) { ok ->
                saving = false
                if (ok) {
                    Toast.makeText(context, "Vehicle saved", Toast.LENGTH_SHORT).show()
                    onSaved()
                } else {
                    Toast.makeText(context, "Could not save vehicle", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
