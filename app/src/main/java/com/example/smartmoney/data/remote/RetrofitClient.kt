package com.example.smartmoney.data.remote

import com.example.smartmoney.data.remote.api.AccountApi
import com.example.smartmoney.data.remote.api.AuthApi
import com.example.smartmoney.data.remote.api.BankIntegrationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client configured for communication with the Spring Boot microservices backend.
 *
 * Microservice ports:
 * - Identity Service: 8081
 * - Accounts Service: 8082
 * - Transactions Service: 8083
 * - API Gateway (optional): 8080
 *
 * If [gatewayPort] is set (e.g. 8080), all APIs route through the API Gateway.
 * Otherwise, each API connects directly to its respective microservice port.
 */
object RetrofitClient {

    var host: String = "127.0.0.1"
    var gatewayPort: Int? = null

    const val IDENTITY_PORT = 8081
    const val ACCOUNTS_PORT = 8082
    const val TRANSACTIONS_PORT = 8083
    const val BANK_INTEGRATION_PORT = 8090
    const val ASSISTANT_PORT = 8091

    /**
     * Hosted URL for the bank-integration-service on Render.
     */
    const val BANK_INTEGRATION_RENDER_URL: String = "https://bank-integration-service.onrender.com/"

    /**
     * Base URL for the bank-integration-service. Defaults to the live Render deployment.
     * Can be dynamically reassigned to point to local host or custom endpoint if needed.
     */
    var bankIntegrationBaseUrl: String = try {
        val field = com.example.smartmoney.BuildConfig::class.java.getField("BANK_INTEGRATION_URL")
        (field.get(null) as? String)?.takeIf { it.isNotBlank() } ?: BANK_INTEGRATION_RENDER_URL
    } catch (_: Throwable) {
        BANK_INTEGRATION_RENDER_URL
    }
        set(value) {
            field = if (value.endsWith("/")) value else "$value/"
            _bankIntegrationApi = null
        }

    private var _bankIntegrationApi: BankIntegrationApi? = null

    /**
     * Provider supplying the active JWT authentication token.
     */
    var tokenProvider: (() -> String?)? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val token = tokenProvider?.invoke()
        val request = if (!token.isNullOrBlank() && original.header("Authorization") == null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    const val FALLBACK_HOST: String = "192.168.68.121"

    private val hostFallbackInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request()
        val originalUrl = request.url
        try {
            chain.proceed(request)
        } catch (e: Exception) {
            if ((e is java.net.ConnectException || e is java.net.SocketTimeoutException) &&
                (originalUrl.host == "127.0.0.1" || originalUrl.host == "localhost")
            ) {
                val newUrl = originalUrl.newBuilder()
                    .host(FALLBACK_HOST)
                    .build()
                val newRequest = request.newBuilder()
                    .url(newUrl)
                    .build()
                chain.proceed(newRequest)
            } else {
                throw e
            }
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(hostFallbackInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * The deployed api-gateway (API_BASE_URL in local.properties). When set, the
     * per-port services go through it, so the app reads the same data as the web
     * app. Empty means local services on [host], as before. The bank-integration
     * client keeps its own Render URL.
     */
    private val deployedGateway: String = com.example.smartmoney.BuildConfig.API_BASE_URL.trim().trimEnd('/')

    private fun buildRetrofit(servicePort: Int): Retrofit {
        val port = gatewayPort ?: servicePort
        val baseUrl = if (deployedGateway.isNotEmpty()) "$deployedGateway/" else "http://$host:$port/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private fun buildRetrofitWithBaseUrl(baseUrl: String): Retrofit {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Singleton instance of the [AuthApi] interface targeting identity-service (:8081).
     */
    val authApi: AuthApi by lazy {
        buildRetrofit(IDENTITY_PORT).create(AuthApi::class.java)
    }

    /**
     * Singleton instance of the [AccountApi] interface targeting accounts-service (:8082).
     */
    val accountApi: AccountApi by lazy {
        buildRetrofit(ACCOUNTS_PORT).create(AccountApi::class.java)
    }

    /**
     * The signed-in user's real transactions from transactions-service (:8083),
     * the same movements the web dashboard shows.
     */
    val activityApi: com.example.smartmoney.data.remote.api.ActivityApi by lazy {
        buildRetrofit(TRANSACTIONS_PORT).create(com.example.smartmoney.data.remote.api.ActivityApi::class.java)
    }

    /**
     * Singleton instance of the [BankIntegrationApi] interface targeting bank-integration-service.
     * Connects directly to the hosted Render cloud deployment (https://bank-integration-service.onrender.com/).
     */
    val bankIntegrationApi: BankIntegrationApi
        get() = _bankIntegrationApi ?: buildRetrofitWithBaseUrl(bankIntegrationBaseUrl)
            .create(BankIntegrationApi::class.java)
            .also { _bankIntegrationApi = it }

    /**
     * Switches bank integration requests to a local machine instance (:8090).
     */
    fun useLocalBankIntegration() {
        bankIntegrationBaseUrl = "http://$host:$BANK_INTEGRATION_PORT/"
    }

    /**
     * Switches bank integration requests to the Render cloud instance.
     */
    fun useRenderBankIntegration() {
        bankIntegrationBaseUrl = BANK_INTEGRATION_RENDER_URL
    }

    /**
     * Singleton instance of the [com.example.smartmoney.data.remote.api.RahaApi] interface targeting assistant-service (:8091).
     */
    val rahaApi: com.example.smartmoney.data.remote.api.RahaApi by lazy {
        buildRetrofit(ASSISTANT_PORT).create(com.example.smartmoney.data.remote.api.RahaApi::class.java)
    }
}
