package com.example.smartmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.smartmoney.data.local.UserProfileManager
import com.example.smartmoney.data.local.database.AppDatabase
import com.example.smartmoney.data.remote.datasource.AccountRemoteDataSource
import com.example.smartmoney.data.remote.datasource.AuthRemoteDataSource
import com.example.smartmoney.data.remote.datasource.TransactionRemoteDataSource
import com.example.smartmoney.data.remote.supabase.SupabaseClientProvider
import com.example.smartmoney.data.repository.AccountRepositoryImpl
import com.example.smartmoney.data.repository.AuthRepositoryImpl
import com.example.smartmoney.data.repository.BankAccountRepositoryImpl
import com.example.smartmoney.data.repository.BudgetRepositoryImpl
import com.example.smartmoney.data.repository.InvestmentRepositoryImpl
import com.example.smartmoney.data.repository.TransactionRepositoryImpl
import com.example.smartmoney.data.local.UserPreferencesRepository
import com.example.smartmoney.ui.accounts.AccountViewModel
import com.example.smartmoney.ui.auth.AuthViewModel
import com.example.smartmoney.ui.auth.LoginScreen
import com.example.smartmoney.ui.auth.SignUpScreen
import com.example.smartmoney.ui.onboarding.OnboardingScreen
import com.example.smartmoney.ui.auth.SignUpScreen
import com.example.smartmoney.ui.budget.BudgetViewModel
import com.example.smartmoney.ui.dashboard.DashboardScreen
import com.example.smartmoney.ui.investment.InvestmentViewModel
import com.example.smartmoney.ui.theme.EnergyTheme
import com.example.smartmoney.ui.transactions.TransactionViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize dependencies asynchronously
        lifecycleScope.launch {
            UserProfileManager.initialize(applicationContext)
        }
        val database = AppDatabase.getDatabase(applicationContext)
        val supabase = SupabaseClientProvider.getClient()

        val authRemoteDataSource = AuthRemoteDataSource()
        val authRepository = AuthRepositoryImpl(authRemoteDataSource)

        val accountRemoteDataSource = AccountRemoteDataSource()
        val accountRepository = AccountRepositoryImpl(accountRemoteDataSource, database.accountDao())

        val transactionRemoteDataSource = TransactionRemoteDataSource(supabase)
        val transactionRepository = TransactionRepositoryImpl(transactionRemoteDataSource, database.transactionDao())

        val userPrefsRepo = UserPreferencesRepository(applicationContext)

        setContent {
            var isDarkMode by rememberSaveable { mutableStateOf(false) }

            EnergyTheme(darkTheme = isDarkMode, dynamicColor = false) {
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModel.Factory(userPrefsRepo, authRepository)
                )

                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(authRepository)
                )

                val startDestination by mainViewModel.startDestination.collectAsState()
                
                // Keep splash screen on screen until start destination is resolved
                splashScreen.setKeepOnScreenCondition {
                    mainViewModel.isLoading.value
                }

                // If loading, don't compose main UI yet
                if (startDestination == null) return@EnergyTheme

                var isSplashFinished by rememberSaveable { mutableStateOf(false) }

                if (!isSplashFinished) {
                    com.example.smartmoney.ui.splash.SplashScreen(
                        onSplashFinished = { isSplashFinished = true }
                    )
                } else {
                    // Local state for manually switching between Login and SignUp after onboarding
                    var showLogin by rememberSaveable { mutableStateOf(startDestination == "login") }

                    when (startDestination) {
                        "onboarding" -> {
                            OnboardingScreen(
                                onFinish = {
                                    mainViewModel.completeOnboarding()
                                    // Since auth logic flows dynamically, when onboarding finishes,
                                    // the startDestination automatically changes because hasSeenOnboarding becomes true.
                                // It will evaluate to "login", but let's reset showLogin to false so it goes to sign-up
                                // Wait, actually if we set hasSeenOnboarding to true and are not logged in, it will evaluate to "login" by default in MainViewModel.
                                // Let's explicitly control showLogin.
                            }
                        )
                    }
                    else -> {
                        val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                        if (isLoggedIn) {
                            val currentUserId = authViewModel.currentUserId() ?: "default-user"
                            val userName = authViewModel.currentUserName() ?: "User"
                            val userEmail = authViewModel.currentUserEmail()

                            val bankAccountRepository = BankAccountRepositoryImpl(
                                remoteDataSource = accountRemoteDataSource,
                                localDao = database.accountDao(),
                                userId = currentUserId
                            )

                            val accountViewModel: AccountViewModel = viewModel(
                                key = "account_$currentUserId",
                                factory = AccountViewModel.Factory(accountRepository, bankAccountRepository, currentUserId)
                            )

                            val transactionViewModel: TransactionViewModel = viewModel(
                                key = "transaction_$currentUserId",
                                factory = TransactionViewModel.Factory(transactionRepository)
                            )

                            val budgetRepository = remember { BudgetRepositoryImpl() }
                            val investmentRepository = remember { InvestmentRepositoryImpl() }

                            val budgetViewModel: BudgetViewModel = viewModel(
                                key = "budget_$currentUserId",
                                factory = BudgetViewModel.Factory(budgetRepository, transactionRepository)
                            )

                            val investmentViewModel: InvestmentViewModel = viewModel(
                                key = "investment_$currentUserId",
                                factory = InvestmentViewModel.Factory(investmentRepository)
                            )

                            DashboardScreen(
                                userName = userName,
                                userEmail = userEmail,
                                accountViewModel = accountViewModel,
                                transactionViewModel = transactionViewModel,
                                budgetViewModel = budgetViewModel,
                                investmentViewModel = investmentViewModel,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { isDarkMode = it },
                                onLogout = {
                                    authViewModel.signOut()
                                    // After logout, showLogin will default based on what it was last, let's force true
                                    showLogin = true 
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
    }
}
}
