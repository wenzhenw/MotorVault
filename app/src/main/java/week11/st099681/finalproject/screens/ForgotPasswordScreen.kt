package week11.st099681.finalproject.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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
import com.google.firebase.auth.FirebaseAuth
import week11.st099681.finalproject.ui.AppTextField
import week11.st099681.finalproject.ui.CarBadge
import week11.st099681.finalproject.ui.PrimaryButton
import week11.st099681.finalproject.ui.theme.BlueLight
import week11.st099681.finalproject.ui.theme.TextSecondary

@Composable
fun ForgotPasswordScreen(onBackToLogin: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CarBadge()
            Spacer(Modifier.height(20.dp))
            Text("Forgot Password?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Enter your email and we'll send you a reset link",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(28.dp))

            AppTextField(email, { email = it }, "Email address")
            Spacer(Modifier.height(16.dp))

            PrimaryButton(if (loading) "Sending…" else "Send Reset Link", enabled = !loading) {
                if (email.isBlank()) {
                    Toast.makeText(context, "Enter your email address", Toast.LENGTH_SHORT).show()
                    return@PrimaryButton
                }
                loading = true
                auth.sendPasswordResetEmail(email.trim())
                    .addOnSuccessListener {
                        loading = false
                        Toast.makeText(context, "Reset link sent — check your inbox", Toast.LENGTH_LONG).show()
                        onBackToLogin()
                    }
                    .addOnFailureListener { e ->
                        loading = false
                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }

            Spacer(Modifier.height(40.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Back to", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Log In",
                    color = BlueLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onBackToLogin() }
                )
            }
        }
    }
}
