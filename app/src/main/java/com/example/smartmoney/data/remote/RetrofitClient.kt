package com.example.smartmoney.data.remote

import com.example.smartmoney.data.remote.api.AccountApi
import com.example.smartmoney.data.remote.api.AuthApi
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

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private fun buildRetrofit(servicePort: Int): Retrofit {
        val port = gatewayPort ?: servicePort
        val baseUrl = "http://$host:$port/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
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
}
