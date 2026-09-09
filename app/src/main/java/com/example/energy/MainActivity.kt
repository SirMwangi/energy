package com.example.energy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.energy.data.local.database.AppDatabase
import com.example.energy.data.remote.datasource.AccountRemoteDataSource
import com.example.energy.data.remote.datasource.AuthRemoteDataSource
import com.example.energy.data.remote.datasource.TransactionRemoteDataSource
import com.example.energy.data.remote.supabase.SupabaseClientProvider
import com.example.energy.data.repository.AccountRepositoryImpl
import com.example.energy.data.repository.AuthRepositoryImpl
import com.example.energy.data.repository.TransactionRepositoryImpl
import com.example.energy.ui.accounts.AccountViewModel
import com.example.energy.ui.auth.AuthViewModel
import com.example.energy.ui.auth.LoginScreen
import com.example.energy.ui.auth.SignUpScreen
import com.example.energy.ui.dashboard.DashboardScreen
import com.example.energy.ui.theme.EnergyTheme
import com.example.energy.ui.transactions.TransactionViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize dependencies
        val database = AppDatabase.getDatabase(applicationContext)
        val supabase = SupabaseClientProvider.getClient()

        val authRemoteDataSource = AuthRemoteDataSource(supabase)
        val authRepository = AuthRepositoryImpl(authRemoteDataSource)

        val accountRemoteDataSource = AccountRemoteDataSource(supabase)
        val accountRepository = AccountRepositoryImpl(accountRemoteDataSource, database.accountDao())

        val transactionRemoteDataSource = TransactionRemoteDataSource(supabase)
        val transactionRepository = TransactionRepositoryImpl(transactionRemoteDataSource, database.transactionDao())

        setContent {
            EnergyTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(authRepository)
                )

                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                var showLogin by remember { mutableStateOf(false) }

                if (isLoggedIn) {
                    val currentUserId = authViewModel.currentUserId() ?: ""
                    val currentUser = authRemoteDataSource.currentUser()
                    val userName = currentUser?.userMetadata?.get("name")?.toString()?.trim('"')
                        ?: currentUser?.email?.substringBefore('@')
                        ?: "User"

                    val accountViewModel: AccountViewModel = viewModel(
                        key = "account_$currentUserId",
                        factory = AccountViewModel.Factory(accountRepository, currentUserId)
                    )

                    val transactionViewModel: TransactionViewModel = viewModel(
                        key = "transaction_$currentUserId",
                        factory = TransactionViewModel.Factory(transactionRepository)
                    )

                    DashboardScreen(
                        userName = userName,
                        userEmail = currentUser?.email,
                        accountViewModel = accountViewModel,
                        transactionViewModel = transactionViewModel,
                        onLogout = {
                            authViewModel.signOut()
                        }
                    )
                } else if (showLogin) {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onBackToSignUp = {
                            showLogin = false
                            authViewModel.clearState()
                        },
                        onLoginSuccess = {
                            showLogin = false
                        }
                    )
                } else {
                    SignUpScreen(
                        authViewModel = authViewModel,
                        onLoginClick = {
                            showLogin = true
                            authViewModel.clearState()
                        }
                    )
                }
            }
        }
    }
}
