package com.example.energy.data.remote

import com.example.energy.data.remote.api.AuthApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client configured for communication with the local Spring Boot backend.
 *x
 * - JSON serialization handled by Kotlinx Serialization.
 * - HttpLoggingInterceptor set to Level.BODY for comprehensive request/response inspection in Logcat.
 */
object RetrofitClient {

    private const val BASE_URL = "http://127.0.0.1:8081/"

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

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    /**
     * Singleton instance of the [AuthApi] interface.
     */
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}
