package week11.st099681.finalproject.screens

import android.widget.Toast
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
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
            Text("Welcome Back", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Sign in to manage your vehicle", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(28.dp))

            AppTextField(email, { email = it }, "Email address")
            Spacer(Modifier.height(12.dp))
            AppTextField(password, { password = it }, "Password", isPassword = true)
            Spacer(Modifier.height(10.dp))

            Text(
                "Forgot Password?",
                color = BlueLight,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onForgotPassword() }
            )
            Spacer(Modifier.height(16.dp))

            PrimaryButton(if (loading) "Signing in…" else "Log In", enabled = !loading) {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Enter your email and password", Toast.LENGTH_SHORT).show()
                    return@PrimaryButton
                }
                loading = true
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener {
                        loading = false
                        onLoginSuccess()
                    }
                    .addOnFailureListener { e ->
                        loading = false
                        Toast.makeText(context, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account?", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                "Create Account",
                color = BlueLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onCreateAccount() }
            )
        }
    }
}
