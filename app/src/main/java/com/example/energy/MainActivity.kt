
package com.example.energy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.energy.ui.theme.EnergyTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.launch
import com.example.energy.data.UserDao
import com.example.energy.data.User
import com.example.energy.data.AppDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EnergyTheme {

                val database = AppDatabase.getDatabase(applicationContext)
                val userDao = database.userDao()

                var showLogin by remember {
                    mutableStateOf(false)
                }

                var isLoggedIn by remember {
                    mutableStateOf(false)
                }

                var loggedInUserName by remember {
                    mutableStateOf("")
                }

                if (isLoggedIn) {
                    DashboardScreen(
                        userName = loggedInUserName,
                        onLogout = {
                            isLoggedIn = false
                        }
                    )
                } else if (showLogin) {

                    LoginScreen(
                        userDao = userDao,
                        onBackToSignUp = {
                            showLogin = false
                        },
                        onLoginSuccess = { name ->
                            loggedInUserName = name
                            isLoggedIn = true
                        }
                    )

                } else {

                    SignUpScreen(
                        userDao = userDao,
                        onLoginClick = {
                            showLogin = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    userDao: UserDao,
    onLoginClick: () -> Unit = {}) {

    var name by remember { mutableStateOf("") }
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
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Create Account",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign up to get started",
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {

                    // User Account is created
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {

                        message = "Please fill in all fields."
                        isError = true

                    } else if (!android.util.Patterns.EMAIL_ADDRESS
                            .matcher(email)
                            .matches()
                    ) {

                        message = "Please enter a valid email address."
                        isError = true

                    } else if (password.length < 6) {

                        message = "Password must be at least 6 characters."
                        isError = true

                    } else {

                        scope.launch {

                            try {

                                val existingUser = userDao.getUserByEmail(email)

                                if (existingUser != null) {

                                    message = "An account with this email already exists."
                                    isError = true

                                } else {

                                    val user = User(
                                        name = name,
                                        email = email,
                                        password = password
                                    )

                                    userDao.insertUser(user)

                                    message = "Account created successfully!"
                                    isError = false

                                    name = ""
                                    email = ""
                                    password = ""
                                }

                            } catch (e: Exception) {

                                message = "Something went wrong. Please try again."
                                isError = true
                            }
                        }
                    }
                }
            ) {
                Text("Sign Up")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? Log In")
            }

            //displays the message
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
