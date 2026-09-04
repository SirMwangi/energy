package com.example.energy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.energy.data.UserDao
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    userDao: UserDao,
    onBackToSignUp: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()


    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            Color(0xFFFFE4EC)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Text(
            text = "Sign In" ,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sign in to your account",
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text("Email")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text("Password")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                if (email.isBlank() || password.isBlank()) {

                    message = "Please enter your email and password."
                    isError = true

                } else {

                    scope.launch {

                        try {

                            val user = userDao.getUserByEmail(email)

                            if (user == null) {

                                message = "No account found with this email."
                                isError = true

                            } else if (user.password != password) {

                                message = "Incorrect password."
                                isError = true

                            } else {

                                message = "Login successful!"
                                isError = false
                                onLoginSuccess(user.name)
                            }

                        } catch (e: Exception) {

                            message = "Something went wrong. Please try again."
                            isError = true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log In")
        }

        if (message.isNotEmpty()) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBackToSignUp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create an account")
        }


    }
}
