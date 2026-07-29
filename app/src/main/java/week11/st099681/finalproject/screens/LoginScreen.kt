package week11.st099681.finalproject.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit,
                modifier: Modifier = Modifier) {
    val auth = FirebaseAuth.getInstance()

    // track if user is logged in
    val currentUser = remember { mutableStateOf(auth.currentUser) }

    // fields
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val message = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center, // center vertically
            horizontalAlignment = Alignment.CenterHorizontally // center horizontally
        ) {
            Text(
                text = "Login", style = MaterialTheme.typography.headlineLarge // BIG text
            )
            Spacer(Modifier.height(32.dp))

            TextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            TextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    auth.signInWithEmailAndPassword(email.value, password.value)
                        .addOnSuccessListener { result ->
                            currentUser.value = result.user
                            onLoginSuccess()
                        }.addOnFailureListener { e ->
                            message.value = "Login failed: ${e.message}"
                        }
                }) {
                    Text("Login")
                }

                Button(onClick = {
                    auth.createUserWithEmailAndPassword(email.value, password.value)
                        .addOnSuccessListener { result ->
                            currentUser.value = result.user
                            onLoginSuccess()
                        }.addOnFailureListener { e ->
                            message.value = "Signup failed: ${e.message}"
                        }
                }) {
                    Text("Sign Up")
                }
            }
        }
    }
}